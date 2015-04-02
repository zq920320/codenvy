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

import com.codenvy.machine.dto.MachineCopyProjectRequest;
import com.codenvy.machine.dto.RemoteSyncListener;
import com.codenvy.machine.dto.SynchronizationConf;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.util.CustomPortService;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.ZipUtils;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Alexander Garagatyi
 */
@Path("/internal/machine")
@Singleton
public class MachineNodeService {
    private static final Logger LOG = LoggerFactory.getLogger(MachineNodeService.class);

    private final String            syncWorkingDir;
    private final String            apiEndpoint;
    private final String            machineSyncTaskExecutable;
    private final String            machineSyncTaskConfTemplate;
    private final String            machineSyncApiToken;
    private final CustomPortService portService;

    private final ConcurrentHashMap<String, SyncthingSynchronizeTask> syncTasks;
    private final ExecutorService                                     executor;

    @Inject
    public MachineNodeService(CustomPortService portService,
                              @Named("machine.sync.slave.api_token") String machineSyncApiToken,
                              @Named("machine.sync.slave.conf") String machineSyncTaskConfTemplate,
                              @Named("machine.sync.slave.exec") String machineSyncTaskExecutable,
                              @Named("machine.sync.workdir") String syncWorkingDir,
                              @Named("api.endpoint") String apiEndpoint) {
        this.portService = portService;
        this.machineSyncApiToken = machineSyncApiToken;
        this.machineSyncTaskConfTemplate = machineSyncTaskConfTemplate;
        this.machineSyncTaskExecutable = machineSyncTaskExecutable;
        this.syncWorkingDir = syncWorkingDir;
        this.apiEndpoint = apiEndpoint;
        this.syncTasks = new ConcurrentHashMap<>();
        this.executor =
                Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("MachineSlaveService-%d").setDaemon(true).build());
    }

    @Path("/binding")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void copyProjectToMachine(MachineCopyProjectRequest binding) throws ServerException {
        try {
            copyProjectSource(new File(binding.getHostFolder(), binding.getProject()),
                              binding.getWorkspaceId(),
                              binding.getProject(),
                              binding.getToken());
        } catch (IOException e) {
            IoUtil.deleteRecursive(new File(binding.getHostFolder()));
            LOG.warn(e.getLocalizedMessage(), e);
            throw new ServerException("Project binding failed. " + e.getLocalizedMessage());
        }
    }

    @Path("/binding")
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    public void removeProjectOnMachine(MachineCopyProjectRequest binding) throws ServerException {
        final File fullPath = new File(binding.getHostFolder(), binding.getProject());
        if (IoUtil.deleteRecursive(fullPath)) {
            throw new ServerException("Error occurred on removing of binding");
        }
    }

    @Path("/sync")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public RemoteSyncListener startSynchronization(SynchronizationConf synchronizationConf) throws ServerException {
        int machineSyncListenPort = 0;
        int machineSyncApiPort = 0;
        SyncthingSynchronizeTask runnerSyncTask = null;

        try {

            if ((machineSyncListenPort = portService.acquire()) == -1) {
                throw new ServerException("Machine synchronization failed");
            }

            if ((machineSyncApiPort = portService.acquire()) == -1) {
                throw new ServerException("Machine synchronization failed");
            }

            runnerSyncTask = new SyncthingSynchronizeTask(machineSyncTaskExecutable,
                                                          machineSyncTaskConfTemplate,
                                                          syncWorkingDir,
                                                          synchronizationConf.getSyncPath(),
                                                          machineSyncListenPort,
                                                          machineSyncApiPort,
                                                          "127.0.0.1:" + synchronizationConf.getSyncPort(),
                                                          machineSyncApiToken);

            syncTasks.put(synchronizationConf.getSyncPath(), runnerSyncTask);

            executor.submit(runnerSyncTask);

            return DtoFactory.getInstance().createDto(RemoteSyncListener.class).withPort(machineSyncListenPort);
        } catch (Exception e) {
            try {
                if (runnerSyncTask == null) {
                    portService.release(machineSyncListenPort);
                    portService.release(machineSyncApiPort);
                } else {
                    release(synchronizationConf.getSyncPath());
                }
            } catch (Exception e1) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    @Path("/sync")
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    public void stopSynchronization(SynchronizationConf synchronizationConf) throws ServerException {
        try {
            release(synchronizationConf.getSyncPath());
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    private void copyProjectSource(File destinationDir, String workspaceId, String project, String token) throws IOException {
        final UriBuilder zipBallUriBuilder = UriBuilder.fromUri(apiEndpoint)
                                                       .path("project")
                                                       .path(workspaceId)
                                                       .path("export")
                                                       .path(project)
                                                       .queryParam("token", token);

        final File zipBall = IoUtil.downloadFile(null, "projectZip", null, zipBallUriBuilder.build().toURL());
        ZipUtils.unzip(zipBall, destinationDir);
    }

    private void release(String taskKey) throws Exception {
        final SyncthingSynchronizeTask syncTask = syncTasks.get(taskKey);
        if (syncTask != null) {
            syncTask.cancel();
            for (int port : syncTask.getPorts()) {
                if (port != 0) {
                    portService.release(port);
                }
            }
        }
    }
}
