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

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.rest.HttpJsonHelper;
import org.eclipse.che.api.core.util.CustomPortService;
import org.eclipse.che.api.machine.server.MachineImpl;
import org.eclipse.che.api.machine.server.MachineNode;
import org.eclipse.che.api.machine.server.MachineRegistry;
import org.eclipse.che.api.machine.shared.ProjectBinding;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.vfs.impl.fs.LocalFSMountStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * REST API for machine slave runners
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class RemoteMachineNodeImpl implements MachineNode {
    private static final Logger LOG = LoggerFactory.getLogger(RemoteMachineNodeImpl.class);

    private final String                            vfsSyncTaskExecutable;
    private final String                            vfsSyncTaskConfTemplate;
    private final int                               vfsMinSyncPort;
    private final int                               vfsMaxSyncPort;
    private final String                            vfsGuiToken;
    private final String                            syncWorkingDir;
    private final LocalFSMountStrategy              mountStrategy;
    private final SyncthingSynchronizeEventListener syncthingSynchronizeEventListener;
    private final CustomPortService                 portService;
    private final MachineRegistry                   machineRegistry;
    private final ExecutorService                   executor;

    private final ConcurrentHashMap<String, Pair<SyncthingSynchronizeTask, SyncthingSynchronizeNotifier>> vfsSyncTasks;

    @Inject
    public RemoteMachineNodeImpl(@Named("machine.sync.vfs.exec") String vfsSyncTaskExecutable,
                                 @Named("machine.sync.vfs.conf") String vfsSyncTaskConfTemplate,
                                 @Named("machine.sync.vfs.port_min") int vfsMinSyncPort,
                                 @Named("machine.sync.vfs.port_max") int vfsMaxSyncPort,
                                 @Named("machine.sync.vfs.api_token") String vfsGuiToken,
                                 @Named("machine.sync.workdir") String syncWorkingDir,
                                 LocalFSMountStrategy mountStrategy,
                                 SyncthingSynchronizeEventListener syncthingSynchronizeEventListener,
                                 CustomPortService portService,
                                 MachineRegistry machineRegistry) {
        this.vfsSyncTaskExecutable = vfsSyncTaskExecutable;
        this.vfsSyncTaskConfTemplate = vfsSyncTaskConfTemplate;
        this.vfsMinSyncPort = vfsMinSyncPort;
        this.vfsMaxSyncPort = vfsMaxSyncPort;
        this.vfsGuiToken = vfsGuiToken;
        this.syncWorkingDir = syncWorkingDir;
        this.mountStrategy = mountStrategy;
        this.syncthingSynchronizeEventListener = syncthingSynchronizeEventListener;
        this.portService = portService;
        this.machineRegistry = machineRegistry;
        this.vfsSyncTasks = new ConcurrentHashMap<>();
        this.executor =
                Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("RemoteMachineSlaveImpl-").setDaemon(true).build());
    }

    @Override
    public void copyProjectToMachine(String machineId, ProjectBinding project) throws ServerException, NotFoundException {
        final MachineImpl machine = machineRegistry.get(machineId);

        final MachineCopyProjectRequest bindingConf = DtoFactory.getInstance().createDto(MachineCopyProjectRequest.class)
                                                                .withWorkspaceId(machine.getWorkspaceId())
                                                                .withProject(project.getPath())
                                                                .withHostFolder(machine.getHostProjectsFolder().toString())
                                                                .withToken(EnvironmentContext.getCurrent().getUser().getToken());

        final URI uri = UriBuilder.fromUri("/machine-runner/internal/machine/binding")
                                  .host(machine.getLocationAddress())
                                  .port(8080)
                                  .scheme("http")
                                  .build();

        try {
            HttpJsonHelper.request(null, uri.toString(), "POST", bindingConf);
        } catch (IOException | UnauthorizedException | ConflictException | ForbiddenException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException("Project binding failed.");
        }
    }

    @Override
    public void removeProjectFromMachine(String machineId, ProjectBinding project) throws NotFoundException, ServerException {
        final MachineImpl machine = machineRegistry.get(machineId);

        final MachineCopyProjectRequest bindingConf = DtoFactory.getInstance().createDto(MachineCopyProjectRequest.class)
                                                                .withProject(project.getPath())
                                                                .withHostFolder(machine.getHostProjectsFolder().toString());

        final URI uri = UriBuilder.fromUri("/machine-runner/internal/machine/binding")
                                  .host(machine.getLocationAddress())
                                  .port(8080)
                                  .scheme("http")
                                  .build();

        try {
            HttpJsonHelper.request(null, uri.toString(), "DELETE", bindingConf);
        } catch (IOException | UnauthorizedException | ConflictException | ForbiddenException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException("Error occurred on removing of binding");
        }
    }

    @Override
    public void startSynchronization(String machineId, ProjectBinding projectBinding) throws ServerException, NotFoundException {
        final MachineImpl machine = machineRegistry.get(machineId);

        int vfsSyncListenPort = 0;
        int vfsSyncApiPort = 0;
        RemoteSyncListener syncListenerConf = null;
        final String project = projectBinding.getPath();

        try {
            final File mountPath = mountStrategy.getMountPath(machine.getWorkspaceId());

            if ((vfsSyncListenPort = portService.acquire(vfsMinSyncPort, vfsMaxSyncPort)) == -1) {
                throw new ServerException("Machine synchronization failed");
            }

            if ((vfsSyncApiPort = portService.acquire(vfsMinSyncPort, vfsMaxSyncPort)) == -1) {
                throw new ServerException("Machine synchronization failed");
            }

            SynchronizationConf synchronizationConf = DtoFactory.getInstance().createDto(SynchronizationConf.class)
                                                                .withSyncPath(new File(machine.getHostProjectsFolder(),
                                                                                       projectBinding.getPath()).toString())
                                                                .withSyncPort(vfsSyncListenPort);

            final URI uri = UriBuilder.fromUri("/machine-runner/internal/machine/sync")
                                      .host(machine.getLocationAddress())
                                      .port(8080)
                                      .scheme("http")
                                      .build();

            try {
                syncListenerConf = HttpJsonHelper.request(RemoteSyncListener.class, uri.toString(), "POST", synchronizationConf);
            } catch (IOException | UnauthorizedException | ConflictException | ForbiddenException e) {
                LOG.error(e.getLocalizedMessage(), e);
                throw new ServerException("Machine synchronization failed");
            }

            String vfsSyncPath = new File(mountPath, project).toString();


            SyncthingSynchronizeTask syncTask = new SyncthingSynchronizeTask(vfsSyncTaskExecutable,
                                                                             vfsSyncTaskConfTemplate,
                                                                             syncWorkingDir,
                                                                             vfsSyncPath,
                                                                             vfsSyncListenPort,
                                                                             vfsSyncApiPort,
                                                                             "127.0.0.1:" + syncListenerConf.getPort(),
                                                                             vfsGuiToken);

            SyncthingSynchronizeNotifier SyncNotifier = new SyncthingSynchronizeNotifier(machine.getWorkspaceId(),
                                                                                         project,
                                                                                         vfsSyncApiPort,
                                                                                         vfsGuiToken);

            vfsSyncTasks.put(machineId + "/" + project, Pair.of(syncTask, SyncNotifier));


            executor.submit(syncTask);

            syncthingSynchronizeEventListener.addProjectSynchronizeNotifier(SyncNotifier);
        } catch (Exception e) {
            try {
                if (syncListenerConf == null) {
                    portService.release(vfsSyncListenPort);
                    portService.release(vfsSyncApiPort);
                } else {
                    release(machineId, project);
                }
            } catch (Exception e1) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }

    @Override
    public void stopSynchronization(String machineId, ProjectBinding project) throws ServerException {
        try {
            release(machineId, project.getPath());
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    private void release(String machineId, String path) throws Exception {
        final Pair<SyncthingSynchronizeTask, SyncthingSynchronizeNotifier> vfsSynchronizer = vfsSyncTasks.get(machineId + "/" + path);
        if (vfsSynchronizer != null) {
            syncthingSynchronizeEventListener.removeProjectSynchronizeNotifier(vfsSynchronizer.second);
            for (int port : vfsSynchronizer.first.getPorts()) {
                if (port != 0) {
                    portService.release(port);
                }
            }
            try {
                vfsSynchronizer.first.cancel();
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }

        final MachineImpl machine = machineRegistry.get(machineId);

        final URI uri = UriBuilder.fromUri("/machine-runner/internal/machine/sync")
                                  .host(machine.getLocationAddress())
                                  .port(8080)
                                  .scheme("http")
                                  .build();

        SynchronizationConf synchronizationConf = DtoFactory.getInstance().createDto(SynchronizationConf.class)
                                                            .withSyncPath(new File(machine.getHostProjectsFolder(), path).toString());

        try {
            HttpJsonHelper.request(null, uri.toString(), "DELETE", synchronizationConf);
        } catch (IOException | UnauthorizedException | ConflictException | ForbiddenException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException("Machine synchronization failed");
        }
    }
}
