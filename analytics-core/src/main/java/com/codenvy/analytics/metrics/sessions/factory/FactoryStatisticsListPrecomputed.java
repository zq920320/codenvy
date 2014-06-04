/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2014] Codenvy, S.A.
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
package com.codenvy.analytics.metrics.sessions.factory;

import com.codenvy.analytics.metrics.*;
import com.mongodb.DBObject;

import javax.annotation.security.RolesAllowed;

/**
 * @author Alexander Reshetnyak
 */
@RolesAllowed({})
@OmitFilters({MetricFilter.WS, MetricFilter.PERSISTENT_WS})
public class FactoryStatisticsListPrecomputed extends AbstractListValueResulted implements PrecomputedDataMetric, ReadBasedSummariziable {

    public FactoryStatisticsListPrecomputed() {
        super(MetricType.FACTORY_STATISTICS_LIST_PRECOMPUTED);
    }

    @Override
    public String getDescription() {
        return "The statistic of factory";
    }

    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.FACTORY_STATISTICS_PRECOMPUTED);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{FACTORY,
                            TIME,
                            RUNS,
                            DEPLOYS,
                            BUILDS,
                            SESSIONS,
                            AUTHENTICATED_SESSION,
                            CONVERTED_SESSION,
                            WS_CREATED,
                            ORG_ID,
                            PROJECT_TYPE};
    }

    @Override
    public DBObject[] getSpecificSummarizedDBOperations(Context clauses) {
        ReadBasedSummariziable summariziable = (ReadBasedSummariziable)MetricFactory.getMetric(getBasedMetric());
        return summariziable.getSpecificSummarizedDBOperations(clauses);
    }

    @Override
    public Context getContextForBasedMetric() {
        return Context.EMPTY;
    }

    @Override
    public MetricType getBasedMetric() {
        return MetricType.FACTORY_STATISTICS_LIST;
    }


    @Override
    public boolean canReadPrecomputedData(Context context) {
        return true;
    }
}
