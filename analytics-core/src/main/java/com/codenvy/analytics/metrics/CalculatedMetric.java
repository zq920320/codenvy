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

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class CalculatedMetric extends AbstractMetric {

    protected final Metric[] basedMetric;

    protected CalculatedMetric(MetricType metricType, MetricType[] basedMetricTypes) {
        super(metricType);

        this.basedMetric = new Metric[basedMetricTypes.length];
        for (int i = 0; i < basedMetricTypes.length; i++) {
            this.basedMetric[i] = MetricFactory.getMetric(basedMetricTypes[i]);
        }
    }

    public CalculatedMetric(String metricType, String[] basedMetricTypes) {
        super(metricType);

        this.basedMetric = new Metric[basedMetricTypes.length];
        for (int i = 0; i < basedMetricTypes.length; i++) {
            this.basedMetric[i] = MetricFactory.getMetric(basedMetricTypes[i]);
        }
    }
    
    protected CalculatedMetric(MetricType metricType, Metric[] basedMetric) {
        this(metricType.name(), basedMetric);
    }
    
    public CalculatedMetric(String metricType, Metric[] basedMetric) {
        super(metricType);
        this.basedMetric = basedMetric;
    }

}
