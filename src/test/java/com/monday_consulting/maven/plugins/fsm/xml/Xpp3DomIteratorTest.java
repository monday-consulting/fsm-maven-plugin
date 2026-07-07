package com.monday_consulting.maven.plugins.fsm.xml;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class Xpp3DomIteratorTest {

    @Test
    void remove_shouldRemoveCurrentElementFromParent_andKeepRemainingSiblings() throws Exception {
        InputStream is = getClass().getResourceAsStream("/com/monday_consulting/maven/plugins/fsm/xml/sample-module.xml");
        assertNotNull(is, "Test resource sample-module.xml should be available");
        Xpp3Dom root = Xpp3DomBuilder.build(new InputStreamReader(is, StandardCharsets.UTF_8));

        Xpp3Dom components = root.getChild("components");
        assertNotNull(components, "components element must exist");
        assertEquals(4, components.getChildren("web-app").length, "There should be exactly four web-app elements before removal");

        // Iterate to web-apps named 'FSM Delete Me' and remove them
        Xpp3DomIterator it = new Xpp3DomIterator(root);
        it.forEachRemaining(xpp3Dom -> {
            if ("web-app".equals(xpp3Dom.getName())) {
                boolean removeNode = Arrays.stream(xpp3Dom.getChildren())
                        .anyMatch(n -> n.getValue() != null && n.getValue().startsWith("FSM Delete Me"));
                if (removeNode) {
                    it.remove();
                }
            }
        });

        Xpp3Dom[] remainingWebapps = components.getChildren("web-app");
        assertEquals(2, remainingWebapps.length, "Exactly two web-app should remain after removal");

        Xpp3Dom remaining1 = remainingWebapps[0];
        Xpp3Dom name = remaining1.getChild("name");
        assertNotNull(name, "Remaining web-app should have a name child");
        assertEquals("FSM Test WebApp 1", name.getValue(), "The remaining web-app should be the first one");
        Xpp3Dom remaining2 = remainingWebapps[1];
        Xpp3Dom name2 = remaining2.getChild("name");
        assertNotNull(name2, "Remaining web-app should have a name child");
        assertEquals("FSM Test WebApp 3", name2.getValue(), "The remaining web-app should be the third one");
    }
}