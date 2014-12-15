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
package com.codenvy.auth.sso.server;

import com.codenvy.api.auth.AuthenticationException;
import com.codenvy.api.core.ApiException;
import com.codenvy.api.user.server.dao.Profile;
import com.codenvy.api.user.server.dao.UserProfileDao;
import com.codenvy.service.http.IdeVersionHolder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Disallows login to IDE2 for users, who created after the specified time.
 */
public class RestrictedAccessAuthenticationHandler extends OrgServiceAuthenticationHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RestrictedAccessAuthenticationHandler.class);

    private UserProfileDao profileDao;
    private long ide2LoginLimit = Long.MAX_VALUE;

    @Inject
    public RestrictedAccessAuthenticationHandler(UserProfileDao profileDao,
                                                 @Nullable @Named("ide2.login.limit.time") String limitTime)
            throws ParseException {
        this.profileDao = profileDao;
        if (limitTime != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(limitTime));
            this.ide2LoginLimit = calendar.getTimeInMillis();
        }
    }

    @Override
    public com.codenvy.commons.user.User authenticate(final String userName, final String password) throws
                                                                                                    AuthenticationException {
        com.codenvy.commons.user.User user = super.authenticate(userName, password);

        if (IdeVersionHolder.get()) {
            try {
                Profile profile = profileDao.getById(user.getId());
                Long created = profile.getAttributes().containsKey("codenvy:created") ? Long
                        .parseLong(profile.getAttributes().get("codenvy:created")) : Long.MIN_VALUE;
                if (created.compareTo(ide2LoginLimit) > 0) {
                    throw new AuthenticationException(401,
                                                      "Authentication failed. Please use latest Codenvy version.");
                }
            } catch (ApiException e) {
                LOG.debug(e.getLocalizedMessage(), e);
                throw new AuthenticationException(401, "Authentication failed. Please check username and password.");
            }
        }

        return user;
    }
}
