/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.vew.layout;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ViewLayout {

    private final Map<String, String>   attributes;
    private final List<List<RowLayout>> layout;

    public ViewLayout(Map<String, String> attributes, List<List<RowLayout>> layout) {
        this.attributes = attributes;
        this.layout = Collections.unmodifiableList(layout);
    }

    /**
     * @return {@link #attributes}
     */
    public Map<String, String> getAttributes() {
        return attributes;
    }

    /**
     * @return {@link #layout}
     */
    public List<List<RowLayout>> getLayout() {
        return layout;
    }
}
