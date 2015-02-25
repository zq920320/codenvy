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

import com.codenvy.api.account.server.Constants;
import com.codenvy.api.account.server.dao.Account;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.core.notification.EventService;
import com.codenvy.api.workspace.server.dao.Workspace;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Locks and unlocks account and its workspaces
 *
 * @author Sergii Leschenko
 */
public class AccountLocker {
    private static final Logger LOG = LoggerFactory.getLogger(AccountLocker.class);

    private final AccountDao   accountDao;
    private final WorkspaceDao workspaceDao;
    private final EventService eventService;

    @Inject
    public AccountLocker(AccountDao accountDao, WorkspaceDao workspaceDao, EventService eventService) {
        this.accountDao = accountDao;
        this.workspaceDao = workspaceDao;
        this.eventService = eventService;
    }

    public void unlockAccountResources(String accountId) {
        try {
            final Account account;
            account = accountDao.getById(accountId);
            account.getAttributes().remove(com.codenvy.api.account.server.Constants.RESOURCES_LOCKED_PROPERTY);
            accountDao.update(account);
        } catch (NotFoundException | ServerException e) {
            LOG.error("Error removing lock property from account  {} .", accountId);
        }

        try {
            for (Workspace ws : workspaceDao.getByAccount(accountId)) {
                ws.getAttributes().remove(Constants.RESOURCES_LOCKED_PROPERTY);
                try {
                    workspaceDao.update(ws);
                } catch (NotFoundException | ServerException | ConflictException e) {
                    LOG.error("Error removing lock property from workspace  {} .", ws.getId());
                }
            }
        } catch (ServerException e) {
            LOG.error("Error removing lock property from workspace {} .", accountId);
        }
        eventService.publish(AccountLockEvent.accountUnlockedEvent(accountId));
    }

    public void lockAccountResources(String accountId) {
        try {
            final Account account = accountDao.getById(accountId);
            account.getAttributes().put(Constants.RESOURCES_LOCKED_PROPERTY, "true");
            accountDao.update(account);
        } catch (ServerException | NotFoundException e) {
            LOG.error("Error writing lock property into account  {} .", accountId);
        }

        eventService.publish(AccountLockEvent.accountLockedEvent(accountId));
        try {
        for (Workspace ws : workspaceDao.getByAccount(accountId)) {
            ws.getAttributes().put(Constants.RESOURCES_LOCKED_PROPERTY, "true");
            try {
                workspaceDao.update(ws);
            } catch (NotFoundException | ServerException | ConflictException e) {
                LOG.error("Error writing  lock property into workspace  {} .", ws.getId());
            }
        }
        } catch (ServerException e) {
            e.printStackTrace();
        }
    }

    public void lockAccount(String accountId) {
        try {
            final Account account = accountDao.getById(accountId);
            account.getAttributes().put(Constants.PAYMENT_LOCKED_PROPERTY, "true");
            accountDao.update(account);
        } catch (NotFoundException | ServerException e) {
            LOG.error("Error writing lock property into account  {} .", accountId);
        }
    }

    //TODO Use this method after successfully charging of invoice
    public void unlockAccount(String accountId) {
        try {
            final Account account;
            account = accountDao.getById(accountId);
            account.getAttributes().remove(com.codenvy.api.account.server.Constants.PAYMENT_LOCKED_PROPERTY);
            accountDao.update(account);
        } catch (NotFoundException | ServerException e) {
            LOG.error("Error removing lock property from account  {} .", accountId);
        }
    }
}
