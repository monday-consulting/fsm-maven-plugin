package com.monday_consulting.maven.plugins.fsm.util;

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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.xml.XmlStreamReader;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates the prototype-xml-file.
 *
 * @author Kassim Hölting
 * @since 1.0.0
 */
public class PrototypeXml {
    private final Log log;
    private final List<PrototypeXml.DependencyJoint> dependencyJointList;
    private Xpp3Dom prototypeDom;

    public PrototypeXml(final Log log, final File prototypeXml) throws XmlPullParserException, IOException {
        this.dependencyJointList = new ArrayList<>();
        this.log = log;
        this.prototypeDom = Xpp3DomBuilder.build(new XmlStreamReader(prototypeXml));
        if (log.isDebugEnabled())
            this.log.debug("Getting dependency-joints\nDependency-Joints to fill:");

        for (final Xpp3Dom xpp3Dom : new Xpp3DomIterator(prototypeDom)) {
            if (xpp3Dom.getName().equals("dependencies")) {
                if (xpp3Dom.getValue().equals("")) {
                    log.error("Prototype-Xml-Error:\nTried to retrieve dependency-joint, but because its value was empty, no connection to a module can be made");
                }
                dependencyJointList.add(new DependencyJoint(xpp3Dom.getValue(), xpp3Dom));
                if (log.isDebugEnabled())
                    log.debug("\t" + xpp3Dom.getValue());
            }
        }
    }

    /**
     * Consume the given prototype structure and add the components dependencies.
     *
     * @param moduleList The list of components for the to build module.
     * @throws MojoExecutionException in case of configuration problems.
     */
    public void fillPrototypeDom(final Map<String, Module> moduleList) throws MojoExecutionException {
        for (final DependencyJoint dJ : dependencyJointList) {
            Module moduleToInsert = moduleList.get(dJ.getDependencyTagValue());
            if (moduleToInsert == null) {
                throw new MojoExecutionException("For the to be added Dependencies for the DependencyTagValue: " + dJ.getDependencyTagValue() + " no configuration could be found");
            }
            final Xpp3Dom domToInsert = moduleToInsert.getModuleDependencyDom();
            final Xpp3Dom dom = dJ.getRoot();
            final Xpp3Dom parent = dom.getParent();
            if (parent == null) {
                // this is the root
                prototypeDom = domToInsert;
                log.warn("Insert-Into-Prototype-Dom-Error\nDependency-Joint-Dom: " + dom.getName() + " has no Elements?! Dependencies cant be added as root");
            } else {
                // this is a child

                // remove temp-node with name "dependencies" and to be added dependencies
                int pos = -1;
                final Xpp3Dom[] arr = parent.getChildren();
                for (int i = 0; i < arr.length; i++) {
                    if (arr[i].equals(dom)) {
                        pos = i;
                        break;
                    }
                }
                if (-1 < pos) {
                    parent.removeChild(pos);
                } else {
                    // dom not found. sb. changed it after init?
                    log.warn("Insert-Into-Prototype-Dom-Error\nDependency-Joint-Dom: " + dom.getName() + " was not found in parent! It will be added as child under root");
                    // instead add new child to parent
                    parent.addChild(domToInsert);
                }

                // add new sub-dom
                for (Xpp3Dom d : domToInsert.getChildren()) {
                    parent.addChild(d);
                }
            }
        }
    }

    public Xpp3Dom getPrototypeDom() {
        return prototypeDom;
    }

    private final static class DependencyJoint {
        private final String dependencyTagValue;
        private final Xpp3Dom root;

        DependencyJoint(final String dependencyTagValue, final Xpp3Dom root) {
            super();
            this.dependencyTagValue = dependencyTagValue;
            this.root = root;
        }

        String getDependencyTagValue() {
            return dependencyTagValue;
        }

        Xpp3Dom getRoot() {
            return root;
        }
    }
}
