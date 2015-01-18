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
package com.codenvy.auth.sso.client;

import com.codenvy.commons.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** @author Sergii Kabashniuk */
public class SsoClientPrincipal implements Principal {
    private static final Logger LOG = LoggerFactory.getLogger(SsoClientPrincipal.class);

    private final Map<RolesContext, User> contextUserMap;
    private final String                  clientUrl;
    private final String                  token;
    private final ServerClient            ssoServerClient;
    private final String                  userName;
    private final String                  userId;


    public SsoClientPrincipal(String token,
                              String clientUrl,
                              RolesContext initialContext,
                              User initialPrincipal,
                              ServerClient ssoServerClient) {
        this.clientUrl = clientUrl;
        this.token = token;
        this.ssoServerClient = ssoServerClient;
        this.userName = initialPrincipal.getName();
        this.userId = initialPrincipal.getId();
        this.contextUserMap = new ConcurrentHashMap<>();
        this.contextUserMap.put(initialContext, initialPrincipal);
    }

    public boolean hasUserInContext(RolesContext context) {
        if (context != null) {
            User user = contextUserMap.get(context);
            if (user == null) {
                user = ssoServerClient.getUser(token, clientUrl,
                                               context.getWorkspaceId(),
                                               context.getAccountId());
                if (user != null && userId.equals(user.getId())) {
                    contextUserMap.put(context, user);
                }
            }
            return user != null;
        }
        return true;


    }

    public String getClientUrl() {
        return clientUrl;
    }

    public String getToken() {
        return token;
    }

    public User getUser(RolesContext rolesContext) {
        return contextUserMap.get(rolesContext);
    }

    /**
     * Remove roles for given context.
     *
     * @param context
     */
    void invialidateRoles(RolesContext context) {
        contextUserMap.remove(context);
    }

    public boolean isUserInRole(String roleName, RolesContext context) {

        User contextRoles = contextUserMap.get(context);
        if (contextRoles != null) {
            return contextRoles.isMemberOf(roleName);
        }
        return false;
    }

    @Override
    public String getName() {
        return userName;
    }
}
