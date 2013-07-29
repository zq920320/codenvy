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

import com.codenvy.analytics.metrics.ValueFromMapMetric.ValueType;
import com.codenvy.analytics.metrics.value.DoubleValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.metrics.value.filters.Filter;

import java.io.IOException;
import java.util.Map;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ProjectCreatedTypeJavaWarNumberMetric extends AbstractProjectsCreatedMetric {

    ProjectCreatedTypeJavaWarNumberMetric() {
        super(MetricType.PROJECT_TYPE_JAVA_WAR_NUMBER, MetricFactory.createMetric(MetricType.PROJECTS_CREATED_LIST), "War",
              ValueType.NUMBER);
    }

    /** {@inheritedDoc} */
    @Override
    public ValueData evaluate(Map<String, String> context) throws IOException {
        Filter filter = getFilter(basedMetric.getValue(context));

        double v1 = filter.size(MetricFilter.FILTER_PROJECT_TYPE, types[0]);
        double v2 = filter.size(MetricFilter.FILTER_PROJECT_TYPE, "Java");
        double result = (v1 + v2);

        return new DoubleValueData(result);
    }
}
