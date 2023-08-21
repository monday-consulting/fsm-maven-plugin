package com.monday_consulting.maven.plugins.fsm.util;

import com.monday_consulting.maven.plugins.fsm.jaxb.ModuleType;
import com.monday_consulting.maven.plugins.fsm.maven.MavenCoordinate;

import java.util.ArrayList;
import java.util.List;

public class ModuleIdParser {

    public static List<MavenCoordinate> parseModuleTypeIds(ModuleType moduleType) {

        final List<MavenCoordinate> artifacts = new ArrayList<>();

        if (moduleType.getId() == null && moduleType.getIds() == null) {
            throw new RuntimeException("ModuleType parsing failed: Missing module property '<id>' or '<ids>'.");
        }

        if (moduleType.getId() != null) {
            artifacts.add(parseID(moduleType.getId()));
        }

        if (moduleType.getIds() != null) {
            for (String id : moduleType.getIds().getId()) {
                artifacts.add(parseID(id));
            }
        }

        return artifacts;
    }

    private static MavenCoordinate parseID(String str) throws RuntimeException {
        final String[] coords = str.split(":");

        if (coords.length < 3) {
            throw new RuntimeException("Incomplete artifact ID specified: " + str);
        }

        final String groupId = coords[0];
        final String artifactId = coords[1];
        final String extension = coords[2];
        final String version = coords.length > 3 ? coords[3] : null;
        final String classifier = coords.length > 4 ? coords[4] : null;

        MavenCoordinate coordinate = new MavenCoordinate();
        coordinate.setGroupId(groupId);
        coordinate.setArtifactId(artifactId);
        coordinate.setVersion(version);
        coordinate.setClassifier(classifier);
        coordinate.setExtension(extension);
        return coordinate;
    }

}
