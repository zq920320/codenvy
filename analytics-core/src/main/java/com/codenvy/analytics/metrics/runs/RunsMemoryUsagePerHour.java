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
package com.codenvy.analytics.metrics.runs;

import com.codenvy.analytics.datamodel.DoubleValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.CalculatedMetric;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Expandable;
import com.codenvy.analytics.metrics.MetricType;

import javax.annotation.security.RolesAllowed;
import java.io.IOException;

import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsLong;

/** @author Anatoliy Bazko */
@RolesAllowed(value = {"user", "system/admin", "system/manager"})
public class RunsMemoryUsagePerHour extends CalculatedMetric implements Expandable {

    public RunsMemoryUsagePerHour() {
        super(MetricType.RUNS_MEMORY_USAGE_PER_HOUR, new MetricType[]{MetricType.RUNS_TIME,
                                                                      MetricType.RUNS_MEMORY_USAGE});
    }

    @Override
    public ValueData getValue(Context context) throws IOException {
        double hours = getAsLong(basedMetric[0], context).getAsDouble() / (1000 * 60 * 60); // in hours
        double gb = getAsLong(basedMetric[1], context).getAsDouble() / 1024; // in GB
        return new DoubleValueData(gb / hours);
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return DoubleValueData.class;
    }

    @Override
    public String getDescription() {
        return "The memory usage in GB per hour";
    }

    @Override
    public ValueData getExpandedValue(Context context) throws IOException {
        return ((Expandable)basedMetric[0]).getExpandedValue(context);
    }

    @Override
    public String getExpandedField() {
        return ((Expandable)basedMetric[0]).getExpandedField();
    }
}
