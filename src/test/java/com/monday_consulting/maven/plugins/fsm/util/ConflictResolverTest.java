package com.monday_consulting.maven.plugins.fsm.util;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ConflictResolverTest {

    private final ConflictResolver conflictResolver = new ConflictResolver(new SystemStreamLog(), "test-Tag");

    @Test
    void resolveVersionConflicts() {
        Artifact version3 = new DefaultArtifact("org.test", "fsm-test", "3.0.0", "compile", "jar", "", null);
        Artifact version4 = new DefaultArtifact("org.test", "fsm-test", "4.0.0", "compile", "jar", "", null);
        Artifact someArtifact = new DefaultArtifact("com.monday", "dependency", "1.5", "compile", "jar", "", null);

        final List<Artifact> version3Before4 = Arrays.asList(version3, version4);
        List<Artifact> artifacts = conflictResolver.resolveVersionConflicts(version3Before4);
        assertEquals(1, artifacts.size());
        assertEquals("3.0.0", artifacts.get(0).getVersion());

        final List<Artifact> version4Before3 = Arrays.asList(version4, version3);
        artifacts = conflictResolver.resolveVersionConflicts(version4Before3);
        assertEquals(1, artifacts.size());
        assertEquals("4.0.0", artifacts.get(0).getVersion());

        final List<Artifact> noConflicts = Arrays.asList(version4, someArtifact);
        artifacts = conflictResolver.resolveVersionConflicts(noConflicts);
        assertEquals(2, artifacts.size());
    }

    @Test
    void findDependencyConflictIds() {
        assertTrue(conflictResolver.findDependencyConflictIds(Collections.emptyList()).isEmpty());

        Set<String> dependencyConflictIds = conflictResolver.findDependencyConflictIds(Arrays.asList(
                new DefaultArtifact("org.test", "fsm-test", "3.0.0", "compile", "jar", "", null),
                new DefaultArtifact("org.test", "fsm-test", "3.8.0", "compile", "jar", "", null)
        ));
        assertTrue(dependencyConflictIds.contains("org.test:fsm-test:jar"));
    }

    @Test
    void getArtifactConflictId() {
        assertEquals("com.monday:dependency:jar", ConflictResolver.getArtifactConflictId(
                new DefaultArtifact("com.monday", "dependency", "1.5", "compile", "jar", "", null)));

        assertEquals("com.monday:dependency:war", ConflictResolver.getArtifactConflictId(
                new DefaultArtifact("com.monday", "dependency", "1.5", "test", "war", "", null)));

        assertEquals("com.monday:dependency:zip", ConflictResolver.getArtifactConflictId(
                new DefaultArtifact("com.monday", "dependency", "1.5", "provided", "zip", "", null)));

        // treat 'bundle' as type 'jar'
        assertEquals("com.monday:dependency:jar", ConflictResolver.getArtifactConflictId(
                new DefaultArtifact("com.monday", "dependency", "1.5", "provided", "bundle", "", null)));
    }

}
