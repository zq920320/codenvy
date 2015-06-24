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
package com.codenvy.api.subscription.saas.server;

import com.codenvy.api.subscription.saas.server.billing.BillingService;
import com.codenvy.api.subscription.saas.server.billing.invoice.InvoiceFilter;
import com.codenvy.api.subscription.saas.shared.dto.Invoice;

import org.eclipse.che.api.account.server.dao.Account;
import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.dao.Workspace;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;
import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.eclipse.che.api.account.server.Constants.PAYMENT_LOCKED_PROPERTY;
import static org.eclipse.che.api.account.server.Constants.RESOURCES_LOCKED_PROPERTY;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Tests for {@link AccountLocker}
 *
 * @author Sergii Leschenko
 */
@Listeners(value = {MockitoTestNGListener.class})
public class AccountLockerTest {
    @Mock
    AccountDao      accountDao;
    @Mock
    WorkspaceDao    workspaceDao;
    @Mock
    WorkspaceLocker workspaceLocker;
    @Mock
    EventService    eventService;
    @Mock
    BillingService  billingService;

    @InjectMocks
    AccountLocker accountLocker;

    @Test
    public void shouldLockResourcesForAccountAndItsWorkspaces() throws NotFoundException, ServerException, ConflictException {
        Account account = new Account().withId("accountId");
        when(accountDao.getById(anyString())).thenReturn(account);

        when(workspaceDao.getByAccount(anyString())).thenReturn(Arrays.asList(createWorkspace("accountId", "ws_1", false),
                                                                              createWorkspace("accountId", "ws_2", false)));
        accountLocker.setResourcesLock("accountId");

        verify(accountDao).update(argThat(new ArgumentMatcher<Account>() {
            @Override
            public boolean matches(Object o) {
                final Account account = (Account)o;
                return "accountId".equals(account.getId())
                       && account.getAttributes().containsKey(RESOURCES_LOCKED_PROPERTY);
            }
        }));
        verify(workspaceLocker).setResourcesLock(eq("ws_1"));
        verify(workspaceLocker).setResourcesLock(eq("ws_2"));
        verify(eventService).publish(argThat(new ArgumentMatcher<Object>() {
            @Override
            public boolean matches(Object o) {
                return AccountLockEvent.EventType.ACCOUNT_LOCKED.equals(((AccountLockEvent)o).getType());
            }
        }));
    }

    @Test
    public void shouldNotLockResourcesForAccountIfItAlreadyHasResourcesLockedProperty() throws Exception {
        Account account = new Account().withId("accountId");
        account.getAttributes().put(RESOURCES_LOCKED_PROPERTY, "true");
        when(accountDao.getById(anyString())).thenReturn(account);

        accountLocker.setResourcesLock("accountId");

        verify(accountDao, never()).update((Account)anyObject());
        verify(eventService, never()).publish(anyObject());
    }

    @Test
    public void shouldUnlockResourcesForAccountAndItsWorkspaces() throws NotFoundException, ServerException, ConflictException {
        Account account = new Account().withId("accountId");
        account.getAttributes().put(RESOURCES_LOCKED_PROPERTY, "true");
        when(accountDao.getById(anyString())).thenReturn(account);

        when(workspaceDao.getByAccount(anyString())).thenReturn(Arrays.asList(createWorkspace("accountId", "ws_1", true),
                                                                              createWorkspace("accountId", "ws_2", true)));
        accountLocker.removeResourcesLock("accountId");

        verify(accountDao).update(argThat(new ArgumentMatcher<Account>() {
            @Override
            public boolean matches(Object o) {
                final Account account = (Account)o;
                return "accountId".equals(account.getId())
                       && !account.getAttributes().containsKey(RESOURCES_LOCKED_PROPERTY);
            }
        }));
        verify(workspaceLocker).removeResourcesLock(eq("ws_1"));
        verify(workspaceLocker).removeResourcesLock(eq("ws_2"));
        verify(eventService).publish(argThat(new ArgumentMatcher<Object>() {
            @Override
            public boolean matches(Object o) {
                return AccountLockEvent.EventType.ACCOUNT_UNLOCKED.equals(((AccountLockEvent)o).getType());
            }
        }));
    }

