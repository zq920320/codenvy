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
package com.codenvy.auth.sso.server;

import com.codenvy.api.dao.authentication.AuthenticationHandler;

import org.eclipse.che.api.auth.AuthenticationException;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.user.server.spi.UserDao;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Map;

/**
 * Provide ability to authenticate users with one time token.
 */
public class BearerAuthenticationHandler implements AuthenticationHandler {

    private static final Logger LOG = LoggerFactory.getLogger(BearerAuthenticationHandler.class);

    @Inject
    BearerTokenManager tokenManager;

    @Inject
    UserDao userDao;

    public Subject authenticate(final String login, final String password)
            throws AuthenticationException {
        if (login == null || login.isEmpty() || password == null || password.isEmpty()) {
            throw new AuthenticationException(401, "Authentication failed. Please check username and password.");
        }
        try {
            Map<String, String> payload = tokenManager.checkValid(password);
            if (!login.equals(payload.get("userName")) && !login.equals("x-bearer")) {
                throw new AuthenticationException(401, "Authentication failed. Please check username and password.");
            }
            User user = userDao.getByEmail(payload.get("email"));
            return new SubjectImpl(user.getName(), user.getId(), null, false);
        } catch (ApiException | InvalidBearerTokenException e) {
            LOG.debug(e.getLocalizedMessage(), e);
            throw new AuthenticationException(401, "Authentication failed. Please check username and password.");
        }
    }


    @Override
    public String getType() {
        return "bearer";
    }
}
