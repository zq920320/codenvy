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

package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.FSValueDataManager;
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.MapStringLongValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.scripts.ScriptType;
import com.codenvy.analytics.scripts.executor.ScriptExecutor;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class DataProcessing {

    /**
     * Executes all scripts to calculated number of events by different entities and store results then.
     *
     * @param metricType the {@link MetricType} under which resulted {@link ValueData} will be stored
     * @param context    the execution context
     */
    public static void runFor(MetricType metricType, Map<String, String> context) throws Exception {
        ScriptExecutor executor = ScriptExecutor.INSTANCE;

        ValueData result = executor.executeAndReturn(ScriptType.NUMBER_OF_EVENTS, context);
        storeNumberOfEvents((LongValueData) result, metricType, context);

        result = executor.executeAndReturn(ScriptType.NUMBER_OF_EVENTS_BY_USERS, context);
        storeNumberOfEventsByEntity((MapStringLongValueData) result, metricType, MetricParameter.ENTITY_TYPE.USERS, context);

        result = executor.executeAndReturn(ScriptType.NUMBER_OF_EVENTS_BY_WS, context);
        storeNumberOfEventsByEntity((MapStringLongValueData) result, metricType, MetricParameter.ENTITY_TYPE.WS, context);

        result = executor.executeAndReturn(ScriptType.NUMBER_OF_EVENTS_BY_DOMAINS, context);
        storeNumberOfEventsByEntity((MapStringLongValueData) result, metricType, MetricParameter.ENTITY_TYPE.DOMAINS, context);
    }

    private static void storeNumberOfEvents(LongValueData valueData, MetricType metricType, Map<String, String> context) throws IOException {
        if (valueData.getAsLong() == 0) { // TODO let's do it in much smarter way
            return;
        }

        LinkedHashMap<String, String> uuid = new LinkedHashMap<>(2);

        Utils.putFromDate(uuid, Utils.getFromDate(context));
        Utils.putToDate(uuid, Utils.getToDate(context));

        FSValueDataManager.store(valueData, metricType, uuid);
    }

    /**
     * Stores {@link ValueData} under the directory related to given entity.
     */
    private static void storeNumberOfEventsByEntity(MapStringLongValueData valueData, MetricType metricType, MetricParameter.ENTITY_TYPE entityType, Map<String, String> context) throws IOException {
        if (valueData.size() == 0) { // TODO let's do it in much smarter way
            return;
        }

        LinkedHashMap<String, String> uuid = new LinkedHashMap<>(4);

        Utils.putFromDate(uuid, Utils.getFromDate(context));
        Utils.putToDate(uuid, Utils.getToDate(context));
        uuid.put(MetricParameter.ENTITY.name(), entityType.name());

        for (Map.Entry<String, Long> entry : valueData.getAll().entrySet()) {
            String alias = entry.getKey();
            Long value = entry.getValue();

            uuid.put(MetricParameter.ALIAS.name(), alias);
            FSValueDataManager.store(new LongValueData(value), metricType, uuid);
        }
    }
}
