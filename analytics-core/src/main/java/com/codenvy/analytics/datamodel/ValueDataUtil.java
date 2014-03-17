/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2014] Codenvy, S.A.
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

import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Metric;

import java.io.IOException;

/** @author Anatoliy Bazko */
public class ValueDataUtil {

    public static NumericValueData getAsNumeric(Metric metric, Context context) throws IOException {
        return (NumericValueData)metric.getValue(context);
    }

    public static LongValueData getAsLong(Metric metric, Context context) throws IOException {
        return (LongValueData)metric.getValue(context);
    }

    public static long treatAsLong(ValueData valueData) {
        return ((LongValueData)valueData).getAsLong();
    }

    public static ListValueData getAsList(Metric metric, Context context) throws IOException {
        return (ListValueData)metric.getValue(context);
    }
}
