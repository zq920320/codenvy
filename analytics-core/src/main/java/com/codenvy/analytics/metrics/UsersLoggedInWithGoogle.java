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
public class UsersLoggedInWithGoogle extends AbstractLoggedInType {

    public UsersLoggedInWithGoogle() {
        super(MetricType.USERS_LOGGED_IN_WITH_GOOGLE, new String[]{UsersLoggedInTypes.GOOGLE});
    }

    @Override
    public String getDescription() {
        return "The number of authentication with Google account";
    }
}
