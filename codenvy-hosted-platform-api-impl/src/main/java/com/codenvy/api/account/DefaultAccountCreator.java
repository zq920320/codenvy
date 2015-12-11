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
package com.codenvy.api.account;

import org.eclipse.che.api.account.server.dao.Account;
import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ServerException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Component which creates fake account if it doesn't exist.
 *
 * <p>This is a temporary solution for account infrastructure as we need to start involving account.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public final class DefaultAccountCreator {

    public static final String DEFAULT_ACCOUNT_ID = "default-account-id";
    public static final String DEFAULT_ACCOUNT_NAME = "default";

    private final AccountDao accountDao;

    @Inject
    public DefaultAccountCreator(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    /**
     * Creates account with identifier equal to {@link #DEFAULT_ACCOUNT_ID} and name 'default' if it doesn't exist and
     * {@link #DEFAULT_ACCOUNT_ID} is not null.
     *
     * @throws ServerException
     *         if any server error occurs during account creation or fetching
     * @throws ConflictException
     *         if any conflict error occurs during account creation
     */
    @PostConstruct
    public void createDefaultAccount() throws ServerException, ConflictException {
        if (!accountDao.exist(DEFAULT_ACCOUNT_ID)) {
            accountDao.create(new Account(DEFAULT_ACCOUNT_ID, DEFAULT_ACCOUNT_NAME));
        }
    }
}
