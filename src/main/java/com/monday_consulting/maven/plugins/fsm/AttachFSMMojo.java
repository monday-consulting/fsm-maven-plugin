package com.monday_consulting.maven.plugins.fsm;

/*
Copyright 2016-2020 Monday Consulting GmbH

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import java.io.File;

/**
 * This Mojo attaches the assembled FSM as a project artifact.
 *
 * @author Oliver Degener
 * @since 1.6.0
 */
@Mojo(name = "attachFSM", defaultPhase = LifecyclePhase.PACKAGE)
class AttachFSMMojo extends AbstractMojo {

    @Component
    private MavenProjectHelper projectHelper;

    /**
     * The filename of the assembled distribution file.
     */
    @Parameter(property = "fsmFile", defaultValue = "${project.artifactId}-${project.version}.fsm.zip")
    private String fsmFile;

    /**
     * The artifact type, defaults to fsm.
     */
    @Parameter(property = "type", defaultValue = "fsm")
    private String type;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Override
    public void execute() {
        final String targetDir =  project.getBuild().getDirectory();

        String path = targetDir.concat(File.separator).concat(fsmFile);
        File file = new File(path);

        getLog().info("Attaching '" + type + "' artifact: " + file.getAbsolutePath());
        projectHelper.attachArtifact(project, type, null, file);
    }

}
