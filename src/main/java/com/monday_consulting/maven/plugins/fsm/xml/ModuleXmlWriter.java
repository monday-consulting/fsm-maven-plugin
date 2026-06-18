package com.monday_consulting.maven.plugins.fsm.xml;

import org.apache.maven.plugin.logging.Log;
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
     * Write a DOM tree into the given target directory.
     * <p>
     * This method always writes {@code ./META-INF/module-isolated.xml}. If {@code legacyModule} is
     * {@code true}, it also writes a legacy descriptor at {@code ./META-INF/module.xml} (a copy of
     * {@code module-isolated.xml}).
     *
     * @param target        the target directory to write to; it will be created along with {@code META-INF} if missing
     * @param dom           the DOM to serialize into the descriptor file(s)
     * @param legacyModule  when {@code true}, additionally create {@code module.xml} alongside {@code module-isolated.xml}
     * @throws IOException  in case of unexpected writer exceptions
     */
    public void writeDomToTarget(final Path target, final Xpp3Dom dom, final boolean legacyModule) throws IOException {
        log.debug("Write TargetXml-File");

        final Path metaInf = target.resolve("META-INF");
        // ensure that the target directory exists
        Files.createDirectories(metaInf);

        final Path moduleIsolatedXml = metaInf.resolve("module-isolated.xml");
        log.info("Writing isolated module descriptor: " + moduleIsolatedXml);
        writeFile(moduleIsolatedXml.toFile(), dom);

        if (legacyModule) {
            final Path moduleXml = metaInf.resolve("module.xml");
            log.info("Writing module descriptor: " + moduleXml);
            Files.copy(moduleIsolatedXml, moduleXml, StandardCopyOption.REPLACE_EXISTING);
        }

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
        try (final XmlStreamWriter writer = new XmlStreamWriter(file)) {
            final PrettyPrintXMLWriter pretty = new PrettyPrintXMLWriter(writer);
            Xpp3DomWriter.write(pretty, dom);
        }
    }
}
