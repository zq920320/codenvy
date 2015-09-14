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

import org.eclipse.che.api.user.server.dao.PreferenceDao;
import org.eclipse.che.api.user.server.dao.UserDao;
import org.eclipse.che.api.user.server.dao.UserProfileDao;
import org.eclipse.che.api.user.server.dao.User;

import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
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

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;

//TODO fix it after account refactoring, memberships refactoring

/**
 * LDAP based implementation of {@code UserDao}.
 *
 * @author andrew00x
 */
@Singleton
public class UserDaoImpl implements UserDao {
    private static final Logger LOG = LoggerFactory.getLogger(UserDaoImpl.class);

    private final String                    userObjectClassFilter;
    private final String                    containerDn;
    private final String                    userDn;
    private final String                    oldUserDn;
    private final EventService              eventService;
    private final AccountDao                accountDao;
    //    private final MemberDao                 memberDao;
    private final UserProfileDao            profileDao;
    private final WorkspaceDao              workspaceDao;
    private final PreferenceDao             preferenceDao;
    private final UserAttributesMapper      mapper;
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
    public UserDaoImpl(AccountDao accountDao,
//                       MemberDao memberDao,
                       UserProfileDao profileDao,
                       WorkspaceDao workspaceDao,
                       PreferenceDao preferenceDao,
                       InitialLdapContextFactory contextFactory,
                       @Named("user.ldap.user_container_dn") String userContainerDn,
                       @Named("user.ldap.user_dn") String userDn,
                       @Named("user.ldap.old_user_dn") String oldUserDn,
                       UserAttributesMapper userAttributesMapper,
                       EventService eventService) {
        this.contextFactory = contextFactory;
        this.containerDn = userContainerDn;
        this.userDn = userDn;
        this.oldUserDn = oldUserDn;
        this.mapper = userAttributesMapper;
        this.eventService = eventService;
        this.accountDao = accountDao;
//        this.memberDao = memberDao;
        this.profileDao = profileDao;
        this.workspaceDao = workspaceDao;
        this.preferenceDao = preferenceDao;
        final StringBuilder sb = new StringBuilder();
        for (String objectClass : userAttributesMapper.userObjectClasses) {
            sb.append("(objectClass=");
            sb.append(objectClass);
            sb.append(')');
        }
        this.userObjectClassFilter = sb.toString();
    }

    @Override
    public boolean authenticate(String alias, String password) throws NotFoundException, ServerException {
        if (isNullOrEmpty(alias) || isNullOrEmpty(password)) {
            LOG.warn("Can't perform authentication for user '{}'. Username or password is empty", alias);
            return false;
        }
        try {
            User user = doGetByAlias(alias);
            if (user == null) {
                user = doGetByName(alias);
            }
            if (user == null) {
                throw new NotFoundException("User '" + alias + "' was not found");
            }
            InitialLdapContext authContext = null;
            final String principal = formatDn(userDn, user.getId());
            try {
                authContext = contextFactory.createContext(principal, password);
            } catch (AuthenticationException e) {
                //if first time authentication failed, try to rename user entity
                doGetById(user.getId());
                //retry authentication
                try {
                    authContext = contextFactory.createContext(principal, password);
                } catch (AuthenticationException e2) {
                    LOG.warn(format("Invalid password for user %s", principal));
                    return false;
                }
            } finally {
                close(authContext);
            }
        } catch (NamingException e) {
            throw new ServerException(format("Authentication failed for user '%s'", alias), e);
        }
        return true;
    }

    @Override
    public void create(User user) throws ConflictException, ServerException {
        InitialLdapContext context = null;
        DirContext newContext = null;
        try {
            for (String alias : user.getAliases()) {
                if (doGetByAlias(alias) != null) {
                    throw new ConflictException(format("User with alias '%s' already exists", alias));
                }
            }
            //TODO consider null value for user name
            if (user.getName() != null && doGetByName(user.getName()) != null) {
                throw new ConflictException(format("User with name '%s' already exists", user.getName()));
            }
            context = contextFactory.createContext();
            newContext = context.createSubcontext(formatDn(userDn, user.getId()), mapper.toAttributes(user));

            logUserEvent("user-created", user);
        } catch (NameAlreadyBoundException e) {
            throw new ConflictException(format("Unable create new user '%s'. User already exists", user.getId()));
        } catch (NamingException e) {
            throw new ServerException(format("Unable create new user '%s'", user.getEmail()), e);
        } finally {
            close(newContext);
            close(context);
        }
    }

