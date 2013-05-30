/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.vew.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
class TitleRowLayoutImpl implements RowLayout {

    private final String name;
    
    public TitleRowLayoutImpl(String name) {
        this.name = name;
    }

    /**
     * {@inheritedDoc}
     */
    public List<String> fill(Map<String, String> context, int length) {
        List<String> row = new ArrayList<String>(length);
        row.add(name);

        for (int i = 1; i < length; i++) {
            row.add("");
        }

        return row;
    }
}