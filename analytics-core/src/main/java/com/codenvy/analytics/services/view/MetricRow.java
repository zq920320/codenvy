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


package com.codenvy.analytics.services.view;


import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.value.DoubleValueData;
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.StringValueData;
import com.codenvy.analytics.metrics.value.ValueData;

import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class MetricRow extends AbstractRow {

    private static final String NAME        = "name";
    private static final String DESCRIPTION = "description";
    private static final String FORMAT      = "format";

    private static final String DEFAULT_FORMAT = "%.0f";

    private final Metric metric;
    private final String format;

    public MetricRow(Map<String, String> parameters) {
        super(parameters);

        metric = MetricFactory.getMetric(parameters.get(NAME));
        format = parameters.containsKey(FORMAT) ? parameters.get(FORMAT) : DEFAULT_FORMAT;
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getDescription() throws IOException {
        return new StringValueData(parameters.get(DESCRIPTION));
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getData(Map<String, String> context) throws IOException {
        ValueData valueData = getMetricValue(context);
        return format(valueData);
    }

    private ValueData format(ValueData valueData) {
        Class<? extends ValueData> clazz = valueData.getClass();

        if (clazz == StringValueData.class) {
            return valueData;

        } else if (clazz == LongValueData.class) {
            long value = ((LongValueData)valueData).getAsLong();
            String formattedValue = value == 0 ? "" : String.format(format, value);

            return new StringValueData(formattedValue);

        } else if (clazz == DoubleValueData.class) {
            double value = ((DoubleValueData)valueData).getAsDouble();
            String formattedValue = value == 0 || Double.isInfinite(value) || Double.isNaN(value)
                                    ? ""
                                    : String.format(format, value);

            return new StringValueData(formattedValue);
        }

        return valueData;
    }


    protected ValueData getMetricValue(Map<String, String> context) throws IOException {
        return metric.getValue(context);
    }
}