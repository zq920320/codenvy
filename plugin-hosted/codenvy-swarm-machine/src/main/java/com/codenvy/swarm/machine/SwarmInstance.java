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

import org.eclipse.che.api.core.model.machine.MachineMetadata;
import org.eclipse.che.api.core.model.machine.MachineState;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.plugin.docker.machine.DockerInstance;
import org.eclipse.che.plugin.docker.machine.DockerInstanceStopDetector;
import org.eclipse.che.plugin.docker.machine.DockerMachineFactory;
import org.eclipse.che.plugin.docker.machine.DockerNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;

/**
 * Swarm implementation of {@link Instance}
 *
 * @author Alexander Garagatyi
 */
public class SwarmInstance extends DockerInstance {
    private static final Logger LOG = LoggerFactory.getLogger(SwarmInstance.class);

    private final SwarmDockerConnector docker;
    private final String               container;
    private final DockerNode           dockerNode;

    private SwarmContainerInfo containerInfo;

    @Inject
    public SwarmInstance(SwarmDockerConnector docker,
                         @Named("machine.docker.registry") String registry,
                         DockerMachineFactory dockerMachineFactory,
                         @Assisted MachineState machineState,
                         @Assisted("container") String container,
                         @Assisted DockerNode node,
                         @Assisted LineConsumer outputConsumer,
                         DockerInstanceStopDetector dockerInstanceStopDetector) {
        super(docker,
              registry,
              dockerMachineFactory,
              machineState,
              container,
              node,
              outputConsumer,
              dockerInstanceStopDetector);
        this.docker = docker;
        this.container = container;
        this.dockerNode = node;
    }

    @Override
    public MachineMetadata getMetadata() {
        try {
            if (containerInfo == null) {
                containerInfo = docker.inspectContainer(container);
            }

            return new SwarmInstanceMetadata(containerInfo, dockerNode);
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return null;
        }
    }
}
