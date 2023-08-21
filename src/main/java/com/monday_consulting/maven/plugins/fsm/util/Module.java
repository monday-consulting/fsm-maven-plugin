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
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.xml.Xpp3Dom;

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

    /**
     * The lib path for JARs in the FSM.
     */
    public static final String LIB_PATH = "lib/";

    private final Log log;
    private final ModuleType moduleType;
    private final List<String> dependencyScopes;
    private final String dependencyTagValueInXml;
    private final Resource resource;
    private final List<Artifact> dependencies;

    private final Map<String, IncludeType> includesMap;
    private final Map<String, ExcludeType> excludesMap;
    private final List<MavenProject> projects;

    /**
     * @param log        The logger.
     * @param moduleType The module component configuration.
     * @param scopes     The dependency scopes that will be included (like compile, runtime).
     */
    public Module(final Log log, final ModuleType moduleType, final List<MavenProject> mavenProjects,
                  final List<String> scopes) throws MojoFailureException {
        this.log = log;
        this.moduleType = moduleType;

        dependencyScopes = scopes;
        dependencyTagValueInXml = this.moduleType.getDependencyTagValueInXml();
        resource = this.moduleType.getResource();

        includesMap = getIncludesMap();
        excludesMap = getExcludesMap(includesMap);

        if (moduleType.getPrefix() != null) {
            if (LIB_PATH.equals(moduleType.getPrefix())) {
                log.info("The module prefix '<prefix>lib/</prefix>' equals the default convention and" +
                        " should be removed as the parameter is no longer considered.");
            } else {
                log.warn("The <prefix> element in the module descriptor has been removed and has no effect." +
                        " All dependencies will be placed under '/lib' by convention.");
            }
        }

        this.projects = mavenProjects;

        this.dependencies = resolveDependencies(mavenProjects);
    }

    public List<Artifact> getDependencies() {
        return dependencies;
    }

    private Xpp3Dom getWebResourceTmpDom(final String name, final String dirPath) {
        final Xpp3Dom dom = new Xpp3Dom(RESOURCE_TAG);
        // fix windows paths to work with FirstSpirit module loader
        dom.setAttribute("target", "/" + dirPath.replace("\\", "/"));
        final String value = resource.getPrefix() != null ? resource.getPrefix() + name : name;
        dom.setValue(value.replace("\\", "/"));
        return dom;
    }

    protected List<Artifact> resolveDependencies(List<MavenProject> projects) {
        final Set<Artifact> resolvedModuleArtifacts = new LinkedHashSet<>();

        for (final MavenProject mavenProject : projects) {
            log.info("Resolving dependencies for module " + mavenProject.getGroupId()
                    + ":" + mavenProject.getArtifactId());

            resolvedModuleArtifacts.addAll(mavenProject.getArtifacts());

            final Artifact projectArtifact = mavenProject.getArtifact();
            final String artifactType = projectArtifact.getType();

            if (artifactType.equals("jar") || artifactType.equals("bundle")) {
                log.debug("Adding the project artifact itself: " + projectArtifact.getArtifactId() +
                        ", with file: " + projectArtifact.getFile() + "; finalname: " +
                        mavenProject.getBuild().getFinalName());
                resolvedModuleArtifacts.add(projectArtifact);
            }
        }

        List<Artifact> filteredModuleArtifacts = getFilteredModuleArtifacts(resolvedModuleArtifacts);

        ConflictResolver conflictResolver = new ConflictResolver(log, dependencyTagValueInXml);
        return conflictResolver.resolveVersionConflicts(filteredModuleArtifacts);
    }

    /**
     * Creates an XML dom for the resolved module artifacts.
     *
     * @throws MojoFailureException in case of plugin configuration problems.
     */
    public Xpp3Dom getModuleDependencyDom() throws MojoFailureException {
        log.info("Processing dependency tag '" + dependencyTagValueInXml + "'");
        final Xpp3Dom dom = new Xpp3Dom(ROOT_TAG);
        final HashSet<String> history = new HashSet<>();

        for (MavenProject project : projects) {

            final Artifact projectArtifact = project.getArtifact();
            final String artifactType = projectArtifact.getType();

            if (artifactType.equals("war") || artifactType.equals("zip")) {
                addArchiveFileIncludesToDom(dom, project, history);
            }
        }

        if (dependencies.isEmpty()) {
            log.warn("No artifacts found for dependency tag '" + dependencyTagValueInXml + "'. This is" +
                    " most likely an error in the build configuration. Please make sure that all reactor projects" +
                    " have been processed before running the FSM plugin and have not been modified after packaging.");
        }

        addArtifactsToDom(dom, dependencies, history);
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
            final File artifactFile = artifact.getFile();

            if (artifactFile == null) {
                final String unresolvedArtifact = String.format("%s:%s:%s:%s", artifact.getGroupId(),
                        artifact.getArtifactId(), artifact.getType(), artifact.getVersion());
                final String errorMsg = "The required artifact " + unresolvedArtifact + " could not be" +
                        " resolved. Please ensure that all modules have been build or execute the plugin from the" +
                        " project root.";
                log.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }

            String value = LIB_PATH + artifactFile.getName();

            if (!history.contains(value)) {
                history.add(value);

                Xpp3Dom tmpDom = new Xpp3Dom(RESOURCE_TAG);

                if (moduleType.getFirstSpiritScope() != null && !moduleType.getFirstSpiritScope().isEmpty()) {
                    tmpDom.setAttribute("scope", moduleType.getFirstSpiritScope().trim());
                }
                if (moduleType.getFirstSpiritMode() != null && !moduleType.getFirstSpiritMode().isEmpty()) {
                    tmpDom.setAttribute("mode", moduleType.getFirstSpiritMode().trim());
                }

                tmpDom.setAttribute("name", formatArtifactName(artifact));
                tmpDom.setAttribute("version", artifact.getBaseVersion());

                tmpDom.setValue(value);
                dom.addChild(tmpDom);
            }
        }
    }

    /**
     * Constructs a formatted string representing the artifact details.<br>
     * Format: groupId:artifactId[:classifier] (classifier included if present).
     *
     * @param artifact Artifact object to be formatted.
     * @return Formatted artifact details as a string.
     */
    private static String formatArtifactName(Artifact artifact) {
        String formattedName = artifact.getGroupId() + ":" + artifact.getArtifactId();

        if (artifact.hasClassifier()) {
            formattedName += ":" + artifact.getClassifier();
        }

        return formattedName;
    }

    private void addArchiveFileIncludesToDom(final Xpp3Dom dom, final MavenProject mavenProject, HashSet<String> history)
            throws MojoFailureException {
        try {
            if (mavenProject == null) {
                throw new MojoFailureException("For this module no maven project was set.");
            }

            File targetDir = extractArtifactToTargetDir(mavenProject);

            final Resource res = resource;

            if (res == null || res.getIncludes() == null || res.getIncludes().getInclude().isEmpty()) {
                // if there are no resources or includes then there are no files to handle
                return;
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

                tmpDom.setValue(LIB_PATH + includeType.getFileName());
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

    private List<Artifact> getFilteredModuleArtifacts(Collection<Artifact> resolvedModuleArtifacts) {
        final List<Artifact> filteredModuleArtifacts = new ArrayList<>();

        for (final Artifact artifact : resolvedModuleArtifacts) {

            String logMsgPostfix = artifact.getArtifactId();

            if (artifact.getScope() != null) {
                logMsgPostfix += " with scope: " + artifact.getScope();
            }

            if (!excludesMap.containsKey(artifact.getArtifactId()) &&
                (artifact.getScope() == null || dependencyScopes.contains(artifact.getScope()))) {
                log.debug(" +included: " + logMsgPostfix);
                filteredModuleArtifacts.add(artifact);
            } else {
                log.debug(" -filtered: " + logMsgPostfix);
            }
        }

        return Collections.unmodifiableList(filteredModuleArtifacts);
    }

    private File extractArtifactToTargetDir(MavenProject mavenProject) throws IOException {
        final String targetFileDir = "/target/" + mavenProject.getName() + "-" + mavenProject.getVersion();
        File baseDir = new File(mavenProject.getBasedir() + targetFileDir);

        if (!baseDir.exists()) {
            Artifact artifact = mavenProject.getArtifact();
            if (artifact == null || artifact.getFile() == null) {
                final String unresolvedArtifact = String.format("%s:%s:%s:%s", mavenProject.getGroupId(),
                        mavenProject.getArtifactId(), mavenProject.getPackaging(), mavenProject.getVersion());
                final String errorMsg = "The required artifact " + unresolvedArtifact + " could not be" +
                        " resolved. Please ensure that all modules have been build or execute the plugin from the" +
                        " project root.";
                log.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }

            final File artifactFile = artifact.getFile();
            try (final ZipFile fileToExtract = new ZipFile(artifactFile)) {

                if (!fileToExtract.isValidZipFile()) {
                    throw new ZipException("No valid ZIP file: " + artifact.getFile());
                }
                if (fileToExtract.isEncrypted()) {
                    throw new ZipException("The ZIP file is password encrypted: " + artifact.getFile());
                }

                baseDir = new File(mavenProject.getParent().getBasedir().getAbsolutePath() + targetFileDir);
                fileToExtract.extractAll(baseDir.getAbsolutePath());
            }
        }

        return baseDir;
    }

}
