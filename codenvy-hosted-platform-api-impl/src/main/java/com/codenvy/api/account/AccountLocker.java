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

import org.eclipse.che.api.account.server.Constants;
import org.eclipse.che.api.account.server.dao.Account;
import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.dao.Workspace;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static java.lang.String.format;
import static org.eclipse.che.api.account.server.Constants.PAYMENT_LOCKED_PROPERTY;
import static org.eclipse.che.api.account.server.Constants.RESOURCES_LOCKED_PROPERTY;

/**
 * Locks and unlocks account and its workspaces
 *
 * @author Sergii Leschenko
 */
public class AccountLocker {
    private static final Logger LOG = LoggerFactory.getLogger(AccountLocker.class);

    private final AccountDao      accountDao;
    private final WorkspaceDao    workspaceDao;
    private final EventService    eventService;
    private final WorkspaceLocker workspaceLocker;

    @Inject
    public AccountLocker(AccountDao accountDao, WorkspaceDao workspaceDao, EventService eventService, WorkspaceLocker workspaceLocker) {
        this.accountDao = accountDao;
        this.workspaceDao = workspaceDao;
        this.eventService = eventService;
        this.workspaceLocker = workspaceLocker;
    }

    /**
     * Sets resources lock for account with given id.
     * Account won't be locked second time if it already has resources lock
     */
    public void setResourcesLock(String accountId) {
        setPaymentLock(accountId, false);
    }

    /**
     * Sets payment lock for account with given id.
     * Setting of payment lock also sets resources lock.
     * Account won't be locked second time if it already has payment lock
     */
    public void setPaymentLock(String accountId) {
        setPaymentLock(accountId, true);
    }

    /**
     * Removes payment lock for account with given id.
     * Account's resources won't be unlocked if account hasn't resources lock.
     * Account's resources won't be unlocked if it has payment lock.
     */
    public void removeResourcesLock(String accountId) {
        unlock(accountId, false);
    }

    /**
     * Removes payment lock for account with given id.
     * Removing of payment lock also removes resources lock.
     * Account won't be unlocked second time if it hasn't payment lock
     */
    //TODO Use this method after successfully charging of invoice
    public void removePaymentLock(String accountId) {
        unlock(accountId, true);
    }

    private void setPaymentLock(String accountId, boolean paymentLock) {
        try {
            final Account account = accountDao.getById(accountId);

            boolean accountChanged = false;
            if (paymentLock) {
                if (!account.getAttributes().containsKey(PAYMENT_LOCKED_PROPERTY)) {
                    account.getAttributes().put(PAYMENT_LOCKED_PROPERTY, "true");
                    account.getAttributes().put(Constants.RESOURCES_LOCKED_PROPERTY, "true");
                    accountChanged = true;
                } else {
                    LOG.warn("Trying to set payment lock for account with id {} that already is locked", accountId);
                }
            } else if (!account.getAttributes().containsKey(RESOURCES_LOCKED_PROPERTY)) {
                account.getAttributes().put(RESOURCES_LOCKED_PROPERTY, "true");
                accountChanged = true;
            } else {
                LOG.warn("Trying to set resources lock for account with id {} that already is locked", accountId);
            }

            if (accountChanged) {
                accountDao.update(account);
                eventService.publish(AccountLockEvent.accountLockedEvent(accountId));
                try {
                    for (Workspace workspace : workspaceDao.getByAccount(accountId)) {
                        workspaceLocker.lockResources(workspace.getId());
                    }
                } catch (ServerException e) {
                    LOG.error(format("Can't get account's workspaces %s for writing lock property", accountId), e);
                }
            }
        } catch (ServerException | NotFoundException e) {
            LOG.error(format("Error writing lock property into account %s .", accountId), e);
        }
    }

    private void unlock(String accountId, boolean paymentLock) {
        try {
            final Account account;
            account = accountDao.getById(accountId);

            boolean accountChanged = false;
            if (paymentLock) {
                if (account.getAttributes().containsKey(PAYMENT_LOCKED_PROPERTY)) {
                    account.getAttributes().remove(PAYMENT_LOCKED_PROPERTY);
                    account.getAttributes().remove(RESOURCES_LOCKED_PROPERTY);
                    accountChanged = true;
                }
            } else if (account.getAttributes().containsKey(RESOURCES_LOCKED_PROPERTY)) {
                account.getAttributes().remove(RESOURCES_LOCKED_PROPERTY);
                accountChanged = true;
            }

            if (accountChanged) {
                accountDao.update(account);
                eventService.publish(AccountLockEvent.accountUnlockedEvent(accountId));

                try {
                    for (Workspace workspace : workspaceDao.getByAccount(accountId)) {
                        workspaceLocker.unlockResources(workspace.getId());
                    }
                } catch (ServerException e) {
                    LOG.error(format("Error removing lock property from workspace %s .", accountId), e);
                }
            }
        } catch (NotFoundException | ServerException e) {
            LOG.error(format("Error removing lock property from account %s .", accountId), e);
        }
    }
}
