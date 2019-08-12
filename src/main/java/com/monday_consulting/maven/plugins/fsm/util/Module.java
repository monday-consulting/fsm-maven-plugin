package com.monday_consulting.maven.plugins.fsm.util;

/*
Copyright 2016-2019 Monday Consulting GmbH

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

import com.monday_consulting.maven.plugins.fsm.jaxb.ModuleType;
import com.monday_consulting.maven.plugins.fsm.jaxb.ExcludeType;
import com.monday_consulting.maven.plugins.fsm.jaxb.IncludeType;
import com.monday_consulting.maven.plugins.fsm.jaxb.Resource;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.io.File;
import java.util.*;

/**
 * The POJO for a module component.
 * This object should represent a configuration via the fsm-plugin.xml.
 * It should be used to configure the FirstSpirit module components.
 *
 * @author Marcel Scheland
 * @author Kassim Hölting
 * @since 1.0.0
 */
public class Module {
    private final Log log;
    private final ModuleType moduleType;
    private final String prefix;
    private final List<String> dependencyScopes;
    private final String dependencyTagValueInXml;
    private final Resource resource;

    private String groupId;
    private String artifactId;
    private String type;
    private String version;
    private String classifier;

    private MavenProject project;
    private List<Artifact> resolvedModuleArtifacts;
    private Xpp3Dom moduleDependencyDom;

    /**
     * @param log        The logger.
     * @param moduleType The module component configuration.
     * @param scopes     The dependency scopes that will be included (like compile, runtime).
     * @throws MojoExecutionException in case of a general failures.
     */
    public Module(final Log log, final ModuleType moduleType, final List<String> scopes) throws MojoExecutionException {
        this.log = log;
        this.moduleType = moduleType;
        final String[] coords = moduleType.getId().split(":");

        if (coords.length == 0) {
            throw new MojoExecutionException("Module-Construction failed: artifactString <=> artifact-coords are empty");
        }
        groupId = coords[0];
        if (coords.length > 1) {
            artifactId = coords[1];
        }
        if (coords.length > 2) {
            type = coords[2];
        }
        if (coords.length > 3) {
            version = coords[3];
        }
        if (coords.length > 4) {
            classifier = coords[4];
        }

        prefix = this.moduleType.getPrefix();
        dependencyScopes = scopes;
        dependencyTagValueInXml = this.moduleType.getDependencyTagValueInXml();
        resource = this.moduleType.getResource();
    }

    private Xpp3Dom getWebResourceTmpDom(final String name, final String dirPath) {
        final Xpp3Dom tmpDom = new Xpp3Dom("resource");
        // fix windows paths to work with FirstSpirit module loader
        tmpDom.setAttribute("target", "/" + dirPath.replace("\\", "/"));
        String prefix = getResource().getPrefix();
        prefix = prefix == null ? "" : prefix;
        final String value = prefix + name;
        tmpDom.setValue(value.replace("\\", "/"));
        return tmpDom;
    }

    /**
     * Setter for the resolved module artifacts. Besides this method is filling the module dependency dom.
     *
     * @param resolvedModuleArtifacts The to be set resolved module artifacts.
     * @throws MojoFailureException in case of plugin configuration problems.
     */
    public void setResolvedModuleArtifacts(final List<Artifact> resolvedModuleArtifacts) throws MojoFailureException {
        this.resolvedModuleArtifacts = resolvedModuleArtifacts;
        final boolean isJarArtifact = getProject().getArtifact().getType().equals("jar");
        if (isJarArtifact) {
            if (project == null) {
                throw new MojoFailureException("For this module no maven project was set.");
            }
            log.info("Adding the project-artifact itself: " + project.getArtifact().getArtifactId() +
                    ", with absolute-file: " + project.getArtifact().getFile().getAbsoluteFile() + "; finalname: " + project.getBuild().getFinalName());
            resolvedModuleArtifacts.add(project.getArtifact());
        }
        moduleDependencyDom = fillDependenciesXml(isJarArtifact, getFilteredModuleArtifacts(resolvedModuleArtifacts), getIncludes(), getExcludes(getIncludes()));
    }

