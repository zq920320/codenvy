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


package com.codenvy.analytics.datamodel;

import com.mongodb.BasicDBList;

import java.util.Arrays;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ValueDataFactory {

    /** Instantiates default {@link com.codenvy.analytics.datamodel.ValueData}. */
    public static ValueData createDefaultValue(Class<? extends ValueData> clazz) throws IllegalArgumentException {
        if (clazz == LongValueData.class) {
            return LongValueData.DEFAULT;

        } else if (clazz == DoubleValueData.class) {
            return DoubleValueData.DEFAULT;

        } else if (clazz == StringValueData.class) {
            return StringValueData.DEFAULT;

        } else if (clazz == MapValueData.class) {
            return MapValueData.DEFAULT;

        } else if (clazz == ListValueData.class) {
            return ListValueData.DEFAULT;

        } else if (clazz == SetValueData.class) {
            return SetValueData.DEFAULT;
        }

        throw new IllegalArgumentException("Unknown class " + clazz.getName());
    }


    /** Creates appropriate {@link ValueData} based on given value. */
    public static ValueData createValueData(Object value) {
        Class<?> clazz = value.getClass();

        if (clazz == String.class) {
            return StringValueData.valueOf((String)value);

        } else if (clazz == Long.class || clazz == Integer.class || clazz == Byte.class) {
            return LongValueData.valueOf(((Number)value).longValue());

        } else if (clazz == Double.class) {
            return DoubleValueData.valueOf((Double)value);

        } else if (clazz == BasicDBList.class) {
            return StringValueData.valueOf(Arrays.toString(((BasicDBList)value).toArray()));
        }

        throw new IllegalArgumentException("Unknown class " + clazz.getName());
    }
}
