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

import com.codenvy.machine.backup.MachineBackupManager;
import com.codenvy.machine.dto.MachineCopyProjectRequest;
import com.codenvy.machine.dto.RemoteSyncListener;
import com.codenvy.machine.dto.SynchronizationConf;
import com.codenvy.swarm.client.SwarmDockerConnector;
import com.codenvy.swarm.client.json.SwarmContainerInfo;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.HttpJsonHelper;
import org.eclipse.che.api.core.util.CustomPortService;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.shared.ProjectBinding;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.machine.DockerNode;
import org.eclipse.che.vfs.impl.fs.LocalFSMountStrategy;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * REST client for remote machine node
 *
 * @author Alexander Garagatyi
 */
public class RemoteDockerNode implements DockerNode {
    private static final Logger LOG = getLogger(RemoteDockerNode.class);

    private final String                            vfsSyncTaskExecutable;
    private final String                            vfsSyncTaskConfTemplate;
    private final String                            vfsGuiToken;
    private final String                            syncWorkingDir;
    private final LocalFSMountStrategy              mountStrategy;
    private final SyncthingSynchronizeEventListener syncthingSynchronizeEventListener;
    private final CustomPortService                 portService;
    private final SyncTasks                         syncTasks;
    private final MachineBackupManager              backupManager;

    private final String hostProjectsFolder;
    private final String nodeLocation;

    @Inject
    public RemoteDockerNode(@Named("machine.sync.vfs.exec") String vfsSyncTaskExecutable,
                            @Named("machine.sync.vfs.conf") String vfsSyncTaskConfTemplate,
                            @Named("machine.sync.vfs.api_token") String vfsGuiToken,
                            @Named("machine.sync.workdir") String syncWorkingDir,
                            @Named("machine.project.location") String machineProjectsDir,
                            LocalFSMountStrategy mountStrategy,
                            SyncthingSynchronizeEventListener syncthingSynchronizeEventListener,
                            CustomPortService portService,
                            DockerConnector dockerConnector,
                            SyncTasks syncTasks,
                            @Assisted String containerId,
                            MachineBackupManager backupManager) throws MachineException {
        this.vfsSyncTaskExecutable = vfsSyncTaskExecutable;
        this.vfsSyncTaskConfTemplate = vfsSyncTaskConfTemplate;
        this.vfsGuiToken = vfsGuiToken;
        this.syncWorkingDir = syncWorkingDir;
        this.mountStrategy = mountStrategy;
        this.syncthingSynchronizeEventListener = syncthingSynchronizeEventListener;
        this.portService = portService;
        this.syncTasks = syncTasks;
        this.backupManager = backupManager;

        this.hostProjectsFolder = new File(machineProjectsDir, containerId).toString();

        String nodeLocation = "127.0.0.1";
        if (dockerConnector instanceof SwarmDockerConnector) {
            try {
                final SwarmContainerInfo info = ((SwarmDockerConnector)dockerConnector).inspectContainerDirectly(containerId);
                if (info != null) {
                    nodeLocation = info.getNode().getIp();
                }
            } catch (IOException e) {
                LOG.error(e.getLocalizedMessage(), e);
                throw new MachineException("Internal server error occurs. Please contact support");
            }
        }
        this.nodeLocation = nodeLocation;

        // We have to create folder, see https://github.com/docker/docker/issues/12061
        final URI uri = UriBuilder.fromUri("/machine-runner/internal/machine/folder/")
                                  .path(hostProjectsFolder)
                                  .host(nodeLocation)
                                  .port(8080)
                                  .scheme("http")
                                  .build();

        try {
            HttpJsonHelper.request(null, uri.toString(), "POST", null);
        } catch (IOException | ApiException e) {
            throw new MachineException(e.getLocalizedMessage());
        }
    }

    @Override
    public void bindProject(String workspaceId, ProjectBinding project)
            throws MachineException {
        copySources(workspaceId, project);
    }

    @Override
    public void unbindProject(String workspaceId, ProjectBinding project)
            throws MachineException {
        try {
            removeSources(project);
        } catch (MachineException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw e;
        }

    }

