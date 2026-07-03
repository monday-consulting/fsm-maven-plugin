package com.monday_consulting.maven.plugins.fsm.validation;

import com.monday_consulting.maven.plugins.fsm.util.Module;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Estimates the {@code lib} directory size overhead caused by multiple versions of the same
 * dependency and warns (with statistics, the biggest offenders and guidance) when the estimated
 * overhead reaches a configurable threshold. This only warns; it never fails the build.
 */
public class LibOverheadCheck {

    private final Log log;

    public LibOverheadCheck(final Log log) {
        this.log = log;
    }

    /**
     * Analyse the lib directory for version overhead.
     *
     * @param moduleDom        the (filled) module descriptor DOM that was written to the fsm-root
     * @param fsmRoot          the fsm-root directory containing the {@code lib} directory
     * @param thresholdPercent the overhead percentage at or above which a warning is logged
     * @throws IOException if a lib file cannot be sized
     */
    public void check(final Xpp3Dom moduleDom, final Path fsmRoot, final int thresholdPercent) throws IOException {
        // Group the referenced libs by artifact coordinate.
        final Map<String, List<LibResource>> byArtifact = new LinkedHashMap<>();
        for (final LibResource lib : LibResource.collectFrom(moduleDom)) {
            byArtifact.computeIfAbsent(lib.name(), key -> new ArrayList<>()).add(lib);
        }

        long actualBytes = 0;
        double deduplicatedBytes = 0;
        final List<Offender> offenders = new ArrayList<>();

        for (final Map.Entry<String, List<LibResource>> entry : byArtifact.entrySet()) {
            final List<LibResource> versions = entry.getValue();

            long groupBytes = 0;
            for (final LibResource lib : versions) {
                final Path file = fsmRoot.resolve(Module.LIB_PATH).resolve(lib.fileName());
                if (Files.isRegularFile(file)) {
                    groupBytes += Files.size(file);
                }
            }
            actualBytes += groupBytes;

            // The "kept single copy" estimate is the average size across the group's versions.
            final double keptBytes = (double) groupBytes / versions.size();
            deduplicatedBytes += keptBytes;

            if (versions.size() > 1) {
                offenders.add(new Offender(entry.getKey(), versions, groupBytes, Math.round(groupBytes - keptBytes)));
            }
        }

        if (deduplicatedBytes <= 0) {
            return;
        }

        if (offenders.isEmpty()) {
            log.info("FSM lib directory is size-optimised: every dependency is present in a single " +
                    "version across all resources.");
            return;
        }

        final double overheadPercent = (actualBytes / deduplicatedBytes - 1) * 100;
        final long redundantBytes = actualBytes - Math.round(deduplicatedBytes);

        offenders.sort(Comparator.comparingLong(Offender::redundantBytes).reversed());

        if (overheadPercent < thresholdPercent) {
            log.info(String.format("Duplicate dependency versions inflate the FSM lib directory by %.1f%% (%s), " +
                            "within the configured threshold of %d%%.",
                    overheadPercent, formatBytes(redundantBytes), thresholdPercent));

            if (log.isDebugEnabled()) {
                log.debug("Duplicate dependency versions in the FSM lib directory:");
                offenders.forEach(offender -> log.debug(formatOffender(offender)));
            }

            return;
        }

        final StringBuilder message = new StringBuilder()
                .append(String.format("Duplicate dependency versions inflate the FSM lib directory by %.1f%%, " +
                                "exceeding the configured threshold of %d%%.%n",
                        overheadPercent, thresholdPercent))
                .append(String.format("  Total size: %s, estimated without duplicate versions: %s (%s redundant across %d librar%s).%n",
                        formatBytes(actualBytes), formatBytes(Math.round(deduplicatedBytes)), formatBytes(redundantBytes),
                        offenders.size(), offenders.size() == 1 ? "y" : "ies"))
                .append("  Biggest offenders:").append(System.lineSeparator());

        offenders.stream().limit(5)
                .forEach(offender -> message.append(formatOffender(offender)).append(System.lineSeparator()));

        message.append("  How to reduce the module size:").append(System.lineSeparator())
                .append("    - Align the versions your components declare so they converge on one.").append(System.lineSeparator())
                .append("    - Pin each library to a single version via <dependencyManagement> in your reactor/parent POM.").append(System.lineSeparator())
                .append("    - Drop the dependency from components that do not need it, or narrow its scope.");

        log.warn(message.toString());
    }

    /**
     * An artifact present in the lib directory in more than one version.
     */
    private static final class Offender {
        private final String name;
        private final List<LibResource> versions;
        private final long totalBytes;
        private final long redundantBytes;

        Offender(final String name, final List<LibResource> versions, final long totalBytes, final long redundantBytes) {
            this.name = name;
            this.versions = versions;
            this.totalBytes = totalBytes;
            this.redundantBytes = redundantBytes;
        }

        String name() {
            return name;
        }

        List<LibResource> versions() {
            return versions;
        }

        long totalBytes() {
            return totalBytes;
        }

        long redundantBytes() {
            return redundantBytes;
        }
    }

    /**
     * Format a single offending artifact as an indented, human-readable line.
     */
    private static String formatOffender(final Offender offender) {
        final String versions = offender.versions().stream()
                .map(LibResource::version)
                .collect(Collectors.joining(", "));
        return String.format("    - %s: %d versions [%s], %s total, ~%s redundant",
                offender.name(), offender.versions().size(), versions,
                formatBytes(offender.totalBytes()), formatBytes(offender.redundantBytes()));
    }

    /**
     * Format a byte count as a human-readable string (B, KB, MB, GB).
     */
    private static String formatBytes(final long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        final String[] units = {"KB", "MB", "GB"};
        double value = bytes;
        int unit = -1;
        do {
            value /= 1024;
            unit++;
        } while (value >= 1024 && unit < units.length - 1);
        return String.format("%.1f %s", value, units[unit]);
    }
}
