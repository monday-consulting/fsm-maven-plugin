package com.monday_consulting.maven.plugins.fsm;

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

import com.monday_consulting.maven.plugins.fsm.util.Module;
import com.monday_consulting.maven.plugins.fsm.xml.PrototypeXml;
import com.monday_consulting.maven.plugins.fsm.xml.ModuleXmlWriter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * The execution mojo for this plugin.
 * <p/>
 * There must be some plugin configurations for successfully build your FirstSpirit module descriptor XML.
 * You have to configure a config.xml, prototype.xml and the target.xml.
 * For example:
 * <configuration>
 * <configXml>${basedir}/target/extra-resources/fsm-plugin.xml</configXml>
 * <prototypeXml>${basedir}/target/extra-resources/prototype.module.xml</prototypeXml>
 * <targetXml>${basedir}/target/extra-resources/module.xml</targetXml>
 * </configuration>
 * <p/>
 * This mojo will automatically resolve your maven project dependencies via reactor or your maven repositories and
 * will write them to your module descriptor XML.
 *
 * @author Kassim Hölting
 * @since 1.0.0
 */
@SuppressWarnings("unused")
@Mojo(name = "dependencyToXML",
        defaultPhase = LifecyclePhase.PACKAGE,
        aggregator = true,
        requiresDependencyResolution = ResolutionScope.COMPILE)
class DependencyToXMLMojo extends BaseDependencyModuleMojo {

    /**
     * The xml the module data write to.
     */
    @Parameter(defaultValue = "${targetXml}")
    private File targetXml;

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            getLog().debug("*** Starting DependencyToXMLMojo");
            checkReactor(reactorProjects);

            final PrototypeXml prototype = createPrototype();
            final Map<String, Module> modules = resolveModulesWithDependencies();
            prototype.fillPrototypeDom(modules);

            ModuleXmlWriter moduleXmlWriter = new ModuleXmlWriter(getLog());
            moduleXmlWriter.writeDomToTarget(targetXml, prototype.getPrototypeDom());

            getLog().debug("*** DependencyToXMLMojo finished");
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

}
