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

import com.monday_consulting.maven.plugins.fsm.jaxb.FsmMavenPluginType;
import com.monday_consulting.maven.plugins.fsm.jaxb.ModuleType;
import com.monday_consulting.maven.plugins.fsm.util.Module;
import com.monday_consulting.maven.plugins.fsm.util.PrototypeXml;
import com.monday_consulting.maven.plugins.fsm.util.XmlValidationEventHandler;
import com.monday_consulting.maven.plugins.fsm.util.resolver.IResolver;
import com.monday_consulting.maven.plugins.fsm.util.resolver.MavenGetArtifactsResolver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.codehaus.plexus.util.WriterFactory;
import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;
import org.codehaus.plexus.util.xml.XmlStreamWriter;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomWriter;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
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
@Mojo(name = "dependencyToXML",
        defaultPhase = LifecyclePhase.PACKAGE,
        aggregator = true,
        requiresDependencyResolution = ResolutionScope.COMPILE)
class DependencyToXMLMojo extends AbstractMojo {

    /**
     * The entry point to Aether, i.e. the component doing all the work.
     */
    @Component
    private RepositorySystem repoSystem;

    @Component
    private ProjectBuilder projectBuilder;

    /**
     * The current repository/network configuration of Maven.
     */
    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    private RepositorySystemSession repoSession;

    /**
     * The Maven-Project
     */
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject mavenProject;

    /**
     * The projects in the reactor.
     */
    @Parameter(defaultValue = "${reactorProjects}", readonly = true)
    private List<MavenProject> reactorProjects;

    /**
     * The xml containing the configuration for the plugin.
     */
    @Parameter(defaultValue = "${configXml}")
    private File configXml;

    /**
     * The xml with the prototype for the target-xml.
     */
    @Parameter(defaultValue = "${prototypeXml}")
    private File prototypeXml;

    /**
     * The xml the module data write to.
     */
    @Parameter(defaultValue = "${targetXml}")
    private File targetXml;

    /**
     * {@inheritDoc}
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            getLog().info("***Starting DependencyToXMLMojo");
            checkReactor(reactorProjects);

            if (getLog().isDebugEnabled())
                getLog().debug("Create config-xml-Object");

            final FsmMavenPluginType config = bindXmlConfigToPojo(configXml);

            if (getLog().isDebugEnabled())
                getLog().debug("Creating PrototypeXml-Object");

            PrototypeXml prototype = new PrototypeXml(getLog(), prototypeXml);

            if (getLog().isDebugEnabled()) {
                getLog().debug("Getting target-file: " + targetXml.getAbsoluteFile());
                getLog().debug("Enhance created Modules");
            }
            final IResolver resolver = new MavenGetArtifactsResolver(getLog(), reactorProjects, repoSystem, repoSession, projectBuilder, mavenProject);
            final Map<String, Module> modules = new HashMap<>();
            for (final ModuleType moduleType : config.getModules().getModule()) {
                if (modules.containsKey(moduleType.getDependencyTagValueInXml())) {
                    throw new MojoFailureException("Properties for Module: " + moduleType.getDependencyTagValueInXml() +
                            "defined twice\tFix to prevent unpredictable behaviour!\tPlease contact Responsible Developer, XSD has to be fixed!");
                }
                modules.put(moduleType.getDependencyTagValueInXml(), resolver.resolve(moduleType, config.getScopes().getScope()));
            }

            if (getLog().isDebugEnabled())
                getLog().debug("Enhance Prototype for TargetXml");

            prototype.fillPrototypeDom(modules);

            if (getLog().isDebugEnabled())
                getLog().debug("Write TargetXml-File");

            writeDomToTarget(targetXml, prototype.getPrototypeDom());

            if (getLog().isDebugEnabled())
                getLog().debug("Dependencies written to Module-XML:\n\t" + prototype.getPrototypeDom().toString());

            getLog().info("***DependencyToXMLMojo finished");
        } catch (XmlPullParserException | IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    /**
     * Log Projects and their resolved dependencies via MavenProject.getArtifacts().
     *
     * @param reactorProjects MavenProjects in the current reactor
     */
    private void checkReactor(final List<MavenProject> reactorProjects) {
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

    /**
     * Validate a config-XML against the Plugin-XSD.
     * Bind config-XML to generated POJOs.
     *
     * @param configXml The config-file to use
     * @return WebformsMavenPluginType  The corresponding Java-Object of the plugin-configuration.
     * @throws MojoExecutionException in case of marshalling exception for the fsm-plugin.xsd.
     */
    private FsmMavenPluginType bindXmlConfigToPojo(final File configXml) throws MojoExecutionException {
        getLog().debug("***Constructing ConfigXml-Object");

        try {
            final SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            final Schema schema = sf.newSchema(getClass().getResource("/fsm-plugin.xsd"));
            final StreamSource streamSource = new StreamSource(configXml);
            final JAXBContext jaxbContext = JAXBContext.newInstance(FsmMavenPluginType.class);
            final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setSchema(schema);
            unmarshaller.setEventHandler(new XmlValidationEventHandler(getLog()));

            final JAXBElement<FsmMavenPluginType> jaxbElement = unmarshaller.unmarshal(streamSource, FsmMavenPluginType.class);
            return jaxbElement.getValue();
        } catch (SAXException e) {
            throw new MojoExecutionException(e, "Error while parsing file with SAX", e.getMessage());
        } catch (JAXBException e) {
            throw new MojoExecutionException(e, "Error while binding xml-file with JAXB", e.getMessage());
        }
    }

    /**
     * Write a DOM-Tree to a target file.
     *
     * @param target File to write to.
     * @param dom    dom to write to file.
     * @throws IOException in case of unexpected writer exceptions.
     */
    private void writeDomToTarget(final File target, final Xpp3Dom dom) throws IOException {
        if (getLog().isDebugEnabled())
            getLog().debug("writing: " + dom.getName() + " to: " + target.getAbsoluteFile());

        final XmlStreamWriter writer = WriterFactory.newXmlWriter(target);
        final PrettyPrintXMLWriter pretty = new PrettyPrintXMLWriter(writer);
        Xpp3DomWriter.write(pretty, dom);
        writer.close();
        if (getLog().isDebugEnabled())
            getLog().debug(dom.getName() + " written to: " + target.getAbsoluteFile());
    }
}
