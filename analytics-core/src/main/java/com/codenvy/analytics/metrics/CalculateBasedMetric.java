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

import com.codenvy.analytics.metrics.value.ValueData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

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
            LOGGER.debug("Calculation " + getType() + " with context " + context.toString());
        }
        return evaluate(context);
    }

    /** Evaluating */
    protected abstract ValueData evaluate(Map<String, String> context) throws IOException;
}
