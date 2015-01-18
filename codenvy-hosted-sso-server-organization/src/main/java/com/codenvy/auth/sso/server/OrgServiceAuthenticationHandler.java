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
package com.codenvy.auth.sso.server;

import com.codenvy.api.auth.AuthenticationException;
import com.codenvy.api.core.ApiException;
import com.codenvy.api.dao.authentication.AuthenticationHandler;
import com.codenvy.api.user.server.dao.UserDao;
import com.codenvy.api.user.server.dao.User;
import com.codenvy.commons.user.UserImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Collections;

/**
 * Authentication using username and password based on
 * Java Authentication and Authorization Service.
 * <p/>
 * As usual you have to configure LoginModule to handle
 * LoginContext login() logout() calls.
 *
 * @author Sergii Kabashniuk
 */
public class OrgServiceAuthenticationHandler implements AuthenticationHandler {
    private static final Logger LOG = LoggerFactory.getLogger(OrgServiceAuthenticationHandler.class);

    @Inject
    UserDao userDao;

    public com.codenvy.commons.user.User authenticate(final String userName, final String password)
            throws AuthenticationException {
        if (userName == null || userName.isEmpty() || password == null || password.isEmpty()) {
            throw new AuthenticationException(401, "Authentication failed. Please check username and password.");
        }

        try {
            if (userDao.authenticate(userName, password)) {
                User user = userDao.getByAlias(userName);
                return new UserImpl(userName, user.getId(), null, Collections.<String>emptyList(), false);
            } else {
                LOG.debug("UserDao fail to authenticate");
                throw new AuthenticationException(401, "Authentication failed. Please check username and password.");
            }
        } catch (ApiException e) {
            LOG.debug(e.getLocalizedMessage(), e);
            throw new AuthenticationException(401, "Authentication failed. Please check username and password.");
        }
    }

    @Override
    public String getType() {
        return "org";
    }

}
