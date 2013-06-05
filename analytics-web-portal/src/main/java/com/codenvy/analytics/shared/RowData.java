/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.shared;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class RowData extends ArrayList<String> {

    private static final long   serialVersionUID = 1L;

    private Map<String, String> attributes;

    /**
     * Default {@link RowData} constructor for serialization.
     */
    public RowData() {
    }

    /**
     * {@link RowData} constructor.
     */
    public RowData(Map<String, String> attributes) {
        this.attributes = attributes;
    }
}
