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

import com.codenvy.analytics.metrics.AbstractListValueResulted;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.PrecomputedDataMetric;

import javax.annotation.security.RolesAllowed;

/**
 * @author Alexander Reshetnyak
 */
@RolesAllowed({})
public class FactoryStatisticsListPrecomputed extends AbstractListValueResulted implements PrecomputedDataMetric {

    public FactoryStatisticsListPrecomputed() {
        super(MetricType.FACTORY_STATISTICS_LIST_PRECOMPUTED);
    }

    @Override
    public String getDescription() {
        return "The statistic of factory";
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{FACTORY,
                            TIME,
                            RUN,
                            DEPLOY,
                            BUILD,
                            SESSIONS,
                            AUTHENTICATED_SESSION,
                            CONVERTED_SESSION,
                            WS_CREATED};
    }

    @Override
    public Context getContextForBasedMetric() {
        return Context.EMPTY;
    }

    @Override
    public MetricType getBasedMetric() {
        return MetricType.FACTORY_STATISTICS_LIST;
    }
}
