package com.monday_consulting.maven.plugins.fsm;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import java.io.File;
import java.nio.file.Path;

abstract class BaseFSMMojo extends AbstractMojo {

    @SuppressWarnings("unused")
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    /**
     * The filename of the assembled distribution file.
     */
    @SuppressWarnings("unused")
    @Parameter(property = "fsmFile", defaultValue = "${project.artifactId}-${project.version}.fsm.zip")
    private String fsmFile;

    /**
     * The artifact type, defaults to fsm.
     */
    @SuppressWarnings("unused")
    @Parameter(property = "type", defaultValue = "fsm")
    private String type;

    @SuppressWarnings("unused")
    @Component
    private MavenProjectHelper projectHelper;

    void attachFSM() {
        final File file = Path.of(project.getBuild().getDirectory()).resolve(fsmFile).toFile();
        getLog().info("Attaching '" + type + "' artifact: " + file.getAbsolutePath());
        projectHelper.attachArtifact(project, type, null, file);
    }
}
