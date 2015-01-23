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

package com.codenvy.subscription.scheduler;

import com.codenvy.api.account.server.Constants;
import com.codenvy.api.account.server.dao.Account;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.workspace.server.dao.Workspace;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.codenvy.commons.quartz.Scheduled;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Refill accounts with RAM limit exceeded at the beginning of new period.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com) on 1/21/15.
 *
 */

@Scheduled(cron = "0 0 7 1 * ?") // 0sec 0min 07hour 1st day of every month
public class RefillJob implements Job {
    private static final Logger LOG = LoggerFactory.getLogger(RefillJob.class);

    @Inject
    WorkspaceDao workspaceDao;

    @Inject
    AccountDao accountDao;

    @Override
    public void execute(final JobExecutionContext ctx)
            throws JobExecutionException {
        try {
            for (Account account : accountDao.getLockedCommunityAccounts()) {
                account.getAttributes().remove(Constants.LOCKED_PROPERTY);
                try {
                    accountDao.update(account);
                } catch (NotFoundException | ServerException e) {
                    LOG.error("Error removing lock property into account  {} .", account.getId());
                }
                for (Workspace ws : workspaceDao.getByAccount(account.getId())) {
                    ws.getAttributes().remove(Constants.LOCKED_PROPERTY);
                    try {
                        workspaceDao.update(ws);
                    } catch (NotFoundException | ServerException | ConflictException e) {
                        LOG.error("Error removing lock property into workspace  {} .", ws.getId());
                    }
                }
            }
        } catch (ServerException | ForbiddenException e) {
            LOG.error("Error on removing lock properties.", e);
        }
    }
}
