package com.monday_consulting.maven.plugins.fsm.validation;

import com.monday_consulting.maven.plugins.fsm.util.Module;
import com.monday_consulting.maven.plugins.fsm.xml.Xpp3DomIterator;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A library referenced in the module descriptor (a {@code <resource>} whose value points below the
 * {@code lib} directory).
 */
final class LibResource {

    private final String name;
    private final String version;
    private final String fileName;

    LibResource(final String name, final String version, final String fileName) {
        this.name = name;
        this.version = version;
        this.fileName = fileName;
    }

    /**
     * Artifact coordinate {@code groupId:artifactId[:classifier]} (the descriptor {@code name} attribute).
     */
    String name() {
        return name;
    }

    /**
     * Artifact version (the descriptor {@code version} attribute).
     */
    String version() {
        return version;
    }

    /**
     * The file name below the {@code lib} directory.
     */
    String fileName() {
        return fileName;
    }

    /**
     * Walk the descriptor DOM once and collect every library reference (a {@code <resource>} whose
     * value starts with {@code lib/}). Web-app/archive resources use non-lib values and are ignored.
     * Duplicate references to the same file (e.g. from multiple components) are collapsed.
     */
    static List<LibResource> collectFrom(final Xpp3Dom moduleDom) {
        final Map<String, LibResource> byFileName = new LinkedHashMap<>();

        for (final Xpp3Dom node : new Xpp3DomIterator(moduleDom)) {
            final String value = node.getValue() != null ? node.getValue().trim() : null;
            if (value == null || !value.startsWith(Module.LIB_PATH)) {
                continue;
            }

            final String fileName = value.substring(Module.LIB_PATH.length());
            byFileName.putIfAbsent(fileName,
                    new LibResource(node.getAttribute("name"), node.getAttribute("version"), fileName));
        }

        return new ArrayList<>(byFileName.values());
    }
}
