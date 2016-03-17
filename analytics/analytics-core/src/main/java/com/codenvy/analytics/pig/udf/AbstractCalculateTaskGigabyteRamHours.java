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
package com.codenvy.analytics.pig.udf;

import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.AbstractMetric;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;

import org.apache.pig.data.Tuple;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsList;
import static com.codenvy.analytics.datamodel.ValueDataUtil.getSummaryValue;
import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsDouble;
import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsList;
import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsLong;
import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsMap;

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

        Double result = Double.valueOf(0);
        try {
            Metric metric = MetricFactory.getMetric(MetricType.TASKS_LIST);

            ListValueData summaryValue = getSummaryValue(metric, builder.build());
            if (!summaryValue.isEmpty()) {
                List<ValueData> l = treatAsList(summaryValue);
                Map<String, ValueData> m = treatAsMap(l.get(0));
                if (!m.containsKey(AbstractMetric.GIGABYTE_RAM_HOURS)) {
                    result += treatAsDouble(m.get(AbstractMetric.GIGABYTE_RAM_HOURS));
                }
            }

            /** DataIntegrity starts later, that's why we have to take into account empty GIGABYTE_RAM_HOURS fields */
            builder.put(AbstractMetric.GIGABYTE_RAM_HOURS, null);
            List<ValueData> l = treatAsList(getAsList(metric, builder.build()));
            for (ValueData v : l) {
                Map<String, ValueData> m = treatAsMap(v);
                if (m.containsKey(AbstractMetric.MEMORY) && m.containsKey(AbstractMetric.START_TIME) && m.containsKey(AbstractMetric.STOP_TIME)) {
                    result += calculateGigabyteRamHours(treatAsLong(m.get(AbstractMetric.MEMORY)),
                                                        treatAsLong(m.get(AbstractMetric.STOP_TIME)) - treatAsLong(m.get(AbstractMetric.START_TIME)));
                }
            }

            return result;
        } catch (Exception e) {
            return null;
        }
    }
}
