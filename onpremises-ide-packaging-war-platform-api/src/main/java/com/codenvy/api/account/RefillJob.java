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

import com.codenvy.api.metrics.server.limit.WorkspaceLockDao;
import com.codenvy.api.metrics.server.limit.WorkspaceLocker;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.workspace.server.dao.Workspace;
import org.eclipse.che.commons.schedule.ScheduleCron;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Refill workspace with RAM limit exceeded at the beginning of new period.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class RefillJob implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(RefillJob.class);

    private final WorkspaceLocker  workspaceLocker;
    private final WorkspaceLockDao workspaceLockDao;

    @Inject
    public RefillJob(WorkspaceLocker workspaceLocker,
                     WorkspaceLockDao workspaceLockDao) {
        this.workspaceLocker = workspaceLocker;
        this.workspaceLockDao = workspaceLockDao;
    }


    @ScheduleCron(cronParameterName = "resources.refill.cron")
    @Override
    public void run() {
        try {
            for (Workspace workspace : workspaceLockDao.getWorkspacesWithLockedResources()) {
                workspaceLocker.removeResourcesLock(workspace.getId());
            }
        } catch (ServerException e) {
            LOG.error("Error on removing lock properties.", e);
        }
    }
}
