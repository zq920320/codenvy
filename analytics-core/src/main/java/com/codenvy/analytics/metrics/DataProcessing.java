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

import com.codenvy.analytics.metrics.value.*;
import com.codenvy.analytics.scripts.ScriptType;
import com.codenvy.analytics.scripts.executor.ScriptExecutor;

import java.io.IOException;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class DataProcessing {

    /**
     * Executes scripts to calculate the total number of events by workspace names.
     *
     * @param metricType the {@link MetricType} under which resulted {@link ValueData} will be stored
     * @param context    the execution context
     */
    public static void calculateForWs(MetricType metricType, Map<String, String> context) throws Exception {
        calculate(metricType, context, EnumSet.of(ScriptType.NUMBER_OF_EVENTS, ScriptType.NUMBER_OF_EVENTS_BY_WS));
    }

    /**
     * Executes scripts to calculate the total number of events by workspace, user and domain names.
     *
     * @param metricType the {@link MetricType} under which resulted {@link ValueData} will be stored
     * @param context    the execution context
     */
    public static void calculateForWsUser(MetricType metricType, Map<String, String> context) throws Exception {
        calculate(metricType, context, EnumSet.of(ScriptType.NUMBER_OF_EVENTS,
                ScriptType.NUMBER_OF_EVENTS_BY_WS,
                ScriptType.NUMBER_OF_EVENTS_BY_USERS,
                ScriptType.NUMBER_OF_EVENTS_BY_DOMAINS));
    }

    /**
     * Executes predefined set of {@link ScriptType}.
     */
    private static void calculate(MetricType metricType, Map<String, String> context, EnumSet<ScriptType> scripts) throws Exception {
        ScriptExecutor executor = ScriptExecutor.INSTANCE;

        for (ScriptType scriptType : scripts) {
            switch (scriptType) {
                case NUMBER_OF_EVENTS:
                    ValueData result = executor.executeAndReturn(scriptType, context);
                    storeNumberOfEvents((LongValueData) result, metricType, context);
                    break;

                case NUMBER_OF_EVENTS_BY_DOMAINS:
                case NUMBER_OF_EVENTS_BY_USERS:
                case NUMBER_OF_EVENTS_BY_WS:
                    result = executor.executeAndReturn(scriptType, context);
                    storeNumberOfEventsByEntity((MapStringLongValueData) result, metricType, getEntity(scriptType), context);
                    break;

                default:
                    throw new IllegalStateException("Script " + scriptType + " is not supported");
            }
        }
    }

    /**
     * Extracts entity out of {@link ScriptType}. The entity name is included into script name.
     */
    private static MetricParameter.ENTITY_TYPE getEntity(ScriptType scriptType) {
        String scriptName = scriptType.name();

        int pos = scriptName.indexOf("_BY_");
        String entityName = scriptName.substring(pos + 4, scriptName.length());

        return MetricParameter.ENTITY_TYPE.valueOf(entityName);
    }

    private static void storeNumberOfEvents(LongValueData valueData, MetricType metricType, Map<String, String> context) throws IOException {
        if (isEmpty(valueData)) {
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
        if (isEmpty(valueData)) {
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

    /**
     * Checks if corresponding {@link ValueData} class contains data.
     * Empty {@link ValueData} is not going to be stored. It improves performance a bit.
     */
    private static boolean isEmpty(ValueData valueData) {
        if (valueData instanceof LongValueData) {
            return valueData.getAsLong() == 0;

        } else if (valueData instanceof DoubleValueData) {
            return valueData.getAsDouble() == 0;

        } else if (valueData instanceof StringValueData) {
            return valueData.getAsString().isEmpty();

        } else if (valueData instanceof MapValueData) {
            return ((MapValueData) valueData).size() == 0;

        } else if (valueData instanceof ListValueData) {
            return ((ListValueData) valueData).size() == 0;

        } else {
            throw new IllegalArgumentException("ValueData class is not supported" + valueData.getClass().getName());
        }
    }
}
