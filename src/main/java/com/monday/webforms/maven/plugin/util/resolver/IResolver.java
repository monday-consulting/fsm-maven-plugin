package com.monday.webforms.maven.plugin.util.resolver;

/*
Copyright 2016 Monday Consulting GmbH

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

import com.monday.webforms.maven.plugin.jaxb.ModuleType;
import com.monday.webforms.maven.plugin.util.Module;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.util.List;

/**
 * Interface to resolve the dependencies of a maven artifact.
 * @author Kassim Hölting
 * @author Hannes Thielker
 * @since 1.0.0
 */
public interface IResolver {
    /**
     * Resolve the dependencies for a maven artifact.
     *
     * @param moduleType The module component configuration.
     * @param scopes     The dependency scopes that will be included.
     * @return The module component.
     * @throws MojoFailureException   in case if no dependency for the configured module type could be found or could not be resolved.
     * @throws MojoExecutionException in case of a general failures.
     */
    Module resolve(ModuleType moduleType, List<String> scopes) throws MojoFailureException, MojoExecutionException;
}
