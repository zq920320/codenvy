/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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
package com.codenvy.auth.sso.server.organization;


import java.io.IOException;

/**
 * Check if user and workspace name is allowed to validate before workspace creation.
 *
 * @author Sergii Kabashniuk
 */
public interface WorkspaceCreationValidator {

    /**
     * Ensure user and workspace name is eligible to validate email before workspace creation.
     *
     * @param email
     *         - user name
     * @param workspaceName
     *         -desired workspace name
     * @throws IOException
     */
    //TODO get better exception
    void ensureUserCreationAllowed(String email, String workspaceName) throws IOException;
}
