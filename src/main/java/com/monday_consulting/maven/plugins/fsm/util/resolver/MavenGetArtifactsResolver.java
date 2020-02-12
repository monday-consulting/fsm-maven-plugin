package com.monday_consulting.maven.plugins.fsm.util.resolver;

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

import com.monday_consulting.maven.plugins.fsm.jaxb.ModuleType;
import com.monday_consulting.maven.plugins.fsm.util.Module;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.*;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the resolution of an artifact within the maven reactor or repository.
 *
 * @author Kassim Hölting
 * @since 1.0.0
 */
public class MavenGetArtifactsResolver implements IResolver {
    private final Log log;
    private final List<MavenProject> reactorProjects;
    private final RepositorySystem repoSystem;
    private final RepositorySystemSession repoSession;
    private final ProjectBuilder projectBuilder;
    private final MavenProject mavenProject;

    /**
     * @param log             The logger.
     * @param reactorProjects The projects within the reactor.
     * @param repoSystem      The maven repository system.
     * @param repoSession     The maven repository session.
     * @param projectBuilder  The maven project builder.
     * @param mavenProject    This maven project.
     */
    public MavenGetArtifactsResolver(final Log log, final List<MavenProject> reactorProjects, final RepositorySystem repoSystem,
                                     final RepositorySystemSession repoSession, final ProjectBuilder projectBuilder, final MavenProject mavenProject) {
        this.repoSystem = repoSystem;
        this.repoSession = repoSession;
        this.log = log;
        this.reactorProjects = reactorProjects;
        this.projectBuilder = projectBuilder;
        this.mavenProject = mavenProject;
    }

    /**
     * {@inheritDoc}
     */
    public Module resolve(final ModuleType moduleType, final List<String> scopes) throws MojoFailureException, MojoExecutionException {
        final Module module = new Module(log, moduleType, scopes);
        MavenProject mavenProject = getMavenProjectViaReactor(module);

        if (mavenProject == null) {
            log.info("Trying to find module " + module.getGroupId() + ":" + module.getArtifactId() + " in repository");
            mavenProject = getMavenProjectViaRepository(module, projectBuilder);
        }

        if (mavenProject != null) {
            List<Artifact> artifacts = new ArrayList<>(mavenProject.getArtifacts());
            module.setProject(mavenProject);
            module.setResolvedModuleArtifacts(artifacts);
        }
        return module;
    }

    private MavenProject getMavenProjectViaReactor(final Module module) {
        MavenProject mProject = null;
        boolean moduleInReactor = false;

        for (final MavenProject prj : reactorProjects) {
            if ((prj.getArtifactId().equals(module.getArtifactId())) && (prj.getGroupId().equals(module.getGroupId()))) {
                if (moduleInReactor) {
                    log.error("module " + module.getGroupId() + ":" + module.getArtifactId() + " found twice in reactor!");
                } else {
                    log.info("module " + module.getGroupId() + ":" + module.getArtifactId() + " found in reactor!");
                    moduleInReactor = true;
                    mProject = prj;
                }
            }
        }
        if (!moduleInReactor) {
            log.warn("module " + module.getGroupId() + ":" + module.getArtifactId() + " not found in reactor!");
        }

        return mProject;
    }

    private MavenProject getMavenProjectViaRepository(final Module module, final ProjectBuilder projectBuilder) throws MojoFailureException {
        try {
            final ProjectBuildingRequest request = new DefaultProjectBuildingRequest();
            request.setResolveDependencies(true);
            request.setRemoteRepositories(mavenProject.getRemoteArtifactRepositories());
            request.setRepositorySession(repoSession);

            final LocalRepositoryManager localRepositoryManager = repoSession.getLocalRepositoryManager();
            final File repoBasedir = localRepositoryManager.getRepository().getBasedir();

            // the module type artifact (war, jar, pom ...)
            final DefaultArtifact moduleArtifact = new DefaultArtifact(module.getGroupId(), module.getArtifactId(), module.getClassifier(), module.getType(), module.getVersion());
            final String pathForLocalArtifact = localRepositoryManager.getPathForLocalArtifact(moduleArtifact);
            final File moduleArtifactFile = new File(repoBasedir, pathForLocalArtifact);

            // the module pom artifact to build maven project
            final DefaultArtifact pomArtifact = new DefaultArtifact(module.getGroupId(), module.getArtifactId(), module.getClassifier(), "pom", module.getVersion());
            final String localArtifactPath = localRepositoryManager.getPathForLocalArtifact(pomArtifact);

            final File projectFile = new File(repoBasedir, localArtifactPath);

            MavenProject result;
            try {
                log.info("try to build maven project for " + module.getArtifactId() + " from local repository...");
                result = projectBuilder.build(projectFile, request).getProject();

                if (!moduleArtifactFile.exists()) {
                    resolveArtifact(module, moduleArtifact);
                }
            } catch (ProjectBuildingException e) {
                log.info("failed... try to resolve " + module.getArtifactId() + " from remote repository...");
                final Artifact mavenArtifact = new org.apache.maven.artifact.DefaultArtifact(module.getGroupId(), module.getArtifactId(), module.getVersion(),
                        null, module.getType(), module.getClassifier(), new DefaultArtifactHandler());
                result = projectBuilder.build(mavenArtifact, request).getProject();

                resolveArtifact(module, moduleArtifact);
            }

            if (result != null) {
                log.info("Dependency resolved: " + module.getArtifactId());
                result.getArtifact().setFile(moduleArtifactFile);
                result.setParent(mavenProject);
            } else {
                throw new MojoFailureException("No dependency for " + module.getArtifactId() + " found in local or remote repository");
            }

            return result;
        } catch (ProjectBuildingException e) {
            throw new MojoFailureException("No dependency for " + module.getArtifactId() + "found in local or remote repository", e);
        }
    }

    private void resolveArtifact(final Module module, final DefaultArtifact moduleArtifact) throws MojoFailureException {
        log.info("try to resolve artifact for " + module.getArtifactId() + " from remote repository...");
        final ArtifactRequest artifactRequest = new ArtifactRequest();
        artifactRequest.setArtifact(moduleArtifact);
        artifactRequest.setRepositories(mavenProject.getRemoteProjectRepositories());
        try {
            repoSystem.resolveArtifact(repoSession, artifactRequest);
        } catch (ArtifactResolutionException e1) {
            throw new MojoFailureException("could not resolve artifact " + module.getArtifactId() + ":" + module.getType() + " for maven project", e1);
        }
    }
}
