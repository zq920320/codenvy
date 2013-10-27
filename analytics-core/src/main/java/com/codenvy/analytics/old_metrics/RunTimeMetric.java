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
package com.codenvy.analytics.old_metrics;

import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.old_metrics.value.LongValueData;
import com.codenvy.analytics.old_metrics.value.ValueData;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
public class RunTimeMetric extends ValueReadBasedMetric {

    RunTimeMetric() {
        super(MetricType.RUN_TIME);
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        ValueData valueData = super.getValue(context);
        return new LongValueData(valueData.getAsLong() / 60);
    }

    /** {@inheritDoc} */
    @Override
    public Set<Parameters> getParams() {
        return new LinkedHashSet<>(
                Arrays.asList(new Parameters[]{Parameters.FROM_DATE,
                                                    Parameters.TO_DATE}));
    }

    @Override
    public String getDescription() {
        return "The total time of all runs in seconds";
    }
}