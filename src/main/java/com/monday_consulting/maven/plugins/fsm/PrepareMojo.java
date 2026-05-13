package com.monday_consulting.maven.plugins.fsm;

import com.monday_consulting.maven.plugins.fsm.assembly.DependencyAssembler;
import com.monday_consulting.maven.plugins.fsm.util.Module;
import com.monday_consulting.maven.plugins.fsm.xml.ModuleXmlWriter;
import com.monday_consulting.maven.plugins.fsm.xml.PrototypeXml;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

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

            getLog().debug("Copying dependencies to " + fsmRootPath);
            new DependencyAssembler(getLog()).copyDependenciesForModuleAssembly(fsmRootPath, modules.values());
            getLog().debug("Module dependencies were successfully copied.");

            if (attach) {
                attachFSM();
            }

            getLog().debug("*** PrepareMojo finished");
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

}
