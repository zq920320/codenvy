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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;
import java.util.ArrayList;

import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

@Listeners(value = {MockitoTestNGListener.class})
public class UserDaoTest extends BaseTest {

    @Mock
    UserProfileDao profileDao;
    @Mock
    AccountDao     accountDao;
    @Mock
    MemberDao      memberDao;
    @Mock
    WorkspaceDao   workspaceDao;

    UserDaoImpl               userDao;
    InitialLdapContextFactory factory;
    UserAttributesMapper      mapper;
    User[]                    users;

    @BeforeMethod
    public void setUp() throws Exception {
        factory = spy(new InitialLdapContextFactory(embeddedLdapServer.getUrl(),
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null));
        mapper = spy(new UserAttributesMapper());
        userDao = new UserDaoImpl(accountDao,
                                  memberDao,
                                  profileDao,
                                  workspaceDao,
                                  factory,
                                  "dc=codenvy;dc=com",
                                  mapper,
                                  new EventService());

        users = new User[]{
                new User().withId("1")
                          .withEmail("user1@mail.com")
                          .withName("user1")
                          .withPassword("secret")
                          .withAliases(singletonList("user1@mail.com")),
                new User().withId("2")
                          .withName("user2")
                          .withEmail("user2@mail.com")
                          .withPassword("secret")
                          .withAliases(singletonList("user2@mail.com")),
                new User().withId("3")
                          .withName("user3")
                          .withEmail("user3@mail.com")
                          .withPassword("secret")
                          .withAliases(singletonList("user3@mail.com"))
        };
        for (User user : users) {
            userDao.create(user);
        }
    }

    @Test
    public void shouldAuthenticateUserUsingAliases() throws Exception {
        assertTrue(userDao.authenticate(users[0].getAliases().get(0), users[0].getPassword()));
    }

    @Test
    public void shouldAuthenticateUserUsingName() throws Exception {
        assertTrue(userDao.authenticate(users[0].getName(), users[0].getPassword()));
    }

    @Test
    public void shouldNotAuthenticateUserWithWrongPassword() throws Exception {
        assertFalse(userDao.authenticate(users[0].getName(), "invalid"));
    }

    @Test
    public void shouldNotAuthenticateUserWithEmptyAlias() throws Exception {
        assertFalse(userDao.authenticate("", "valid"));
    }

    @Test
    public void shouldNotAuthenticateUserWithEmptyPassword() throws Exception {
        assertFalse(userDao.authenticate(users[0].getName(), ""));
    }

    @Test
    public void shouldNotAuthenticateUserWithNullAlias() throws Exception {
        assertFalse(userDao.authenticate(null, "valid"));
    }

