package com.monday_consulting.maven.plugins.fsm.util;

/*
Copyright 2016-2020 Monday Consulting GmbH

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

import com.monday_consulting.maven.plugins.fsm.jaxb.*;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.aether.artifact.DefaultArtifact;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * The POJO for a module component. This object should represent a configuration via the fsm-plugin.xml. It should be used to
 * configure the FirstSpirit module components.
 *
 * @author Marcel Scheland
 * @author Kassim Hölting
 * @author Dirk Schrödter
 * @since 1.0.0
 */
public class Module {

    private static final String RESOURCE_TAG = "resource";
    private static final String ROOT_TAG = "root";

    private final Log log;
    private final ModuleType moduleType;
    private final String prefix;
    private final List<String> dependencyScopes;
    private final String dependencyTagValueInXml;
    private final Resource resource;
    private final List<DefaultArtifact> artifacts = new ArrayList<>();

    private List<MavenProject> projects;

    private final Map<String, IncludeType> includesMap;
    private final Map<String, ExcludeType> excludesMap;

    /**
     * @param log        The logger.
     * @param moduleType The module component configuration.
     * @param scopes     The dependency scopes that will be included (like compile, runtime).
     * @throws MojoExecutionException in case of a general failures.
     */
    public Module(final Log log, final ModuleType moduleType, final List<String> scopes) throws MojoExecutionException,
            MojoFailureException {
        this.log = log;
        this.moduleType = moduleType;

        if (moduleType.getId() == null && moduleType.getIds() == null) {
            throw new MojoExecutionException("Module construction failed: Missing module property '<id>'.");
        }

        if (moduleType.getId() != null) {
            artifacts.add(parseID(moduleType.getId()));
        }

        if (moduleType.getIds() != null) {
            for (String id : moduleType.getIds().getId()) {
                artifacts.add(parseID(id));
            }
        }

        prefix = this.moduleType.getPrefix();
        dependencyScopes = scopes;
        dependencyTagValueInXml = this.moduleType.getDependencyTagValueInXml();
        resource = this.moduleType.getResource();

        includesMap = getIncludesMap();
        excludesMap = getExcludesMap(includesMap);
    }

    private static DefaultArtifact parseID(String str) throws MojoExecutionException {
        final String[] coords = str.split(":");

        if (coords.length < 3) {
            throw new MojoExecutionException("Incomplete artifact ID specified: " + str);
        }

        return new DefaultArtifact(coords[0], coords[1],
                                   coords.length > 4 ? coords[4] : null, coords[2], coords.length > 3 ? coords[3] : null);
    }

    private Xpp3Dom getWebResourceTmpDom(final String name, final String dirPath) {
        final Xpp3Dom dom = new Xpp3Dom(RESOURCE_TAG);
        // fix windows paths to work with FirstSpirit module loader
        dom.setAttribute("target", "/" + dirPath.replace("\\", "/"));
        final String value = getResource().getPrefix() != null ? getResource().getPrefix() + name : name;
        dom.setValue(value.replace("\\", "/"));
        return dom;
    }

    /**
     * Creates an XML dom for the resolved module artifacts.
     *
     * @throws MojoFailureException in case of plugin configuration problems.
     */
    public Xpp3Dom getModuleDependencyDom() throws MojoFailureException {
        final Xpp3Dom dom = new Xpp3Dom(ROOT_TAG);
        final HashSet<String> history = new HashSet<>();

        for (final MavenProject mavenProject : getProjects()) {
            final List<Artifact> resolvedModuleArtifacts = new ArrayList<>(mavenProject.getArtifacts());

            final String artifactType = mavenProject.getArtifact().getType();

            if (artifactType.equals("jar")) {
                log.info("Adding the project artifact itself: " + mavenProject.getArtifact().getArtifactId() +
                         ", with absolute file: " + mavenProject.getArtifact().getFile().getAbsoluteFile() + "; finalname: " +
                         mavenProject.getBuild().getFinalName());
                resolvedModuleArtifacts.add(mavenProject.getArtifact());
            }

            addArtifactsToDom(dom, getFilteredModuleArtifacts(resolvedModuleArtifacts), history);

            if (artifactType.equals("war") || artifactType.equals("zip")) {
                addArchiveFileIncludesToDom(dom, mavenProject, history);
            }
        }

        addIncludesToDom(dom, history);
        sortChildren(dom);
        return dom;
    }

