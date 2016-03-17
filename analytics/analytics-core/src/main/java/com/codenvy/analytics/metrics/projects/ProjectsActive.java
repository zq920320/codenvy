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

import com.codenvy.analytics.metrics.AbstractActiveEntities;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;

import java.io.IOException;

/**
 * @author Alexander Reshetnyak
 */
public class ProjectsActive extends AbstractActiveEntities {

    public ProjectsActive() {
        super(MetricType.PROJECTS_ACTIVE, MetricType.PROJECTS_STATISTICS, PROJECT_ID);
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "The number of active projects";
    }

    /** {@inheritDoc} */
    @Override
    public Context applySpecificFilter(Context clauses) throws IOException {
        Context.Builder builder = new Context.Builder(super.applySpecificFilter(clauses));
        if (!clauses.exists(MetricFilter.WS_ID)) {
            builder.put(MetricFilter.PERSISTENT_WS, 1);
        }
        return builder.build();
    }
}
