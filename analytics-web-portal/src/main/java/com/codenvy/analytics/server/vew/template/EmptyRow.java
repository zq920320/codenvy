/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.vew.template;


import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
class EmptyRow implements Row {

    /**
     * {@link EmptyRow} constructor.
     */
    private EmptyRow() {
    }

    /** {@inheritDoc} */
    @Override
    public List<List<String>> fill(Map<String, String> context, int length) {
        List<String> row = new ArrayList<String>(length);

        for (int i = 0; i < length; i++) {
            row.add("");
        }

        ArrayList<List<String>> result = new ArrayList<>();
        result.add(row);

        return result;
    }

    /** Factory method */
    public static EmptyRow initialize(Element element) {
        return new EmptyRow();
    }
}