    private Xpp3Dom fillDependenciesXml(boolean isJarArtifact, List<Artifact> filteredModuleArtifacts, Map<String, IncludeType> includes, Map<String, ExcludeType> excludes) throws MojoFailureException {
        //create Xpp3Dom out of the dependency-list
        final Xpp3Dom dom = new Xpp3Dom("root");
        for (final Artifact artifact : filteredModuleArtifacts) {
            Xpp3Dom tmpDom = new Xpp3Dom("resource");

            if (moduleType.getFirstSpiritScope() != null && !moduleType.getFirstSpiritScope().isEmpty()) {
                tmpDom.setAttribute("scope", moduleType.getFirstSpiritScope().trim());
            }
            tmpDom.setAttribute("name", artifact.getGroupId() + ":" + artifact.getArtifactId());
            tmpDom.setAttribute("version", artifact.getVersion());

            final ExcludeType excludeType = excludes.get(artifact.getArtifactId());
            final IncludeType includeType = includes.get(artifact.getArtifactId());

            if (excludeType != null) {
                log.info("Artifact: " + artifact.getArtifactId() + " excluded!");
                excludes.remove(excludeType.getArtifactId());
            } else if (includeType != null) {
                if (Boolean.valueOf(includeType.getOverride())) {
                    log.info("Artifact: " + artifact.getArtifactId() + " overridden by Include");
                    tmpDom.setValue(getPrefix() + includeType.getFileName());
                } else {
                    log.info("Artifact: " + includeType.getArtifactId() + " is defined in Includes, but is not set to override existing dependencies");
                    tmpDom.setValue(getPrefix() + artifact.getFile().getAbsoluteFile());
                    dom.addChild(tmpDom);
                }
                includes.remove(includeType.getArtifactId());
            } else {
                tmpDom.setValue(getPrefix() + artifact.getFile().getName());
                dom.addChild(tmpDom);
            }
        }

        for (final String excludeKey : excludes.keySet()) {
            log.info("Artifact: " + excludeKey + " defined but not used!");
        }
        for (final IncludeType includeType : includes.values()) {
            log.info("Artifact: " + includeType.getArtifactId() + " included");
            final Xpp3Dom tmpDom = new Xpp3Dom("resource");

            //set firstSpiritScope
            if (moduleType.getFirstSpiritScope() != null && !moduleType.getFirstSpiritScope().isEmpty()) {
                tmpDom.setAttribute("scope", moduleType.getFirstSpiritScope().trim());
            }

            tmpDom.setValue(getPrefix() + includeType.getFileName());
            dom.addChild(tmpDom);
        }

        if (!isJarArtifact) {
            handleArchiveFileIncludes(dom);
        }
        return dom;
    }

    private Map<String, ExcludeType> getExcludes(Map<String, IncludeType> includes) throws MojoFailureException {
        final Map<String, ExcludeType> excludes = new HashMap<>();
        if (moduleType.getExcludes() != null) {
            for (ExcludeType excludeType : moduleType.getExcludes().getExclude()) {
                if (Boolean.valueOf(excludeType.getOverrideIncludes()) && includes.containsKey(excludeType.getArtifactId())) {
                    log.info("Possible configuration problem: Excluding the configured Inclusion of Artifact: " + excludeType.getArtifactId());
                    includes.remove(excludeType.getArtifactId());
                }
                if (excludes.containsKey(excludeType.getArtifactId())) {
                    throw new MojoFailureException("Exclusion of ArtifactId: " + excludeType.getArtifactId() + " is defined twice!");
                }
                excludes.put(excludeType.getArtifactId(), excludeType);
            }
        }
        return excludes;
    }

    private Map<String, IncludeType> getIncludes() throws MojoFailureException {
        final Map<String, IncludeType> includes = new HashMap<>();
        if (moduleType.getIncludes() != null) {
            for (IncludeType includeType : moduleType.getIncludes().getInclude()) {
                if (includes.containsKey(includeType.getArtifactId())) {
                    throw new MojoFailureException("Inclusion of ArtifactId: " + includeType.getArtifactId() + " is defined twice!");
                }
                includes.put(includeType.getArtifactId(), includeType);
            }
        }
        return includes;
    }

