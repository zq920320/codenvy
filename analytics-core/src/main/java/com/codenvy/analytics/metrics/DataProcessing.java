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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class DataProcessing {

    /** Executes predefined set of {@link ScriptType}. */
    public static void calculateAndStore(MetricType metricType, Map<String, String> context) throws Exception {
        Utils.prepareDirectories(metricType);

        context = Utils.clone(context);
        metricType.modifyContext(context);

        ScriptExecutor executor = ScriptExecutor.INSTANCE;
        for (ScriptType scriptType : metricType.getScripts()) {
            ValueData result = executor.executeAndReturn(scriptType, context);

            if (isDefaultValue(result)) {
                continue;
            }

            store(result, metricType, scriptType, context);
        }
    }

    private static boolean isDefaultValue(ValueData result) throws IOException {
        return result.equals(ValueDataFactory.createDefaultValue(result.getClass()));
    }

    /** Stores {@link ValueData} under the directory related to given metric. */
    private static void store(ValueData valueData,
                              MetricType metricType,
                              ScriptType scriptType,
                              Map<String, String> context) throws IOException {

        LinkedHashMap<String, String> uuid = new LinkedHashMap<>(4);

        if (Utils.containsFromDateParam(context)) {
            Utils.putFromDate(uuid, Utils.getFromDate(context));
        }
        if (Utils.containsToDateParam(context)) {
            Utils.putToDate(uuid, Utils.getToDate(context));
        }

        MetricParameter[] resultScheme = scriptType.getResultScheme();
        if (resultScheme.length == 0) {
            doStore(valueData, metricType, uuid);

        } else {
            MetricParameter.ENTITY_TYPE entityType = getEntity(scriptType);

            Map<?, ?> items = ((MapValueData)valueData).getAll();

            for (Map.Entry<?, ?> entry : items.entrySet()) {
                Object key = entry.getKey();

                updateUUID(key, resultScheme, entityType, uuid);

                ValueData value2store = ValueDataFactory.createValueData(entry.getValue());
                doStore(value2store, metricType, uuid);
            }
        }
    }

    private static void doStore(ValueData valueData, MetricType metricType, LinkedHashMap<String, String> uuid)
            throws IOException {
        FSValueDataManager.storeValue(valueData, metricType, uuid);

        if (valueData instanceof ListValueData) {
            FSValueDataManager
                    .storeNumber(new LongValueData(((CollectionableValueData)valueData).size()), metricType, uuid);
        }
    }

    private static void updateUUID(Object key, MetricParameter[] resultScheme,
                                   MetricParameter.ENTITY_TYPE entityType,
                                   Map<String, String> uuid) {
        switch (resultScheme.length) {
            case 1:
                if (resultScheme[0] == MetricParameter.ALIAS) {
                    Utils.putEntity(uuid, entityType);
                }
                uuid.put(resultScheme[0].name(), key.toString());
                break;

            default:
                List<String> items = ((ListStringValueData)key).getAll();

                if (items.size() != resultScheme.length) {
                    throw new IllegalStateException("Result doesn't correspond to scheme");
                }

                for (int i = 0; i < items.size(); i++) {
                    if (resultScheme[i] == MetricParameter.ALIAS) {
                        Utils.putEntity(uuid, entityType);
                    }

                    uuid.put(resultScheme[i].name(), items.get(i));
                }
        }
    }

    /**
     * Extracts entity out of {@link ScriptType}.
     *
     * @return {@link MetricParameter.ENTITY_TYPE} or null
     */
    private static MetricParameter.ENTITY_TYPE getEntity(ScriptType scriptType) {
        String scriptName = scriptType.name();

        int pos = scriptName.indexOf("_BY_");
        String entityName = scriptName.substring(pos + 4, scriptName.length());

        return pos == -1 ? null : MetricParameter.ENTITY_TYPE.valueOf(entityName);
    }
}
