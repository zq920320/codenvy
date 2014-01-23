/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 * [2012] - [2014] Codenvy, S.A.
 * All Rights Reserved.
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
package com.codenvy.analytics.jaas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.security.Principal;
import java.util.Map;
import java.util.Set;

/**
 * @author Vitaly Parfonov
 * @author Anatoliy Bazko
 */
public class TomcatLoginModule implements LoginModule {

    private static final Logger LOG = LoggerFactory.getLogger(TomcatLoginModule.class);

    private Subject         subject;
    private CallbackHandler callbackHandler;
    private String          user;

    @Override
    public boolean commit() throws LoginException {
        Set<Principal> principals = subject.getPrincipals();
        principals.add(new UserPrincipal(user));
        principals.add(new RolePrincipal("developer"));
        return true;
    }

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
                           Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
    }

    @Override
    public boolean login() throws LoginException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("In login of TomcatLoginModule.");
        }
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Try create identity");
            }
            Callback[] callbacks = new Callback[2];
            callbacks[0] = new NameCallback("Username");
            callbacks[1] = new PasswordCallback("Password", false);

            callbackHandler.handle(callbacks);
            String username = ((NameCallback)callbacks[0]).getName();
            String password = new String(((PasswordCallback)callbacks[1]).getPassword());
            ((PasswordCallback)callbacks[1]).clearPassword();
            if (username == null || password.isEmpty()) {
                return false;
            }
            subject.getPrivateCredentials().add(password);
            subject.getPublicCredentials().add(username);
            user = username;
            return true;
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw new LoginException(e.getMessage());
        }
    }

    @Override
    public boolean abort() throws LoginException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("In abort of TomcatLoginModule.");
        }
        return true;
    }

    @Override
    public boolean logout() throws LoginException {
        if (LOG.isDebugEnabled())
            LOG.debug("In logout of TomcatLoginModule.");

        return true;
    }
}