    private List<Artifact> getFilteredModuleArtifacts(List<Artifact> resolvedModuleArtifacts) {
        final List<Artifact> filteredModuleArtifacts = new ArrayList<>();
        log.info("Plugin will include dependencies with scope null for the project-artifact itself and with user defined scopes: " + Arrays.toString(dependencyScopes.toArray()));
        for (final Artifact resolvedModuleArtifact : resolvedModuleArtifacts) {
            if (dependencyScopes.contains(resolvedModuleArtifact.getScope()) || resolvedModuleArtifact.getScope() == null) {
                log.info(" +included: " + resolvedModuleArtifact.getArtifactId() + " with scope: " + resolvedModuleArtifact.getScope());
                filteredModuleArtifacts.add(resolvedModuleArtifact);
            } else {
                log.info(" -filtered: " + resolvedModuleArtifact.getArtifactId() + " with scope: " + resolvedModuleArtifact.getScope());
            }
        }
        return Collections.unmodifiableList(filteredModuleArtifacts);
    }

    private void handleArchiveFileIncludes(final Xpp3Dom dom) throws MojoFailureException {
        try {
            if (project == null) {
                throw new MojoFailureException("For this module no maven project was set.");
            }

            final String targetFileDir = "/target/" + project.getName() + "-" + project.getVersion();
            File baseDir = new File(project.getBasedir() + targetFileDir);

            if (!baseDir.exists()) {
                final File artifactFile = project.getArtifact().getFile();
                final ZipFile fileToExtract = new ZipFile(artifactFile);
                if (!fileToExtract.isValidZipFile()) {
                    throw new ZipException("No valid ZIP File: " + project.getArtifact().getFile());
                }
                if (fileToExtract.isEncrypted()) {
                    throw new ZipException("The ZIP File is Password encrypted: " + project.getArtifact().getFile());
                }
                baseDir = new File(project.getParent().getBasedir().getAbsolutePath() + targetFileDir);
                fileToExtract.extractAll(baseDir.getAbsolutePath());
            }

            final Resource resource = getResource();
            if (resource == null || resource.getIncludes() == null || resource.getIncludes().getInclude().isEmpty()) {
                // if there are no resources or includes then there are no files to handle
                return;
            }
            if (resource.getPrefix() == null || resource.getPrefix().isEmpty()) {
                log.warn("No <prefix> defined. Prefix would be set to root");
            }
            if (resource.getWebXml() == null || resource.getWebXml().isEmpty()) {
                throw new MojoFailureException("Module " + project.getArtifactId() + " from archive type " + project.getArtifact().getType() + " detected. No <web-xml> defined");
            }

            final List<String> includes = resource.getIncludes().getInclude();
            final List<String> excludes = resource.getExcludes() != null ? resource.getExcludes().getExclude() : new ArrayList<>();
            // always exclude web.xml
            excludes.add(resource.getWebXml());

            DirectoryScanner ds = new DirectoryScanner();
            ds.setIncludes(includes.toArray(new String[0]));
            ds.setExcludes(excludes.toArray(new String[0]));
            ds.setBasedir(baseDir);
            ds.setCaseSensitive(true);
            ds.scan();

            for (final String incl : ds.getIncludedFiles()) {

                final int indexOf = incl.lastIndexOf(File.separator);
                String dir = "";
                if (indexOf > 0) {
                    dir = incl.substring(0, indexOf);
                }
                log.info(" +included: Archive resource: " + incl);
                dom.addChild(getWebResourceTmpDom(incl, dir));
            }

        } catch (ZipException e) {
            throw new MojoFailureException("Could not extract artifact file.", e);
        }
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getClassifier() {
        return classifier;
    }

    private String getPrefix() {
        return prefix;
    }

    public String getDependencyTagValueInXml() {
        return dependencyTagValueInXml;
    }

    private Resource getResource() {
        return resource;
    }

    private MavenProject getProject() {
        return project;
    }

    public void setProject(MavenProject project) {
        this.project = project;
    }

    public List<Artifact> getResolvedModuleArtifacts() {
        return resolvedModuleArtifacts;
    }

    public Xpp3Dom getModuleDependencyDom() {
        return moduleDependencyDom;
    }

    public List<String> getDependencyScopes() {
        return dependencyScopes;
    }
}
