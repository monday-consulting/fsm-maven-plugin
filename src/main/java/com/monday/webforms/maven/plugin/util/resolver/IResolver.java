package com.monday.webforms.maven.plugin.util.resolver;

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
