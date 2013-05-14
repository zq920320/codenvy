/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value.filters;

import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.value.ValueData;

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
     * @throws IllegalArgumentException if filter is not supported
     */
    int size(MetricFilter key, String value) throws IllegalArgumentException;

    /**
     * @return the size of groups
     * @throws IllegalArgumentException if filter is not supported
     */
    ValueData sizeOfGroups(MetricFilter key) throws IllegalArgumentException;

    /**
     * @return applied filter
     * @throws IllegalArgumentException if filter is not supported
     */
    ValueData apply(MetricFilter key, String value) throws IllegalArgumentException;

    /**
     * @return available values for given filter
     * @throws IllegalArgumentException if filter is not supported
     */
    ValueData getAvailable(MetricFilter key) throws IllegalArgumentException;
}
