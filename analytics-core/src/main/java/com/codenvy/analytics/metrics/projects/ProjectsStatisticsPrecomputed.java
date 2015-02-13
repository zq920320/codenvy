/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.analytics.metrics.projects;

import com.codenvy.analytics.metrics.AbstractCount;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.PrecomputedMetric;

import javax.annotation.security.RolesAllowed;

/** @author Anatoliy Bazko */
@RolesAllowed({})
public class ProjectsStatisticsPrecomputed extends AbstractCount implements PrecomputedMetric {


    public ProjectsStatisticsPrecomputed() {
        super(MetricType.PROJECTS_STATISTICS_PRECOMPUTED,
              MetricType.PROJECTS_STATISTICS_LIST_PRECOMPUTED,
              PROJECT_ID);
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "The number of projects in statistics";
    }

    /** {@inheritDoc} */
    @Override
    public boolean canReadPrecomputedData(Context context) {
        return ((PrecomputedMetric)MetricFactory.getMetric(MetricType.PROJECTS_STATISTICS_LIST_PRECOMPUTED)).canReadPrecomputedData(context);
    }
}
