package com.monday.maven.plugins.fsm.util;

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

import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;


/**
 * Class for {@link Xpp3Dom} iteration.
 *
 * @author Dirk Schrödter
 * @author Kassim Hölting
 * @since 1.0.0
 */
public class Xpp3DomIterator implements Iterator<Xpp3Dom>, Iterable<Xpp3Dom> {
    private Xpp3Dom root;
    private LinkedList<Xpp3Dom> list = new LinkedList<Xpp3Dom>();
    private Xpp3Dom current;

    public Xpp3DomIterator(final Xpp3Dom dom) {
        this.root = dom;
        list.add(root);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasNext() {
        return (((current != null) && (0 < current.getChildCount())) || !list.isEmpty());
    }

    /**
     * {@inheritDoc}
     */
    public Xpp3Dom next() {
        if (current == null) {
            if (list.isEmpty()) {
                current = null;
            } else {
                current = list.pop();
            }
        } else {
            if (0 < current.getChildCount()) {
                list.addAll(0, Arrays.asList(current.getChildren()));
                current = list.pop();
            } else if (!list.isEmpty()) {
                current = list.pop();
            } else {
                current = null;
            }
        }
        return current;
    }

    /**
     * {@inheritDoc}
     */
    public void remove() {
        if (current != null) {
            final Xpp3Dom parent = current.getParent();
            if (parent == null) {
                // this is root, remove everything
                list.clear();
                root = null;
            } else {
                int pos = -1;
                Xpp3Dom[] arr = parent.getChildren();
                for (int i = 0; i < arr.length; i++) {
                    if (arr[i].getName().equals(current.getName())) {
                        pos = i;
                        break;
                    }
                }
                if (-1 < pos) {
                    parent.removeChild(pos);
                    current = null;
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public Iterator<Xpp3Dom> iterator() {
        return this;
    }
}
