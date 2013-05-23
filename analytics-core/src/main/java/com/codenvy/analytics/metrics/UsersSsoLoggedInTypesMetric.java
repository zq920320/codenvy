/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.MapStringLongValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.metrics.value.filters.UsersSSOLoggedInFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class UsersSsoLoggedInTypesMetric extends CalculateBasedMetric {

    private final UsersSsoLoggedInListMetric basedMetric;

    UsersSsoLoggedInTypesMetric() throws IOException {
        super(MetricType.USERS_SSO_LOGGED_IN_TYPES);
        this.basedMetric = (UsersSsoLoggedInListMetric)MetricFactory.createMetric(MetricType.USERS_SSO_LOGGED_IN_LIST);
    }

    /** {@inheritDoc} */
    public Set<MetricParameter> getParams() {
        return basedMetric.getParams();
    }

    /** {@inheritDoc} */
    @Override
    protected Class< ? extends ValueData> getValueDataClass() {
        return MapStringLongValueData.class;
    }

    /** {@inheritDoc} */
    @Override
    protected ValueData evaluate(Map<String, String> context) throws IOException {
        ListListStringValueData value = (ListListStringValueData)basedMetric.getValue(context);

        UsersSSOLoggedInFilter filter = basedMetric.createFilter(value);
        filter = basedMetric.createFilter(filter.getUniqueLoggedInEvents());

        return new MapStringLongValueData(filter.sizeOfGroups(MetricFilter.FILTER_USER_SSO_LOGGEDIN_USING));
    }
}
