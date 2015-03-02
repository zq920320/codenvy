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

import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.server.dao.Account;
import com.codenvy.api.account.server.dao.Member;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.dao.authentication.AccessTicket;
import com.codenvy.api.user.server.dao.PreferenceDao;
import com.codenvy.api.user.server.dao.User;
import com.codenvy.api.user.server.dao.UserDao;
import com.codenvy.api.workspace.server.dao.MemberDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.lang.Boolean.parseBoolean;

/**
 * Get user principal with roles from account service.
 *
 * @author Alexander Garagatyi
 */
public class OrgServiceRolesExtractor implements RolesExtractor {
    private static final Logger LOG = LoggerFactory.getLogger(OrgServiceRolesExtractor.class);

    private final UserDao        userDao;
    private final AccountDao     accountDao;
    private final MemberDao      memberDao;
    private final PreferenceDao  preferenceDao;

    @Inject
    public OrgServiceRolesExtractor(UserDao userDao,
                                    AccountDao accountDao,
                                    MemberDao memberDao,
                                    PreferenceDao preferenceDao) {
        this.userDao = userDao;
        this.accountDao = accountDao;
        this.memberDao = memberDao;
        this.preferenceDao = preferenceDao;
    }

    @Override
    public Set<String> extractRoles(AccessTicket ticket, String workspaceId, String accountId) {

        try {
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

            for (com.codenvy.api.workspace.server.dao.Member workspaceMember : memberDao.getUserRelationships(user.getId())) {
                if (workspaceMember.getWorkspaceId().equals(workspaceId))
                    userRoles.addAll(workspaceMember.getRoles());
            }
            return userRoles;
        } catch (NotFoundException e) {
            return Collections.emptySet();
        } catch (ServerException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new RuntimeException(e.getLocalizedMessage());
        }

    }

}
