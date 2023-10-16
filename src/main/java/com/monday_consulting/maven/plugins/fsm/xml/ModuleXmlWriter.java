package com.monday_consulting.maven.plugins.fsm.xml;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.WriterFactory;
import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;
import org.codehaus.plexus.util.xml.XmlStreamWriter;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomWriter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ModuleXmlWriter {

    private final Log log;

    public ModuleXmlWriter(Log log) {
        this.log = log;
    }

    /**
     * Write a DOM-Tree to a target directory.
     * This will create {@code ./META-INF/module.xml} and {@code ./META-INF/module-isolated.xml}
     *
     * @param target target directory to write to
     * @param dom    dom to write to file.
     * @throws IOException in case of unexpected writer exceptions.
     */
    public void writeDomToTarget(final Path target, final Xpp3Dom dom) throws IOException {
        log.debug("Write TargetXml-File");

        final Path metaInf = target.resolve("META-INF");
        // ensure that the target directory exists
        Files.createDirectories(metaInf);

        final Path moduleXml = metaInf.resolve("module.xml");
        log.info("Writing module descriptor: " + moduleXml);
        writeFile(moduleXml.toFile(), dom);

        final Path moduleIsolatedXml = metaInf.resolve("module-isolated.xml");
        log.info("Writing isolated module descriptor: " + moduleIsolatedXml);
        Files.copy(moduleXml, moduleIsolatedXml, StandardCopyOption.REPLACE_EXISTING);

        log.debug("Dependencies written to Module-XML:\n\t" + dom);
    }

    /**
     * Write a DOM-Tree to a target XML file.
     *
     * @param target File to write to.
     * @param dom    dom to write to file.
     * @throws IOException in case of unexpected writer exceptions.
     */
    public void writeDomToTarget(final File target, final Xpp3Dom dom) throws IOException {
        log.info("Writing module descriptor: " + target.getAbsolutePath());

        // ensure that the target directory exists
        Files.createDirectories(target.toPath().getParent());
        writeFile(target, dom);
    }

    private void writeFile(final File file, final Xpp3Dom dom) throws IOException {
        final XmlStreamWriter writer = WriterFactory.newXmlWriter(file);
        final PrettyPrintXMLWriter pretty = new PrettyPrintXMLWriter(writer);
        Xpp3DomWriter.write(pretty, dom);
        writer.close();
    }
}
