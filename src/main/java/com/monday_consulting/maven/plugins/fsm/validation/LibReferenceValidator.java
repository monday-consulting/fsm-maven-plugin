package com.monday_consulting.maven.plugins.fsm.validation;

import com.monday_consulting.maven.plugins.fsm.util.Module;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Verifies that the module descriptor and the {@code lib} directory below the fsm-root are consistent:
 * every library referenced by the descriptor must be present as a file (a missing library fails the
 * build), and JARs in the lib directory that are not referenced by the descriptor are reported as
 * warnings.
 */
public class LibReferenceValidator {

    private final Log log;

    public LibReferenceValidator(final Log log) {
        this.log = log;
    }

    /**
     * Validate the descriptor's library references against the lib directory.
     *
     * @param moduleDom the (filled) module descriptor DOM that was written to the fsm-root
     * @param fsmRoot   the fsm-root directory containing the descriptor and the {@code lib} directory
     * @throws MojoFailureException if a referenced library is missing in the lib directory
     * @throws IOException          if the lib directory cannot be listed
     */
    public void validate(final Xpp3Dom moduleDom, final Path fsmRoot)
            throws MojoFailureException, IOException {
        log.info("Validating FSM lib references in: " + fsmRoot);

        final List<LibResource> libResources = LibResource.collectFrom(moduleDom);

        final Set<String> referencedFileNames = new LinkedHashSet<>();
        final Set<String> missing = new LinkedHashSet<>();

        for (final LibResource lib : libResources) {
            referencedFileNames.add(lib.fileName());
            if (!Files.isRegularFile(fsmRoot.resolve(Module.LIB_PATH).resolve(lib.fileName()))) {
                missing.add(Module.LIB_PATH + lib.fileName());
            }
        }

        if (!missing.isEmpty()) {
            final String message = buildMissingLibraryMessage(fsmRoot, missing);
            // Log the full message inline (before the reactor summary) in addition to failing the build.
            log.error(message);
            throw new MojoFailureException(message);
        }

        final int libFilesOnDisk = warnAboutUnreferencedLibraries(fsmRoot, referencedFileNames);

        log.info(String.format("FSM library validation succeeded: %d referenced librar%s in module descriptor, " +
                        "%d file%s in lib directory (%d unreferenced).",
                referencedFileNames.size(), referencedFileNames.size() == 1 ? "y" : "ies",
                libFilesOnDisk, libFilesOnDisk == 1 ? "" : "s",
                Math.max(0, libFilesOnDisk - referencedFileNames.size())));
    }

    /**
     * Build a detailed failure message naming the descriptor, the missing libraries and the
     * configuration sources to inspect.
     */
    private String buildMissingLibraryMessage(final Path fsmRoot, final Set<String> missing) {
        final Path descriptor = fsmRoot.resolve("META-INF").resolve("module-isolated.xml");
        final String libWord = missing.size() == 1 ? "a library that is" : "libraries that are";

        return String.format(
                "The module descriptor %s references %s missing from the lib directory: %s.%n" +
                        "The dependency ended up in the descriptor but its JAR was not copied to '%s', which " +
                        "usually means it could not be resolved during the build. Check the affected dependency " +
                        "in your fsm-plugin configuration xml and module prototype, and make sure it resolves " +
                        "(it must not be optional, provided or excluded).",
                descriptor, libWord, String.join(", ", missing), Module.LIB_PATH);
    }

    /**
     * Warn about files in the lib directory that are not referenced by the descriptor.
     *
     * @return the number of regular files present in the lib directory
     */
    private int warnAboutUnreferencedLibraries(final Path fsmRoot, final Set<String> referencedFileNames)
            throws IOException {
        final Path libDir = fsmRoot.resolve(Module.LIB_PATH);
        if (!Files.isDirectory(libDir)) {
            return 0;
        }

        final List<Path> libFiles;
        try (final Stream<Path> files = Files.list(libDir)) {
            libFiles = files.filter(Files::isRegularFile).collect(Collectors.toList());
        }

        for (final Path libFile : libFiles) {
            final String fileName = libFile.getFileName().toString();
            if (!referencedFileNames.contains(fileName)) {
                log.warn("Library in lib directory is not referenced by the module descriptor: " + fileName);
            }
        }

        return libFiles.size();
    }
}
