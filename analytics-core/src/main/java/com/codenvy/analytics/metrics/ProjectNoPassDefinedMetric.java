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

import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.ValueData;

import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ProjectNoPassDefinedMetric extends CalculatedMetric {

    ProjectNoPassDefinedMetric() {
        super(MetricType.PROJECT_NO_PAAS_DEFINED, MetricType.PROJECT_CREATED);
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        ValueData createdProjects = super.getValue(context);
        ValueData projectsAnyPaaS = MetricFactory.createMetric(MetricType.PROJECT_PAAS_ANY).getValue(context);

        return new LongValueData(createdProjects.getAsLong() - projectsAnyPaaS.getAsLong());
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "The number of created projects without PaaS defined";
    }
}
