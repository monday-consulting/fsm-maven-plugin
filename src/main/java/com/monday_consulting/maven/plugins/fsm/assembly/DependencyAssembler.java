package com.monday_consulting.maven.plugins.fsm.assembly;

import com.monday_consulting.maven.plugins.fsm.util.Module;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.List;

public class DependencyAssembler {

    private final Log log;

    public DependencyAssembler(Log log) {
        this.log = log;
    }

    public void copyDependenciesForModuleAssembly(Path fsmRoot, Collection<Module> modules) throws IOException {
        final Path libDir = fsmRoot.resolve(Module.LIB_PATH);
        Files.createDirectories(libDir);

        modules.stream()
                .map(Module::getDependencies).flatMap(List::stream)
                .filter(artifact -> {
                    File artifactFile = artifact.getFile();
                    if (artifactFile == null) {
                        log.warn("A dependency cannot be resolved and will be missing in the FSM: " + artifact);
                        return false;
                    }
                    return true;
                })
                .forEach(artifact -> {
                    Path targetPath = libDir.resolve(artifact.getFile().getName());
                    try {
                        Files.copy(artifact.getFile().toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to copy artifact to FSM directory.", e);
                    }
                });
    }
}
