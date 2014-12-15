/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
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
package com.codenvy.auth.sso.client;

import com.codenvy.commons.user.User;

/**
 * Provided communication bridge between sso client and server.
 *
 * @author Sergii Kabashniuk
 */
public interface ServerClient {
    /**
     * Get user associated with given token for the given execution context(workspaceId, accountId)
     *
     * @param token
     *         - sso authentication token.
     * @param clientUrl
     *         - url of client who asking the principal.
     * @param workspaceId
     *         - id of workspace for which requested roles.
     * @param accountId
     *         - id of account for which requested roles.
     * @return - principal with roles. If token is not valid return null.
     */
    User getUser(String token, String clientUrl, String workspaceId, String accountId);

    /**
     * Notify server about termination sso session.
     *
     * @param token
     *         - sso authentication token.
     * @param clientUrl
     *         - url of client who asking the principal.
     */
    void unregisterClient(String token, String clientUrl);

}
