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
package com.codenvy.auth.sso.client;

import javax.servlet.http.HttpServletRequest;

/**
 * Dummy stub. Return empty context parameters
 *
 * @author Sergii Kabashniuk
 */
public class EmptyContextResolver implements SSOContextResolver {
    @Override
    public RolesContext getRequestContext(HttpServletRequest request) {
        return new RolesContext();
    }
}
