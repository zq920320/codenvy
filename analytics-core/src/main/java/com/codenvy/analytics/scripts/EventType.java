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

package com.codenvy.analytics.scripts;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public enum EventType {
    FILE_MANIPULATION,
    USER_CODE_REFACTOR,
    USER_CODE_COMPLETE,
    BUILD_STARTED,
    BUILD_FINISHED,
    RUN_STARTED,
    RUN_FINISHED,
    DEBUG_STARTED,
    DEBUG_FINISHED,
    TENANT_CREATED,
    TENANT_DESTROYED,
    PROJECT_DESTROYED,
    USER_CREATED,
    USER_REMOVED,
    USER_INVITE,
    USER_SSO_LOGGED_IN,
    SESSION_STARTED,
    SESSION_FINISHED,
    SESSION_FACTORY_STARTED,
    SESSION_FACTORY_FINISHED,
    USER_ADDED_TO_WS;

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return super.toString().toLowerCase().replace("_", "-");
    }
}