    @Test
    public void shouldNotAuthenticateUserWithNullPassword() throws Exception {
        assertFalse(userDao.authenticate(users[0].getName(), null));
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenAuthenticatingNotExistingUser() throws Exception {
        userDao.authenticate("not_found", "secret");
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldWrapAnyNamingExceptionWithServerExceptionWhenAuthenticatingUser() throws Exception {
        when(factory.createContext()).thenThrow(new NamingException("message"));

        userDao.authenticate(users[0].getName(), users[0].getPassword());
    }

    @Test
    public void shouldCreateUser() throws Exception {
        final User newUser = new User().withId("user123")
                                       .withName("user123_name")
                                       .withEmail("user123@mail.com")
                                       .withPassword("password");

        userDao.create(newUser);

        final User result = userDao.getById("user123");
        assertEquals(result.getId(), newUser.getId());
        assertEquals(result.getName(), newUser.getName());
        assertEquals(result.getEmail(), newUser.getEmail());
        assertEquals(result.getAliases(), singletonList(newUser.getEmail()));
        assertNull(result.getPassword());
    }

    @Test
    public void shouldUseUserNameAsEmailAndAliasWhenCreatingUser() throws Exception {
        final User newUser = new User().withId("user123")
                                       .withName("user123_name")
                                       .withPassword("password");

        userDao.create(newUser);

        final User result = userDao.getById("user123");
        assertEquals(result.getId(), newUser.getId());
        assertEquals(result.getName(), newUser.getName());
        assertEquals(result.getEmail(), newUser.getName());
        assertEquals(result.getAliases(), singletonList(newUser.getEmail()));
        assertNull(result.getPassword());
    }

    @Test
    public void shouldUseUserIdWhenUserNameIsNullWhenCreatingUser() throws Exception {
        final User newUser = new User().withId("user123")
                                       .withName("user123")
                                       .withPassword("password");

        userDao.create(newUser);

        final User result = userDao.getByAlias("user123");
        assertEquals(result.getId(), newUser.getId());
        assertEquals(result.getName(), newUser.getId());
        assertEquals(result.getEmail(), newUser.getEmail());
        assertEquals(result.getAliases(), singletonList(newUser.getEmail()));
        assertNull(result.getPassword());
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Unable create new user .*. User already exists")
    public void shouldThrowConflictExceptionWhenCreatingUserWithReservedId() throws Exception {
        userDao.create(doClone(users[0]).withEmail("example@mail.com")
                                        .withName("new_name")
                                        .withAliases(singletonList("example@mail.com"))
                                        .withPassword("new password"));
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "User with alias .* already exists")
    public void shouldThrowConflictExceptionWhenCreatingUserWithReservedAlias() throws Exception {
        final User copy = doClone(users[0]).withName("new_name")
                                           .withId("new_id")
                                           .withEmail("example@mail.com")
                                           .withPassword("new_secret");
        copy.getAliases().add("example@mail.com");

        userDao.create(copy);
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "User with name .* already exists")
    public void shouldThrowConflictExceptionWhenCreatingUserWithReservedName() throws Exception {
        userDao.create(doClone(users[0]).withId("new_id")
                                        .withEmail("example@mail.com")
                                        .withAliases(singletonList("example@mail.com"))
                                        .withPassword("new password"));
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Unable create new user .*. User already exists")
    public void shouldWrapAnyNameAlreadyBoundExceptionWithConflictExceptionWhenCreatingUser() throws Exception {
        when(factory.createContext()).thenThrow(new NameAlreadyBoundException());

        userDao.create(users[0]);
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldWrapAnyNamingExceptionWithServerExceptionWhenCreatingUser() throws Exception {
        when(factory.createContext()).thenThrow(new NamingException());

        userDao.create(users[0]);
    }

    @Test
    public void shouldUpdateUser() throws Exception {
        final User copy = doClone(users[0]);
        copy.setEmail("example@mail.com");
        copy.setName("new_name");
        copy.setPassword("new_secret");
        copy.setAliases(singletonList("example@mail.com"));

        userDao.update(copy);

        final User updated = userDao.getById(copy.getId());
        assertEquals(updated.getId(), copy.getId());
        assertEquals(updated.getName(), copy.getName());
        assertEquals(updated.getEmail(), copy.getEmail());
        assertEquals(updated.getPassword(), null);
        assertEquals(updated.getAliases(), copy.getAliases());
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenUpdatingNotExistingUser() throws Exception {
        userDao.update(doClone(users[0]).withId("invalid")
                                        .withName("new-name")
                                        .withEmail("new_email@mail.com")
                                        .withPassword("new secret"));
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Unable update user .*, alias .* is already in use")
    public void shouldThrowConflictExceptionWhenUpdatingUserWithAliasWhichIsReserved() throws Exception {
        final User copy = doClone(users[0]);
        copy.setEmail("example@mail.com");
        copy.setPassword("new_secret");
        final String conflictAlias = users[1].getAliases().get(0);
        copy.getAliases().add("example@mail.com");
        copy.getAliases().add(conflictAlias);

        userDao.update(copy);
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Unable update user .*, name .* is already in use")
    public void shouldThrowConflictExceptionWhenUpdatingUserWithNameWhichIsReserved() throws Exception {
        userDao.update(doClone(users[0]).withEmail("new-email@mail.com")
                                        .withPassword("new secret")
                                        .withName(users[1].getName()));
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldWrapAnyNamingExceptionWithServerExceptionWhenUpdatingUser() throws Exception {
        when(factory.createContext()).thenThrow(new NamingException());

        userDao.update(users[0]);
    }

    @Test
    public void shouldBeAbleToGetUserByAlias() throws Exception {
        final User user = userDao.getByAlias(users[2].getAliases().get(0));

        assertEquals(user.getId(), users[2].getId());
        assertEquals(user.getEmail(), users[2].getEmail());
        assertEquals(user.getName(), users[2].getName());
        assertEquals(user.getPassword(), null);
        assertEquals(user.getAliases(), users[2].getAliases());
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenUserWithGivenAliasDoesNotExist() throws Exception {
        userDao.getByAlias("invalid");
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldWrapAnyNamingExceptionWithServerExceptionWhenGettingUserByAlias() throws Exception {
        when(factory.createContext()).thenThrow(new NamingException());

        userDao.getByAlias("valid");
    }

    @Test
    public void shouldBeAbleToGetUserById() throws Exception {
        final User user = userDao.getById(users[1].getId());

        assertEquals(user.getId(), users[1].getId());
        assertEquals(user.getEmail(), users[1].getEmail());
        assertEquals(user.getName(), users[1].getName());
        assertEquals(user.getPassword(), null);
        assertEquals(user.getAliases(), users[1].getAliases());
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenUserWithGivenIdDoesNotExist() throws Exception {
        userDao.getById("invalid");
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldWrapAnyNamingExceptionWithServerExceptionWhenGettingUserById() throws Exception {
        when(factory.createContext()).thenThrow(new NamingException());

        userDao.getById("valid");
    }

    @Test
    public void testRemoveUser() throws Exception {
        final Account testAccount = new Account().withId("account_id");
        when(accountDao.getByOwner(users[0].getId())).thenReturn(singletonList(testAccount));
        final org.eclipse.che.api.account.server.dao.Member accountMember = new org.eclipse.che.api.account.server.dao.Member();
        accountMember.withUserId(users[0].getId())
                     .withAccountId(testAccount.getId())
                     .withRoles(singletonList("account/owner"));
        when(accountDao.getMembers(testAccount.getId())).thenReturn(singletonList(accountMember));
        when(accountDao.getByMember(users[0].getId())).thenReturn(singletonList(accountMember));
        final Member workspaceMember = new Member().withUserId(users[0].getId())
                                                   .withWorkspaceId("test_workspace_id")
                                                   .withRoles(singletonList("workspace/developer"));
        when(memberDao.getUserRelationships(users[0].getId())).thenReturn(singletonList(workspaceMember));
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

    private User doClone(User other) {
        return new User().withId(other.getId())
                         .withName(other.getName())
                         .withEmail(other.getEmail())
                         .withPassword(other.getPassword())
                         .withAliases(new ArrayList<>(other.getAliases()));
    }
}