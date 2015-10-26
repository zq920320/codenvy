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
import com.codenvy.swarm.client.SwarmDockerConnector;
import com.codenvy.swarm.client.json.SwarmContainerInfo;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.util.ValueHolder;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.Exec;
import org.eclipse.che.plugin.docker.client.LogMessage;
import org.eclipse.che.plugin.docker.machine.node.DockerNode;
import org.eclipse.che.plugin.docker.machine.node.WorkspaceFolderPathProvider;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * REST client for remote machine node
 *
 * @author Alexander Garagatyi
 */
public class RemoteDockerNode implements DockerNode {
    private static final Logger LOG = getLogger(RemoteDockerNode.class);

    private final String               workspaceId;
    private final MachineBackupManager backupManager;
    private final DockerConnector      dockerConnector;
    private final String               containerId;

    private final String hostProjectsFolder;
    private final String nodeLocation;

    @Inject
    public RemoteDockerNode(DockerConnector dockerConnector,
                            @Assisted("container") String containerId,
                            @Assisted("workspace") String workspaceId,
                            MachineBackupManager backupManager,
                            WorkspaceFolderPathProvider workspaceFolderPathProvider) throws MachineException {
        this.workspaceId = workspaceId;
        this.backupManager = backupManager;
        this.dockerConnector = dockerConnector;
        this.containerId = containerId;

        this.hostProjectsFolder = workspaceFolderPathProvider.getPath(workspaceId);

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
    }

    @Override
    public void bindWorkspace() throws MachineException {
        try {
            final Exec exec = dockerConnector.createExec(containerId, false, "/bin/sh", "-c", "id -u && id -g");
            final List<String> ownerIds = new ArrayList<>(2);
            final ValueHolder<String> error = new ValueHolder<>();
            dockerConnector.startExec(exec.getId(), message -> {
                if (message.getType() == LogMessage.Type.STDOUT) {
                    ownerIds.add(message.getContent());
                } else {
                    LOG.error("Can't detect container user ids to chown backed up workspace " + workspaceId + "files. " + message);
                    error.set("Can't detect container user ids to chown backed up workspace " + workspaceId + "files");
                }
            });

            if (error.get() != null) {
                throw new MachineException(error.get());
            }

            backupManager.restoreWorkspaceBackup(workspaceId,
                                                 hostProjectsFolder,
                                                 ownerIds.get(0),
                                                 ownerIds.get(1),
                                                 nodeLocation);
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new MachineException("Can't restore workspace file system");
        } catch (ServerException e) {
            throw new MachineException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public void unbindWorkspace() throws MachineException {
        try {
            backupManager.backupWorkspaceAndCleanup(workspaceId, hostProjectsFolder, nodeLocation);
        } catch (ServerException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public String getProjectsFolder() {
        return hostProjectsFolder;
    }

    @Override
    public String getHost() {
        return nodeLocation;
    }
}
