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

import com.codenvy.analytics.old_metrics.value.LongValueData;
import com.codenvy.analytics.old_metrics.value.ValueData;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ProjectPaasAnyMetric extends ListMetric {

    public ProjectPaasAnyMetric() {
        super(MetricType.PROJECT_PAAS_ANY, new MetricType[]{MetricType.PROJECT_PAAS_APPFOG,
                                                            MetricType.PROJECT_PAAS_AWS,
                                                            MetricType.PROJECT_PAAS_CLOUDBEES,
                                                            MetricType.PROJECT_PAAS_CLOUDFOUNDRY,
                                                            MetricType.PROJECT_PAAS_GAE,
                                                            MetricType.PROJECT_PAAS_HEROKU,
                                                            MetricType.PROJECT_PAAS_OPENSHIFT,
                                                            MetricType.PROJECT_PAAS_TIER3});
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "The number of created project with some PaaS defined";
    }
}
