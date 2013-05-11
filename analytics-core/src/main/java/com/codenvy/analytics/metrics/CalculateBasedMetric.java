/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codenvy.analytics.metrics.value.ValueData;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class CalculateBasedMetric extends AbstractMetric {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(CalculateBasedMetric.class);

    CalculateBasedMetric(MetricType metricType) {
        super(metricType);
    }

    /** {@inheritedDoc} */
    public ValueData getValue(Map<String, String> context) throws IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.info("Calculation " + getType() + " with context " + context.toString());
        }
        return evaluate(context);
    }
}
