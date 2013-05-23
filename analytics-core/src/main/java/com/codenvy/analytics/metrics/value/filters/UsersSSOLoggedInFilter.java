/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value.filters;

import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.value.ListListStringValueData;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class UsersSSOLoggedInFilter extends AbstractFilter {

    public UsersSSOLoggedInFilter(ListListStringValueData valueData) {
        super(valueData);
    }

    public ListListStringValueData getUniqueLoggedInEvents() {
        return getUniqueActions(MetricFilter.FILTER_USER, MetricFilter.FILTER_USER_SSO_LOGGEDIN_USING);
    }

    /** {@inheritDoc} */
    @Override
    protected int getIndex(MetricFilter key) throws IllegalArgumentException {
        switch (key) {
            case FILTER_USER:
                return 0;
            case FILTER_USER_SSO_LOGGEDIN_USING:
                return 1;
            default:
                throw new IllegalArgumentException();
        }
    }

}
