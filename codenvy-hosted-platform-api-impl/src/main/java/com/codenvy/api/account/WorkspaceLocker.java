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

import org.eclipse.che.api.account.server.Constants;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.workspace.server.dao.Workspace;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static java.lang.String.format;

/**
 * Locks and unlocks resources usage in workspace
 *
 * @author Sergii Leschenko
 */
public class WorkspaceLocker {
    private static final Logger LOG = LoggerFactory.getLogger(WorkspaceLocker.class);
    private final WorkspaceDao workspaceDao;

    @Inject
    public WorkspaceLocker(WorkspaceDao workspaceDao) {
        this.workspaceDao = workspaceDao;
    }

    public void lockResources(String workspaceId) {
        try {
            Workspace workspace = workspaceDao.getById(workspaceId);
            workspace.getAttributes().put(Constants.RESOURCES_LOCKED_PROPERTY, "true");
            try {
                workspaceDao.update(workspace);
            } catch (NotFoundException | ServerException | ConflictException e) {
                LOG.error(format("Error writing lock property into workspace %s .", workspace.getId()), e);
            }
        } catch (NotFoundException | ServerException e) {
            LOG.error(format("Can't get workspace %s for writing resources lock property", workspaceId), e);
        }
    }

    public void unlockResources(String workspaceId) {
        try {
            Workspace workspace = workspaceDao.getById(workspaceId);
            if (workspace.getAttributes().containsKey(Constants.RESOURCES_LOCKED_PROPERTY)) {
                workspace.getAttributes().remove(Constants.RESOURCES_LOCKED_PROPERTY);
                try {
                    workspaceDao.update(workspace);
                } catch (NotFoundException | ServerException | ConflictException e) {
                    LOG.error(format("Error writing lock property into workspace %s .", workspace.getId()), e);
                }
            }
        } catch (NotFoundException | ServerException e) {
            LOG.error(format("Can't get workspace %s for writing resources lock property", workspaceId), e);
        }
    }
}
