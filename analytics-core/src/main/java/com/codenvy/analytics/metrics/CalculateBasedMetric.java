/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;



/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class CalculateBasedMetric extends AbstractMetric {

    CalculateBasedMetric(MetricType metricType) {
        super(metricType);
    }
}
