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

import com.codenvy.analytics.metrics.AbstractListValueResulted;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.PrecomputedDataMetric;
import com.codenvy.analytics.metrics.ReadBasedSummariziable;
import com.mongodb.DBObject;

import javax.annotation.security.RolesAllowed;

/** @author Anatoliy Bazko */
@RolesAllowed({})
public class ProjectsStatisticsListPrecomputed extends AbstractListValueResulted implements PrecomputedDataMetric, ReadBasedSummariziable {

    public ProjectsStatisticsListPrecomputed() {
        super(MetricType.PROJECTS_STATISTICS_LIST_PRECOMPUTED);
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "Projects statistics";
    }

    /** {@inheritDoc} */
    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.PROJECTS_STATISTICS_PRECOMPUTED);
    }

    /** {@inheritDoc} */
    @Override
    public String[] getTrackedFields() {
        return new String[]{PROJECT_ID,
                            PROJECT,
                            WS,
                            BUILDS,
                            BUILD_TIME,
                            BUILD_WAITING_TIME,
                            RUNS,
                            RUN_TIME,
                            RUN_WAITING_TIME,
                            DEBUGS,
                            DEBUG_TIME,
                            DEPLOYS,
                            PROJECT_CREATES,
                            PROJECT_DESTROYS,
                            PROJECT_TYPE,
                            DATE,
                            USER
        };
    }

    /** {@inheritDoc} */
    @Override
    public DBObject[] getSpecificSummarizedDBOperations(Context clauses) {
        ReadBasedSummariziable summariziable = (ReadBasedSummariziable)MetricFactory.getMetric(getBasedMetric());
        return summariziable.getSpecificSummarizedDBOperations(clauses);
    }

    /** {@inheritDoc} */
    @Override
    public Context getContextForBasedMetric() {
        return Context.EMPTY;
    }

    /** {@inheritDoc} */
    @Override
    public MetricType getBasedMetric() {
        return MetricType.PROJECTS_STATISTICS_LIST;
    }

    /** {@inheritDoc} */
    @Override
    public boolean canReadPrecomputedData(Context context) {
        return true;
    }
}