    private void sortChildren(Xpp3Dom src) {
        ArrayList<Xpp3Dom> children = new ArrayList<>();
        Collections.addAll(children, src.getChildren());

        for (int n = src.getChildCount(); n > 0; n = src.getChildCount()) {
            src.removeChild(n - 1);
        }

        children.sort((d1, d2) -> {
            final String n1 = d1.getAttribute("name");
            final String n2 = d2.getAttribute("name");

            if (n1 != null && n2 != null) {
                return n1.compareTo(n2);
            }

            if (d1.getValue() != null && d2.getValue() != null) {
                return d1.getValue().compareTo(d2.getValue());
            }

            return 0;
        });

        for (Xpp3Dom child : children) {
            src.addChild(child);
        }
    }

    private void addArtifactsToDom(Xpp3Dom dom, List<Artifact> filteredModuleArtifacts, HashSet<String> history) {
        for (final Artifact artifact : filteredModuleArtifacts) {
            String value = getPrefix() + artifact.getFile().getName();

            if (!history.contains(value)) {
                history.add(value);

                Xpp3Dom tmpDom = new Xpp3Dom(RESOURCE_TAG);

                if (moduleType.getFirstSpiritScope() != null && !moduleType.getFirstSpiritScope().isEmpty()) {
                    tmpDom.setAttribute("scope", moduleType.getFirstSpiritScope().trim());
                }
                if (moduleType.getFirstSpiritMode() != null && !moduleType.getFirstSpiritMode().isEmpty()) {
                    tmpDom.setAttribute("mode", moduleType.getFirstSpiritMode().trim());
                }
                tmpDom.setAttribute("name", artifact.getGroupId() + ":" + artifact.getArtifactId());
                tmpDom.setAttribute("version", artifact.getVersion());

                tmpDom.setValue(value);
                dom.addChild(tmpDom);
            }
        }
    }

    private void addArchiveFileIncludesToDom(final Xpp3Dom dom, final MavenProject mavenProject, HashSet<String> history)
            throws MojoFailureException {
        try {
            if (mavenProject == null) {
                throw new MojoFailureException("For this module no maven project was set.");
            }

            File targetDir = extractArtifactToTargetDir(mavenProject);

            final Resource res = getResource();

            if (res == null || res.getIncludes() == null || res.getIncludes().getInclude().isEmpty()) {
                // if there are no resources or includes then there are no files to handle
                return;
            }
            if (res.getPrefix() == null || res.getPrefix().isEmpty()) {
                log.warn("No <prefix> defined. Prefix would be set to root.");
            }
            if (res.getWebXml() == null || res.getWebXml().isEmpty()) {
                throw new MojoFailureException("Module " + mavenProject.getArtifactId() + " from archive type " +
                                               mavenProject.getArtifact().getType() + " detected. No <web-xml> defined.");
            }

            final List<String> includes = res.getIncludes().getInclude();
            final List<String> excludes = res.getExcludes() != null ? res.getExcludes().getExclude() : new ArrayList<>();

            // always exclude web.xml
            excludes.add(res.getWebXml());

            DirectoryScanner ds = new DirectoryScanner();
            ds.setIncludes(includes.toArray(new String[0]));
            ds.setExcludes(excludes.toArray(new String[0]));
            ds.setBasedir(targetDir);
            ds.setCaseSensitive(true);
            ds.scan();

            for (final String incl : ds.getIncludedFiles()) {

                final int indexOf = incl.lastIndexOf(File.separator);
                String dir = "";
                if (indexOf > 0) {
                    dir = incl.substring(0, indexOf);
                }
                log.info(" +included: Archive res: " + incl);

                Xpp3Dom child = getWebResourceTmpDom(incl, dir);

                if (!history.contains(child.getValue())) {
                    dom.addChild(child);
                    history.add(child.getValue());
                }
            }
        } catch (IOException e) {
            throw new MojoFailureException("Could not extract artifact file.", e);
        }
    }

