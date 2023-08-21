package com.monday_consulting.maven.plugins.fsm;

import com.monday_consulting.maven.plugins.fsm.jaxb.FsmMavenPluginType;
import com.monday_consulting.maven.plugins.fsm.jaxb.ModuleType;
import com.monday_consulting.maven.plugins.fsm.maven.MavenCoordinate;
import com.monday_consulting.maven.plugins.fsm.util.Module;
import com.monday_consulting.maven.plugins.fsm.util.ModuleIdParser;
import com.monday_consulting.maven.plugins.fsm.util.resolver.IResolver;
import com.monday_consulting.maven.plugins.fsm.util.resolver.MavenGetArtifactsResolver;
import com.monday_consulting.maven.plugins.fsm.xml.FsmPluginConfigParser;
import com.monday_consulting.maven.plugins.fsm.xml.PrototypeXml;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class BaseDependencyModuleMojo extends BaseFSMMojo {

    /**
     * The xml containing the configuration for the plugin.
     */
    @SuppressWarnings("unused")
    @Parameter(defaultValue = "${configXml}")
    private File configXml;

    /**
     * The xml with the prototype for the target-xml.
     */
    @SuppressWarnings("unused")
    @Parameter(defaultValue = "${prototypeXml}")
    private File prototypeXml;

    /**
     * The Maven Session.
     */
    @SuppressWarnings("unused")
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    /**
     * The projects in the reactor.
     */
    @Parameter(defaultValue = "${reactorProjects}", readonly = true)
    protected List<MavenProject> reactorProjects;

    /**
     * The entry point towards a Maven version independent way of resolving
     * artifacts (handles both Maven 3.0 Sonatype and Maven 3.1+ eclipse Aether
     * implementations).
     */
    @SuppressWarnings("unused")
    @Component
    private ArtifactResolver artifactResolver;

    @Component
    @SuppressWarnings("unused")
    private ProjectBuilder projectBuilder;

    /**
     * Log Projects and their resolved dependencies via MavenProject.getArtifacts().
     *
     * @param reactorProjects MavenProjects in the current reactor
     */
    protected void checkReactor(final List<MavenProject> reactorProjects) {
        for (final MavenProject reactorProject : reactorProjects) {
            final String msg = "Check resolved Artifacts for: " +
                    "\ngroudId:    " + reactorProject.getGroupId() +
                    "\nartifactId: " + reactorProject.getArtifactId() +
                    "\nversion:    " + reactorProject.getVersion();

            getLog().debug(msg);

            if (reactorProject.getArtifacts() == null || reactorProject.getArtifacts().isEmpty()) {
                getLog().debug("+ Dependencies not resolved or reactor project has no dependencies!");
            } else {
                for (final Artifact artifact : reactorProject.getArtifacts()) {
                    if (getLog().isDebugEnabled()) {
                        getLog().debug("  + " + artifact.toString());
                    }
                }
            }
        }
    }

    protected Map<String, Module> resolveModulesWithDependencies() throws MojoExecutionException, MojoFailureException {
        final FsmMavenPluginType config = parseConfig();
        final IResolver resolver = new MavenGetArtifactsResolver(getLog(), session, artifactResolver, reactorProjects, projectBuilder, project);
        final Map<String, Module> modules = new HashMap<>();
        for (final ModuleType moduleType : config.getModules().getModule()) {
            if (modules.containsKey(moduleType.getDependencyTagValueInXml())) {
                throw new MojoFailureException("Properties for Module: " + moduleType.getDependencyTagValueInXml() +
                        "defined twice\tFix to prevent unpredictable behaviour!\tPlease contact Responsible Developer, XSD has to be fixed!");
            }

            getLog().debug("Resolving module for dependency tag " + moduleType.getDependencyTagValueInXml());

            List<MavenCoordinate> artifacts = ModuleIdParser.parseModuleTypeIds(moduleType);
            List<MavenProject> mavenProjects = resolver.resolveMavenProjects(artifacts);

            Module module = new Module(getLog(), moduleType, mavenProjects, config.getScopes().getScope());

            modules.put(moduleType.getDependencyTagValueInXml(), module);
        }
        return modules;
    }

    protected FsmMavenPluginType parseConfig() throws MojoExecutionException {
        getLog().debug("Create config-xml-Object");
        FsmPluginConfigParser fsmPluginConfigParser = new FsmPluginConfigParser(getLog());
        return fsmPluginConfigParser.bindXmlConfigToPojo(configXml);
    }

    protected PrototypeXml createPrototype() {
        getLog().debug("Creating PrototypeXml-Object");
        return new PrototypeXml(getLog(), prototypeXml);
    }

}
