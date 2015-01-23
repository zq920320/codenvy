/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
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
package com.codenvy.analytics.metrics.workspaces;

import com.codenvy.analytics.metrics.AbstractListValueResulted;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.PrecomputedDataMetric;
import com.codenvy.analytics.metrics.ReadBasedSummariziable;
import com.mongodb.DBObject;

import javax.annotation.security.RolesAllowed;

import static com.codenvy.analytics.Utils.getFilterAsSet;
import static com.codenvy.analytics.Utils.isTemporaryExist;

/**
 * @author Alexander Reshetnyak
 */
@RolesAllowed({})
public class WorkspacesStatisticsListPrecomputed extends AbstractListValueResulted implements PrecomputedDataMetric, ReadBasedSummariziable {

    public WorkspacesStatisticsListPrecomputed() {
        super(MetricType.WORKSPACES_STATISTICS_LIST_PRECOMPUTED);
    }

    @Override
    public String getDescription() {
        return "Workspaces' statistics data";
    }

    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.WORKSPACES_STATISTICS_PRECOMPUTED);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{WS,
                            PROJECTS,
                            SESSIONS,
                            TIME,
                            BUILDS,
                            BUILD_TIME,
                            BUILD_WAITING_TIME,
                            RUNS,
                            RUN_TIME,
                            RUN_WAITING_TIME,
                            DEBUGS,
                            DEBUG_TIME,
                            DEPLOYS,
                            FACTORIES,
                            INVITES,
                            WorkspacesStatisticsList.JOINED_USERS};
    }

    @Override
    public DBObject[] getSpecificSummarizedDBOperations(Context clauses) {
        ReadBasedSummariziable summariziable = (ReadBasedSummariziable)MetricFactory.getMetric(getBasedMetric());
        return summariziable.getSpecificSummarizedDBOperations(clauses);
    }

    @Override
    public Context getContextForBasedMetric() {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.WS, Parameters.WS_TYPES.PERSISTENT.name());
        return builder.build();
    }

    @Override
    public MetricType getBasedMetric() {
        return MetricType.WORKSPACES_STATISTICS_LIST;
    }

    @Override
    public boolean canReadPrecomputedData(Context context) {
        String value = context.getAsString(MetricFilter.WS_ID);
        return value == null || !isTemporaryExist(getFilterAsSet(value));
    }
}
