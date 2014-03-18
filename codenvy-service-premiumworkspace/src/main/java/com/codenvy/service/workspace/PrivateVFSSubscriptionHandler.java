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

import com.codenvy.api.organization.server.SubscriptionEvent;
import com.codenvy.api.organization.server.SubscriptionHandler;
import com.codenvy.api.vfs.server.VirtualFileSystem;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.codenvy.api.workspace.shared.dto.Workspace;
import com.codenvy.commons.env.EnvironmentContext;
import com.codenvy.commons.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Change VFS permissions for premium subscriptions.
 * It makes workspaces private after successfully created subscription.
 *
 * @author Sergii Kabashniuk
 */
public class PrivateVFSSubscriptionHandler implements SubscriptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(PrivateVFSSubscriptionHandler.class);

    private final WorkspaceDao workspaceDao;
    private final String       apiEndpoint;


    @Inject
    public PrivateVFSSubscriptionHandler(WorkspaceDao workspaceDao, @Named("api.endpoint") String apiEndpoint) {
        this.workspaceDao = workspaceDao;
        this.apiEndpoint = apiEndpoint;
    }


    @Override
    public void onCreateSubscription(SubscriptionEvent subscription) {
        setOrganizationPermissions(subscription.getOrganizationId());

    }

    @Override
    public void onRemoveSubscription(SubscriptionEvent subscription) {

    }

    @Override
    public void onCheckSubscription(SubscriptionEvent subscription) {
        setOrganizationPermissions(subscription.getOrganizationId());
    }

    private void setOrganizationPermissions(String organizationId) {
        String authToken = null;
        User user = EnvironmentContext.getCurrent().getUser();
        if (user != null && user.getToken() != null) {
            authToken = user.getToken();


            try {
                List<Workspace> workspaces = workspaceDao.getByOrganization(organizationId);
                for (Workspace workspace : workspaces) {
                    //setWorkspacePermission(workspace.getId(), authToken);
                    LOG.error("Not implemented. Set private permissions in workspace {} for user with token {}");

                }
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }


    public void setWorkspacePermission(String workspaceId, String authToken) throws IOException {
        HttpURLConnection connection = null;

        try {
            UriBuilder ub = UriBuilder.fromUri(apiEndpoint + "/vfs/" + workspaceId + "/v2")
                                      .path(VirtualFileSystem.class, "updateACL")
                                      .path(getWorkspaceRootFolderId(workspaceId));

            ub.queryParam("token", authToken);

            URL url = ub.build().toURL();
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestProperty("Referer", apiEndpoint);
            connection.setAllowUserInteraction(false);
            connection.setRequestMethod("POST");

            int code = connection.getResponseCode();

//                        Map<Principal, Set<VirtualFileSystemInfo.BasicPermissions>> acl = new HashMap<>();
//                        acl.put(new DtoServerImpls.PrincipalImpl("workspace/developer", Principal.Type.GROUP), EnumSet.of(
//                                VirtualFileSystemInfo.BasicPermissions.ALL));
//                        return new AccessControlList(acl);

//                        vfs.importZip(folder.getId(), inputStream, true);
//                        Project project = (Project)vfs.getItem(folder.getId(), false, PropertyFilter.ALL_FILTER);
//                        LOG.info("EVENT#factory-project-imported# WS#" + tmpWorkspace + "# USER#" +
//                                 ConversationState.getCurrent().getIdentity().getUserId() + "# PROJECT#" + project.getName() + "# TYPE#" +
//                                 project.getProjectType() + "#");
//                        importedProjects.add(project);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String getWorkspaceRootFolderId(String workspaceId) {
        //vfs.get VirtualFileSystemInfo.getRoot().getId();
        return null;
    }
}
