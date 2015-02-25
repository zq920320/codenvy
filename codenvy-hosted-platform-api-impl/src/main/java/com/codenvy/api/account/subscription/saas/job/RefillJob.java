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

package com.codenvy.api.account.subscription.saas.job;

import com.codenvy.api.account.AccountLocker;
import com.codenvy.api.account.server.Constants;
import com.codenvy.api.account.server.dao.Account;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.ServerException;
import com.codenvy.commons.schedule.ScheduleCron;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Refill accounts with RAM limit exceeded at the beginning of new period.
 *
 * @author Max Shaposhnik
 */

@Singleton
public class RefillJob implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(RefillJob.class);

    private final AccountDao    accountDao;
    private final AccountLocker accountLocker;

    @Inject
    public RefillJob(AccountDao accountDao,
                     AccountLocker accountLocker) {
        this.accountDao = accountDao;
        this.accountLocker = accountLocker;
    }


    @ScheduleCron(cronParameterName = "billing.resources.refill.cron")
    @Override
    public void run() {
        try {
            for (Account account : accountDao.getAccountsWithLockedResources()) {
                if (!account.getAttributes().containsKey(Constants.PAYMENT_LOCKED_PROPERTY)) {
                    accountLocker.unlockAccountResources(account.getId());
                }
            }
        } catch (ServerException | ForbiddenException e) {
            LOG.error("Error on removing lock properties.", e);
        }
    }
}
