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
package com.codenvy.workspace.activity;

import com.codenvy.service.http.WorkspaceInfoCache;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
/*
import org.eclipse.che.api.runner.RunQueue;
import org.eclipse.che.api.runner.RunQueueTask;
*/
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Notifies about activity in workspace if there exists any active run
 *
 * @author Sergii Leschenko
 */
@Singleton
public class RunActivityChecker {
    /*
    private static final Logger LOG = LoggerFactory.getLogger(RunActivityChecker.class);

    private final RunQueue              runQueue;
    private final WsActivityEventSender wsActivityEventSender;
    private final WorkspaceInfoCache    workspaceInfoCache;

    @Inject
    public RunActivityChecker(RunQueue runQueue,]
                              WsActivityEventSender wsActivityEventSender,
                              WorkspaceInfoCache workspaceInfoCache) {
        this.runQueue = runQueue;
        this.wsActivityEventSender = wsActivityEventSender;
        this.workspaceInfoCache = workspaceInfoCache;
    }

    @ScheduleRate(period = 60)
    public void check() {
        for (RunQueueTask runQueueTask : runQueue.getTasks()) {
            final String workspaceId = runQueueTask.getRequest().getWorkspace();
            try {
                WorkspaceDescriptor workspace = workspaceInfoCache.getById(workspaceId);
                wsActivityEventSender.onActivity(workspaceId, workspace.isTemporary());
            } catch (ServerException | NotFoundException e) {
                LOG.warn("Can't get workspace " + workspaceId + " information from cache for sending run activity");
            }
        }
    }
    */
}
