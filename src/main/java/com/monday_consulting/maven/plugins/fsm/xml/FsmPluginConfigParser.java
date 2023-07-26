package com.monday_consulting.maven.plugins.fsm.xml;

import com.monday_consulting.maven.plugins.fsm.jaxb.FsmMavenPluginType;
import com.monday_consulting.maven.plugins.fsm.jaxb.ScopesType;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
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
import java.util.List;

public class FsmPluginConfigParser {

    private final Log log;

    public FsmPluginConfigParser(Log log) {
        this.log = log;
    }

    /**
     * Validate a config-XML against the Plugin-XSD.
     * Bind config-XML to generated POJOs.
     *
     * @param configXml The config-file to use
     * @return WebformsMavenPluginType  The corresponding Java-Object of the plugin-configuration.
     * @throws MojoExecutionException in case of marshalling exception for the fsm-plugin.xsd.
     */
    public FsmMavenPluginType bindXmlConfigToPojo(final File configXml) throws MojoExecutionException {
        log.debug("*** Constructing ConfigXml-Object");

        try {
            final SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            final Schema schema = sf.newSchema(getClass().getResource("/fsm-plugin.xsd"));
            final StreamSource streamSource = new StreamSource(configXml);
            final JAXBContext jaxbContext = JAXBContext.newInstance(FsmMavenPluginType.class);
            final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setSchema(schema);
            unmarshaller.setEventHandler(new XmlValidationEventHandler(log));

            final JAXBElement<FsmMavenPluginType> jaxbElement = unmarshaller.unmarshal(streamSource, FsmMavenPluginType.class);
            FsmMavenPluginType config = jaxbElement.getValue();
            applyDefaultConfiguration(config);
            return config;
        } catch (SAXException e) {
            throw new MojoExecutionException(e, "Error while parsing file with SAX", e.getMessage());
        } catch (JAXBException e) {
            throw new MojoExecutionException(e, "Error while binding xml-file with JAXB", e.getMessage());
        }
    }

    void applyDefaultConfiguration(FsmMavenPluginType config) {
        if (config.getScopes() == null) {
            ScopesType defaultScopesType = new ScopesType();
            List<String> defaultScopes = defaultScopesType.getScope();
            defaultScopes.add("runtime");
            defaultScopes.add("compile");
            config.setScopes(defaultScopesType);
        }
    }

}
