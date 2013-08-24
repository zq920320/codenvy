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
        context = Utils.clone(context);

        putLoadStoreDirectoriesIntoContext(metricType, context);
        metricType.modifyContext(context);

        Utils.initLoadStoreDirectories(context);

        ScriptExecutor executor = ScriptExecutor.INSTANCE;
        for (ScriptType scriptType : metricType.getScripts()) {
            ValueData result = executor.executeAndReturn(scriptType, context);

            if (isDefaultValue(result)) {
                continue;
            }

            store(result, metricType, scriptType, context);
        }

        Utils.initLoadStoreDirectories(context);
    }

    /**
     * If script requires {@link MetricParameter#LOAD_DIR} or {@link MetricParameter#STORE_DIR} then corresponding
     * parameters will be put into context automatically depending on {@link MetricType}
     */
    private static void putLoadStoreDirectoriesIntoContext(MetricType metricType, Map<String, String> context) {
        for (ScriptType scriptType : metricType.getScripts()) {
            if (scriptType.getParams().contains(MetricParameter.STORE_DIR)) {
                MetricParameter.STORE_DIR.put(context, Utils.getStoreDirFor(metricType));
            }

            if (scriptType.getParams().contains(MetricParameter.LOAD_DIR)) {
                MetricParameter.LOAD_DIR.put(context, Utils.getLoadDirFor(metricType));
            }
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

        Metric metric = MetricFactory.createMetric(metricType);

        if (metric.getParams().contains(MetricParameter.FROM_DATE)) {
            Utils.putFromDate(uuid, Utils.getFromDate(context));
        }
        if (metric.getParams().contains(MetricParameter.TO_DATE)) {
            Utils.putToDate(uuid, Utils.getToDate(context));
        }

        MetricParameter[] resultScheme = scriptType.getResultScheme();
        if (resultScheme.length == 0) {
            doStore(valueData, metricType, uuid);

        } else {
            MetricParameter.ENTITY_TYPES entityType = getEntity(scriptType);

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
                                   MetricParameter.ENTITY_TYPES entityType,
                                   Map<String, String> uuid) {
        switch (resultScheme.length) {
            case 1:
                if (resultScheme[0] == MetricParameter.ALIAS || resultScheme[0] == MetricParameter.URL) { // TODO
                    MetricParameter.ENTITY.put(uuid, entityType.name());
                }
                uuid.put(resultScheme[0].name(), key.toString());
                break;

            default:
                List<String> items = ((ListStringValueData)key).getAll();

                if (items.size() != resultScheme.length) {
                    throw new IllegalStateException("Result doesn't correspond to scheme");
                }

                for (int i = 0; i < items.size(); i++) {
                    if (resultScheme[i] == MetricParameter.ALIAS || resultScheme[i] == MetricParameter.URL) {
                        MetricParameter.ENTITY.put(uuid, entityType.name());
                    }

                    uuid.put(resultScheme[i].name(), items.get(i));
                }
        }
    }

    /**
     * Extracts entity out of {@link ScriptType}.
     *
     * @return {@link com.codenvy.analytics.metrics.MetricParameter.ENTITY_TYPES} or null
     */
    private static MetricParameter.ENTITY_TYPES getEntity(ScriptType scriptType) {
        String scriptName = scriptType.name();

        int pos = scriptName.indexOf("_BY_");
        String entityName = scriptName.substring(pos + 4, scriptName.length());

        return pos == -1 ? null : MetricParameter.ENTITY_TYPES.valueOf(entityName);
    }
}
