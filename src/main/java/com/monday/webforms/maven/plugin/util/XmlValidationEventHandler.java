package com.monday.webforms.maven.plugin.util;

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
