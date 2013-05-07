/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value.filters;

import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ValueData;

/**
 * Wraps {@link ValueData} into easy to use object.
 * 
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public interface ValueDataFilter {

    /**
     * Process filtering upon passed parameters.
     * 
     * @param key the key, i.e. {@link Metric#USER_FILTER_PARAM} If key is unsupported will be done nothing.
     * @param value the value of filtering key
     * @return filtered result
     * @see Metric
     */
    public abstract ListListStringValueData doFilter(String key, String value);
}
