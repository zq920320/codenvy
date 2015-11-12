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
package com.codenvy.auth.sso.server;

import com.codenvy.api.dao.authentication.AccessTicket;

import org.eclipse.che.api.account.server.dao.Account;
import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.dao.MembershipDo;
import org.eclipse.che.api.user.server.dao.PreferenceDao;
import org.eclipse.che.api.user.server.dao.User;
import org.eclipse.che.api.user.server.dao.UserDao;
import org.eclipse.che.api.user.shared.model.Membership;
import org.eclipse.che.commons.user.UserImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashSet;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link OrgServiceRolesExtractor}
 *
 * @author Eugene Voevodin
 */
@Listeners(MockitoTestNGListener.class)
public class OrgServiceRolesExtractorTest {

    @Mock
    UserDao                  userDao;
    @Mock
    AccountDao               accountDao;
    @Mock
    PreferenceDao            preferenceDao;
    @InjectMocks
    OrgServiceRolesExtractor extractor;

    AccessTicket ticket;

    @BeforeMethod
    public void setUp() throws Exception {
        final UserImpl user = new UserImpl("name",
                                           "id",
                                           "token",
                                           Collections.<String>emptyList(),
                                           false);

        ticket = new AccessTicket("token", user, "authHandler");

        when(userDao.getById(user.getId())).thenReturn(new User().withId("id"));
    }

    @Test
    public void shouldSkipLdapRoleCheckWhenAllowedRoleIsNull() throws Exception {
        final OrgServiceRolesExtractor extractor = spy(new OrgServiceRolesExtractor(preferenceDao,
                                                                                    null,
                                                                                    null,
                                                                                    null,
                                                                                    "employeeType",
                                                                                    null,
                                                                                    null));
        doReturn(Collections.<String>emptySet()).when(extractor).getRoles(ticket.getPrincipal().getId());

        assertEquals(extractor.extractRoles(ticket, "wsId", "accId"), singleton("user"));
    }

    @Test
    public void shouldReturnEmptySetWhenLdapRolesDoNotContainAllowedRole() throws Exception {
        final OrgServiceRolesExtractor extractor = spy(new OrgServiceRolesExtractor(//userDao,
                                                                                    //accountDao,
                                                                                    preferenceDao,
                                                                                    null,
                                                                                    null,
                                                                                    null,
                                                                                    "member",
                                                                                    "admin",
                                                                                    null));
        doReturn(Collections.<String>emptySet()).when(extractor).getRoles(ticket.getPrincipal().getId());

        assertTrue(extractor.extractRoles(ticket, "wsId", "accId").isEmpty());
    }

    @Test
    public void shouldReturnNormalUserRolesWhenLdapRolesContainAllowedRole() throws Exception {
        final OrgServiceRolesExtractor extractor = spy(new OrgServiceRolesExtractor(
                                                                                    //userDao,
                                                                                    //accountDao,
                                                                                    preferenceDao,
                                                                                    null,
                                                                                    null,
                                                                                    null,
                                                                                    "member",
                                                                                    "codenvy-user",
                                                                                    null));
        doReturn(singleton("codenvy-user")).when(extractor).getRoles(ticket.getPrincipal().getId());

        assertEquals(extractor.extractRoles(ticket, "wsId", "accId"), singleton("user"));
    }

    @Test
    public void shouldReturnTempUserRoleWhenPreferencesContainTemporaryAttribute() throws Exception {
        final OrgServiceRolesExtractor extractor = spy(new OrgServiceRolesExtractor(preferenceDao,
                                                                                    null,
                                                                                    null,
                                                                                    null,
                                                                                    "employeeType",
                                                                                    null,
                                                                                    null));
        when(preferenceDao.getPreferences(ticket.getPrincipal().getId())).thenReturn(singletonMap("temporary", "true"));
        doReturn(Collections.<String>emptySet()).when(extractor).getRoles(ticket.getPrincipal().getId());

        assertEquals(extractor.extractRoles(ticket, "wsId", "accId"), singleton("temp_user"));
    }

    @Test(enabled = false)
    public void shouldReturnAccountRolesWithUserRoleWhenUserHasAccessToAccount() throws Exception {
        org.eclipse.che.api.account.server.dao.Member member = new org.eclipse.che.api.account.server.dao.Member();
        member.withUserId(ticket.getPrincipal().getId()).withRoles(asList("account/owner", "account/member"));
        when(accountDao.getMembers("accId")).thenReturn(asList(member));

        when(accountDao.getById("accId")).thenReturn(new Account());

        assertEquals(extractor.extractRoles(ticket, "wsId", "accId"), new HashSet<>(asList("user", "account/owner", "account/member")));
    }

    @Test
    public void shouldReturnWorkspaceRolesWithUserRoleWhenUserHasAccessToWorkspace() throws Exception {
        final OrgServiceRolesExtractor extractor = spy(new OrgServiceRolesExtractor(preferenceDao,
                                                                                    null,
                                                                                    null,
                                                                                    null,
                                                                                    "employeeType",
                                                                                    null,
                                                                                    null));
        doReturn(Collections.<String>emptySet()).when(extractor).getRoles(ticket.getPrincipal().getId());
        final Membership membership = new MembershipDo("workspace",
                                                       asList("workspace/admin", "workspace/developer"),
                                                       null,
                                                       null,
                                                       "wsId",
                                                       null);

        //final HashSet<String> expectedRoles = new HashSet<>(asList("user", "workspace/developer", "workspace/admin"));
        final HashSet<String> expectedRoles = new HashSet<>(asList("user"));
        assertEquals(extractor.extractRoles(ticket, "wsId", "accId"), expectedRoles);
    }

    @Test(enabled = false)
    public void shouldReturnEmptySetWhenUserDoesNotExist() throws Exception {
        when(userDao.getById(ticket.getPrincipal().getId())).thenThrow(new NotFoundException("fake"));

        assertTrue(extractor.extractRoles(ticket, "wsId", "accId").isEmpty());
    }

    @Test(expectedExceptions = RuntimeException.class,
          expectedExceptionsMessageRegExp = "fake", enabled = false)
    public void shouldRethrowServerExceptionAsRuntimeException() throws Exception {
        when(userDao.getById(ticket.getPrincipal().getId())).thenThrow(new ServerException("fake"));

        extractor.extractRoles(ticket, "wsId", "accId");
    }

    @Test
    public void shouldReturnEmptySetWhenAuthHandlerTypeIsSysLdap() {
        final AccessTicket ticket = new AccessTicket("token", mock(UserImpl.class), "sysldap");

        assertTrue(extractor.extractRoles(ticket, null, null).isEmpty());
    }

    @Test
    public void shouldReturnSystemAdminAndManagerRolesIfGetRolesMethodReturnsIt() throws Exception {
        final OrgServiceRolesExtractor extractor = spy(new OrgServiceRolesExtractor(preferenceDao,
                                                                                    null,
                                                                                    null,
                                                                                    null,
                                                                                    "employeeType",
                                                                                    null,
                                                                                    null));
        doReturn(new HashSet<>(asList("system/admin", "system/manager", "fake"))).when(extractor).getRoles(ticket.getPrincipal().getId());

        assertTrue(extractor.extractRoles(ticket, "wsId", "accId").contains("system/admin"));
        assertTrue(extractor.extractRoles(ticket, "wsId", "accId").contains("system/manager"));
        assertFalse(extractor.extractRoles(ticket, "wsId", "accId").contains("fake"));
    }
}
