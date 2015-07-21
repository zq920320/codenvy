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

import com.codenvy.swarm.client.json.SwarmContainerInfo;

import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.spi.InstanceMetadata;
import org.eclipse.che.plugin.docker.machine.DockerInstanceMetadata;

import java.util.Map;

/**
 * Swarm implementation of {@link InstanceMetadata}
 *
 * @author Alexander Garagatyi
 */
public class SwarmInstanceMetadata extends DockerInstanceMetadata {

    private final SwarmContainerInfo info;

    public SwarmInstanceMetadata(SwarmContainerInfo containerInfo) throws MachineException {
        super(containerInfo);
        info = containerInfo;
    }

    @Override
    public Map<String, String> getProperties() {
        final Map<String, String> dockerProperties = super.getProperties();

        dockerProperties.put("node.ip", info.getNode().getIp());
        dockerProperties.put("node.addr", info.getNode().getAddr());
        dockerProperties.put("node.id", info.getNode().getId());
        dockerProperties.put("node.name", info.getNode().getName());

        return dockerProperties;
    }

    @Override
    public String toJson() {
        return info.toString();
    }
}
