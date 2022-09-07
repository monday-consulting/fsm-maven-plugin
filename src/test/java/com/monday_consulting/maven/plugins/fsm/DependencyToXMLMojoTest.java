package com.monday_consulting.maven.plugins.fsm;

import com.monday_consulting.maven.plugins.fsm.jaxb.FsmMavenPluginType;
import com.monday_consulting.maven.plugins.fsm.jaxb.ModuleType;
import com.monday_consulting.maven.plugins.fsm.jaxb.ModulesType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DependencyToXMLMojoTest {

    @Test
    void applyDefaultConfiguration() {
        // build 'empty' config
        final FsmMavenPluginType config = new FsmMavenPluginType();
        ModulesType modulesType = new ModulesType();
        modulesType.getModule().add(new ModuleType());
        config.setModules(modulesType);

        // apply default config
        DependencyToXMLMojo mojo = new DependencyToXMLMojo();
        mojo.applyDefaultConfiguration(config);

        // validate config
        final List<String> defaultScopes = config.getScopes().getScope();
        assertTrue(defaultScopes.contains("runtime"), "'runtime' should be a default scope");
        assertTrue(defaultScopes.contains("compile"), "'compile' should be a default scope");

        String prefix = config.getModules().getModule().get(0).getPrefix();
        assertEquals("", prefix, "Config should have an empty prefix");
    }
}
