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
package com.codenvy.service.workspace;

import com.codenvy.api.account.server.SubscriptionEvent;
import com.codenvy.api.account.server.SubscriptionHandler;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.codenvy.api.workspace.shared.dto.Workspace;
import com.codenvy.commons.env.EnvironmentContext;
import com.codenvy.commons.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

/**
 * Change VFS permissions for premium subscriptions.
 * It makes workspaces private after successfully created subscription.
 *
 * @author Sergii Kabashniuk
 */
public abstract class PrivateVFSSubscriptionHandler implements SubscriptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(PrivateVFSSubscriptionHandler.class);

    private final WorkspaceDao workspaceDao;


    @Inject
    public PrivateVFSSubscriptionHandler(WorkspaceDao workspaceDao) {
        this.workspaceDao = workspaceDao;
    }

    //TODO add codenvy_workspace_multiple_till=<date> attribute
    @Override
    public void onCreateSubscription(SubscriptionEvent subscription) {
        setAccountPermissions(subscription.getSubscription().getAccountId());

    }

    @Override
    public void onRemoveSubscription(SubscriptionEvent subscription) {
    }

    @Override
    public void onCheckSubscription(SubscriptionEvent subscription) {
        setAccountPermissions(subscription.getSubscription().getAccountId());
    }

    private void setAccountPermissions(String accountId) {
        String authToken;
        User user = EnvironmentContext.getCurrent().getUser();
        if (user != null && user.getToken() != null) {
            authToken = user.getToken();
            try {
                List<Workspace> workspaces = workspaceDao.getByAccount(accountId);
                for (Workspace workspace : workspaces) {
                    setWorkspacePermission(workspace.getId(), authToken);
                    LOG.error("Not implemented. Set private permissions in workspace {} for user with token {}");
                }
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }

    protected abstract void setWorkspacePermission(String workspaceId, String authToken) throws IOException;
}
