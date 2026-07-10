package com.monday_consulting.maven.plugins.fsm;

import com.monday_consulting.maven.plugins.fsm.assembly.DependencyAssembler;
import com.monday_consulting.maven.plugins.fsm.util.Module;
import com.monday_consulting.maven.plugins.fsm.validation.LibOverheadCheck;
import com.monday_consulting.maven.plugins.fsm.validation.LibReferenceValidator;
import com.monday_consulting.maven.plugins.fsm.xml.ModuleXmlWriter;
import com.monday_consulting.maven.plugins.fsm.xml.PrototypeXml;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.project.ProjectBuilder;
import org.eclipse.aether.RepositorySystem;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
@Mojo(name = "prepare",
        defaultPhase = LifecyclePhase.PACKAGE,
        aggregator = true,
        threadSafe = true,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
class PrepareMojo extends BaseDependencyModuleMojo {

    /**
     * Flag to control whether the FSM file should be attached as a project artifact.
     */
    @Parameter(property = "attach", defaultValue = "true", required = true)
    private boolean attach;

    /**
     * When true, also generates a legacy module.xml descriptor next to the default
     * module-isolated.xml. This is intended for backward compatibility with older
     * environments and tools that still expect a non-isolated module descriptor.
     */
    @Parameter(property = "legacyModuleDescriptor", defaultValue = "false", required = true)
    private boolean legacyModuleDescriptor;

    /**
     * The xml the module data write to.
     */
    @Parameter(name = "fsm-root", defaultValue = "fsm-root")
    private String fsmRoot;

    /**
     * When true (default), validates the assembled FSM after the descriptor is written and the
     * dependencies are copied. Currently, this verifies that every library referenced in the module
     * descriptor is present in the lib directory (a missing library fails the build) and warns about
     * JARs in the lib directory that are not referenced by the descriptor. Further validation logic
     * may be added under this flag in the future.
     */
    @Parameter(property = "validate", defaultValue = "true", required = true)
    private boolean validate;

    /**
     * When true, analyses the assembled lib directory for size overhead caused by multiple versions
     * of the same dependency and logs a warning (with statistics and the biggest offenders) when the
     * estimated overhead exceeds {@link #libOverheadThreshold}. Off by default; only warns, never fails
     * the build. Independent of {@link #validate}.
     */
    @Parameter(property = "warnOnLibOverhead", defaultValue = "true", required = true)
    private boolean warnOnLibOverhead;

    /**
     * The lib-overhead percentage (see {@link #warnOnLibOverhead}) at or above which a warning is logged.
     */
    @Parameter(property = "libOverheadThreshold", defaultValue = "10", required = true)
    private int libOverheadThreshold;

    @Inject
    PrepareMojo(MavenProjectHelper mavenProjectHelper,
                RepositorySystem repositorySystem,
                ProjectBuilder projectBuilder) {
        super(mavenProjectHelper, repositorySystem, projectBuilder);
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            getLog().debug("*** Starting PrepareMojo");
            checkReactor(reactorProjects);

            final PrototypeXml prototype = createPrototype();

            final Map<String, Module> modules = resolveModulesWithDependencies();

            getLog().debug("Enhance Prototype for TargetXml");

            prototype.fillPrototypeDom(modules);

            Path fsmRootPath = Path.of(project.getBuild().getDirectory(), fsmRoot);
            getLog().debug("FSM Root Directory is " + fsmRootPath);
            Files.createDirectories(fsmRootPath);

            ModuleXmlWriter moduleXmlWriter = new ModuleXmlWriter(getLog());
            moduleXmlWriter.writeDomToTarget(fsmRootPath, prototype.getPrototypeDom(), legacyModuleDescriptor);

            final Collection<Module> referencedModules = filterUnreferencedModules(prototype, modules);

            getLog().debug("Copying dependencies to " + fsmRootPath);
            new DependencyAssembler(getLog()).copyDependenciesForModuleAssembly(fsmRootPath, referencedModules);
            getLog().debug("Module dependencies were successfully copied.");

            if (validate) {
                new LibReferenceValidator(getLog())
                        .validate(prototype.getPrototypeDom(), fsmRootPath);
            }

            if (warnOnLibOverhead) {
                new LibOverheadCheck(getLog()).check(prototype.getPrototypeDom(), fsmRootPath, libOverheadThreshold);
            }

            if (attach) {
                attachFSM();
            }

            getLog().debug("*** PrepareMojo finished");
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    /**
     * Filters the resolved modules down to those that are actually referenced by a
     * {@code <dependencies>} joint in the prototype module descriptor. Modules configured in the
     * fsm-plugin.xml but not referenced in the prototype are not part of the assembled module, so
     * copying their dependencies would only bloat the lib directory. Every skipped module is logged
     * on INFO level.
     *
     * @param prototype the parsed prototype module descriptor.
     * @param modules   all modules resolved from the fsm-plugin.xml, keyed by dependency tag value.
     * @return the modules that are referenced by the prototype.
     */
    private Collection<Module> filterUnreferencedModules(final PrototypeXml prototype, final Map<String, Module> modules) {
        final Set<String> referencedTagValues = prototype.getReferencedDependencyTagValues();
        final Collection<Module> referencedModules = new ArrayList<>();

        for (final Map.Entry<String, Module> entry : modules.entrySet()) {
            if (referencedTagValues.contains(entry.getKey())) {
                referencedModules.add(entry.getValue());
            } else {
                getLog().info("Skipping module '" + entry.getKey() + "' because it is not referenced in the" +
                        " prototype module descriptor. Its dependencies will not be copied to the lib directory.");
            }
        }

        return referencedModules;
    }

}
