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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
public class ProjectsRunnedJRebelMetric extends ValueReadBasedMetric {

    ProjectsRunnedJRebelMetric() {
        super(MetricType.PROJECT_RUNNED_WITH_JREBEL);
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    /** {@inheritDoc} */
    @Override
    public Set<Parameters> getParams() {
        return new LinkedHashSet<>(
                Arrays.asList(new Parameters[]{Parameters.FROM_DATE,
                                                    Parameters.TO_DATE}));
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "Total number of projects run with JRebel configured";
    }
}
