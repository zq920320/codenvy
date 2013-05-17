/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value.filters;

import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;

/**
 * <li>0 - the workspace name</li><br>
 * <li>1 - the user name</li><br>
 * <li>2 - the session start time</li><br>
 * <li>3 - the session duration</li><br>
 * 
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ProductUsageTimeFilter extends AbstractFilter {

    private final static int SESSION_DURATION = 3;
    
    public ProductUsageTimeFilter(ListListStringValueData valueData) {
        super(valueData);
    }

    /**
     * @return total time for all sessions
     */
    public long getTotalUsageTime() {
        long total = 0;

        for (ListStringValueData item : valueData.getAll()) {
            total += Long.valueOf(item.getAll().get(SESSION_DURATION));
        }

        return total / 60;
    }

    protected int getIndex(MetricFilter key) throws IllegalArgumentException {
        switch (key) {
            case FILTER_WS:
            case FILTER_USER:
                return key.ordinal();
            default:
                throw new IllegalArgumentException();
        }
    }
}
