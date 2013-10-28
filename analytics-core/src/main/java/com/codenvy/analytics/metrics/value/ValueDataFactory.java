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

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ValueDataFactory {

    /** Instantiates default {@link com.codenvy.analytics.old_metrics.value.ValueData}. */
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
}
