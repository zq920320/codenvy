/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptExecutor;
import com.codenvy.analytics.scripts.ScriptParameters;
import com.codenvy.analytics.scripts.ScriptType;

import java.io.IOException;
import java.util.Map;
import java.util.Set;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public abstract class ScriptCalculatedMetric extends AbstractMetric {

    ScriptCalculatedMetric(MetricType metricType) {
        super(metricType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<ScriptParameters> getMandatoryParams() {
        return getScriptType().getMandatoryParams();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<ScriptParameters> getAdditionalParams() {
        return getScriptType().getAdditionalParams();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object queryValue(Map<String, String> context) throws IOException {
        ScriptExecutor executor = getScriptExecutor(getScriptType());
        executor.setParams(context);

        return executor.executeAndReturnResult();
    }

    /**
     * @return corresponding {@link ScriptType} for metric calculation.
     */
    abstract protected ScriptType getScriptType();
}
