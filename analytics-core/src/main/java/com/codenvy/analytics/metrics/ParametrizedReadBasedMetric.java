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

import com.codenvy.analytics.datamodel.ValueData;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class ParametrizedReadBasedMetric extends ReadBasedMetric {

    protected ParametrizedReadBasedMetric(String metricName) {
        super(metricName);
    }

    protected ParametrizedReadBasedMetric(MetricType metricType) {
        super(metricType);
    }

    /** {@inheritDoc} */
    @Override
    protected ValueData loadValue(Map<String, String> context) throws IOException {
        return dataLoader.loadValue(this, context);
    }

    @Override
    public Set<Parameters> getParams() {
        return new HashSet<>(
                Arrays.asList(new Parameters[]{Parameters.FROM_DATE, Parameters.TO_DATE, Parameters.PARAM}));
    }
}
