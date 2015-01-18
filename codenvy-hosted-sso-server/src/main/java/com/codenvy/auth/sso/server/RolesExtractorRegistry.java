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

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

/**
 * A holder of set RolesExtractor. Used to aggregate roles from all roles extractors
 *
 * @author Sergii Kabashniuk
 */
public class RolesExtractorRegistry {

    private final Set<RolesExtractor> rolesExtractors;

    @Inject
    public RolesExtractorRegistry(Set<RolesExtractor> rolesExtractors) {
        this.rolesExtractors = rolesExtractors;
    }

    /**
     * Aggregate roles from all roles extractors for the given user.
     *
     * @param ticket
     *         - represent user and all his identification information like id for which we have to find all his roles.
     * @param workspaceId
     *         - if user have some special roles against workspace with given id - get it.
     * @param accountId
     *         - if user have some special roles against account with given id - get it.
     * @return combined set of roles.
     */
    public Set<String> getRoles(AccessTicket ticket, String workspaceId, String accountId) {
        Set<String> userRoles = new HashSet<>();
        for (RolesExtractor rolesExtractor : rolesExtractors) {
            userRoles.addAll(rolesExtractor.extractRoles(ticket, workspaceId, accountId));
        }
        return userRoles;
    }
}
