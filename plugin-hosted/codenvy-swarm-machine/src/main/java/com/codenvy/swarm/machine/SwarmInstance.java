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
package com.codenvy.swarm.machine;

import com.codenvy.swarm.client.SwarmDockerConnector;
import com.codenvy.swarm.client.json.SwarmContainerInfo;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceMetadata;
import org.eclipse.che.plugin.docker.machine.DockerInstance;
import org.eclipse.che.plugin.docker.machine.DockerMachineFactory;
import org.eclipse.che.plugin.docker.machine.DockerNode;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;

/**
 * Swarm implementation of {@link Instance}
 *
 * @author Alexander Garagatyi
 */
public class SwarmInstance extends DockerInstance {
    private final SwarmDockerConnector docker;
    private final String               container;

    private SwarmContainerInfo containerInfo;

    @Inject
    public SwarmInstance(SwarmDockerConnector docker,
                         @Named("machine.docker.registry") String registry,
                         DockerMachineFactory dockerMachineFactory,
                         @Assisted("machineId") String machineId,
                         @Assisted("workspaceId") String workspaceId,
                         @Assisted boolean workspaceIsBound,
                         @Assisted("creator") String creator,
                         @Assisted("displayName") String displayName,
                         @Assisted("container") String container,
                         @Assisted DockerNode node,
                         @Assisted LineConsumer outputConsumer,
                         @Assisted int memorySizeMB) {
        super(docker,
              registry,
              dockerMachineFactory,
              machineId,
              workspaceId,
              workspaceIsBound,
              creator,
              displayName,
              container,
              node,
              outputConsumer,
              memorySizeMB);
        this.docker = docker;
        this.container = container;
    }

    @Override
    public InstanceMetadata getMetadata() throws MachineException {
        try {
            if (containerInfo == null) {
                containerInfo = docker.inspectContainer(container);
            }

            return new SwarmInstanceMetadata(containerInfo);
        } catch (IOException e) {
            throw new MachineException(e.getLocalizedMessage(), e);
        }
    }
}
