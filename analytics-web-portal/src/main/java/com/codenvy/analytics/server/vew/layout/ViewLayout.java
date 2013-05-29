/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.vew.layout;

import com.codenvy.analytics.shared.TimeLineViewData;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ViewLayout {

    private final Map<String, String>   attributes;
    private final List<List<RowLayout>> rowLayouts;

    ViewLayout(Map<String, String> attributes, List<List<RowLayout>> rowLayouts) {
        this.attributes = attributes;
        this.rowLayouts = rowLayouts;
    }

    /**
     * @return {@link #attributes}
     */
    public Map<String, String> getAttributes() {
        return attributes;
    }

    /**
     * @return {@link #rowLayouts}
     */
    public List<List<RowLayout>> getLayout() {
        return rowLayouts;
    }

    /**
     * Layout initialization.
     */
    public static ViewLayout initialize(String resource) {
        try {
            return LayoutReader.read(resource);
        } catch (IOException | SAXException | ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Retrieves data based on given context.
     */
    public List<TimeLineViewData> retrieveData(Map<String, String> context, int length) throws Exception {
        List<TimeLineViewData> result = new ArrayList<TimeLineViewData>();

        for (List<RowLayout> rows : rowLayouts) {
            TimeLineViewData data = new TimeLineViewData();

            for (RowLayout row : rows) {
                data.add(row.fill(context, length));
            }

            result.add(data);
        }

        return result;
    }
}
