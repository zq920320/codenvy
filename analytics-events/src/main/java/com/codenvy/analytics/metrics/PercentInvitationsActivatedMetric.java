/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.DoubleValueManager;
import com.codenvy.analytics.scripts.ScriptParameters;
import com.codenvy.analytics.scripts.ValueManager;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class PercentInvitationsActivatedMetric extends AbstractMetric {

    private final Metric totalMetric;
    private final Metric relativeMetric;

    PercentInvitationsActivatedMetric() throws IOException {
        super(MetricType.PERCENT_INVITATIONS_ACTIVATED);

        this.totalMetric = MetricFactory.createMetric(MetricType.INVITATIONS_SENT);
        this.relativeMetric = MetricFactory.createMetric(MetricType.USERS_ADDED_TO_WORKSPACE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object evaluateValue(Map<String, String> context) throws IOException {
        Map<String, Long> values = (Map<String, Long>)relativeMetric.getValue(context);
        Long value = values.containsKey("invite") ? values.get("invite") : Long.valueOf(0);

        Long total = (Long)totalMetric.getValue(context);
        return Double.valueOf(100D * value / total);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "% Invitations Accepted";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<ScriptParameters> getMandatoryParams() {
        Set<ScriptParameters> params = totalMetric.getMandatoryParams();
        params.addAll(relativeMetric.getMandatoryParams());

        return params;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<ScriptParameters> getAdditionalParams() {
        Set<ScriptParameters> params = totalMetric.getAdditionalParams();
        params.addAll(relativeMetric.getAdditionalParams());
        params.removeAll(getMandatoryParams());

        return params;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ValueManager getValueManager() {
        return new DoubleValueManager();
    }
}
