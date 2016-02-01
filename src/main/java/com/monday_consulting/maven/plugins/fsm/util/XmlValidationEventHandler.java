package com.monday_consulting.maven.plugins.fsm.util;

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

import org.apache.maven.plugin.logging.Log;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

/**
 * Handler for the xml validation.
 *
 * @author Kassim Hölting
 * @author Hannes Thielker
 * @since 1.0.0
 */
public class XmlValidationEventHandler implements ValidationEventHandler {

    private final Log log;

    public XmlValidationEventHandler(final Log log) {
        this.log = log;
    }

    /**
     * {@inheritDoc}
     */
    public boolean handleEvent(final ValidationEvent event) {
        final String msg = "\n" + "EVENT"
                + "\n" + "SEVERITY:  " + event.getSeverity()
                + "\n" + "MESSAGE:  " + event.getMessage()
                + "\n" + "LINKED EXCEPTION:  " + event.getLinkedException()
                + "\n" + "LOCATOR"
                + "\n" + "    LINE NUMBER:  " + event.getLocator().getLineNumber()
                + "\n" + "    COLUMN NUMBER:  " + event.getLocator().getColumnNumber()
                + "\n" + "    OFFSET:  " + event.getLocator().getOffset()
                + "\n" + "    OBJECT:  " + event.getLocator().getObject()
                + "\n" + "    NODE:  " + event.getLocator().getNode()
                + "\n" + "    URL:  " + event.getLocator().getURL();
        log.error(msg);
        return true;
    }
}
