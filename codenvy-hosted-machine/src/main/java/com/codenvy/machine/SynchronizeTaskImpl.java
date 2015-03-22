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

import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.util.CustomPortService;
import org.eclipse.che.api.machine.server.SynchronizeTask;
import org.eclipse.che.api.machine.shared.ProjectBinding;
import org.eclipse.che.commons.lang.NamedThreadFactory;
import org.eclipse.che.vfs.impl.fs.LocalFSMountStrategy;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Alexander Garagatyi
 */
public class SynchronizeTaskImpl implements SynchronizeTask {
    private final CustomPortService        portService;
    private final SynchronizeEventListener synchronizeEventListener;
    private final ExecutorService          executorService;

    private final int            apiListenPort;
    private final int            apiGuiPort;
    private final int            runnerListenPort;
    private final int            runnerGuiPort;

    private final SyncthingSynchronizeTask     apiSyncTask;
    private final SyncthingSynchronizeTask     runnerSyncTask;
    private final SyncthingSynchronizeNotifier apiSynchronizeNotifier;

    @Inject
    public SynchronizeTaskImpl(@Named("machine.sync.vfs.exec") String apiSyncTaskExecutable,
                               @Named("machine.sync.runner.exec") String runnerSyncTaskExecutable,
                               @Named("machine.sync.vfs.conf") String apiSyncTaskConfTemplate,
                               @Named("machine.sync.runner.conf") String runnerSyncTaskConfTemplate,
                               @Named("machine.sync.vfs.port.min") int apiMinSyncPort,
                               @Named("machine.sync.vfs.port.max") int apiMaxSyncPort,
                               @Named("machine.sync.runner.port.min") int runnerMinSyncPort,
                               @Named("machine.sync.runner.port.max") int runnerMaxSyncPort,
                               @Named("machine.sync.vfs.gui_token") String apiGuiToken,
                               @Named("machine.sync.runner.gui_token") String runnerGuiToken,
                               CustomPortService portService,
                               LocalFSMountStrategy mountStrategy,
                               SynchronizeEventListener synchronizeEventListener,
                               @Assisted("workspaceId") String workspaceId,
                               @Assisted ProjectBinding projectBinding,
                               @Assisted("machineProjectFolder") String machineProjectFolder) throws ServerException {

        this.portService = portService;
        this.synchronizeEventListener = synchronizeEventListener;

        try {
            if ((this.runnerListenPort = portService.acquire(runnerMinSyncPort, runnerMaxSyncPort)) == -1) {
                throw new ServerException("Machine synchronization failed");
            }
            if ((this.runnerGuiPort = portService.acquire(runnerMinSyncPort, runnerMaxSyncPort)) == -1) {
                throw new ServerException("Machine synchronization failed");
            }

            if ((this.apiListenPort = portService.acquire(apiMinSyncPort, apiMaxSyncPort)) == -1) {
                throw new ServerException("Machine synchronization failed");
            }

            String runnerSyncPath = new File(machineProjectFolder, projectBinding.getPath()).toString();

            runnerSyncTask = new SyncthingSynchronizeTask(runnerSyncTaskExecutable,
                                                          runnerSyncTaskConfTemplate,
                                                          runnerSyncPath,
                                                          runnerListenPort,
                                                          runnerGuiPort,
                                                          "127.0.0.1:" + apiListenPort,
                                                          runnerGuiToken);

            final File mountPath = mountStrategy.getMountPath(workspaceId);
            String apiSyncPath = new File(mountPath, projectBinding.getPath()).toString();

            if ((this.apiGuiPort = portService.acquire(apiMinSyncPort, apiMaxSyncPort)) == -1) {
                throw new ServerException("Machine synchronization failed");
            }

            apiSyncTask = new SyncthingSynchronizeTask(apiSyncTaskExecutable,
                                                       apiSyncTaskConfTemplate,
                                                       apiSyncPath,
                                                       apiListenPort,
                                                       apiGuiPort,
                                                       "127.0.0.1:" + runnerListenPort,
                                                       apiGuiToken);

            this.executorService = Executors.newSingleThreadExecutor(new NamedThreadFactory("MachineSynchronization-", true));

            apiSynchronizeNotifier = new SyncthingSynchronizeNotifier(workspaceId,
                                                                      projectBinding.getPath(),
                                                                      apiGuiPort,
                                                                      apiGuiToken);
        } catch (Exception e) {
            releasePorts();
            throw e;
        }
    }

    private void releasePorts() {
        if (apiListenPort != 0) {
            portService.release(apiListenPort);
        }
        if (apiGuiPort != 0) {
            portService.release(apiGuiPort);
        }
        if (runnerListenPort != 0) {
            portService.release(runnerListenPort);
        }
        if (runnerGuiPort != 0) {
            portService.release(runnerGuiPort);
        }
    }

    @Override
    public void run() {
        executorService.submit(runnerSyncTask);
        synchronizeEventListener.addProjectSynchronizeNotifier(apiSynchronizeNotifier);
        apiSyncTask.run();
    }

    @Override
    public void cancel() throws Exception {
        apiSyncTask.cancel();
        synchronizeEventListener.removeProjectSynchronizeNotifier(apiSynchronizeNotifier);
        runnerSyncTask.cancel();

        releasePorts();
    }
}
