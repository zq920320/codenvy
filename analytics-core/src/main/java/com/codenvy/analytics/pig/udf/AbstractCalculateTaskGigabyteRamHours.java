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
package com.codenvy.analytics.pig.udf;

import com.codenvy.analytics.datamodel.DoubleValueData;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.AbstractMetric;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.Summaraziable;
import org.apache.pig.data.Tuple;

import java.io.IOException;
import java.util.Map;

/** @author Dmytro Nochevnov */
public abstract class AbstractCalculateTaskGigabyteRamHours extends CalculateGigabyteRamHours {
    private String taskType;

    public AbstractCalculateTaskGigabyteRamHours(String taskType) {
        this.taskType = taskType;
    }

    /** {@inheritDoc} */
    @Override
    public Double exec(Tuple input) throws IOException {
        if (input == null || input.size() < 1) {
            return null;
        }

        String factory_id = (String) input.get(0);
        if (factory_id == null || factory_id.isEmpty()) {
            return null;
        }

        return getGigabyteRamHours(factory_id);
    }

    private Double getGigabyteRamHours(String factory_id) throws IOException {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.FACTORY_ID, factory_id);
        builder.put(MetricFilter.TASK_TYPE, taskType);
        builder.put(Parameters.PAGE, 1);
        builder.put(Parameters.PER_PAGE, 10);

        try {
            Metric metric = MetricFactory.getMetric(MetricType.TASKS_LIST);
            ListValueData summaryValue = (ListValueData)((Summaraziable)metric).getSummaryValue(builder.build());

            if (summaryValue.size() == 0) {
                return null;
            }

            Map<String, ValueData> valueMap = ((MapValueData)summaryValue.getAll().get(0)).getAll();
            if (!valueMap.containsKey(AbstractMetric.GIGABYTE_RAM_HOURS)) {
                return null;
            }

            return ((DoubleValueData) valueMap.get(AbstractMetric.GIGABYTE_RAM_HOURS)).getAsDouble();
        } catch (NullPointerException e) {
            return null;
        }
    }
}