    @Override
    public void update(User update) throws NotFoundException, ServerException, ConflictException {
        final String id = update.getId();
        try {
            final User target = doGetById(id);

            //check that user exists
            if (target == null) {
                throw new NotFoundException("User " + id + " was not found");
            }

            //check that new aliases are unique
            final Set<String> newAliases = new HashSet<>(update.getAliases());
            newAliases.removeAll(target.getAliases());
            for (String alias : newAliases) {
                if (doGetByAlias(alias) != null) {
                    throw new ConflictException(format("Unable update user '%s', alias %s is already in use", id, alias));
                }
            }

            //check that new name is unique
            if (update.getName() != null && !target.getName().equals(update.getName()) && doGetByName(update.getName()) != null) {
                throw new ConflictException(format("Unable update user '%s', name %s is already in use", id, update.getName()));
            }

            InitialLdapContext context = null;
            try {
                final ModificationItem[] mods = mapper.createModifications(target, update);
                if (mods.length > 0) {
                    context = contextFactory.createContext();
                    context.modifyAttributes(formatDn(userDn, id), mods);
                }
            } catch (NamingException e) {
                throw new ServerException(format("Unable update (user) '%s'", update.getEmail()), e);
            } finally {
                close(context);
            }

            logUserEvent("user-updated", update);
        } catch (NamingException e) {
            throw new ServerException(format("Unable update user '%s'", update.getEmail()), e);
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
//                if (workspaceDao.getByAccount(account.getId()).isEmpty()) {
//                    accountsToRemove.add(account);
//                } else {
//                    throw new ConflictException(format("Account %s has related workspaces", account.getId()));
//                }
            }
        }
        //remove user relationships with workspaces
//        for (Member member : memberDao.getUserRelationships(id)) {
//            memberDao.remove(member);
//        }
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
        //remove preferences
        preferenceDao.remove(id);
        //remove user
        InitialLdapContext context = null;
        try {
            context = contextFactory.createContext();
            context.destroySubcontext(formatDn(userDn, id));
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
                throw new NotFoundException("User " + id + " was not found ");
            }
            return doClone(user);
        } catch (NamingException e) {
            throw new ServerException(format("Unable get user '%s'", id), e);
        }
    }

    private User doClone(User other) {
        return new User().withId(other.getId())
                         .withEmail(other.getEmail())
                         .withName(other.getName())
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
                user = mapper.fromAttributes(attributes);
            }
        } finally {
            close(context);
        }
        return user;
    }

    private User doGetByName(String name) throws NamingException {
        User user = null;
        InitialLdapContext context = null;
        try {
            context = contextFactory.createContext();
            final Attributes attributes = getUserAttributesByName(context, name);
            if (attributes != null) {
                user = mapper.fromAttributes(attributes);
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
                user = mapper.fromAttributes(attributes);
            }
        } finally {
            close(context);
        }
        return user;
    }

    private Attributes getUserAttributesById(InitialLdapContext context, String id) throws NamingException {
        try {
            //try to find user using dn
            return context.getAttributes(formatDn(userDn, id));
        } catch (NameNotFoundException nfEx) {
            //if not found -> try to find user using old dn
            try {
                final Attributes attributes = context.getAttributes(formatDn(oldUserDn, id));

                //if attributes were found then rename current entity
                final String fromDnVal = attributes.get(oldUserDn).get().toString();
                final String toDnVal = attributes.get(userDn).get().toString();
                context.rename(formatDn(oldUserDn, fromDnVal), formatDn(userDn, toDnVal));

                return attributes;
            } catch (NameNotFoundException nfEx2) {
                return null;
            }
        }
    }

    private Attributes getUserAttributesByName(InitialLdapContext context, String name) throws NamingException {
        try {
            final SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            final String filter = createFilter(mapper.userNameAttr, name);
            NamingEnumeration<SearchResult> enumeration = null;
            try {
                enumeration = context.search(containerDn, filter, controls);
                if (enumeration.hasMore()) {
                    return enumeration.next().getAttributes();
                }
                return null;
            } finally {
                close(enumeration);
            }
        } catch (NameNotFoundException nfEx) {
            return null;
        }
    }

    private String formatDn(String userDn, String id) {
        return userDn + '=' + id + ',' + containerDn;
    }

    private String createFilter(String attribute, String value) {
        return "(&(" + attribute + '=' + value + ")(" + userObjectClassFilter + "))";
    }

    private Attributes getUserAttributesByAlias(InitialLdapContext context, String alias) throws NamingException {
        final SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        final String filter = "(&(" + mapper.userAliasesAttr + '=' + alias + ")(" + userObjectClassFilter + "))";
        NamingEnumeration<SearchResult> enumeration = null;
        try {
            enumeration = context.search(containerDn, filter, controls);
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
