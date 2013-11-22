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

import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;

import java.io.IOException;
import java.util.Map;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ProjectPaasAny extends CalculatedMetric {

    public ProjectPaasAny() {
        super(MetricType.PROJECT_PAAS_ANY, new MetricType[]{MetricType.PROJECT_PAAS_APPFOG,
                                                            MetricType.PROJECT_PAAS_AWS,
                                                            MetricType.PROJECT_PAAS_CLOUDBEES,
                                                            MetricType.PROJECT_PAAS_CLOUDFOUNDRY,
                                                            MetricType.PROJECT_PAAS_GAE,
                                                            MetricType.PROJECT_PAAS_HEROKU,
                                                            MetricType.PROJECT_PAAS_OPENSHIFT,
                                                            MetricType.PROJECT_PAAS_TIER3,
                                                            MetricType.PROJECT_PAAS_MANYAMO});

    }

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        long projectsAnyPaaS = 0;

        for (Metric metric : basedMetric) {
            projectsAnyPaaS += ((LongValueData)metric.getValue(context)).getAsLong();
        }

        return new LongValueData(projectsAnyPaaS);
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
