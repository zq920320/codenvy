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


package com.codenvy.analytics.old_metrics;

/**
 * Number of users, who created at least one project.
 *
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class UsersSentInviteOnceMetric extends ToDateValueReadBasedMetric {

    UsersSentInviteOnceMetric() {
        super(MetricType.USERS_SENT_INVITE_ONCE);
    }

    @Override
    public String getDescription() {
        return "The number of users sent invite at least once";
    }
}
