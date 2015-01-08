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
package com.codenvy.api.account;

import com.codenvy.api.account.server.ResourcesManager;
import com.codenvy.api.account.shared.dto.UpdateResourcesDescriptor;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.runner.dto.ResourcesDescriptor;
import com.codenvy.api.runner.internal.Constants;
import com.codenvy.api.workspace.server.dao.Workspace;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.codenvy.dto.server.DtoFactory;

import org.everrest.websockets.WSConnectionContext;
import org.everrest.websockets.message.ChannelBroadcastMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/**
 * Implementation of {@link com.codenvy.api.account.server.ResourcesManager}
 *
 * @author Sergii Leschenko
 */
public class ResourcesManagerImpl implements ResourcesManager {
    private static final Logger LOG = LoggerFactory.getLogger(ResourcesManagerImpl.class);

    private final WorkspaceDao workspaceDao;

    @Inject
    public ResourcesManagerImpl(WorkspaceDao workspaceDao) {
        this.workspaceDao = workspaceDao;
    }

    @Override
    public void redistributeResources(String accountId, List<UpdateResourcesDescriptor> updates) throws NotFoundException,
                                                                                                        ServerException,
                                                                                                        ConflictException,
                                                                                                        ForbiddenException {
        final Map<String, Workspace> ownWorkspaces = new HashMap<>();
        for (Workspace workspace : workspaceDao.getByAccount(accountId)) {
            ownWorkspaces.put(workspace.getId(), workspace);
        }

        Map<String, UpdateResourcesDescriptor> resources = new HashMap<>();
        for (UpdateResourcesDescriptor resDescriptor : updates) {
            resources.put(resDescriptor.getWorkspaceId(), resDescriptor);
        }

        for (String wsId : resources.keySet()) {
            if (!ownWorkspaces.containsKey(wsId)) {
                throw new ForbiddenException(format("Workspace %s is not related to account %s", wsId, accountId));
            }
        }

        if (ownWorkspaces.size() != resources.size()) {
            for (String workspaceId : ownWorkspaces.keySet()) {
                if (!resources.containsKey(workspaceId)) {
                    throw new ConflictException(format("Missed description of resources for workspace %s", workspaceId));
                }
            }
        }

        for (UpdateResourcesDescriptor resourcesDescriptor : resources.values()) {
            if (resourcesDescriptor.getResources() == null) {
                throw new ConflictException(format("Missed description of resources for workspace %s",
                                                   resourcesDescriptor.getWorkspaceId()));
            }

            if (!resourcesDescriptor.getResources().containsKey("RAM")) {
                throw new ConflictException(
                        format("Missed size of RAM in resources description for workspace %s", resourcesDescriptor.getWorkspaceId()));
            }
            try {
                int sizeOfRAM = Integer.parseInt(resourcesDescriptor.getResources().get("RAM"));
                if (sizeOfRAM < 0) {
                    throw new ConflictException(format("Size of RAM for workspace %s is a negative number",
                                                       resourcesDescriptor.getWorkspaceId()));
                }
            } catch (NumberFormatException nfe) {
                throw new ConflictException(format("Invalid size of RAM for workspace %s", resourcesDescriptor.getWorkspaceId()));
            }
        }

        //redistributing resources
        for (UpdateResourcesDescriptor resDescriptor : updates) {
            Workspace workspace = ownWorkspaces.get(resDescriptor.getWorkspaceId());
            workspace.getAttributes().put(Constants.RUNNER_MAX_MEMORY_SIZE, resDescriptor.getResources().get("RAM"));
            workspaceDao.update(workspace);
            publishResourcesChangedWsEvent(resDescriptor.getWorkspaceId(), resDescriptor.getResources().get("RAM"));
        }
    }

    private void publishResourcesChangedWsEvent(String workspaceId, String totalMemory) {
        try {
            final ChannelBroadcastMessage bm = new ChannelBroadcastMessage();

            bm.setChannel(format("workspace:resources:%s", workspaceId));

            final ResourcesDescriptor resourcesDescriptor = DtoFactory.getInstance().createDto(ResourcesDescriptor.class)
                                                                      .withTotalMemory(totalMemory);
            bm.setBody(DtoFactory.getInstance().toJson(resourcesDescriptor));
            WSConnectionContext.sendMessage(bm);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
