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


package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ProductUsageUsers1060Metric extends AbstractProductUsageUsersMetric {

    public ProductUsageUsers1060Metric() {
        super(MetricType.PRODUCT_USAGE_USERS_10_60, 10, 60);
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "The number of registered users who spent in product between 10 and 60 minutes";
    }
}
