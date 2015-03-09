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
package com.codenvy.api.dao.ldap;

import org.eclipse.che.api.account.server.dao.Account;
import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.user.server.dao.UserProfileDao;
import org.eclipse.che.api.user.server.dao.User;
import org.eclipse.che.api.workspace.server.dao.Member;
import org.eclipse.che.api.workspace.server.dao.MemberDao;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;

import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

@Listeners(value = {MockitoTestNGListener.class})
public class UserDaoTest extends BaseTest {
    UserDaoImpl userDao;
    User[]      users;

    @Mock
    UserProfileDao profileDao;
    @Mock
    AccountDao     accountDao;
    @Mock
    MemberDao      memberDao;
    @Mock
    WorkspaceDao   workspaceDao;

    @BeforeMethod
    public void setUp() throws Exception {
        final InitialLdapContextFactory ldapEnv = new InitialLdapContextFactory(embeddedLdapServer.getUrl(),
                                                            null,
                                                            null,
                                                            null,
                                                            null,
                                                            null,
                                                            null,
                                                            null,
                                                            null);
        userDao = new UserDaoImpl(accountDao,
                                  memberDao,
                                  profileDao,
                                  workspaceDao,
                                  ldapEnv,
                                  "dc=codenvy;dc=com",
                                  new UserAttributesMapper(),
                                  new EventService());
        users = new User[]{
                new User().withId("1")
                          .withEmail("user1@mail.com")
                          .withPassword("secret")
                          .withAliases(asList("user1@mail.com")),
                new User().withId("2")
                          .withEmail("user2@mail.com")
                          .withPassword("secret")
                          .withAliases(asList("user2@mail.com")),
                new User().withId("3")
                          .withEmail("user3@mail.com")
                          .withPassword("secret")
                          .withAliases(asList("user3@mail.com"))
        };
        for (User user : users) {
            userDao.create(user);
        }
    }

    @Test
    public void testAuthenticate() throws Exception {
        Assert.assertTrue(userDao.authenticate(users[0].getAliases().get(0), users[0].getPassword()));
    }

