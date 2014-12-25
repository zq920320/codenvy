/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
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
package com.codenvy.analytics.metrics.tasks;

import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.datamodel.ValueDataFactory;
import com.codenvy.analytics.metrics.CalculatedMetric;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Expandable;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricType;

import java.io.IOException;

/** @author Dmytro Nochevnov */
public abstract class AbstractTasksMetric extends CalculatedMetric implements Expandable {

    public AbstractTasksMetric(MetricType metric, MetricType... basedMetricTypes) {
        super(metric, basedMetricTypes);
    }

    /** {@inheritDoc} */
    @Override
    public String getExpandedField() {
        return TASK_ID;
    }

    @Override
    public ValueData getValue(Context context) throws IOException {
        ValueData value = ValueDataFactory.createDefaultValue(getValueDataClass());

        for (Metric metric : basedMetric) {
            value = value.add(metric.getValue(context));
        }

        return value;
    }

    @Override
    public ValueData getExpandedValue(Context context) throws IOException {
        ValueData result = ListValueData.DEFAULT;

        for (Metric metric : basedMetric) {
            ValueData expandedValue = ((Expandable)metric).getExpandedValue(context);
            result = result.add(expandedValue);
        }

        return result;
    }
}