    @Override
    public void bindWorkspace(String workspaceId, String hostProjectsFolder) throws MachineException {
        try {
            backupManager.restoreWorkspaceBackup(workspaceId, hostProjectsFolder, nodeLocation);
        } catch (ServerException e) {
            throw new MachineException(e.getLocalizedMessage(), e);
        }

        try {
            startSynchronization(workspaceId, null);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public void unbindWorkspace(String workspaceId, String hostProjectsFolder) throws MachineException {
        try {
            stopSynchronization(null);
        } catch (MachineException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }

        try {
            backupManager.backupWorkspace(workspaceId, hostProjectsFolder, nodeLocation);
        } catch (ServerException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }

        removeSources(null);
    }

    @Override
    public String getProjectsFolder() {
        return hostProjectsFolder;
    }

    @Override
    public String getHost() {
        return nodeLocation;
    }

    private void copySources(String workspaceId, ProjectBinding project) throws MachineException {
        final MachineCopyProjectRequest bindingConf = DtoFactory.getInstance().createDto(MachineCopyProjectRequest.class)
                                                                .withWorkspaceId(workspaceId)
                                                                .withProject(project == null ? null : project.getPath())
                                                                .withHostFolder(hostProjectsFolder)
                                                                .withToken(EnvironmentContext.getCurrent().getUser().getToken());

        final URI uri = UriBuilder.fromUri("/machine-runner/internal/machine/binding")
                                  .host(nodeLocation)
                                  .port(8080)
                                  .scheme("http")
                                  .build();

        try {
            HttpJsonHelper.request(null, uri.toString(), "POST", bindingConf);
        } catch (IOException | ApiException e) {
            throw new MachineException("Source binding failed. " + e.getLocalizedMessage());
        }
    }

    private void removeSources(ProjectBinding projectBinding) throws MachineException {
        final MachineCopyProjectRequest bindingConf = DtoFactory.getInstance().createDto(MachineCopyProjectRequest.class)
                                                                .withProject(projectBinding == null ? null : projectBinding.getPath())
                                                                .withHostFolder(hostProjectsFolder);

        final URI uri = UriBuilder.fromUri("/machine-runner/internal/machine/binding")
                                  .host(nodeLocation)
                                  .port(8080)
                                  .scheme("http")
                                  .build();

        try {
            HttpJsonHelper.request(null, uri.toString(), "DELETE", bindingConf);
        } catch (IOException | ApiException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new MachineException("Source unbinding failed. " + e.getLocalizedMessage());
        }
    }

    private void startSynchronization(String workspaceId, ProjectBinding projectBinding) {
        int vfsSyncListenPort = 0;
        int vfsSyncApiPort = 0;
        RemoteSyncListener syncListenerConf = null;
        final String projectPath = projectBinding != null ? projectBinding.getPath() : null;
        final String machineHostSyncPath = hostProjectsFolder + "/" + (projectPath != null ? projectPath : "");

        try {
            final File mountPath = mountStrategy.getMountPath(workspaceId);

            final String vfsSyncPath = projectPath != null ? new File(mountPath, projectPath).toString() : mountPath.toString();


            if ((vfsSyncListenPort = portService.acquire()) == -1) {
                throw new ServerException("Machine synchronization failed");
            }

            if ((vfsSyncApiPort = portService.acquire()) == -1) {
                throw new ServerException("Machine synchronization failed");
            }

            SynchronizationConf synchronizationConf = DtoFactory.getInstance().createDto(SynchronizationConf.class)
                                                                .withSyncPath(machineHostSyncPath)
                                                                .withSyncPort(vfsSyncListenPort)
                                                                .withSyncAddress(nodeLocation);

            final URI uri = UriBuilder.fromUri("/machine-runner/internal/machine/sync")
                                      .host(nodeLocation)
                                      .port(8080)
                                      .scheme("http")
                                      .build();

            try {
                syncListenerConf = HttpJsonHelper.request(RemoteSyncListener.class, uri.toString(), "POST", synchronizationConf);
            } catch (IOException | ApiException e) {
                LOG.error(e.getLocalizedMessage(), e);
                throw new ServerException("Machine synchronization failed");
            }

            SyncthingSynchronizeTask syncTask = new VfsSyncthingSynchronizerTask(vfsSyncTaskExecutable,
                                                                                 vfsSyncTaskConfTemplate,
                                                                                 syncWorkingDir,
                                                                                 vfsSyncPath,
                                                                                 vfsSyncListenPort,
                                                                                 vfsSyncApiPort,
                                                                                 nodeLocation + ":" + syncListenerConf.getPort(),
                                                                                 vfsGuiToken,
                                                                                 workspaceId,
                                                                                 projectPath != null ? projectPath : "/",
                                                                                 syncthingSynchronizeEventListener);

            syncTasks.startTask(machineHostSyncPath, syncTask);
        } catch (Exception e) {
            try {
                if (syncListenerConf == null) {
                    portService.release(vfsSyncListenPort);
                    portService.release(vfsSyncApiPort);
                } else {
                    release(projectPath);
                }
            } catch (Exception e1) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }

    private void stopSynchronization(String path)
            throws MachineException {
        try {
            release(path);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new MachineException(e.getLocalizedMessage(), e);
        }
    }

    private void release(String path) throws Exception {
        final String syncPath = hostProjectsFolder + "/" + (path != null ? path : "");
        final SyncthingSynchronizeTask vfsSynchronizer = syncTasks.stopTask(syncPath);
        if (vfsSynchronizer != null) {

            for (int port : vfsSynchronizer.getPorts()) {
                if (port != 0) {
                    portService.release(port);
                }
            }
        }

        final URI uri = UriBuilder.fromUri("/machine-runner/internal/machine/sync")
                                  .host(nodeLocation)
                                  .port(8080)
                                  .scheme("http")
                                  .build();

        SynchronizationConf synchronizationConf = DtoFactory.getInstance().createDto(SynchronizationConf.class)
                                                            .withSyncPath(syncPath);

        try {
            HttpJsonHelper.request(null, uri.toString(), "DELETE", synchronizationConf);
        } catch (IOException | ApiException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException("Machine synchronization failed");
        }
    }
}
