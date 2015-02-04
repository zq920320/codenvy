/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
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
import com.codenvy.analytics.metrics.Summaraziable;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** @author Anatoliy Bazko */
public class ValueDataUtil {


    /** Casts given value data to {@link com.codenvy.analytics.datamodel.LongValueData} and returns all its internal value */
    public static long treatAsLong(ValueData valueData) {
        return ((LongValueData)valueData).getAsLong();
    }

    /** Casts given value data to {@link com.codenvy.analytics.datamodel.DoubleValueData} and returns all its internal value */
    public static double treatAsDouble(ValueData valueData) {
        return ((DoubleValueData)valueData).getAsDouble();
    }

    /** Casts given value data to {@link com.codenvy.analytics.datamodel.MapValueData} and returns all internal values */
    public static Map<String, ValueData> treatAsMap(ValueData valueData) {
        return ((MapValueData)valueData).getAll();
    }

    /** Casts given value data to {@link com.codenvy.analytics.datamodel.ListValueData} and returns all internal values */
    public static List<ValueData> treatAsList(ValueData valueData) {
        return ((ListValueData)valueData).getAll();
    }

    /** Casts given value data to {@link com.codenvy.analytics.datamodel.SetValueData} and returns all internal values */
    public static Set<ValueData> treatAsSet(ValueData valueData) {
        return ((SetValueData)valueData).getAll();
    }

    /** @return the metric value and casts to {@link com.codenvy.analytics.datamodel.ListValueData} */
    public static ListValueData getAsList(Metric metric, Context context) throws IOException {
        return (ListValueData)metric.getValue(context);
    }

    /** @return the metric summary value */
    public static ListValueData getSummaryValue(Metric metric, Context context) throws IOException {
        return (ListValueData)((Summaraziable)metric).getSummaryValue(context);
    }

    /** @return the metric value and casts to {@link com.codenvy.analytics.datamodel.SetValueData} */
    public static SetValueData getAsSet(Metric metric, Context context) throws IOException {
        return (SetValueData)metric.getValue(context);
    }

    /** @return the metric value and casts to {@link com.codenvy.analytics.datamodel.LongValueData} */
    public static LongValueData getAsLong(Metric metric, Context context) throws IOException {
        return (LongValueData)metric.getValue(context);
    }

    /** @return the metric value and casts to {@link com.codenvy.analytics.datamodel.DoubleValueData} */
    public static DoubleValueData getAsDouble(Metric metric, Context context) throws IOException {
        return (DoubleValueData)metric.getValue(context);
    }

    /** Indicates if given value is default one */
    public static boolean isDefault(ValueData valueData) {
        return valueData.equals(ValueDataFactory.createDefaultValue(valueData.getClass()));
    }
}
