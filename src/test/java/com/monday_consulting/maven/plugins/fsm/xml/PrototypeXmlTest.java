package com.monday_consulting.maven.plugins.fsm.xml;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class PrototypeXmlTest {

    @TempDir
    Path tempDir;

    private File writePrototype(final String xml) throws Exception {
        final File file = tempDir.resolve("prototype.module.xml").toFile();
        Files.write(file.toPath(), xml.getBytes(StandardCharsets.UTF_8));
        return file;
    }

    @Test
    void filterDynamicIncludes_removesFalseElements_keepsOthers_andStripsAttribute() throws Exception {
        final File prototype = writePrototype(
                "<module>\n"
                        + "  <components>\n"
                        + "    <web-app><name>Kept default</name></web-app>\n"
                        + "    <web-app fsm-include=\"true\"><name>Kept true</name></web-app>\n"
                        + "    <web-app fsm-include=\"false\"><name>Removed</name></web-app>\n"
                        + "  </components>\n"
                        + "</module>");

        final Xpp3Dom dom = new PrototypeXml(mock(Log.class), prototype).getPrototypeDom();

        final Xpp3Dom[] webApps = dom.getChild("components").getChildren("web-app");
        assertEquals(2, webApps.length, "The web-app with fsm-include=false must be removed");
        assertEquals("Kept default", webApps[0].getChild("name").getValue());
        assertEquals("Kept true", webApps[1].getChild("name").getValue());
        assertNull(webApps[1].getAttribute("fsm-include"), "The fsm-include attribute must be stripped from the output");
    }

    @Test
    void filterDynamicIncludes_failsOnUnresolvedProperty_insteadOfSilentlyDropping() throws Exception {
        final File prototype = writePrototype(
                "<module>\n"
                        + "  <components>\n"
                        + "    <web-app fsm-include=\"${fsm.include.exampleWebapp}\"><name>Ambiguous</name></web-app>\n"
                        + "  </components>\n"
                        + "</module>");

        final RuntimeException ex = assertThrows(RuntimeException.class,
                () -> new PrototypeXml(mock(Log.class), prototype));
        assertTrue(ex.getMessage().contains("fsm-include"), "The error should name the offending attribute");
    }
}
