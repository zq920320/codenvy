/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2016] Codenvy, S.A.
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
package com.codenvy.api.workspace;

import org.eclipse.che.api.account.server.dao.Account;
import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.account.server.dao.Member;
import org.eclipse.che.api.core.ForbiddenException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import static org.eclipse.che.api.workspace.server.Constants.START_WORKSPACE;

/**
 * Tests for {@link WorkspacePermissionManager}.
 *
 * @author Eugene Voevodin
 */
@Listeners(MockitoTestNGListener.class)
public class WorkspacePermissionManagerTest {

    @Mock
    AccountDao                 accountDao;
    @InjectMocks
    WorkspacePermissionManager permissionManager;

    @Test(expectedExceptions = ForbiddenException.class,
          expectedExceptionsMessageRegExp = "Workspace start rejected. User '.+' doesn't own account '.+'")
    public void rejectWorkspaceStartWithIdOfAccountWhichIsNotOwnedByCurrentUser() throws Exception {
        Account account = new Account("account123");
        when(accountDao.getById(account.getId())).thenReturn(account);
        Member member = new Member().withUserId("user123").withRoles(singletonList("account/member"));
        when(accountDao.getMembers(anyString())).thenReturn(singletonList(member));

        permissionManager.checkPermission(START_WORKSPACE, "user123", "accountId", "account123");
    }

    @Test
    public void allowWorkspaceStartWithIdOfAccountWhichIsOwnedByCurrentUser() throws Exception {
        Account account = new Account("account123");
        when(accountDao.getById(account.getId())).thenReturn(account);
        Member member = new Member().withUserId("user123").withRoles(singletonList("account/owner"));
        when(accountDao.getMembers(anyString())).thenReturn(singletonList(member));

        permissionManager.checkPermission(START_WORKSPACE, "user123", "accountId", "account123");
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Operation must not be null")
    public void shouldThrowNullPointerExceptionWhenOperationIsNull() throws Exception {
        permissionManager.checkPermission(null, "user123", emptyMap());
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "User id must not be null")
    public void shouldThrowNullPointerExceptionWhenUserIdIsNull() throws Exception {
        permissionManager.checkPermission(START_WORKSPACE, null, emptyMap());
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Parameters must not be null")
    public void shouldThrowNullPointerExceptionWhenParametersAreNull() throws Exception {
        permissionManager.checkPermission(START_WORKSPACE, "user123", null);
    }
}
