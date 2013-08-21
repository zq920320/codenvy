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
import com.codenvy.analytics.metrics.value.SetStringValueData;
import com.codenvy.analytics.scripts.ScriptType;
import com.codenvy.analytics.scripts.executor.ScriptExecutor;
import com.codenvy.analytics.server.vew.template.Display;
import com.codenvy.analytics.shared.TableData;

import java.io.IOException;
import java.util.*;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class FactoryUrlTimeLineServiceImpl extends AbstractService {

    private static final String    FILE_NAME_PREFIX = "factory-url-timeline";
    private static final Display[] DISPLAYS         =
            new Display[]{Display.initialize("view/factory-url-time-line-1.xml"),
                          Display.initialize("view/factory-url-time-line-2.xml")};

    public List<TableData> getData(TimeUnit timeUnit, Map<String, String> filter) {
        try {
            Map<String, String> context = Utils.initializeContext(timeUnit);
            context.putAll(filter);

            return super.getData(context, !filter.isEmpty());
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    protected List<TableData> doFilter(Map<String, String> context) throws Exception {
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
        Set<String> tempWs = getTempWs(factoryUrls, context);

        context.put(MetricFilter.FILTER_FACTORY_URL.name(), Utils.removeBracket(factoryUrls.toString()));
        List<TableData> data = DISPLAYS[0].retrieveData(context);

        context.remove(MetricFilter.FILTER_FACTORY_URL.name());
        context.put(MetricFilter.FILTER_WS.name(), Utils.removeBracket(tempWs.toString()));
        data.addAll(DISPLAYS[1].retrieveData(context));

        return data;
    }

    private Set<String> getTempWs(List<String> factoryUrls, Map<String, String> context) throws IOException {
        Metric metric = MetricFactory.createMetric(MetricType.FACTORY_URL_ACCEPTED);

        Map<String, String> clonedContext = Utils.clone(context);
        MetricFilter.FILTER_FACTORY_URL.put(clonedContext, Utils.removeBracket(factoryUrls.toString()));

        SetStringValueData value = (SetStringValueData)metric.getValue(clonedContext);

        return value.getAll();
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

    /** {@inheritDoc} */
    @Override
    protected String getFileName(Map<String, String> context) {
        TimeUnit timeUnit = Utils.getTimeUnit(context);
        return FILE_NAME_PREFIX + "_" + timeUnit.name();
    }

    /** {@inheritDoc} */
    @Override
    protected Display[] getDisplays() {
        return DISPLAYS;
    }
}
