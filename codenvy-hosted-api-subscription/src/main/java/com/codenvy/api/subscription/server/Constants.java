/*
 *  [2012] - [2016] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.api.subscription.server;

/**
 * Constants for Account API
 *
 * @author Eugene Voevodin
 * @author Alexander Garagatyi
 */
public final class Constants {
    public static final String LINK_REL_GET_SUBSCRIPTION        = "get subscription by id";
    public static final String LINK_REL_GET_SUBSCRIPTIONS       = "subscriptions";
    public static final String LINK_REL_ADD_SUBSCRIPTION        = "add subscription";
    public static final String LINK_REL_DEACTIVATE_SUBSCRIPTION = "deactivate subscription";
    public static final String LINK_REL_GET_ACCOUNT_RESOURCES   = "account resources";
    public static final int    ID_LENGTH                        = 16;

    private Constants() {
    }
}
