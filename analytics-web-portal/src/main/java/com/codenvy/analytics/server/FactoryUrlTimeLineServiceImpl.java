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


package com.codenvy.analytics.server;

import com.codenvy.analytics.metrics.*;
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.scripts.ScriptType;
import com.codenvy.analytics.scripts.executor.ScriptExecutor;
import com.codenvy.analytics.server.vew.template.Display;
import com.codenvy.analytics.shared.TableData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class FactoryUrlTimeLineServiceImpl {

    private static final Logger    LOGGER           = LoggerFactory.getLogger(FactoryUrlTimeLineServiceImpl.class);
    private static final String    FILE_NAME_PREFIX = "factory-url-timeline";
    private static final Display[] DISPLAYS         =
            new Display[]{Display.initialize("view/factory-url-time-line-1.xml"),
                          Display.initialize("view/factory-url-time-line-2.xml")};


    public List<TableData> getData(TimeUnit timeUnit, Map<String, String> filter) {
        try {
            Map<String, String> context = Utils.initializeContext(timeUnit);

            if (filter.isEmpty()) {
                try {
                    return PersisterUtil.loadTablesFromBinFile(getFileName(context) + PersisterUtil.BIN_EXT);
                } catch (FileNotFoundException e) {
                    // let's calculate then
                }

                return calculateAndSave(context);
            } else {
                context.putAll(filter);
                return doFilter(context);
            }
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private List<TableData> doFilter(Map<String, String> context) throws Exception {
        List<String> factoryUrls = new ArrayList<>();
        if (context.containsKey(MetricFilter.FILTER_WS.name())) {
            factoryUrls = getFactoryUrls("ws", context.get(MetricFilter.FILTER_WS.name()));
            context.remove(MetricFilter.FILTER_WS.name());

        } else if (context.containsKey(MetricFilter.FILTER_USER.name())) {
            factoryUrls = getFactoryUrls("user", context.get(MetricFilter.FILTER_USER.name()));
            context.remove(MetricFilter.FILTER_USER.name());

        } else if (context.containsKey(MetricFilter.FILTER_REPO_URL.name())) {
            factoryUrls = getFactoryUrls("repoUrl", context.get(MetricFilter.FILTER_REPO_URL.name()));
            context.remove(MetricFilter.FILTER_REPO_URL.name());

        } else if (context.containsKey(MetricFilter.FILTER_FACTORY_URL.name())) {
            factoryUrls = Arrays.asList(context.get(MetricFilter.FILTER_FACTORY_URL.name()).split(","));
            context.remove(MetricFilter.FILTER_FACTORY_URL.name());

        } else if (context.containsKey(MetricFilter.FILTER_PROJECT_TYPE.name())) {
            factoryUrls = getFactoryUrls("type", context.get(MetricFilter.FILTER_PROJECT_TYPE.name()));
            context.remove(MetricFilter.FILTER_PROJECT_TYPE.name());
        }
        List<String> tempWs = getTempWs(factoryUrls);

        context.put(MetricFilter.FILTER_FACTORY_URL.name(), Utils.removeBracket(factoryUrls.toString()));
        List<TableData> data = DISPLAYS[0].retrieveData(context);

        context.remove(MetricFilter.FILTER_FACTORY_URL.name());
        context.put(MetricFilter.FILTER_WS.name(), Utils.removeBracket(tempWs.toString()));
        data.addAll(DISPLAYS[1].retrieveData(context));

        return data;
    }

    private List<String> getTempWs(List<String> factoryUrls) throws IOException {
        Map<String, String> context = Utils.newContext();

        MetricParameter.TO_DATE.putDefaultValue(context);
        MetricParameter.LOAD_DIR.put(context, Utils.getLoadDirFor(MetricType.FACTORY_URL_ACCEPTED));
        MetricParameter.PARAM.put(context, factoryUrls.toString());

        ListStringValueData valueData =
                (ListStringValueData)ScriptExecutor.INSTANCE
                                                   .executeAndReturn(ScriptType.TEMP_WS_BY_FACTORY_URL, context);
        return valueData.getAll();
    }

    private List<String> getFactoryUrls(String field, String param) throws IOException {
        Map<String, String> context = Utils.newContext();

        MetricParameter.TO_DATE.putDefaultValue(context);
        MetricParameter.LOAD_DIR.put(context, Utils.getLoadDirFor(MetricType.FACTORY_CREATED));
        MetricParameter.FIELD.put(context, field);
        MetricParameter.PARAM.put(context, param);

        ListStringValueData valueData =
                (ListStringValueData)ScriptExecutor.INSTANCE
                                                   .executeAndReturn(ScriptType.FACTORY_URL_BY_ENTITY, context);

        return valueData.getAll();
    }

    private List<TableData> calculateAndSave(Map<String, String> context) throws Exception {
        List<TableData> data = new ArrayList<>();
        for (Display display : DISPLAYS) {
            data.addAll(display.retrieveData(context));
        }

        PersisterUtil.saveTablesToCsvFile(data, getFileName(context) + PersisterUtil.CSV_EXT);
        PersisterUtil.saveTablesToBinFile(data, getFileName(context) + PersisterUtil.BIN_EXT);

        return data;
    }

    /** Updates time-line fully. */
    public void update() {
        try {
            calculateAndSave(Utils.initializeContext(TimeUnit.DAY));
            calculateAndSave(Utils.initializeContext(TimeUnit.WEEK));
            calculateAndSave(Utils.initializeContext(TimeUnit.MONTH));
            calculateAndSave(Utils.initializeContext(TimeUnit.LIFETIME));
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /** @return corresponding file name */
    private String getFileName(Map<String, String> context) {
        TimeUnit timeUnit = Utils.getTimeUnit(context);
        return FILE_NAME_PREFIX + "_" + timeUnit.name();
    }
}
