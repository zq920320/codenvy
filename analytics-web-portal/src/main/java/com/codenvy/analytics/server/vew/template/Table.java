/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.vew.template;

import com.codenvy.analytics.shared.TableData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class Table {

    private static final String       ATTRIBUTE_LENGTH = "length";

    private final Map<String, String> attributes;
    private final List<Row>           rows;

    /**
     * {@link Table} constructor.
     */
    public Table(Map<String, String> attributes, List<Row> rows) {
        this.attributes = new HashMap<>(attributes);
        this.rows = new ArrayList<>(rows);
    }

    public TableData retrieveData(Map<String, String> context) throws Exception {
        TableData data = new TableData(attributes);

        int length = getLength();
        for (Row row : rows) {
            data.addAll(row.fill(context, length));
        }

        return data;
    }

    public List<Row> getRows() {
        return Collections.unmodifiableList(rows);
    }

    public int getLength() {
        return Integer.valueOf(attributes.get(ATTRIBUTE_LENGTH));
    }
}
