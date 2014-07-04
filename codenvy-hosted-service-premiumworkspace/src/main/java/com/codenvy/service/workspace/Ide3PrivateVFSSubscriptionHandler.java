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

import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.rest.HttpJsonHelper;
import com.codenvy.api.vfs.server.VirtualFileSystem;
import com.codenvy.api.vfs.server.VirtualFileSystemFactory;
import com.codenvy.api.vfs.shared.dto.AccessControlEntry;
import com.codenvy.api.vfs.shared.dto.Principal;
import com.codenvy.api.vfs.shared.dto.VirtualFileSystemInfo;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.codenvy.commons.lang.Pair;
import com.codenvy.dto.server.DtoFactory;
import com.codenvy.dto.server.JsonArrayImpl;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Sergii Leschenko
 * @author Sergii Kabashniuk
 */
public class Ide3PrivateVFSSubscriptionHandler extends PrivateVFSSubscriptionHandler {
    private final String apiEndpoint;

    @Inject
    public Ide3PrivateVFSSubscriptionHandler(WorkspaceDao workspaceDao,
                                             @Named("api.endpoint") String apiEndpoint) {
        super(workspaceDao);
        this.apiEndpoint = apiEndpoint;
    }

    /**
     * Set permission in virtual file system only for users with role "workspace/developer".
     * This users will be have all permissions(read, write and update acl).
     *
     * @param workspaceId
     *         id of workspace for updating access control list.
     * @param authToken
     *         token that will be used for send request to services
     * @throws IOException
     *         if any error sending request to service
     */
    @Override
    protected void setWorkspacePermission(String workspaceId, String authToken) throws IOException {
        // Getting id of vfs root for current workspace
        UriBuilder ub = UriBuilder.fromUri(apiEndpoint)
                                  .path(VirtualFileSystemFactory.class)
                                  .path(VirtualFileSystemFactory.class, "getFileSystem");

        VirtualFileSystemInfo virtualFileSystemInfo;
        try {
            virtualFileSystemInfo = HttpJsonHelper
                    .get(VirtualFileSystemInfo.class, ub.build(workspaceId).toString(), new Pair<>("token", authToken));
        } catch (ApiException e) {
            throw new IOException("Can not get virtual file system info", e);
        }

        // Updating access control list
        ub = UriBuilder.fromUri(apiEndpoint)
                       .path(VirtualFileSystemFactory.class)
                       .path(VirtualFileSystemFactory.class, "getFileSystem")
                       .path(VirtualFileSystem.class, "updateACL")
                       .path(virtualFileSystemInfo.getRoot().getId());
        Principal principal = DtoFactory.getInstance().createDto(Principal.class)
                                        .withType(Principal.Type.GROUP)
                                        .withName("workspace/developer");
        List<String> permissions = new ArrayList<>(Arrays.asList(VirtualFileSystemInfo.BasicPermissions.ALL.toString()));
        AccessControlEntry accessControlEntry = DtoFactory.getInstance().createDto(AccessControlEntry.class)
                                                          .withPermissions(permissions)
                                                          .withPrincipal(principal);

        List<AccessControlEntry> accessControlList = new ArrayList<>(Arrays.asList(accessControlEntry));
        try {
            // DTO interface set to null because we not want get json response
            HttpJsonHelper.post(null, ub.build(workspaceId).toString(), new JsonArrayImpl<>(accessControlList),
                                new Pair<>("token", authToken), new Pair<>("override", true));
        } catch (ApiException e) {
            throw new IOException("Can not update access control list", e);
        }
    }
}
