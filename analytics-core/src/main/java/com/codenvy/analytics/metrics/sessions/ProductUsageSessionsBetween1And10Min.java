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
package com.codenvy.analytics.metrics.sessions;

import com.codenvy.analytics.metrics.MetricType;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ProductUsageSessionsBetween1And10Min extends AbstractProductUsageSessions {

    public ProductUsageSessionsBetween1And10Min() {
        super(MetricType.PRODUCT_USAGE_SESSIONS_BETWEEN_1_AND_10_MIN, 1 * 60, 10 * 60, false, false);
    }

    @Override
    public String getDescription() {
        return "The number of all sessions in persistent workspaces with duration between 1 and 10 minutes " +
               "exclusively";
    }
}