    @Test
    public void testAuthenticateInvalidPassword() throws Exception {
        Assert.assertFalse(userDao.authenticate(users[0].getAliases().get(0), "invalid"));
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void testAuthenticateNotExistedUser() throws Exception {
        userDao.authenticate("no_found", "secret");
    }

    @Test
    public void testGetUserById() throws Exception {
        User userById = userDao.getById(users[1].getId());
        Assert.assertEquals(userById.getId(), users[1].getId());
        Assert.assertEquals(userById.getEmail(), users[1].getEmail());
        Assert.assertEquals(userById.getPassword(), null);
        Assert.assertEquals(userById.getAliases(), users[1].getAliases());
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void testGetNotExistedUserById() throws Exception {
        userDao.getById("invalid");
    }

    @Test
    public void testGetUserByAlias() throws Exception {
        User userByAlias = userDao.getByAlias(users[2].getAliases().get(0));
        Assert.assertEquals(userByAlias.getId(), users[2].getId());
        Assert.assertEquals(userByAlias.getEmail(), users[2].getEmail());
        Assert.assertEquals(userByAlias.getPassword(), null);
        Assert.assertEquals(userByAlias.getAliases(), users[2].getAliases());
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void testGetNotExistedUserByAlias() throws Exception {
        userDao.getByAlias("invalid");
    }

    @Test
    public void testUpdateUser() throws Exception {
        User copy = doClone(users[0]);
        copy.setEmail("example@mail.com");
        copy.setPassword("new_secret");
        copy.setAliases(asList("example@mail.com"));
        userDao.update(copy);
        User updated = userDao.getById(copy.getId());
        Assert.assertEquals(updated.getId(), copy.getId());
        Assert.assertEquals(updated.getEmail(), copy.getEmail());
        Assert.assertEquals(updated.getPassword(), null);
        Assert.assertEquals(updated.getAliases(), copy.getAliases());
    }

    @Test
    public void testUpdateNotExistedUser() throws Exception {
        User copy = doClone(users[0]);
        copy.setId("invalid"); // ID may not be updated
        copy.setEmail("example@mail.com");
        copy.setPassword("new_secret");
        copy.setAliases(asList("example@mail.com"));
        try {
            userDao.update(copy);
            fail();
        } catch (NotFoundException ignored) {
        }
        User updated = userDao.getById(users[0].getId());
        Assert.assertEquals(updated.getId(), users[0].getId());
        Assert.assertEquals(updated.getEmail(), users[0].getEmail());
        Assert.assertEquals(updated.getPassword(), null);
        Assert.assertEquals(updated.getAliases(), users[0].getAliases());
    }

    @Test
    public void testUpdateUserConflictAlias() throws Exception {
        User copy = doClone(users[0]);
        copy.setEmail("example@mail.com");
        copy.setPassword("new_secret");
        String conflictAlias = users[1].getAliases().get(0);
        copy.getAliases().add("example@mail.com");
        copy.getAliases().add(conflictAlias); // try use alias that is already used by another user
        try {
            userDao.update(copy);
            fail();
        } catch (ServerException e) {
            Assert.assertEquals(e.getMessage(),
                                String.format("Unable update user '%s'. User alias %s is already in use.", copy.getId(), conflictAlias));
        }
        User updated = userDao.getById(users[0].getId());
        Assert.assertEquals(updated.getId(), users[0].getId());
        Assert.assertEquals(updated.getEmail(), users[0].getEmail());
        Assert.assertEquals(updated.getPassword(), null);
        Assert.assertEquals(updated.getAliases(), users[0].getAliases());
    }

    @Test
    public void testRemoveUser() throws Exception {
        final Account testAccount = new Account().withId("account_id");
        when(accountDao.getByOwner(users[0].getId())).thenReturn(asList(testAccount));
        final org.eclipse.che.api.account.server.dao.Member accountMember = new org.eclipse.che.api.account.server.dao.Member();
        accountMember.withUserId(users[0].getId())
                     .withAccountId(testAccount.getId())
                     .withRoles(asList("account/owner"));
        when(accountDao.getMembers(testAccount.getId())).thenReturn(asList(accountMember));
        when(accountDao.getByMember(users[0].getId())).thenReturn(asList(accountMember));
        final Member workspaceMember = new Member().withUserId(users[0].getId())
                                                   .withWorkspaceId("test_workspace_id")
                                                   .withRoles(asList("workspace/developer"));
        when(memberDao.getUserRelationships(users[0].getId())).thenReturn(asList(workspaceMember));
        assertNotNull(userDao.getById(users[0].getId()));

        userDao.remove(users[0].getId());

        try {
            userDao.getById(users[0].getId());
            fail();
        } catch (NotFoundException ignored) {
        }
        verify(accountDao).remove(testAccount.getId());
        verify(accountDao).removeMember(accountMember);
        verify(memberDao).remove(workspaceMember);
        verify(profileDao).remove(users[0].getId());
    }

    @Test
    public void testRemoveNotExistedUser() throws Exception {
        try {
            userDao.remove("invalid");
            fail();
        } catch (NotFoundException ignored) {
        }
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = ".*User already exists.*")
    public void testCreateUserConflictId() throws Exception {
        User copy = doClone(users[0]);
        copy.setEmail("example@mail.com");
        copy.setPassword("new_secret");
        copy.setAliases(asList("example@mail.com"));
        userDao.create(copy);
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = ".*User alias .* is already in use.*")
    public void testCreateUserConflictAlias() throws Exception {
        User copy = doClone(users[0]);
        copy.setId("new_id");
        copy.setEmail("example@mail.com");
        copy.setPassword("new_secret");
        // Keep one of aliases from existed user. Duplication of aliases is not allowed!!
        copy.getAliases().add("example@mail.com");
        userDao.create(copy);
    }

    private User doClone(User other) {
        return new User().withId(other.getId())
                         .withEmail(other.getEmail())
                         .withPassword(other.getPassword())
                         .withAliases(new ArrayList<>(other.getAliases()));
    }
}