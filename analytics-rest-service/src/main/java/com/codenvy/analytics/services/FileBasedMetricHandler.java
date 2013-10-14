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

package com.codenvy.analytics.services;

import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricParameter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.services.model.MetricPojo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:dkuleshov@codenvy.com">Dmitry Kuleshov</a> */
public class FileBasedMetricHandler implements MetricHandler {

    private static final Set<MetricParameter> sampleParameterSet = new LinkedHashSet<>();

    static {
        sampleParameterSet.add(MetricParameter.FROM_DATE);
        sampleParameterSet.add(MetricParameter.TO_DATE);
    }

    public String getMetricValue(String metricName, Map<String, String> executionContext) throws IOException {
        return MetricFactory.createMetric(metricName).getValue(executionContext).getAsString();
    }

    public MetricPojo getMetricInfo(String metricName) {
        Metric metric = MetricFactory.createMetric(metricName);
        if (sampleParameterSet.equals(metric.getParams())) {
            return generateMetricPojo(metricName, metric);
        }
        //  TODO consider throwing exception instead of returning null-value
        return null;
    }

    public List<MetricPojo> getAllMetricsInfo() {
        List<MetricPojo> metricPojos = new ArrayList<>();
        for (MetricType metricType : MetricType.values()) {
            Metric metric = MetricFactory.createMetric(metricType);
            if (sampleParameterSet.equals(metric.getParams())) {
                metricPojos.add(generateMetricPojo(metricType.name(), metric));
            }
        }
        return metricPojos;
    }

    private MetricPojo generateMetricPojo(String metricName, Metric metric) {
        MetricPojo metricPojo = new MetricPojo();
        metricPojo.setName(metricName);
        metricPojo.setDescription(metric.getDescription());

        return metricPojo;
    }
}
