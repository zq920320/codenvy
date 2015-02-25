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

import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Tests for {@link com.codenvy.api.account.AccountLocker}
 *
 * @author Sergii Leschenko
 */
@Listeners(value = {MockitoTestNGListener.class})
public class AccountLockerTest {
    @Mock
    AccountDao   accountDao;
    @Mock
    WorkspaceDao workspaceDao;
    @Mock
    EventService eventService;

    @InjectMocks
    AccountLocker accountLocker;

    @Test
    public void shouldLockResourcesForAccountAndItsWorkspaces() throws NotFoundException, ServerException, ConflictException {
        Account account = new Account().withId("accountId");
        when(accountDao.getById(anyString())).thenReturn(account);

        when(workspaceDao.getByAccount(anyString())).thenReturn(Arrays.asList(createWorkspace("accountId", "ws_1", false),
                                                                              createWorkspace("accountId", "ws_2", false)));
        accountLocker.lockAccountResources("accountId");

        verify(accountDao).update(argThat(new ArgumentMatcher<Account>() {
            @Override
            public boolean matches(Object o) {
                final Account account = (Account)o;
                return "accountId".equals(account.getId())
                       && account.getAttributes().containsKey(Constants.RESOURCES_LOCKED_PROPERTY);
            }
        }));
        verify(workspaceDao, times(2)).update(argThat(new ArgumentMatcher<Workspace>() {
            @Override
            public boolean matches(Object o) {
                Workspace workspace = (Workspace)o;
                return workspace.getAttributes().containsKey(Constants.RESOURCES_LOCKED_PROPERTY);
            }
        }));
        verify(eventService).publish(argThat(new ArgumentMatcher<Object>() {
            @Override
            public boolean matches(Object o) {
                return AccountLockEvent.EventType.ACCOUNT_LOCKED.equals(((AccountLockEvent)o).getType());
            }
        }));
    }

    @Test
    public void shouldUnlockResourcesForAccountAndItsWorkspaces() throws NotFoundException, ServerException, ConflictException {
        Account account = new Account().withId("accountId");
        account.getAttributes().put(Constants.RESOURCES_LOCKED_PROPERTY, "true");
        when(accountDao.getById(anyString())).thenReturn(account);

        when(workspaceDao.getByAccount(anyString())).thenReturn(Arrays.asList(createWorkspace("accountId", "ws_1", true),
                                                                              createWorkspace("accountId", "ws_2", true)));
        accountLocker.unlockAccountResources("accountId");

        verify(accountDao).update(argThat(new ArgumentMatcher<Account>() {
            @Override
            public boolean matches(Object o) {
                final Account account = (Account)o;
                return "accountId".equals(account.getId())
                       && !account.getAttributes().containsKey(Constants.RESOURCES_LOCKED_PROPERTY);
            }
        }));
        verify(workspaceDao, times(2)).update(argThat(new ArgumentMatcher<Workspace>() {
            @Override
            public boolean matches(Object o) {
                Workspace workspace = (Workspace)o;
                return !workspace.getAttributes().containsKey(Constants.RESOURCES_LOCKED_PROPERTY);
            }
        }));
        verify(eventService).publish(argThat(new ArgumentMatcher<Object>() {
            @Override
            public boolean matches(Object o) {
                return AccountLockEvent.EventType.ACCOUNT_UNLOCKED.equals(((AccountLockEvent)o).getType());
            }
        }));
    }

    @Test
    public void shouldLockAccount() throws NotFoundException, ServerException, ConflictException {
        Account account = new Account().withId("accountId");
        when(accountDao.getById(anyString())).thenReturn(account);

        accountLocker.lockAccount("accountId");

        verify(accountDao).update(argThat(new ArgumentMatcher<Account>() {
            @Override
            public boolean matches(Object o) {
                final Account account = (Account)o;
                return "accountId".equals(account.getId())
                       && account.getAttributes().containsKey(Constants.PAYMENT_LOCKED_PROPERTY);
            }
        }));
    }

    @Test
    public void shouldUnlockAccount() throws NotFoundException, ServerException, ConflictException {
        Account account = new Account().withId("accountId");
        account.getAttributes().put(Constants.RESOURCES_LOCKED_PROPERTY, "true");
        when(accountDao.getById(anyString())).thenReturn(account);

        accountLocker.unlockAccount("accountId");

        verify(accountDao).update(argThat(new ArgumentMatcher<Account>() {
            @Override
            public boolean matches(Object o) {
                final Account account = (Account)o;
                return "accountId".equals(account.getId())
                       && !account.getAttributes().containsKey(Constants.PAYMENT_LOCKED_PROPERTY);
            }
        }));
    }

    private Workspace createWorkspace(String accountId, String workspaceId, boolean lockedResources) {
        Workspace workspace = new Workspace().withAccountId(accountId)
                                             .withId(workspaceId);
        if (lockedResources) {
            workspace.getAttributes().put(Constants.RESOURCES_LOCKED_PROPERTY, "true");
        }
        return workspace;
    }
}
