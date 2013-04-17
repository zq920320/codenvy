/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class PaasDeploymentTypesMetric extends ScriptBasedMetric {

    PaasDeploymentTypesMetric() {
        super(MetricType.PAAS_DEPLOYEMNT_TYPES);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "PAAS Deployment Types";
    }

    @Override
    protected ScriptType getScriptType() {
        return ScriptType.DETAILS_APPLICATION_CREATED_PAAS;
    }
}