    private void addIncludesToDom(Xpp3Dom dom, HashSet<String> history) {
        for (final IncludeType includeType : includesMap.values()) {

            if (history.contains(includeType.getFileName())) {
                history.add(includeType.getFileName());

                log.info("Artifact: " + includeType.getArtifactId() + " included.");
                final Xpp3Dom tmpDom = new Xpp3Dom(RESOURCE_TAG);

                // set firstSpiritScope & firstSpiritMode
                if (moduleType.getFirstSpiritScope() != null && !moduleType.getFirstSpiritScope().isEmpty()) {
                    tmpDom.setAttribute("scope", moduleType.getFirstSpiritScope().trim());
                }
                if (moduleType.getFirstSpiritMode() != null && !moduleType.getFirstSpiritMode().isEmpty()) {
                    tmpDom.setAttribute("mode", moduleType.getFirstSpiritMode().trim());
                }

                tmpDom.setValue(getPrefix() + includeType.getFileName());
                dom.addChild(tmpDom);
            }
        }
    }

    private Map<String, ExcludeType> getExcludesMap(Map<String, IncludeType> includes) throws MojoFailureException {
        final Map<String, ExcludeType> excludes = new HashMap<>();

        if (moduleType.getExcludes() != null) {
            for (ExcludeType excludeType : moduleType.getExcludes().getExclude()) {
                if (Boolean.parseBoolean(excludeType.getOverrideIncludes()) && includes.containsKey(excludeType.getArtifactId())) {
                    log.info("Possible configuration problem: Excluding the configured Inclusion of Artifact: " +
                             excludeType.getArtifactId());
                    includes.remove(excludeType.getArtifactId());
                }
                if (excludes.containsKey(excludeType.getArtifactId())) {
                    throw new MojoFailureException("Exclusion of ArtifactId: " + excludeType.getArtifactId() +
                                                   " is defined twice!");
                }
                excludes.put(excludeType.getArtifactId(), excludeType);
            }
        }
        return excludes;
    }

    private Map<String, IncludeType> getIncludesMap() throws MojoFailureException {
        final Map<String, IncludeType> includes = new HashMap<>();

        if (moduleType.getIncludes() != null) {
            for (IncludeType includeType : moduleType.getIncludes().getInclude()) {
                if (includes.containsKey(includeType.getArtifactId())) {
                    throw new MojoFailureException("Inclusion of ArtifactId: " + includeType.getArtifactId() +
                                                   " is defined twice!");
                }
                includes.put(includeType.getArtifactId(), includeType);
            }
        }
        return includes;
    }

    private List<Artifact> getFilteredModuleArtifacts(List<Artifact> resolvedModuleArtifacts) {
        final List<Artifact> filteredModuleArtifacts = new ArrayList<>();

        for (final Artifact artifact : resolvedModuleArtifacts) {

            String logMsgPostfix = artifact.getArtifactId();

            if (artifact.getScope() != null) {
                logMsgPostfix += " with scope: " + artifact.getScope();
            }

            if (!excludesMap.containsKey(artifact.getArtifactId()) &&
                (artifact.getScope() == null || dependencyScopes.contains(artifact.getScope()))) {
                log.info(" +included: " + logMsgPostfix);
                filteredModuleArtifacts.add(artifact);
            } else {
                log.info(" -filtered: " + logMsgPostfix);
            }
        }

        return Collections.unmodifiableList(filteredModuleArtifacts);
    }

    private File extractArtifactToTargetDir(MavenProject mavenProject) throws IOException {
        final String targetFileDir = "/target/" + mavenProject.getName() + "-" + mavenProject.getVersion();
        File baseDir = new File(mavenProject.getBasedir() + targetFileDir);

        if (!baseDir.exists()) {
            final File artifactFile = mavenProject.getArtifact().getFile();
            try (final ZipFile fileToExtract = new ZipFile(artifactFile)) {

                if (!fileToExtract.isValidZipFile()) {
                    throw new ZipException("No valid ZIP file: " + mavenProject.getArtifact().getFile());
                }
                if (fileToExtract.isEncrypted()) {
                    throw new ZipException("The ZIP file is password encrypted: " + mavenProject.getArtifact().getFile());
                }

                baseDir = new File(mavenProject.getParent().getBasedir().getAbsolutePath() + targetFileDir);
                fileToExtract.extractAll(baseDir.getAbsolutePath());
            }
        }

        return baseDir;
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

    private List<MavenProject> getProjects() {
        return projects;
    }

    public void setProjects(List<MavenProject> projects) {
        this.projects = projects;
    }

    public List<String> getDependencyScopes() {
        return dependencyScopes;
    }

    public List<DefaultArtifact> getArtifacts() {
        return artifacts;
    }
}
