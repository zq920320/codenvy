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

package com.codenvy.api.subscription.saas.server.job;

import com.codenvy.api.metrics.server.limit.WorkspaceLockDao;
import com.codenvy.api.metrics.server.limit.WorkspaceLocker;
import com.codenvy.api.subscription.saas.server.AccountLocker;
import com.codenvy.api.subscription.saas.server.dao.sql.AccountLockDao;

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

    private final AccountLockDao   accountLockDao;
    private final AccountLocker    accountLocker;
    private final WorkspaceLocker  workspaceLocker;
    private final WorkspaceLockDao workspaceLockDao;

    @Inject
    public RefillJob(AccountLockDao accountLockDao,
                     AccountLocker accountLocker,
                     WorkspaceLocker workspaceLocker,
                     WorkspaceLockDao workspaceLockDao) {
        this.accountLockDao = accountLockDao;
        this.accountLocker = accountLocker;
        this.workspaceLocker = workspaceLocker;
        this.workspaceLockDao = workspaceLockDao;
    }


    @ScheduleCron(cronParameterName = "resources.refill.cron")
    @Override
    public void run() {
        try {
            Set<String> accountIdsWithPaymentLock = new HashSet<>();
            for (Account account : accountLockDao.getAccountsWithLockedResources()) {
                if (!account.getAttributes().containsKey(Constants.PAYMENT_LOCKED_PROPERTY)) {
                    accountLocker.removeResourcesLock(account.getId());
                } else {
                    accountIdsWithPaymentLock.add(account.getId());
                }
            }

            for (Workspace workspace : workspaceLockDao.getWorkspacesWithLockedResources()) {
                if (!accountIdsWithPaymentLock.contains(workspace.getAccountId())) {
                    workspaceLocker.removeResourcesLock(workspace.getId());
                }
            }
        } catch (ServerException | ForbiddenException e) {
            LOG.error("Error on removing lock properties.", e);
        }
    }
}
