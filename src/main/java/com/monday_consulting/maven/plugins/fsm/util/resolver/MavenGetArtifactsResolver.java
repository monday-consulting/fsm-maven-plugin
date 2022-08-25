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
    private final MavenProject parentMavenProject;

    /**
     * @param log                The logger.
     * @param reactorProjects    The projects within the reactor.
     * @param repoSystem         The maven repository system.
     * @param repoSession        The maven repository session.
     * @param projectBuilder     The maven project builder.
     * @param parentMavenProject This maven project.
     */
    public MavenGetArtifactsResolver(final Log log, final List<MavenProject> reactorProjects, final RepositorySystem repoSystem,
                                     final RepositorySystemSession repoSession, final ProjectBuilder projectBuilder, final MavenProject parentMavenProject) {
        this.repoSystem = repoSystem;
        this.repoSession = repoSession;
        this.log = log;
        this.reactorProjects = reactorProjects;
        this.projectBuilder = projectBuilder;
        this.parentMavenProject = parentMavenProject;
    }

    /**
     * {@inheritDoc}
     */
    public Module resolve(final ModuleType moduleType, final List<String> scopes) throws MojoFailureException, MojoExecutionException {
        log.debug("Resolving module for dependency tag " + moduleType.getDependencyTagValueInXml());
        final Module module = new Module(log, moduleType, scopes);
        List<MavenProject> mavenProjects = getMavenProjects(module);
        module.setProjects(mavenProjects);
        return module;
    }

    private List<MavenProject> getMavenProjects(final Module module) throws MojoFailureException {
        List<MavenProject> mavenProjects = new ArrayList<>();

        for (final DefaultArtifact artifactInfo : module.getArtifacts()) {
            MavenProject mavenProject = getMavenProjectViaReactor(artifactInfo);
            
            if (mavenProject != null) {
                mavenProjects.add(mavenProject);
            } else {
                log.debug("Module " + artifactInfo.getGroupId() + ":" + artifactInfo.getArtifactId()
                        + " not found in reactor, trying to find it in the local repository.");
                mavenProjects.add(getMavenProjectViaRepository(artifactInfo));
            }
        }

        return mavenProjects;
    }

    private MavenProject getMavenProjectViaReactor(final DefaultArtifact artifact) {
        MavenProject mavenProject = null;
        boolean moduleInReactor = false;

        final String logMessagePrefix = "Module " + artifact.getGroupId() + ":" + artifact.getArtifactId();

        for (final MavenProject prj : reactorProjects) {
            if ((prj.getArtifactId().equals(artifact.getArtifactId())) && (prj.getGroupId().equals(artifact.getGroupId()))) {
                if (moduleInReactor) {
                    log.error(logMessagePrefix + " found twice in reactor!");
                } else {
                    log.debug(logMessagePrefix + " found in reactor!");
                    moduleInReactor = true;
                    mavenProject = prj;
                }
            }
        }

        return mavenProject;
    }

    private MavenProject getMavenProjectViaRepository(final DefaultArtifact artifact) throws MojoFailureException {
        try {
            final ProjectBuildingRequest request = new DefaultProjectBuildingRequest()
                    .setResolveDependencies(true)
                    .setRemoteRepositories(parentMavenProject.getRemoteArtifactRepositories())
                    .setRepositorySession(repoSession)
                    .setSystemProperties(System.getProperties());

            final LocalRepositoryManager localRepositoryManager = repoSession.getLocalRepositoryManager();
            final File repoBasedir = localRepositoryManager.getRepository().getBasedir();

            // the module type artifact (war, jar, pom ...)

            final String pathForLocalArtifact = localRepositoryManager.getPathForLocalArtifact(artifact);
            final File moduleArtifactFile = new File(repoBasedir, pathForLocalArtifact);

            // the module pom artifact to build maven project
            final DefaultArtifact pomArtifact = new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(),
                    artifact.getClassifier(), "pom", artifact.getVersion());

            final String localArtifactPath = localRepositoryManager.getPathForLocalArtifact(pomArtifact);

            final File projectFile = new File(repoBasedir, localArtifactPath);

            MavenProject result;
            try {
                result = projectBuilder.build(projectFile, request).getProject();

                if (!moduleArtifactFile.exists()) {
                    resolveArtifact(artifact);
                }
            } catch (ProjectBuildingException e) {
                log.debug("failed... try to resolve " + artifact.getArtifactId() + " from remote repository...");
                final Artifact mavenArtifact = new org.apache.maven.artifact.DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(),
                        artifact.getVersion(), null, artifact.getExtension(), artifact.getClassifier(), new DefaultArtifactHandler());
                result = projectBuilder.build(mavenArtifact, request).getProject();

                resolveArtifact(artifact);
            }

            if (result != null) {
                log.debug("Dependency resolved: " + artifact.getArtifactId() + ":" + artifact.getVersion());
                result.getArtifact().setFile(moduleArtifactFile);
                result.setParent(parentMavenProject);
            } else {
                throw new MojoFailureException("No dependency for " + artifact.getArtifactId() + " found in local or remote repository.");
            }

            return result;
        } catch (ProjectBuildingException e) {
            throw new MojoFailureException("No dependency for " + artifact.getArtifactId() + "found in local or remote repository.", e);
        }
    }

    private void resolveArtifact(final DefaultArtifact moduleArtifact) throws MojoFailureException {
        log.info("Try to resolve artifact for " + moduleArtifact.getArtifactId() + " from remote repository...");
        final ArtifactRequest artifactRequest = new ArtifactRequest();
        artifactRequest.setArtifact(moduleArtifact);
        artifactRequest.setRepositories(parentMavenProject.getRemoteProjectRepositories());
        
        try {
            repoSystem.resolveArtifact(repoSession, artifactRequest);
        } catch (ArtifactResolutionException e1) {
            throw new MojoFailureException("Could not resolve artifact " + moduleArtifact.getArtifactId() + ":" +
                    moduleArtifact.getExtension() + " for maven project.", e1);
        }
    }
}
