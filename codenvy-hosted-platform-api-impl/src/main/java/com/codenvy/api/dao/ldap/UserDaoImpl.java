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
import com.codenvy.api.event.user.RemoveUserEvent;
import org.eclipse.che.api.user.server.dao.UserDao;
import org.eclipse.che.api.user.server.dao.UserProfileDao;
import org.eclipse.che.api.user.server.dao.User;
import org.eclipse.che.api.workspace.server.dao.Member;
import org.eclipse.che.api.workspace.server.dao.MemberDao;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;

/**
 * LDAP based implementation of {@code UserDao}.
 *
 * @author andrew00x
 */
@Singleton
public class UserDaoImpl implements UserDao {
    private static final Logger LOG = LoggerFactory.getLogger(UserDaoImpl.class);

    private final String                    userObjectclassFilter;
    private final String                    userContainerDn;
    private final EventService              eventService;
    private final AccountDao                accountDao;
    private final MemberDao                 memberDao;
    private final UserProfileDao            profileDao;
    private final WorkspaceDao              workspaceDao;
    private final UserAttributesMapper      userAttributesMapper;
    private final InitialLdapContextFactory contextFactory;


    /**
     * Creates new instance of {@code UserDaoImpl}.
     *
     * @param userContainerDn
     *         full name of root object for user records, e.g. {@code ou=People,dc=codenvy,dc=com}
     * @param userAttributesMapper
     *         UserAttributesMapper
     */
    @Inject
    @SuppressWarnings("unchecked")
    public UserDaoImpl(AccountDao accountDao,
                       MemberDao memberDao,
                       UserProfileDao profileDao,
                       WorkspaceDao workspaceDao,
                       InitialLdapContextFactory contextFactory,
                       @Named("user.ldap.user_container_dn") String userContainerDn,
                       UserAttributesMapper userAttributesMapper,
                       EventService eventService) {
        this.contextFactory = contextFactory;
        this.userContainerDn = userContainerDn;
        this.userAttributesMapper = userAttributesMapper;
        this.eventService = eventService;
        this.accountDao = accountDao;
        this.memberDao = memberDao;
        this.profileDao = profileDao;
        this.workspaceDao = workspaceDao;
        StringBuilder sb = new StringBuilder();
        for (String objectClass : userAttributesMapper.userObjectClasses) {
            sb.append("(objectClass=");
            sb.append(objectClass);
            sb.append(')');
        }
        this.userObjectclassFilter = sb.toString();
    }

    @Override
    public boolean authenticate(String alias, String password) throws NotFoundException, ServerException {
        if (alias == null || alias.isEmpty() || password == null || password.isEmpty()) {
            LOG.warn("Empty username or password");
            return false;
        }
        try {
            final User user = doGetByAlias(alias);
            if (user == null) {
                throw new NotFoundException("User not found " + alias);
            }
            final String id = user.getId();
            final String userDn = getUserDn(id);
            InitialLdapContext authContext = null;
            try {
                authContext = contextFactory.createContext(userDn, password);
                return true;
            } catch (AuthenticationException e) {
                LOG.warn(format("Invalid password for user %s", userDn));
                return false;
            } catch (NamingException e) {
                throw new ServerException(format("Authentication failed for user '%s'", alias), e);
            } finally {
                close(authContext);
            }
        } catch (NamingException e) {
            throw new ServerException(format("Authentication failed for user '%s'", alias), e);
        }
    }

    @Override
    public void create(User user) throws ConflictException, ServerException {
        InitialLdapContext context = null;
        DirContext newContext = null;
        try {
            for (String alias : user.getAliases()) {
                if (doGetByAlias(alias) != null) {
                    throw new ConflictException(
                            format("Unable create new user '%s'. User alias %s is already in use.", user.getEmail(), alias));
                }
            }
            context = contextFactory.createContext();
            newContext = context.createSubcontext(getUserDn(user.getId()), userAttributesMapper.toAttributes(user));

            logUserEvent("user-created", user);
        } catch (NameAlreadyBoundException e) {
            throw new ConflictException(format("Unable create new user '%s'. User already exists.", user.getId()));
        } catch (NamingException e) {
            throw new ServerException(format("Unable create new user '%s'", user.getEmail()), e);
        } finally {
            close(newContext);
            close(context);
        }
    }

    @Override
    public void update(User user) throws NotFoundException, ServerException {
        final String id = user.getId();
        try {

            final User existed = doGetById(id);
            if (existed == null) {
                throw new NotFoundException("User not found " + id);
            }

            for (String alias : user.getAliases()) {
                final User byAlias = doGetByAlias(alias);
                if (!(byAlias == null || id.equals(byAlias.getId()))) {
                    throw new ServerException(format("Unable update user '%s'. User alias %s is already in use.", id, alias));
                }
            }


            InitialLdapContext context = null;
            try {
                final ModificationItem[] mods = userAttributesMapper.createModifications(existed, user);
                if (mods.length > 0) {
                    context = contextFactory.createContext();
                    context.modifyAttributes(getUserDn(id), mods);
                }
            } catch (NamingException e) {

                throw new ServerException(format("Unable update (user) '%s'", user.getEmail()), e);
            } finally {
                close(context);
            }

            logUserEvent("user-updated", user);
        } catch (NamingException e) {
            throw new ServerException(format("Unable update user '%s'", user.getEmail()), e);
        }
    }

