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
package com.codenvy.api.account;

import com.codenvy.api.account.server.ResourcesChangesNotifier;

import org.eclipse.che.api.account.server.ResourcesManager;
import org.eclipse.che.api.account.shared.dto.UpdateResourcesDescriptor;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.runner.internal.Constants;
import org.eclipse.che.api.workspace.server.dao.Workspace;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/**
 * Implementation of ResourcesManager for OnPremises packaging
 *
 * @author Sergii Leschenko
 */
public class ResourcesManagerImpl implements ResourcesManager {
    private final WorkspaceDao             workspaceDao;
    private final ResourcesChangesNotifier resourcesChangesNotifier;

    @Inject
    public ResourcesManagerImpl(WorkspaceDao workspaceDao,
                                ResourcesChangesNotifier resourcesChangesNotifier) {
        this.workspaceDao = workspaceDao;
        this.resourcesChangesNotifier = resourcesChangesNotifier;
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
        validateUpdates(accountId, updates, ownWorkspaces);

        for (UpdateResourcesDescriptor resourcesDescriptor : updates) {
            Workspace workspace = ownWorkspaces.get(resourcesDescriptor.getWorkspaceId());

            if (resourcesDescriptor.getRunnerRam() != null) {
                workspace.getAttributes().put(Constants.RUNNER_MAX_MEMORY_SIZE, Integer.toString(resourcesDescriptor.getRunnerRam()));
            }

            if (resourcesDescriptor.getBuilderTimeout() != null) {
                workspace.getAttributes().put(org.eclipse.che.api.builder.internal.Constants.BUILDER_EXECUTION_TIME,
                                              Integer.toString(resourcesDescriptor.getBuilderTimeout()));
            }

            if (resourcesDescriptor.getRunnerTimeout() != null) {
                workspace.getAttributes().put(Constants.RUNNER_LIFETIME, Integer.toString(resourcesDescriptor.getRunnerTimeout()));
            }

            workspaceDao.update(workspace);

            if (resourcesDescriptor.getRunnerRam() != null) {
                resourcesChangesNotifier.publishTotalMemoryChangedEvent(resourcesDescriptor.getWorkspaceId(),
                                                                        Integer.toString(resourcesDescriptor.getRunnerRam()));
            }
        }
    }

    private void validateUpdates(String accountId, List<UpdateResourcesDescriptor> updates, Map<String, Workspace> ownWorkspaces)
            throws ForbiddenException, ConflictException, NotFoundException, ServerException {

        for (UpdateResourcesDescriptor resourcesDescriptor : updates) {
            if (!ownWorkspaces.containsKey(resourcesDescriptor.getWorkspaceId())) {
                throw new ForbiddenException(
                        format("Workspace %s is not related to account %s", resourcesDescriptor.getWorkspaceId(), accountId));
            }

            if (resourcesDescriptor.getRunnerTimeout() == null && resourcesDescriptor.getRunnerRam() == null
                && resourcesDescriptor.getBuilderTimeout() == null && resourcesDescriptor.getResourcesUsageLimit() == null) {
                throw new ConflictException(
                        format("Missed description of resources for workspace %s", resourcesDescriptor.getWorkspaceId()));
            }

            Integer runnerRam = resourcesDescriptor.getRunnerRam();
            if (runnerRam != null) {
                if (runnerRam < 0) {
                    throw new ConflictException(format("Size of RAM for workspace %s is a negative number",
                                                       resourcesDescriptor.getWorkspaceId()));
                }
            }

            if (resourcesDescriptor.getBuilderTimeout() != null && resourcesDescriptor.getBuilderTimeout() < 0) {
                throw new ConflictException(format("Builder timeout for workspace %s is a negative number",
                                                   resourcesDescriptor.getWorkspaceId()));
            }

            if (resourcesDescriptor.getRunnerTimeout() != null && resourcesDescriptor.getRunnerTimeout() < -1) {// we allow -1 here
                throw new ConflictException(format("Runner timeout for workspace %s is a negative number",
                                                   resourcesDescriptor.getWorkspaceId()));
            }

            if (resourcesDescriptor.getResourcesUsageLimit() != null && resourcesDescriptor.getResourcesUsageLimit() < -1) {
                throw new ConflictException(format("Resources usage limit for workspace %s is a negative number",
                                                   resourcesDescriptor.getWorkspaceId()));
            }
        }
    }
}
