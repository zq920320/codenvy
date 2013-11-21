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

/**
 * Predefined metrics.
 *
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public enum MetricType {
    CREATED_WORKSPACES,
    DESTROYED_WORKSPACES,
    TOTAL_WORKSPACES,
    ACTIVE_WORKSPACES,
    NON_ACTIVE_WORKSPACES,
    NEW_ACTIVE_WORKSPACES,
    RETURNING_ACTIVE_WORKSPACES,
    ACTIVE_WORKSPACES_LIST,

    CREATED_USERS,
    CREATED_USERS_FROM_FACTORY,
    CREATED_USERS_FROM_AUTH,
    REMOVED_USERS,
    TOTAL_USERS,
    ACTIVE_USERS,
    ACTIVE_USERS_LIST,
    NON_ACTIVE_USERS,
    NEW_ACTIVE_USERS,
    RETURNING_ACTIVE_USERS,


    PROJECT_CREATED_TYPES
}

