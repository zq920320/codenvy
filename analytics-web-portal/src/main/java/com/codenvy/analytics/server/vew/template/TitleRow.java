/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.vew.template;


import com.codenvy.analytics.shared.RowData;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
class TitleRow implements Row {

    private static final String ATTRIBUTE_NAME = "name";

    private final String[]      titles;

    /**
     * {@link TitleRow} constructor.
     */
    private TitleRow(String[] titles) {
        this.titles = titles;
    }

    /**
     * {@inheritedDoc}
     */
    public List<RowData> fill(Map<String, String> context, int length) {
        RowData row = new RowData();

        int count = Math.min(length, titles.length);
        for (int i = 0; i < count; i++) {
            row.add(titles[i]);
        }

        for (int i = count; i < length; i++) {
            row.add("");
        }

        ArrayList<RowData> result = new ArrayList<>();
        result.add(row);

        return result;
    }

    /** Factory method */
    public static TitleRow initialize(Element element) {
        String names = element.getAttribute(ATTRIBUTE_NAME);
        return new TitleRow(names.split(","));
    }
}