    @Override
    public void remove(String id) throws NotFoundException, ServerException, ConflictException {
        final User user = getById(id);
        //search for accounts which should be removed
        final List<Account> accountsToRemove = new LinkedList<>();
        for (Account account : accountDao.getByOwner(id)) {
            //if user is last account owner we should remove account
            if (isOnlyOneOwner(account.getId())) {
                if (workspaceDao.getByAccount(account.getId()).isEmpty()) {
                    accountsToRemove.add(account);
                } else {
                    throw new ConflictException(format("Account %s has related workspaces", account.getId()));
                }
            }
        }
        //remove user relationships with workspaces
        for (Member member : memberDao.getUserRelationships(id)) {
            memberDao.remove(member);
        }
        //remove user relationships with accounts
        for (org.eclipse.che.api.account.server.dao.Member member : accountDao.getByMember(id)) {
            accountDao.removeMember(member);
        }
        //remove accounts
        for (Account account : accountsToRemove) {
            accountDao.remove(account.getId());
        }
        //remove profile
        profileDao.remove(id);
        //remove user
        InitialLdapContext context = null;
        try {
            context = contextFactory.createContext();
            context.destroySubcontext(getUserDn(id));
            logUserEvent("user-removed", user);
            eventService.publish(new RemoveUserEvent(id));
        } catch (NameNotFoundException e) {
            throw new NotFoundException("User not found " + id);
        } catch (NamingException e) {
            throw new ServerException(format("Unable remove user '%s'", id), e);
        } finally {
            close(context);
        }
    }

    @Override
    public User getByAlias(String alias) throws NotFoundException, ServerException {
        try {

            final User user = doGetByAlias(alias);

            if (user == null) {
                throw new NotFoundException("User not found " + alias);
            }
            return doClone(user);
        } catch (NamingException e) {
            throw new ServerException(format("Unable get user '%s'", alias), e);
        }
    }

    @Override
    public User getById(String id) throws NotFoundException, ServerException {
        try {
            final User user = doGetById(id);
            if (user == null) {
                throw new NotFoundException("User not found " + id);
            }
            return doClone(user);
        } catch (NamingException e) {
            throw new ServerException(format("Unable get user '%s'", id), e);
        }
    }

    private User doClone(User other) {
        return new User().withId(other.getId())
                         .withEmail(other.getEmail())
                         .withPassword(other.getPassword())
                         .withAliases(new ArrayList<>(other.getAliases()));
    }

    private boolean isOnlyOneOwner(String accountId) throws ServerException {
        int owners = 0;
        for (org.eclipse.che.api.account.server.dao.Member member : accountDao.getMembers(accountId)) {
            if (member.getRoles().contains("account/owner")) {
                owners++;
            }
        }
        return owners == 1;
    }

    private User doGetByAlias(String alias) throws NamingException {
        User user = null;
        InitialLdapContext context = null;
        try {
            context = contextFactory.createContext();
            final Attributes attributes = getUserAttributesByAlias(context, alias);
            if (attributes != null) {
                user = userAttributesMapper.fromAttributes(attributes);
            }
        } finally {
            close(context);
        }
        return user;
    }

    private User doGetById(String id) throws NamingException {
        User user = null;
        InitialLdapContext context = null;
        try {
            context = contextFactory.createContext();
            final Attributes attributes = getUserAttributesById(context, id);
            if (attributes != null) {
                user = userAttributesMapper.fromAttributes(attributes);
            }
        } finally {
            close(context);
        }
        return user;
    }

    protected String getUserDn(String userId) {
        return userAttributesMapper.userDn + '=' + userId + ',' + userContainerDn;
    }

    protected Attributes getUserAttributesById(InitialLdapContext context, String userId) throws NamingException {
        try {
            return context.getAttributes(getUserDn(userId));
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    protected Attributes getUserAttributesByAlias(InitialLdapContext context, String alias) throws NamingException {
        final SearchControls constraints = new SearchControls();
        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
        final String filter = "(&(" + userAttributesMapper.userAliasesAttr + '=' + alias + ")(" + userObjectclassFilter + "))";
        NamingEnumeration<SearchResult> enumeration = null;
        try {
            enumeration = context.search(userContainerDn, filter, constraints);
            if (enumeration.hasMore()) {
                return enumeration.nextElement().getAttributes();
            }
            return null;
        } finally {
            close(enumeration);
        }
    }

    private void close(Context ctx) {
        if (ctx != null) {
            try {
                ctx.close();
            } catch (NamingException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    private void close(NamingEnumeration<SearchResult> enumeration) {
        if (enumeration != null) {
            try {
                enumeration.close();
            } catch (NamingException e) {
                LOG.warn(e.getMessage(), e);
            }
        }
    }

    private void logUserEvent(String event, User user) {
        Set<String> emails = new HashSet<>(user.getAliases());
        emails.add(user.getEmail());

        LOG.info("EVENT#{}# USER#{}# USER-ID#{}# EMAILS#{}#",
                 event,
                 user.getEmail(),
                 user.getId(),
                 emails);
    }
}
