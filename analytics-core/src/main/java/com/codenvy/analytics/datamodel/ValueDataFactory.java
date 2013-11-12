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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ValueDataFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ValueDataFactory.class);

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
        }

        throw new IllegalArgumentException("Unknown class " + clazz.getName());
    }


    /**
     * Creates appropriate {@link ValueData} based on given value.
     *
     * @param value
     * @return {@link ValueData}
     */
    public static ValueData createValueData(Object value) {
        Class<?> clazz = value.getClass();

        if (clazz == String.class) {
            return new StringValueData((String)value);

        } else if (clazz == LongValueData.class) {
            return new LongValueData((Long)value);

        }
        if (clazz == DoubleValueData.class) {
            return new DoubleValueData((Double)value);
        }

        throw new IllegalArgumentException("Unknown class " + clazz.getName());
    }
}
