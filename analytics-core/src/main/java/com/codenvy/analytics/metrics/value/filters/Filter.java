/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
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