    @Test
    public void shouldDoNotUnlockResourcesIfAccountAlreadyHasNotResourcesLockedProperty() throws Exception {
        Account account = new Account().withId("accountId");
        when(accountDao.getById(anyString())).thenReturn(account);

        when(workspaceDao.getByAccount(anyString())).thenReturn(Arrays.asList(createWorkspace("accountId", "ws_1", true),
                                                                              createWorkspace("accountId", "ws_2", true)));
        accountLocker.removeResourcesLock("accountId");

        verify(accountDao, never()).update((Account)anyObject());
        verify(workspaceLocker, never()).removeResourcesLock(anyString());
        verify(eventService, never()).publish(account);
    }

    @Test
    public void shouldSetPaymentLockInAccount() throws NotFoundException, ServerException, ConflictException {
        Account account = new Account().withId("accountId");
        when(accountDao.getById(anyString())).thenReturn(account);

        accountLocker.setPaymentLock("accountId");

        verify(accountDao).update(argThat(new ArgumentMatcher<Account>() {
            @Override
            public boolean matches(Object o) {
                final Account account = (Account)o;
                return "accountId".equals(account.getId())
                       && account.getAttributes().containsKey(PAYMENT_LOCKED_PROPERTY)
                       && account.getAttributes().containsKey(RESOURCES_LOCKED_PROPERTY);
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
    public void shouldNotSetPaymentLockAccountIfItAlreadyHasPaymentLockedProperty()
            throws NotFoundException, ServerException, ConflictException {
        Account account = new Account().withId("accountId");
        account.getAttributes().put(PAYMENT_LOCKED_PROPERTY, "true");
        when(accountDao.getById(anyString())).thenReturn(account);

        accountLocker.setPaymentLock("accountId");

        verify(accountDao, never()).update((Account)anyObject());
        verify(eventService, never()).publish(anyObject());
    }

    @Test
    public void shouldRemovePaymentLockInAccount() throws NotFoundException, ServerException, ConflictException {
        Account account = new Account().withId("accountId");
        account.getAttributes().put(RESOURCES_LOCKED_PROPERTY, "true");
        account.getAttributes().put(PAYMENT_LOCKED_PROPERTY, "true");
        when(accountDao.getById(anyString())).thenReturn(account);

        accountLocker.removePaymentLock("accountId");

        verify(accountDao).update(argThat(new ArgumentMatcher<Account>() {
            @Override
            public boolean matches(Object o) {
                final Account account = (Account)o;
                return "accountId".equals(account.getId())
                       && !account.getAttributes().containsKey(PAYMENT_LOCKED_PROPERTY)
                       && !account.getAttributes().containsKey(RESOURCES_LOCKED_PROPERTY);
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
    public void shouldNotUnlockAccountIfItDoNotHavePaymentLockProperty() throws NotFoundException, ServerException, ConflictException {
        Account account = new Account().withId("accountId");
        when(accountDao.getById(anyString())).thenReturn(account);
        when(billingService.getInvoices((InvoiceFilter)anyObject())).thenReturn(Collections.<Invoice>emptyList());

        accountLocker.removePaymentLock("accountId");

        verify(accountDao, never()).update((Account)anyObject());
        verify(eventService, never()).publish(anyObject());
    }

    @Test
    public void shouldNotUnlockAccountIfItHasUnpaidInvoices() throws NotFoundException, ServerException, ConflictException {
        Account account = new Account().withId("accountId");
        account.getAttributes().put(RESOURCES_LOCKED_PROPERTY, "true");
        account.getAttributes().put(PAYMENT_LOCKED_PROPERTY, "true");
        when(accountDao.getById(anyString())).thenReturn(account);
        when(billingService.getInvoices((InvoiceFilter)anyObject())).thenReturn(Collections.singletonList(DtoFactory.getInstance().createDto(Invoice.class)));

        accountLocker.removePaymentLock("accountId");

        verify(accountDao, never()).update((Account)anyObject());
        verify(eventService, never()).publish(anyObject());
    }

    private Workspace createWorkspace(String accountId, String workspaceId, boolean lockedResources) {
        Workspace workspace = new Workspace().withAccountId(accountId)
                                             .withId(workspaceId);
        if (lockedResources) {
            workspace.getAttributes().put(RESOURCES_LOCKED_PROPERTY, "true");
        }
        return workspace;
    }
}
