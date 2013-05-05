/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.vew.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
class EmptyRowLayoutImpl implements RowLayout {

    /**
     * Fills row by empty strings<br>
     * {@inheritedDoc}
     */
    public List<String> fill(Map<String, String> context, int length) {
        List<String> row = new ArrayList<String>(length);

        for (int i = 0; i < length; i++) {
            row.add("");
        }

        return row;
    }
}