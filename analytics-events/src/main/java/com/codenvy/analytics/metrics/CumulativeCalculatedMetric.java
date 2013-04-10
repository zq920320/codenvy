/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptParameters;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public abstract class CumulativeCalculatedMetric extends AbstractMetric {

    private final MetricType addedType;
    private final MetricType removedType;

    CumulativeCalculatedMetric(MetricType metricType, MetricType addedType, MetricType removedType) {
        super(metricType);
        this.addedType = addedType;
        this.removedType = removedType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<ScriptParameters> getMandatoryParams() {
        Set<ScriptParameters> params = addedType.getInstance().getMandatoryParams();
        params.addAll(removedType.getInstance().getMandatoryParams());

        return params;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<ScriptParameters> getAdditionalParams() {
        Set<ScriptParameters> params = addedType.getInstance().getAdditionalParams();
        params.addAll(removedType.getInstance().getAdditionalParams());
        params.removeAll(getMandatoryParams());

        return params;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object queryValue(Map<String, String> context) throws IOException {
        long addedEntities = (Long)addedType.getInstance().getValue(context);
        long removedEntities = (Long)removedType.getInstance().getValue(context);

        try {
            TimeIntervalUtil.prevDateInterval(context);
        } catch (ParseException e) {
            throw new IOException(e);
        } catch (IllegalArgumentException e) {
            throw new IOException(e);
        }

        long previousEntities = (Long)metricType.getInstance().getValue(context);

        return new Long(previousEntities + addedEntities - removedEntities);
    }
}
