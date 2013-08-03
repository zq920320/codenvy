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
     */
    public static void numberOfEventsByWs(MetricType metricType, Map<String, String> context) throws Exception {
        calculateAndStore(metricType, context, EnumSet.of(ScriptType.NUMBER_EVENTS, ScriptType.NUMBER_EVENTS_BY_WS));
    }

    /**
     * Executes scripts to calculate the total number of events by workspace, user and domain names.
     */
    public static void numberOfEventsByAll(MetricType metricType, Map<String, String> context) throws Exception {
        calculateAndStore(metricType, context, EnumSet.of(ScriptType.NUMBER_EVENTS,
                ScriptType.NUMBER_EVENTS_BY_WS,
                ScriptType.NUMBER_EVENTS_BY_USERS,
                ScriptType.NUMBER_EVENTS_BY_DOMAINS));
    }

    /**
     * Executes scripts to alculate the set of active users by workspace, user and domain names.
     */
    public static void setOfActiveUsers(MetricType metricType, Map<String, String> context) throws Exception {
        calculateAndStore(metricType, context, EnumSet.of(ScriptType.SET_ACTIVE_USERS,
                ScriptType.SET_ACTIVE_USERS_BY_DOMAINS,
                ScriptType.SET_ACTIVE_USERS_BY_USERS,
                ScriptType.SET_ACTIVE_USERS_BY_WS));
    }

    /**
     * Executes predefined set of {@link ScriptType}.
     */
    private static void calculateAndStore(MetricType metricType, Map<String, String> context, EnumSet<ScriptType> scripts) throws Exception {
        ScriptExecutor executor = ScriptExecutor.INSTANCE;

        for (ScriptType scriptType : scripts) {
            ValueData result = executor.executeAndReturn(scriptType, context);

            if (isDefaultValue(result)) {
                continue;
            }

            String entityName = getEntity(scriptType);
            if (entityName.isEmpty()) {
                store(result, metricType, context);
            } else {
                MetricParameter.ENTITY_TYPE entityType = MetricParameter.ENTITY_TYPE.valueOf(entityName);
                storeByEntity((MapValueData) result, metricType, entityType, context);
            }
        }
    }

    private static boolean isDefaultValue(ValueData result) throws IOException {
        return result.equals(ValueDataFactory.createDefaultValue(result.getClass()));
    }

    /**
     * Stores {@link ValueData} under the directory related to given metric.
     */
    private static void store(ValueData valueData, MetricType metricType, Map<String, String> context) throws IOException {
        LinkedHashMap<String, String> uuid = new LinkedHashMap<>(2);

        Utils.putFromDate(uuid, Utils.getFromDate(context));
        Utils.putToDate(uuid, Utils.getToDate(context));

        FSValueDataManager.store(valueData, metricType, uuid);
    }

    /**
     * Stores {@link ValueData} under the directory related to given metric and entity.
     */
    private static void storeByEntity(MapValueData<?, ?> valueData, MetricType metricType, MetricParameter.ENTITY_TYPE entityType, Map<String, String> context) throws IOException {
        LinkedHashMap<String, String> uuid = new LinkedHashMap<>(4);

        Utils.putFromDate(uuid, Utils.getFromDate(context));
        Utils.putToDate(uuid, Utils.getToDate(context));
        Utils.putEntity(uuid, entityType);

        for (Map.Entry<?, ?> entry : valueData.getAll().entrySet()) {
            String alias = entry.getKey().toString();
            ValueData value2store = ValueDataFactory.createValueData(entry.getValue());

            uuid.put(MetricParameter.ALIAS.name(), alias);
            FSValueDataManager.store(value2store, metricType, uuid);
        }
    }

    /**
     * Extracts entity out of {@link ScriptType}. The entity name is included into script name.
     */
    private static String getEntity(ScriptType scriptType) {
        String scriptName = scriptType.name();

        int pos = scriptName.indexOf("_BY_");
        return pos == - 1 ? "" : scriptName.substring(pos + 4, scriptName.length());
    }
}
