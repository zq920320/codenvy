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
import com.codenvy.api.account.WorkspaceLocker;
import com.codenvy.api.dao.mongo.AccountDaoImpl;
import com.codenvy.api.dao.mongo.WorkspaceDaoImpl;

import org.eclipse.che.api.account.server.Constants;
import org.eclipse.che.api.account.server.dao.Account;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.workspace.server.dao.Workspace;
import org.eclipse.che.commons.schedule.ScheduleCron;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

/**
 * Refill accounts with RAM limit exceeded at the beginning of new period.
 *
 * @author Max Shaposhnik
 */

@Singleton
public class RefillJob implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(RefillJob.class);

    private final AccountDaoImpl   accountDao;
    private final WorkspaceDaoImpl workspaceDao;
    private final AccountLocker    accountLocker;
    private final WorkspaceLocker  workspaceLocker;

    @Inject
    public RefillJob(AccountDaoImpl accountDao,
                     WorkspaceDaoImpl workspaceDao,
                     AccountLocker accountLocker,
                     WorkspaceLocker workspaceLocker) {
        this.accountDao = accountDao;
        this.workspaceDao = workspaceDao;
        this.accountLocker = accountLocker;
        this.workspaceLocker = workspaceLocker;
    }


    @ScheduleCron(cronParameterName = "billing.resources.refill.cron")
    @Override
    public void run() {
        try {
            Set<String> accountIdsWithPaymentLock = new HashSet<>();
            for (Account account : accountDao.getAccountsWithLockedResources()) {
                if (!account.getAttributes().containsKey(Constants.PAYMENT_LOCKED_PROPERTY)) {
                    accountLocker.unlockResources(account.getId(), true);
                } else {
                    accountIdsWithPaymentLock.add(account.getId());
                }
            }

            for (Workspace workspace : workspaceDao.getWorkspacesWithLockedResources()) {
                if (!accountIdsWithPaymentLock.contains(workspace.getAccountId())) {
                    workspaceLocker.unlockResources(workspace.getId());
                }
            }
        } catch (ServerException | ForbiddenException e) {
            LOG.error("Error on removing lock properties.", e);
        }
    }
}
