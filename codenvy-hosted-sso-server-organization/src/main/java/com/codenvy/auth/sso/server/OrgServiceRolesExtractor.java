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

import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.account.server.dao.Account;
import org.eclipse.che.api.account.server.dao.Member;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;

import com.codenvy.api.dao.authentication.AccessTicket;
import com.codenvy.api.dao.ldap.InitialLdapContextFactory;

import org.eclipse.che.api.user.server.dao.PreferenceDao;
import org.eclipse.che.api.user.server.dao.User;
import org.eclipse.che.api.user.server.dao.UserDao;
import org.eclipse.che.api.workspace.server.dao.MemberDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.ldap.InitialLdapContext;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;
import static java.util.Collections.emptySet;

/**
 * Get user principal with roles from account service.
 *
 * @author Alexander Garagatyi
 */
public class OrgServiceRolesExtractor implements RolesExtractor {
    private static final Logger LOG = LoggerFactory.getLogger(OrgServiceRolesExtractor.class);

    private final UserDao                   userDao;
    private final AccountDao                accountDao;
    private final MemberDao                 memberDao;
    private final PreferenceDao             preferenceDao;
    private final InitialLdapContextFactory contextFactory;
    private final String                    userContainerDns[];
    private final String                    userDn;
    private final String                    roleAttrName;
    private final String                    allowedRole;

    @Inject
    public OrgServiceRolesExtractor(UserDao userDao,
                                    AccountDao accountDao,
                                    MemberDao memberDao,
                                    PreferenceDao preferenceDao,
                                    @Named("user.ldap.user_container_dn") String[] userContainerDns,
                                    @Named("user.ldap.user_dn") String userDn,
                                    @Nullable @Named("user.ldap.attr.role_name") String roleAttrName,
                                    @Nullable @Named("user.ldap.allowed_role") String allowedRole,
                                    InitialLdapContextFactory contextFactory) {
        this.userDao = userDao;
        this.accountDao = accountDao;
        this.memberDao = memberDao;
        this.preferenceDao = preferenceDao;
        this.roleAttrName = roleAttrName;
        this.allowedRole = allowedRole;
        this.userContainerDns = userContainerDns;
        this.userDn = userDn;
        this.contextFactory = contextFactory;
    }

    @Override
    public Set<String> extractRoles(AccessTicket ticket, String workspaceId, String accountId) {
        //TODO: find better solution
        //skip roles extraction if ticket handler type equals to 'sysldap'
        if ("sysldap".equals(ticket.getAuthHandlerType())) {
            return emptySet();
        }
        try {

            if (allowedRole != null && !getRoles(ticket.getPrincipal().getId()).contains(allowedRole)) {
                return emptySet();
            }

            final Set<String> userRoles = new HashSet<>();

            final Map<String, String> preferences = preferenceDao.getPreferences(ticket.getPrincipal().getId());
            if (parseBoolean(preferences.get("temporary"))) {
                userRoles.add("temp_user");
            } else {
                userRoles.add("user");
            }

            User user = userDao.getById(ticket.getPrincipal().getId());

            if (accountId != null) {
                Account account = accountDao.getById(accountId);
                if (account != null) {
                    for (Member accountMember : accountDao.getMembers(accountId)) {
                        if (accountMember.getUserId().equals(user.getId()))
                            userRoles.addAll(accountMember.getRoles());
                    }
                }
            }

            for (org.eclipse.che.api.workspace.server.dao.Member workspaceMember : memberDao.getUserRelationships(user.getId())) {
                if (workspaceMember.getWorkspaceId().equals(workspaceId))
                    userRoles.addAll(workspaceMember.getRoles());
            }
            return userRoles;
        } catch (NotFoundException e) {
            return emptySet();
        } catch (ServerException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new RuntimeException(e.getLocalizedMessage());
        }

    }

    //package-private used in test
    Set<String> getRoles(String id) throws ServerException, NotFoundException {
        InitialLdapContext context = null;
        NamingEnumeration rolesEnum = null;
        try {
            context = contextFactory.createContext();

            Attributes userAttrs = null;

            for (String containerDn : userContainerDns) {
                try {
                    userAttrs = context.getAttributes(formatDn(id, containerDn));
                } catch (NameNotFoundException ignored) {
                    //its okay
                }
            }

            if (userAttrs == null) {
                throw new NotFoundException(format("User '%s' was not found", id));
            }

            rolesEnum = userAttrs.get(roleAttrName).getAll();

            final Set<String> roles = new HashSet<>();
            while (rolesEnum.hasMoreElements()) {
                roles.add(rolesEnum.next().toString());
            }

            return roles;
        } catch (NameNotFoundException nfEx) {
            throw new NotFoundException(format("User with id '%s' was not found", id));
        } catch (NamingException e) {
            throw new ServerException(e.getMessage(), e);
        } finally {
            close(context);
            close(rolesEnum);
        }
    }

    private String formatDn(String userId, String containerDn) {
        return userDn + '=' + userId + ',' + containerDn;
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

    private void close(NamingEnumeration<?> enumeration) {
        if (enumeration != null) {
            try {
                enumeration.close();
            } catch (NamingException e) {
                LOG.warn(e.getMessage(), e);
            }
        }
    }
}
