/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.vew.template;


import com.codenvy.analytics.shared.RowData;

import org.w3c.dom.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
class TitleRow extends AbstractRow {

    private static final String ATTRIBUTE_NAME = "name";

    private final String[] titles;

    /** {@link TitleRow} constructor. */
    private TitleRow(String[] titles) {
        this.titles = titles;
    }

    /** {@inheritDoc} */
    @Override
    protected String doRetrieve(Map<String, String> context, int columnNumber) throws IOException {
        if (columnNumber < titles.length) {
            return titles[columnNumber];
        } else {
            return "";
        }
    }

    /** Factory method */
    public static TitleRow initialize(Element element) {
        String names = element.getAttribute(ATTRIBUTE_NAME);
        return new TitleRow(names.split(","));
    }
}