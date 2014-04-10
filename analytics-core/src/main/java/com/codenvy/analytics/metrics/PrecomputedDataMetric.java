/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2014] Codenvy, S.A.
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
package com.codenvy.analytics.metrics;

/**
 * @author Alexander Reshetnyak
 * @author Anatoliy Bazko
 */
public interface PrecomputedDataMetric extends PrecomputedMetric {

    /**
     * @return {@link com.codenvy.analytics.metrics.Context} to be passed into based metric to retrieve data from
     */
    Context getContextForBasedMetric();

    /**
     * @return {@link com.codenvy.analytics.metrics.MetricType} which will return data to store into collection
     */
    MetricType getBasedMetric();
}
