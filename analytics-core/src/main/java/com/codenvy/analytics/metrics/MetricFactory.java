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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class MetricFactory {

    private static final Logger                            LOG     = LoggerFactory.getLogger(MetricFactory.class);
    private static final ConcurrentHashMap<String, Metric> metrics = new ConcurrentHashMap<>();

    static {
        try {

            for (Metric metric : ServiceLoader.load(Metric.class)) {
                Metric existed = metrics.put(metric.getName(), metric);
                if (existed != null) {
                    throw new IllegalStateException("There is 2 metrics with name " + existed.getName());
                }
            }
        } catch (Throwable e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

    public static Metric getMetric(MetricType metricType) {
        return getMetric(metricType.toString());
    }

    public static Metric getMetric(String name) {
        Metric metric = metrics.get(name.toLowerCase());
        if (metric == null) {
            throw new MetricNotFoundException("There is no metric with name " + name);
        }

        return metric;
    }

    public static Metric[] getAllMetrics() {
        return metrics.values().toArray(new Metric[metrics.size()]);
    }
}
