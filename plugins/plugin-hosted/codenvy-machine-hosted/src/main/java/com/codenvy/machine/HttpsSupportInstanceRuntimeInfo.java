/*
 *  [2012] - [2016] Codenvy, S.A.
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

import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.Server;
import org.eclipse.che.api.core.model.machine.ServerConf;
import org.eclipse.che.api.machine.server.model.impl.ServerImpl;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;
import org.eclipse.che.plugin.docker.machine.DockerInstanceRuntimeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Rewrites protocol, host and port of machine servers to proxy requests through https decryption proxy
 *
 * @author Alexander Garagatyi
 */
public class HttpsSupportInstanceRuntimeInfo extends DockerInstanceRuntimeInfo {
    private static final Logger LOG = LoggerFactory.getLogger(HttpsSupportInstanceRuntimeInfo.class);

    private final boolean                        isHttpsEnabled;
    private final MachineServerHostPortGenerator serverHostPortGenerator;

    @Inject
    public HttpsSupportInstanceRuntimeInfo(@Assisted ContainerInfo containerInfo,
                                           @Assisted String dockerNodeHost,
                                           @Assisted MachineConfig machineConfig,
                                           @Named("api.endpoint") URI apiEndpoint,
                                           MachineServerHostPortGenerator serverHostPortGenerator,
                                           @Named("machine.docker.dev_machine.machine_servers") Set<ServerConf> devMachineServers,
                                           @Named("machine.docker.machine_servers") Set<ServerConf> allMachinesServers) {
        super(containerInfo, dockerNodeHost, machineConfig, devMachineServers, allMachinesServers);
        this.serverHostPortGenerator = serverHostPortGenerator;

        isHttpsEnabled = apiEndpoint.getScheme().equals("https");
    }

    @Override
    public Map<String, ServerImpl> getServers() {
        final HashMap<String, ServerImpl> servers = new HashMap<>(super.getServers());
        if (isHttpsEnabled) {
            for (Map.Entry<String, ServerImpl> serverEntry : servers.entrySet()) {
                try {
                    final Server server = serverEntry.getValue();
                    final URI serverUri = server.getUrl() != null ? new URI(server.getUrl()) : new URI("http://" + server.getAddress());
                    if ("https".equals(serverUri.getScheme()) || "http".equals(serverUri.getScheme())) {
                        final String newHostPort = serverHostPortGenerator.generate(server.getRef(),
                                                                                    serverUri.getScheme(),
                                                                                    serverUri.getHost(),
                                                                                    serverUri.getPort());
                        serverEntry.setValue(new ServerImpl(server.getRef(),
                                                            server.getProtocol(),
                                                            newHostPort,
                                                            server.getPath(),
                                                            serverUri.getScheme() + "://" + newHostPort));
                    }
                } catch (URISyntaxException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }

        return servers;
    }
}
