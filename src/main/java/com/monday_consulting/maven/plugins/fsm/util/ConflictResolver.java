package com.monday_consulting.maven.plugins.fsm.util;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;

import java.util.*;
import java.util.stream.Collectors;

public class ConflictResolver {

    private final Log log;
    private final String dependencyTag;

    public ConflictResolver(Log log, String dependencyTag) {
        this.log = log;
        this.dependencyTag = dependencyTag;
    }

    public List<Artifact> resolveVersionConflicts(List<Artifact> artifacts) {
        final Set<String> dependencyConflicts = findDependencyConflictIds(artifacts);
        final Map<String, Artifact> resolvedConflicts = new HashMap<>();

        for (String dependencyConflict : dependencyConflicts) {
            log.warn("Found version conflict for dependency tag '"
                    + dependencyTag + "': " + dependencyConflict);

            artifacts.forEach(artifact -> {
                String conflictId = getArtifactConflictId(artifact);
                if (conflictId.equals(dependencyConflict)) {
                    if (!resolvedConflicts.containsKey(conflictId)) {
                        // first occurrence wins
                        log.info("Resolving conflict for " + conflictId + " with first seen version " + artifact.getVersion());
                        resolvedConflicts.put(conflictId, artifact);
                    } else {
                        log.debug("Alternative version for " + conflictId + " is " + artifact.getVersion());
                    }
                }
            });
        }

        List<Artifact> artifactList = artifacts.stream()
                .filter(artifact -> !dependencyConflicts.contains(getArtifactConflictId(artifact)))
                .collect(Collectors.toList());
        resolvedConflicts.forEach((key, value) -> artifactList.add(value));
        return artifactList;
    }

    Set<String> findDependencyConflictIds(Collection<Artifact> artifacts) {
        Set<String> items = new HashSet<>();
        return artifacts.stream()
                .map(ConflictResolver::getArtifactConflictId)
                .filter(conflictId -> !items.add(conflictId))
                .collect(Collectors.toSet());
    }

    static String getArtifactConflictId(Artifact artifact) {
        String conflictId = artifact.getDependencyConflictId();

        if (conflictId.endsWith(":bundle")) {
            conflictId = conflictId.substring(0, conflictId.length() - "bundle".length()) + "jar";
        }

        return conflictId;
    }

}
