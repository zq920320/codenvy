/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value.filters;

import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.value.ValueData;

import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public interface Filter {

    /**
     * @return the number of elements
     */
    int size();

    /**
     * @return the number of elements if given filer will be applied
     */
    int size(MetricFilter key, String value);

    /**
     * @return the size of groups
     */
    Map<String, Long> sizeOfGroups(MetricFilter key);

    /**
     * @return applied filter
     */
    ValueData apply(MetricFilter key, String value);

    /**
     * @return available values for given filter
     */
    Set<String> getAvailable(MetricFilter key);
}
