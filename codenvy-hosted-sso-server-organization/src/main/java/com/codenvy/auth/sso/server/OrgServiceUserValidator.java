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

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.dao.UserDao;

import com.codenvy.auth.sso.server.organization.UserCreationValidator;


import javax.inject.Inject;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Sergii Kabashniuk
 */
public class OrgServiceUserValidator implements UserCreationValidator {

    private UserDao userDao;

    @Inject
    public OrgServiceUserValidator(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public void ensureUserCreationAllowed(String email, String userName) throws ConflictException, ServerException {

        if (isNullOrEmpty(email)) {
            throw new IllegalArgumentException("Email cannot be empty or null");
        }

        if (isNullOrEmpty(userName)) {
            throw new IllegalArgumentException("User name cannot be empty or null");
        }

        try {
            userDao.getByAlias(email);
            throw new ConflictException("User with given email already exists. Please, choose another one.");
        } catch (NotFoundException e) {
            // ok
        }

        try {
            userDao.getByName(userName);
            throw new ConflictException("User with given name already exists. Please, choose another one.");
        } catch (NotFoundException e) {
            // ok
        }
    }
}
