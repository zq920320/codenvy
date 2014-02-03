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
package com.codenvy.analytics.metrics.users;

import com.codenvy.analytics.metrics.AbstractListValueResulted;
import com.codenvy.analytics.metrics.MetricType;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class UsersActivityList extends AbstractListValueResulted {

    public static final String MESSAGE = "message";
    public static final String EVENT = "event";
    public static final String USER = "user";
    public static final String WS = "ws";

    public UsersActivityList() {
        super(MetricType.USERS_ACTIVITY_LIST);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{DATE, MESSAGE, EVENT, WS, USER};
    }

    @Override
    public String getDescription() {
        return "Users' actions";
    }
}
