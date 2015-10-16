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
import com.codenvy.swarm.client.SwarmDockerConnector;
import com.codenvy.swarm.client.json.SwarmContainerInfo;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.HttpJsonHelper;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.machine.DockerNode;
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

    private final MachineBackupManager              backupManager;

    private final String hostProjectsFolder;
    private final String nodeLocation;

    @Inject
    public RemoteDockerNode(@Named("machine.project.location") String machineProjectsDir,
                            DockerConnector dockerConnector,
                            @Assisted String containerId,
                            MachineBackupManager backupManager) throws MachineException {
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
    public void bindWorkspace(String workspaceId, String hostProjectsFolder) throws MachineException {
        try {
            backupManager.restoreWorkspaceBackup(workspaceId, hostProjectsFolder, nodeLocation);
        } catch (ServerException e) {
            throw new MachineException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public void unbindWorkspace(String workspaceId, String hostProjectsFolder) throws MachineException {
        try {
            backupManager.backupWorkspace(workspaceId, hostProjectsFolder, nodeLocation);
        } catch (ServerException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }

        removeSources();
    }

    @Override
    public String getProjectsFolder() {
        return hostProjectsFolder;
    }

    @Override
    public String getHost() {
        return nodeLocation;
    }

    private void removeSources() throws MachineException {
        final MachineCopyProjectRequest bindingConf = DtoFactory.getInstance().createDto(MachineCopyProjectRequest.class)
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
}
