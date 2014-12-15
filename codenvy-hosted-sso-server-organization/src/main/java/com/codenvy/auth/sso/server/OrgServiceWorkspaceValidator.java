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
package com.codenvy.auth.sso.server;

import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.workspace.server.dao.MemberDao;
import com.codenvy.api.user.server.dao.UserDao;
import com.codenvy.api.workspace.server.dao.Member;
import com.codenvy.api.user.server.dao.User;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.codenvy.auth.organization.WorkspaceCreationValidator;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

/**
 * @author Sergii Kabashniuk
 */
public class OrgServiceWorkspaceValidator implements WorkspaceCreationValidator {

    @Inject
    private UserDao userDao;

    @Inject
    private MemberDao memberDao;

    @Inject
    private WorkspaceDao workspaceDao;

    @Override
    public void ensureUserCreationAllowed(String email, String workspaceName) throws IOException {
        try {
            User user;
            try {
                user = userDao.getByAlias(email);

                List<Member> memberships = memberDao.getUserRelationships(user.getId());
                try {
                    for (Member member : memberships) {
                        if (member.getRoles().contains("workspace/admin") &&
                            !workspaceDao.getById(member.getWorkspaceId()).isTemporary()) {
                            throw new IOException("You are the owner of another persistent workspace.");
                        }
                    }
                } catch (NotFoundException e) {
                    throw new IOException(e.getLocalizedMessage(), e);
                }
            } catch (NotFoundException e) {
                //ok
            }

            try {
                workspaceDao.getByName(workspaceName);

                throw new IOException("This workspace name is reserved, please choose another name.");
            } catch (NotFoundException e) {
                //ok
            }
        } catch (ServerException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }
}
