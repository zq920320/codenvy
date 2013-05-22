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
public class UsersAddedToWsFilter extends AbstractFilter {

    public UsersAddedToWsFilter(ListListStringValueData valueData) {
        super(valueData);
    }


    /** {@inheritDoc} */
    @Override
    protected int getIndex(MetricFilter key) throws IllegalArgumentException {
        switch (key) {
            case FILTER_WS:
            case FILTER_USER:
                return key.ordinal();
            case FILTER_USER_ADDED_FROM:
                return 2;
            default:
                throw new IllegalArgumentException();
        }
    }
}
