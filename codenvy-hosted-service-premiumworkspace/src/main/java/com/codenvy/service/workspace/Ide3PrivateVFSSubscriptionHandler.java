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

import com.codenvy.api.workspace.server.dao.WorkspaceDao;

import javax.inject.Named;
import java.io.IOException;

/**
 * @author Sergii Kabashniuk
 */
public class Ide3PrivateVFSSubscriptionHandler extends PrivateVFSSubscriptionHandler {
    private final String apiEndpoint;

    public Ide3PrivateVFSSubscriptionHandler(WorkspaceDao workspaceDao,
                                             @Named("api.endpoint") String apiEndpoint) {
        super(workspaceDao);
        this.apiEndpoint = apiEndpoint;
    }

    @Override
    protected void setWorkspacePermission(String workspaceId, String authToken) throws IOException {

    }
//    public void setWorkspacePermission(String workspaceId, String authToken) throws IOException {
//        HttpURLConnection connection = null;
//
//        try {
//            UriBuilder ub = UriBuilder.fromUri(apiEndpoint + "/vfs/" + workspaceId + "/v2")
//                                      .path(VirtualFileSystem.class, "updateACL")
//                                      .path(getWorkspaceRootFolderId(workspaceId));
//
//            ub.queryParam("token", authToken);
//
//            URL url = ub.build().toURL();
//            connection = (HttpURLConnection)url.openConnection();
//            connection.setRequestProperty("Referer", apiEndpoint);
//            connection.setAllowUserInteraction(false);
//            connection.setRequestMethod("POST");
//
//            int code = connection.getResponseCode();
//
////                        Map<Principal, Set<VirtualFileSystemInfo.BasicPermissions>> acl = new HashMap<>();
////                        acl.put(new DtoServerImpls.PrincipalImpl("workspace/developer", Principal.Type.GROUP), EnumSet.of(
////                                VirtualFileSystemInfo.BasicPermissions.ALL));
////                        return new AccessControlList(acl);
//
////                        vfs.importZip(folder.getId(), inputStream, true);
////                        Project project = (Project)vfs.getItem(folder.getId(), false, PropertyFilter.ALL_FILTER);
////                        LOG.info("EVENT#factory-project-imported# WS#" + tmpWorkspace + "# USER#" +
////                                 ConversationState.getCurrent().getIdentity().getUserId() + "# PROJECT#" + project.getName() + "#
// TYPE#" +
////                                 project.getProjectType() + "#");
////                        importedProjects.add(project);
//        } finally {
//            if (connection != null) {
//                connection.disconnect();
//            }
//        }
//    }
//
//    private String getWorkspaceRootFolderId(String workspaceId) {
//        //vfs.get VirtualFileSystemInfo.getRoot().getId();
//        return null;
//    }
}
