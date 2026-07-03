package com.monday_consulting.maven.plugins.fsm.validation;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class LibOverheadCheckTest {

    /**
     * Builds a descriptor DOM referencing two versions of the same artifact
     * ({@code com.google.guava:guava}) plus a single-version artifact, to exercise the overhead check.
     */
    private static Xpp3Dom overheadDom() {
        final Xpp3Dom module = new Xpp3Dom("module");
        final Xpp3Dom webResources = new Xpp3Dom("web-resources");
        module.addChild(webResources);

        webResources.addChild(resource("com.google.guava:guava", "1", "lib/guava-1.jar"));
        webResources.addChild(resource("com.google.guava:guava", "2", "lib/guava-2.jar"));
        webResources.addChild(resource("org.example:solo", "1", "lib/solo-1.jar"));

        return module;
    }

    private static Xpp3Dom resource(final String name, final String version, final String value) {
        final Xpp3Dom resource = new Xpp3Dom("resource");
        resource.setValue(value);
        resource.setAttribute("name", name);
        resource.setAttribute("version", version);
        return resource;
    }

    private static void writeSized(final Path dir, final String fileName, final int bytes) throws IOException {
        Files.write(dir.resolve(fileName), new byte[bytes]);
    }

    @Test
    void warnsWhenOverheadExceedsThreshold(@TempDir final Path fsmRoot) throws IOException {
        final Path libDir = Files.createDirectory(fsmRoot.resolve("lib"));
        // guava: 400 + 400 = 800, avg 400, redundant 400; solo: 200.
        // actual = 1000, deduplicated = 400 + 200 = 600, overhead = 1000/600 - 1 = ~66%.
        writeSized(libDir, "guava-1.jar", 400);
        writeSized(libDir, "guava-2.jar", 400);
        writeSized(libDir, "solo-1.jar", 200);

        final Log log = mock(Log.class);

        assertDoesNotThrow(() -> new LibOverheadCheck(log).check(overheadDom(), fsmRoot, 15));

        final ArgumentCaptor<String> warnings = ArgumentCaptor.forClass(String.class);
        verify(log).warn(warnings.capture());
        final String message = warnings.getValue();
        assertTrue(message.contains("com.google.guava:guava"),
                "Overhead warning should name the offending artifact, was: " + message);
        assertTrue(message.contains("dependencyManagement"),
                "Overhead warning should include guidance, was: " + message);
    }

    @Test
    void reportsNoDuplicatesWhenEveryArtifactHasOneVersion(@TempDir final Path fsmRoot) throws IOException {
        final Path libDir = Files.createDirectory(fsmRoot.resolve("lib"));
        writeSized(libDir, "solo-1.jar", 500);

        final Xpp3Dom dom = new Xpp3Dom("module");
        dom.addChild(resource("org.example:solo", "1", "lib/solo-1.jar"));

        final Log log = mock(Log.class);

        assertDoesNotThrow(() -> new LibOverheadCheck(log).check(dom, fsmRoot, 15));

        verify(log, never()).warn(org.mockito.ArgumentMatchers.anyString());
        final ArgumentCaptor<String> info = ArgumentCaptor.forClass(String.class);
        verify(log).info(info.capture());
        assertTrue(info.getValue().contains("size-optimised"),
                "Expected a positive 'size-optimised' info message, was: " + info.getValue());
    }

    @Test
    void doesNotWarnWhenOverheadBelowThreshold(@TempDir final Path fsmRoot) throws IOException {
        final Path libDir = Files.createDirectory(fsmRoot.resolve("lib"));
        // guava: 50 + 50 = 100, avg 50, redundant 50; solo: 900.
        // actual = 1000, deduplicated = 50 + 900 = 950, overhead = 1000/950 - 1 = ~5%.
        writeSized(libDir, "guava-1.jar", 50);
        writeSized(libDir, "guava-2.jar", 50);
        writeSized(libDir, "solo-1.jar", 900);

        final Log log = mock(Log.class);

        assertDoesNotThrow(() -> new LibOverheadCheck(log).check(overheadDom(), fsmRoot, 15));
        verify(log, never()).warn(org.mockito.ArgumentMatchers.anyString());
    }
}
