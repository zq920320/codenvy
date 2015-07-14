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
package com.codenvy.machine;

import org.eclipse.che.api.core.ServerException;

/**
 * Synchronizes project source between VFS and machine using Syncthing.
 * Uses eventbus listener to detect changes on VFS
 *
 * @author Alexander Garagatyi
 */
public class VfsSyncthingSynchronizerTask extends SyncthingSynchronizeTask {
    private final SyncthingSynchronizeEventListener vfsSynchronizeListener;
    private final SyncthingSynchronizeNotifier      syncNotifier;

    public VfsSyncthingSynchronizerTask(String syncTaskExecutable,
                                        String syncTaskConfTemplate,
                                        String workingDir,
                                        String syncPath,
                                        int listenPort,
                                        int apiPort,
                                        String remoteClientAddress,
                                        String apiToken,
                                        String workspaceId,
                                        String projectPath,
                                        SyncthingSynchronizeEventListener vfsSynchronizeListener) throws ServerException {
        super(syncTaskExecutable, syncTaskConfTemplate, workingDir, syncPath, listenPort, apiPort, remoteClientAddress, apiToken);

        this.vfsSynchronizeListener = vfsSynchronizeListener;
        this.syncNotifier = new SyncthingSynchronizeNotifier(workspaceId, projectPath, apiPort, apiToken);
    }

    @Override
    public void run() {
        vfsSynchronizeListener.addProjectSynchronizeNotifier(syncNotifier);
        try {
            super.run();
        } finally {
            vfsSynchronizeListener.removeProjectSynchronizeNotifier(syncNotifier);
        }
    }
}
