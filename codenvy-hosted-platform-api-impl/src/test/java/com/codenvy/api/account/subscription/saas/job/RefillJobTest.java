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
import org.eclipse.che.api.workspace.server.dao.Workspace;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link com.codenvy.api.account.subscription.saas.job.RefillJob}.
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class RefillJobTest {
    @Mock
    AccountDaoImpl   accountDao;
    @Mock
    AccountLocker    accountLocker;
    @Mock
    WorkspaceDaoImpl workspaceDao;
    @Mock
    WorkspaceLocker  workspaceLocker;

    @InjectMocks
    RefillJob refillJob;

    @Test
    public void shouldRefillResources() throws Exception {
        when(workspaceDao.getWorkspacesWithLockedResources()).thenReturn(Collections.<Workspace>emptyList());
        final Account lockedAccountId = new Account().withId("lockedAccountId");
        when(accountDao.getAccountsWithLockedResources()).thenReturn(Collections.singletonList(lockedAccountId));

        refillJob.run();

        verify(accountLocker).removeResourcesLock(eq("lockedAccountId"));
        verifyZeroInteractions(workspaceLocker);
    }

    @Test
    public void shouldNotRefillResourcesForPaidLockedAccount() throws Exception {
        when(workspaceDao.getWorkspacesWithLockedResources()).thenReturn(Collections.<Workspace>emptyList());
        final Account lockedAccountId = new Account().withId("lockedAccountId");
        lockedAccountId.getAttributes().put(Constants.PAYMENT_LOCKED_PROPERTY, "true");
        when(accountDao.getAccountsWithLockedResources()).thenReturn(Collections.singletonList(lockedAccountId));

        refillJob.run();

        verifyZeroInteractions(accountLocker);
        verifyZeroInteractions(workspaceLocker);
    }

    @Test
    public void shouldRefillResourcesForLockedWorkspace() throws Exception {
        when(accountDao.getAccountsWithLockedResources()).thenReturn(Collections.<Account>emptyList());
        final Workspace workspace = new Workspace().withId("workspaceId");
        when(workspaceDao.getWorkspacesWithLockedResources()).thenReturn(Collections.singletonList(workspace));

        refillJob.run();

        verify(workspaceLocker).removeResourcesLock(eq("workspaceId"));
        verifyZeroInteractions(accountLocker);
    }

    @Test
    public void shouldNotRefillResourcesForLockedWorkspaceWithPaidLockedAccount() throws Exception {
        final Account lockedAccountId = new Account().withId("lockedAccountId");
        lockedAccountId.getAttributes().put(Constants.PAYMENT_LOCKED_PROPERTY, "true");
        when(accountDao.getAccountsWithLockedResources()).thenReturn(Collections.singletonList(lockedAccountId));
        final Workspace workspace = new Workspace().withId("workspaceId").withAccountId("lockedAccountId");
        when(workspaceDao.getWorkspacesWithLockedResources()).thenReturn(Collections.singletonList(workspace));

        refillJob.run();

        verifyZeroInteractions(workspaceLocker);
        verifyZeroInteractions(accountLocker);
    }
}
