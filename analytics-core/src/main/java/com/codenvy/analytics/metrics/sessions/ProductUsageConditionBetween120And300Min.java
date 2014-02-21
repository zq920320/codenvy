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
package com.codenvy.analytics.metrics.sessions;

import com.codenvy.analytics.metrics.MetricType;

import javax.annotation.security.RolesAllowed;

/** @author Anatoliy Bazko */
@RolesAllowed({"system/admin", "system/manager"})
public class ProductUsageConditionBetween120And300Min extends AbstractProductUsageCondition {

    public ProductUsageConditionBetween120And300Min() {
        super(MetricType.PRODUCT_USAGE_CONDITION_BETWEEN_120_AND_300_MIN,
              120 * 60 * 1000,
              300 * 60 * 1000,
              true,
              false,
              "$and",
              5,
              Integer.MAX_VALUE,
              true,
              true);
    }

    @Override
    public String getDescription() {
        return "The number of users who have the number of sessions more or equal to 5 and usage time more or equal " +
               "to 120 minutes and usage time less than 300 minutes";
    }
}
