/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
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

import com.codenvy.api.account.server.dao.Account;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.core.notification.EventService;
import com.codenvy.api.event.user.RemoveUserEvent;
import com.codenvy.api.user.server.dao.UserDao;
import com.codenvy.api.user.server.dao.UserProfileDao;
import com.codenvy.api.user.server.dao.User;
import com.codenvy.api.workspace.server.dao.Member;
import com.codenvy.api.workspace.server.dao.MemberDao;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
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
import java.util.Hashtable;
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

    protected final String providerUrl;
    protected final String systemDn;
    protected final String systemPassword;
    protected final String authType;
    protected final String usePool;
    protected final String initPoolSize;
    protected final String maxPoolSize;
    protected final String prefPoolSize;
    protected final String poolTimeout;
    protected final String userContainerDn;

    protected final UserAttributesMapper userAttributesMapper;
    private final   String               userObjectclassFilter;

    private final EventService   eventService;
    private final AccountDao     accountDao;
    private final MemberDao      memberDao;
    private final UserProfileDao profileDao;
    private final WorkspaceDao   workspaceDao;

    /**
     * Creates new instance of {@code UserDaoImpl}.
     *
     * @param providerUrl
     *         URL of LDAP service provider, e.g. {@code ldap://localhost:389}.
     * @param systemDn
     *         principal used to open LDAP connection, e.g. {@code cn=Admin,ou=system,dc=codenvy,dc=com}. May be omitted if authentication
     *         is not needed, e.g. in tests. See {@link javax.naming.Context#SECURITY_PRINCIPAL}.
     * @param systemPassword
     *         password of principal to open LDAP connection.  May be omitted if authentication is not needed, e.g. in tests. See {@link
     *         javax.naming.Context#SECURITY_CREDENTIALS} .
     * @param authType
     *         authentication type, see {@link javax.naming.Context#SECURITY_AUTHENTICATION}
     * @param usePool
     *         setup policy for connection pooling. Allowed value of this parameter is "true" or "false". See <a
     *         href="http://docs.oracle.com/javase/jndi/tutorial/ldap/connect/config.html">details</a> about connection pooling.
     * @param initPoolSize
     *         initial size of connection pool. Parameter MUST be string representation of an integer. Make sense ONLY if parameter {@code
     *         usePool} is equals to "true".
     * @param maxPoolSize
     *         max size for connection poll. Parameter MUST be string representation of an integer. Make sense ONLY if parameter {@code
     *         usePool} is equals to "true".
     * @param prefPoolSize
     *         preferred size for connection poll. Parameter MUST be string representation of an integer. Make sense ONLY if parameter
     *         {@code usePool} is equals to "true". Often this parameter may be omitted.
     * @param poolTimeout
     *         time (in milliseconds) that an idle connection may remain in the pool. Parameter MUST be string representation of an
     *         integer.
     *         Make sense ONLY if parameter {@code usePool} is equals to "true".
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
                       @Named(Context.PROVIDER_URL) String providerUrl,
                       @Nullable @Named(Context.SECURITY_PRINCIPAL) String systemDn,
                       @Nullable @Named(Context.SECURITY_CREDENTIALS) String systemPassword,
                       @Nullable @Named(Context.SECURITY_AUTHENTICATION) String authType,
                       @Nullable @Named("com.sun.jndi.ldap.connect.pool") String usePool,
                       @Nullable @Named("com.sun.jndi.ldap.connect.pool.initsize") String initPoolSize,
                       @Nullable @Named("com.sun.jndi.ldap.connect.pool.maxsize") String maxPoolSize,
                       @Nullable @Named("com.sun.jndi.ldap.connect.pool.prefsize") String prefPoolSize,
                       @Nullable @Named("com.sun.jndi.ldap.connect.pool.timeout") String poolTimeout,
                       @Named("user.ldap.user_container_dn") String userContainerDn,
                       UserAttributesMapper userAttributesMapper,
                       EventService eventService) {
        this.providerUrl = providerUrl;
        this.systemDn = systemDn;
        this.systemPassword = systemPassword;
        this.authType = authType;
        this.usePool = usePool;
        this.initPoolSize = initPoolSize;
        this.maxPoolSize = maxPoolSize;
        this.prefPoolSize = prefPoolSize;
        this.poolTimeout = poolTimeout;
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

    UserDaoImpl(AccountDao accountDao,
                MemberDao memberDao,
                UserProfileDao profileDao,
                WorkspaceDao workspaceDao,
                @Named(Context.PROVIDER_URL) String providerUrl,
                @Nullable @Named(Context.SECURITY_PRINCIPAL) String systemDn,
                @Nullable @Named(Context.SECURITY_CREDENTIALS) String systemPassword,
                @Nullable @Named(Context.SECURITY_AUTHENTICATION) String authType,
                @Named("user.ldap.user_container_dn") String userContainerDn,
                UserAttributesMapper userAttributesMapper,
                EventService eventService) {
        this(accountDao,
             memberDao,
             profileDao,
             workspaceDao,
             providerUrl,
             systemDn,
             systemPassword,
             authType,
             null,
             null,
             null,
             null,
             null,
             userContainerDn,
             userAttributesMapper,
             eventService);
    }

    UserDaoImpl(AccountDao accountDao,
                MemberDao memberDao,
                UserProfileDao profileDao,
                WorkspaceDao workspaceDao,
                @Named(Context.PROVIDER_URL) String providerUrl,
                @Named("user.ldap.user_container_dn") String userContainerDn,
                UserAttributesMapper userAttributesMapper,
                EventService eventService) {
        this(accountDao,
             memberDao,
             profileDao,
             workspaceDao,
             providerUrl,
             null,
             null,
             null,
             null,
             null,
             null,
             null,
             null,
             userContainerDn,
             userAttributesMapper,
             eventService);
    }

    protected InitialLdapContext getLdapContext() throws NamingException {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, providerUrl);
        if (authType != null) {
            env.put(Context.SECURITY_AUTHENTICATION, authType);
        }
        if (systemDn != null) {
            env.put(Context.SECURITY_PRINCIPAL, systemDn);
        }
        if (systemPassword != null) {
            env.put(Context.SECURITY_CREDENTIALS, systemPassword);
        }
        if ("true".equalsIgnoreCase(usePool)) {
            env.put("com.sun.jndi.ldap.connect.pool", "true");
            if (initPoolSize != null) {
                env.put("com.sun.jndi.ldap.connect.pool.initsize", initPoolSize);
            }
            if (maxPoolSize != null) {
                env.put("com.sun.jndi.ldap.connect.pool.maxsize", maxPoolSize);
            }
            if (prefPoolSize != null) {
                env.put("com.sun.jndi.ldap.connect.pool.prefsize", prefPoolSize);
            }
            if (poolTimeout != null) {
                env.put("com.sun.jndi.ldap.connect.pool.timeout", poolTimeout);
            }
        }
        return new InitialLdapContext(env, null);
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
            Hashtable<String, String> env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, providerUrl);
            if (authType != null) {
                env.put(Context.SECURITY_AUTHENTICATION, authType);
            }
            env.put(Context.SECURITY_PRINCIPAL, userDn);
            env.put(Context.SECURITY_CREDENTIALS, password);
            env.put("com.sun.jndi.ldap.connect.pool", "false");
            InitialLdapContext authContext = null;
            try {
                authContext = new InitialLdapContext(env, null);
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
            context = getLdapContext();
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
                    context = getLdapContext();
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
        //check removal user is not last workspace/admin of any workspace
        final List<Member> wsRelationships = memberDao.getUserRelationships(id);
        for (Member member : wsRelationships) {
            if (isLastWorkspaceAdmin(member)) {
                throw new ConflictException(format("User %s is last 'workspace/admin' in workspace %s", id, member.getWorkspaceId()));
            }
        }
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
        for (Member member : wsRelationships) {
            memberDao.remove(member);
        }
        //remove user relationships with accounts
        for (com.codenvy.api.account.server.dao.Member member : accountDao.getByMember(id)) {
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
            context = getLdapContext();
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

    private boolean isLastWorkspaceAdmin(Member removal) throws NotFoundException, ServerException {
        if (!removal.getRoles().contains("workspace/admin")) {
            return false;
        }
        for (Member member : memberDao.getWorkspaceMembers(removal.getWorkspaceId())) {
            if (!member.getUserId().equals(removal.getUserId()) && member.getRoles().contains("workspace/admin")) {
                return false;
            }
        }
        return true;
    }

    private boolean isOnlyOneOwner(String accountId) throws ServerException {
        int owners = 0;
        for (com.codenvy.api.account.server.dao.Member member : accountDao.getMembers(accountId)) {
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
            context = getLdapContext();
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
            context = getLdapContext();
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
