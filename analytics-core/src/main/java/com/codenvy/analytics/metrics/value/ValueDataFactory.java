/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */


package com.codenvy.analytics.metrics.value;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;

import java.util.*;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ValueDataFactory {

    /** Instantiates default {@link com.codenvy.analytics.metrics.value.ValueData}. */
    public static ValueData createDefaultValue(Class<? extends ValueData> clazz) throws IllegalArgumentException {
        if (clazz == LongValueData.class) {
            return LongValueData.DEFAULT;

        } else if (clazz == DoubleValueData.class) {
            return DoubleValueData.DEFAULT;

        } else if (clazz == StringValueData.class) {
            return StringValueData.DEFAULT;

        } else if (clazz == RowValueData.class) {
            return RowValueData.DEFAULT;

        } else if (clazz == ListValueData.class) {
            return ListValueData.DEFAULT;
        }


        throw new IllegalArgumentException("Unknown class " + clazz.getName());
    }

    public static ValueData createdValueData(Class<? extends ValueData> clazz, ResultSet resultSet) {
        List<RowValueData> rows = readRows(resultSet);

        if ((clazz == LongValueData.class || clazz == DoubleValueData.class || clazz == StringValueData.class)
            && rows.size() == 1
            && rows.get(0).size() == 1) {

            ValueData valueData = rows.get(0).getAll().values().iterator().next();
            if (valueData.getClass() == clazz) {
                return valueData;
            }
        } else if (clazz == RowValueData.class || rows.size() == 1) {
            return rows.get(0);

        } else if (clazz == ListValueData.class) {
            return new ListValueData(rows);
        }

        throw new IllegalArgumentException("Unknown class " + clazz.getName() + " or wrong data structure");
    }

    private static List<RowValueData> readRows(ResultSet resultSet) {
        List<RowValueData> rows = new ArrayList<>();

        Iterator<Row> iterator = resultSet.iterator();
        while (iterator.hasNext()) {
            Row row = iterator.next();
            ColumnDefinitions definitions = row.getColumnDefinitions();

            Map<String, ValueData> map = new HashMap<>(definitions.size());

            for (int i = 1; i < definitions.size(); i++) {
                ValueData valueData;

                DataType type = definitions.getType(i);
                switch (type.getName()) {
                    case BIGINT:
                        valueData = new LongValueData(row.getLong(i));
                        break;
                    case DOUBLE:
                        valueData = new DoubleValueData(row.getDouble(i));
                        break;
                    case TEXT:
                        valueData = new StringValueData(row.getString(i));
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported data type " + type.getName());
                }

                map.put(definitions.getName(i), valueData);
            }

            rows.add(new RowValueData(map));
        }

        return rows;
    }
}
