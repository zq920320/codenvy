/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.metrics.value.filters.Filter;
import com.codenvy.analytics.metrics.value.filters.UsersWorkspacesFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ActiveWorkspacesNumberMetric extends CalculateBasedMetric {

    private final Metric basedMetric;

    ActiveWorkspacesNumberMetric() throws IOException {
        super(MetricType.ACTIVE_WORKSPACES_NUMBER);
        this.basedMetric = MetricFactory.createMetric(MetricType.ACTIVE_USERS_WORKAPCES_LIST);
    }

    /** {@inheritDoc} */
    @Override
    public Set<MetricParameter> getParams() {
        return basedMetric.getParams();
    }

    /** {@inheritDoc} */
    @Override
    protected Class< ? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    /** {@inheritDoc} */
    @Override
    protected ValueData evaluate(Map<String, String> context) throws IOException {
        ListListStringValueData listVD = (ListListStringValueData)basedMetric.getValue(context);

        Filter filter = new UsersWorkspacesFilter(listVD);
        return new LongValueData(filter.getAvailable(MetricFilter.FILTER_WS).size());
    }
}
