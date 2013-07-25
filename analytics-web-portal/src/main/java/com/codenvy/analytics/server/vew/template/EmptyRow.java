/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.vew.template;


import org.w3c.dom.Element;

import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
class EmptyRow extends AbstractRow {

    /** {@link EmptyRow} constructor. */
    private EmptyRow() {
        super();
    }

    /** {@inheritDoc} */
    @Override
    protected String doRetrieve(Map<String, String> context, int columnNumber) throws IOException {
        return "";
    }

    /** Factory method */
    public static EmptyRow initialize() {
        return new EmptyRow();
    }
}