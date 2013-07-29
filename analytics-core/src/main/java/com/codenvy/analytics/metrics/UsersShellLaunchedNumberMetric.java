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

import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.metrics.value.filters.Filter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class UsersShellLaunchedNumberMetric extends  CalculateBasedMetric {

    private final UsersShellLaunchedListMetric basedMetric;

    public UsersShellLaunchedNumberMetric() {
        super(MetricType.USERS_SHELL_LAUNCHED_NUMBER);
        basedMetric = (UsersShellLaunchedListMetric) MetricFactory.createMetric(MetricType.USERS_SHELL_LAUNCHED_LIST);
    }

    @Override
    protected ValueData evaluate(Map<String, String> context) throws IOException {
        Filter filter = basedMetric.createFilter(basedMetric.getValue(context));
        return new LongValueData(filter.getAvailable(MetricFilter.FILTER_USER).size());
    }

    @Override
    protected Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    @Override
    public Set<MetricParameter> getParams() {
        return basedMetric.getParams();
    }
}
