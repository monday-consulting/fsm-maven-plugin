package com.monday_consulting.maven.plugins.fsm.validation;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class LibReferenceValidatorTest {

    /**
     * Builds a module descriptor DOM referencing {@code lib/a.jar} and {@code lib/b.jar} plus a
     * non-lib web-app resource that must be ignored by the validation.
     */
    private static Xpp3Dom moduleDom() {
        final Xpp3Dom module = new Xpp3Dom("module");
        final Xpp3Dom webResources = new Xpp3Dom("web-resources");
        module.addChild(webResources);

        webResources.addChild(resource("lib/a.jar"));
        webResources.addChild(resource("lib/b.jar"));
        // non-lib resource (web-app path) that must be ignored
        webResources.addChild(resource("web.xml"));

        return module;
    }

    private static Xpp3Dom resource(final String value) {
        final Xpp3Dom resource = new Xpp3Dom("resource");
        resource.setValue(value);
        return resource;
    }

    private static void touch(final Path dir, final String fileName) throws IOException {
        Files.createFile(dir.resolve(fileName));
    }

    @Test
    void passesWhenAllReferencedLibrariesArePresent(@TempDir final Path fsmRoot) throws IOException {
        final Path libDir = Files.createDirectory(fsmRoot.resolve("lib"));
        touch(libDir, "a.jar");
        touch(libDir, "b.jar");

        final Log log = mock(Log.class);

        assertDoesNotThrow(() -> new LibReferenceValidator(log).validate(moduleDom(), fsmRoot));
        verify(log, never()).warn(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void failsWhenReferencedLibraryIsMissing(@TempDir final Path fsmRoot) throws IOException {
        final Path libDir = Files.createDirectory(fsmRoot.resolve("lib"));
        touch(libDir, "a.jar"); // b.jar intentionally missing

        final LibReferenceValidator validator = new LibReferenceValidator(mock(Log.class));
        final Xpp3Dom dom = moduleDom();

        final MojoFailureException ex =
                assertThrows(MojoFailureException.class, () -> validator.validate(dom, fsmRoot));
        assertTrue(ex.getMessage().contains("lib/b.jar"),
                "Failure message should name the missing library, was: " + ex.getMessage());
    }

    @Test
    void warnsAboutUnreferencedLibrary(@TempDir final Path fsmRoot) throws IOException {
        final Path libDir = Files.createDirectory(fsmRoot.resolve("lib"));
        touch(libDir, "a.jar");
        touch(libDir, "b.jar");
        touch(libDir, "extra.jar"); // present but not referenced

        final Log log = mock(Log.class);

        assertDoesNotThrow(() -> new LibReferenceValidator(log).validate(moduleDom(), fsmRoot));

        final ArgumentCaptor<String> warnings = ArgumentCaptor.forClass(String.class);
        verify(log).warn(warnings.capture());
        final List<String> messages = warnings.getAllValues();
        assertTrue(messages.stream().anyMatch(m -> m.contains("extra.jar")),
                "Expected a warning naming the unreferenced library, was: " + messages);
    }
}
