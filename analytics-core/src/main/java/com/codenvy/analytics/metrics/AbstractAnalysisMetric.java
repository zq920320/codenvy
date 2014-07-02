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
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author Anatoliy Bazko
 */
public abstract class AbstractAnalysisMetric extends AbstractActiveEntities implements WithoutFromDateParam {

    public AbstractAnalysisMetric(MetricType metricType, MetricType basedMetric, String valueField) {
        super(metricType, basedMetric, valueField);
    }

    @Override
    public Context applySpecificFilter(Context clauses) throws IOException {
        Context.Builder builder = new Context.Builder(clauses);
        builder.remove(Parameters.FROM_DATE);

        if (!clauses.exists(MetricFilter.USER)) {
            builder.put(MetricFilter.REGISTERED_USER, 1);
        }

        return super.applySpecificFilter(builder.build());
    }
}
