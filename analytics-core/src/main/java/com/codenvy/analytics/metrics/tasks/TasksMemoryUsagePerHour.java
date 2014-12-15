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

import com.codenvy.analytics.datamodel.DoubleValueData;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.datamodel.ValueDataUtil;
import com.codenvy.analytics.metrics.CalculatedMetric;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Expandable;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricType;

import javax.annotation.security.RolesAllowed;
import java.io.IOException;

/** @author Dmytro Nochevnov */
@RolesAllowed(value = {"user", "system/admin", "system/manager"})
public class TasksMemoryUsagePerHour extends CalculatedMetric implements Expandable {

    public TasksMemoryUsagePerHour() {
        super(MetricType.TASKS_MEMORY_USAGE_PER_HOUR, new MetricType[]{MetricType.BUILDS_MEMORY_USAGE_PER_HOUR,
                                                            MetricType.RUNS_MEMORY_USAGE_PER_HOUR});

    }

    @Override
    public ValueData getValue(Context context) throws IOException {
        double sum = 0;

        for (Metric metric : basedMetric) {
            sum += ValueDataUtil.getAsDouble(metric, context).getAsDouble();
        }

        return new DoubleValueData(sum);
    }

    /** {@inheritDoc} */
    @Override
    public String getExpandedField() {
        return PROJECT_ID;
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

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return DoubleValueData.class;
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "The tasks memory usage in GB per hour";
    }

}
