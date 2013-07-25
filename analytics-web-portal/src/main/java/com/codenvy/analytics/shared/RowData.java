/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.shared;

import java.util.ArrayList;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class RowData extends ArrayList<String> {

    private static final long serialVersionUID = 1L;
    private Map<String, String> attributes;

    /** Default {@link RowData} constructor for serialization. */
    public RowData() {
    }

    /** {@link RowData} constructor. */
    public RowData(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public String getCsv() {
        StringBuffer rowBuffer = new StringBuffer(this.size());

        for (String element : this) {
            StringBuffer elementBuffer = new StringBuffer(element.replace("\"", "\"\""));
            elementBuffer.insert(0, '\"');
            elementBuffer.append('\"');
            rowBuffer.append(element);
            rowBuffer.append(",");
        }
        rowBuffer.deleteCharAt(rowBuffer.lastIndexOf(","));

        return rowBuffer.toString();
    }
}
