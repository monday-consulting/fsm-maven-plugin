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

import com.monday_consulting.maven.plugins.fsm.maven.MavenCoordinate;
import org.apache.maven.project.MavenProject;

import java.util.List;

/**
 * Interface to resolve the dependencies of a maven artifact.
 *
 * @author Kassim Hölting
 * @since 1.0.0
 */
public interface IResolver {

    /**
     * Resolve a MavenProject for every artifact.
     * The MavenProject may be part of the reactor, part of the local workspace or a virtual project from a repository.
     *
     * @param artifacts This list of artifacts represented as a coordinate
     * @return The module component.
     */
    List<MavenProject> resolveMavenProjects(List<MavenCoordinate> artifacts);

}
