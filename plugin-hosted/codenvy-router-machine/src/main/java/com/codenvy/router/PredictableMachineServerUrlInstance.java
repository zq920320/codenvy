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
package com.codenvy.router;

import com.codenvy.swarm.client.SwarmDockerConnector;
import com.codenvy.swarm.machine.SwarmInstance;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.core.model.machine.Recipe;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.impl.ServerImpl;
import org.eclipse.che.api.machine.shared.Server;
import org.eclipse.che.plugin.docker.machine.DockerMachineFactory;
import org.eclipse.che.plugin.docker.machine.DockerNode;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.UriBuilder;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link org.eclipse.che.api.machine.server.spi.Instance} that rewrites servers addresses
 * to predictable addresses
 *
 * @author Alexander Garagatyi
 */
public class PredictableMachineServerUrlInstance extends SwarmInstance {
    private final RouterRulesRegistry routerRulesRegistry;
    private final String              machineId;

    @Inject
    public PredictableMachineServerUrlInstance(SwarmDockerConnector docker,
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
                                               RouterRulesRegistry routerRulesRegistry,
                                               @Assisted Recipe recipe,
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
              recipe,
              outputConsumer,
              memorySizeMB);
        this.routerRulesRegistry = routerRulesRegistry;
        this.machineId = machineId;
    }

    @Override
    public Map<String, Server> getServers() throws MachineException {
        final Map<String, Server> servers = new HashMap<>(getServersWithRealAddress());

        // returns only tcp ports which are used in servers map without '/tcp' suffix
        for (RoutingRule routingRule : routerRulesRegistry.getRules(machineId)) {
            // suppose there is only 1 rule and we use it
            String[] routingRuleAddress = routingRule.getUri().split(":", 2);

            final String exposedPort = Integer.toString(routingRule.getExposedPort());
            final Server serverWithRealAddress = servers.get(exposedPort);

            String routedUrl = null;
            if (serverWithRealAddress.getUrl() != null) {
                routedUrl = UriBuilder.fromUri(serverWithRealAddress.getUrl())
                                      .host(routingRuleAddress[0])
                                      .port(routingRuleAddress.length == 1 ? -1 : Integer.valueOf(routingRuleAddress[1]))
                                      .build()
                                      .toString();
            }

            servers.put(exposedPort, new ServerImpl(serverWithRealAddress.getRef(),
                                                    routingRule.getUri(),
                                                    routedUrl));
        }

        return servers;
    }

    protected Map<String, Server> getServersWithRealAddress() throws MachineException {
        return super.getServers();
    }
}
