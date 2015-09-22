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

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.UsersWorkspace;
import org.eclipse.che.api.user.server.dao.MembershipDao;
import org.eclipse.che.api.user.server.dao.UserDao;
import org.eclipse.che.api.user.server.dao.User;
import org.eclipse.che.api.workspace.server.WorkspaceManager;

import com.codenvy.auth.sso.server.organization.WorkspaceCreationValidator;


import javax.inject.Inject;
import java.io.IOException;

/**
 * @author Sergii Kabashniuk
 */
public class OrgServiceWorkspaceValidator implements WorkspaceCreationValidator {

    @Inject
    private UserDao userDao;

    @Inject
    private WorkspaceManager workspaceManager;

    @Override
    public void ensureUserCreationAllowed(String email, String workspaceName) throws IOException {
        try {
            User user = null;
            try {
                user = userDao.getByAlias(email);
                try {
                    for (UsersWorkspace ws : workspaceManager.getWorkspaces(user.getId())) {
                        if (!ws.isTemporary()) {
                                throw new IOException("You are the owner of another persistent workspace.");
                        }
                    }
                } catch (BadRequestException e) {
                    throw new IOException(e.getLocalizedMessage(), e); //TODO: refactor interface exception
                }
            } catch (NotFoundException e) {
                //ok
            }

            if (user == null) {
                return;
            }
            try {
                workspaceManager.getWorkspace(workspaceName, user.getId());
                throw new IOException("This workspace name is reserved, please choose another name.");
            } catch (NotFoundException e) {
                //ok
            } catch (BadRequestException e) {
                throw new IOException(e.getLocalizedMessage(), e); //TODO: refactor interface exception
            }
        } catch (ServerException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }
}
