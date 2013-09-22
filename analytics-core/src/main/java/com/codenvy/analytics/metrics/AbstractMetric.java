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

import com.codenvy.analytics.metrics.value.SetStringValueData;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class AbstractMetric implements Metric {

    protected final MetricType metricType;

    AbstractMetric(MetricType metricType) {
        this.metricType = metricType;
    }

    /** Preparation unique sequences to identify stored value. */
    protected LinkedHashMap<String, String> makeUUID(Map<String, String> context) throws IOException {
        LinkedHashMap<String, String> keys = new LinkedHashMap<>();

        for (MetricParameter param : getParams()) {
            String paramKey = param.name();
            String paramValue = context.get(paramKey);

            if (paramValue == null) {
                throw new IOException("There is no parameter " + paramKey + " in context");
            }

            keys.put(paramKey, paramValue);
        }

        return keys;
    }

    /**
     * Altering context by replacing any available filter by {@link MetricFilter#WS} with values of names of temporary
     * workspaces. If it already exists in context then method does nothing.
     */
    protected Map<String, String> alterFactoryFilter(Map<String, String> context) throws IOException {
        context = Utils.clone(context);

        Set<MetricFilter> filters = Utils.getAvailableFilters(context);
        if (filters.isEmpty()) {
            return context;

        } else if (filters.size() > 1) {
            throw new IllegalStateException("It supports only single filter");

        } else {
            MetricFilter filter = filters.iterator().next();
            String value = filter.get(context);

            if (MetricFilter.WS == filter && value.toUpperCase().startsWith("TMP-")) {
                return  context;
            }

            Set<String> tmpWs = getTemporaryWsCreated(context);

            filter.remove(context);
            MetricFilter.WS.put(context, Utils.removeBracket(tmpWs.toString()));

            return context;
        }
    }

    /** Returns the list of created temporary workspace by given filter in context. */
    protected Set<String> getTemporaryWsCreated(Map<String, String> context) throws IOException {
        MetricFilter filter = Utils.getAvailableFilters(context).iterator().next();

        switch (filter) {
            case REFERRER_URL:
            case FACTORY_URL:
                return ((SetStringValueData)MetricFactory.createMetric(MetricType.FACTORY_URL_ACCEPTED)
                                                         .getValue(context)).getAll();

            case WS:
            case USERS:
            case AFFILIATE_ID:
            case ORG_ID:
            case PROJECT_TYPE:
            case REPOSITORY_URL:
                SetStringValueData factoryUrl =
                        (SetStringValueData)MetricFactory.createMetric(MetricType.SET_FACTORY_CREATED)
                                                         .getValue(context);

                context = Utils.cloneAndClearFilters(context);
                MetricFilter.FACTORY_URL.put(context, Utils.removeBracket(factoryUrl.getAll().toString()));

                return ((SetStringValueData)MetricFactory.createMetric(MetricType.FACTORY_URL_ACCEPTED)
                                                         .getValue(context)).getAll();

            default:
                throw new IllegalStateException("Unknown filter");
        }
    }
}
