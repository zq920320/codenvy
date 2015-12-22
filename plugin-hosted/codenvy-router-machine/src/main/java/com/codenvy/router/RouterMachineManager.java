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

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.machine.Channels;
import org.eclipse.che.api.core.model.machine.Limits;
import org.eclipse.che.api.core.model.machine.MachineMetadata;
import org.eclipse.che.api.core.model.machine.MachineSource;
import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.core.model.machine.Server;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.machine.server.MachineInstanceProviders;
import org.eclipse.che.api.machine.server.MachineManager;
import org.eclipse.che.api.machine.server.MachineRegistry;
import org.eclipse.che.api.machine.server.dao.SnapshotDao;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.model.impl.ServerImpl;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceKey;
import org.eclipse.che.api.machine.server.spi.InstanceNode;
import org.eclipse.che.api.machine.server.spi.InstanceProcess;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Extends {@link org.eclipse.che.api.machine.server.MachineManager} with methods that returns instance with direct servers urls.
 * Is used to avoid routing requests from {@link org.eclipse.che.api.machine.server.proxy.MachineExtensionProxyServlet}
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class RouterMachineManager extends MachineManager {
    private final SnapshotDao              snapshotDao;
    private final MachineRegistry          machineRegistry;
    private final MachineInstanceProviders machineInstanceProviders;
    private final String                   machineLogsDir;
    private final EventService             eventService;
    private final int                      defaultMachineMemorySizeMB;
    private final String                   apiEndpoint;
    private final RouterRulesRegistry      routerRulesRegistry;

    @Inject
    public RouterMachineManager(SnapshotDao snapshotDao,
                                MachineRegistry machineRegistry,
                                MachineInstanceProviders machineInstanceProviders,
                                @Named("machine.logs.location") String machineLogsDir,
                                EventService eventService,
                                @Named("machine.default_mem_size_mb") int defaultMachineMemorySizeMB,
                                @Named("api.endpoint") String apiEndpoint,
                                RouterRulesRegistry routerRulesRegistry) {
        super(snapshotDao, machineRegistry, machineInstanceProviders, machineLogsDir, eventService, defaultMachineMemorySizeMB,
              apiEndpoint);
        this.snapshotDao = snapshotDao;
        this.machineRegistry = machineRegistry;
        this.machineInstanceProviders = machineInstanceProviders;
        this.machineLogsDir = machineLogsDir;
        this.eventService = eventService;
        this.defaultMachineMemorySizeMB = defaultMachineMemorySizeMB;
        this.apiEndpoint = apiEndpoint;
        this.routerRulesRegistry = routerRulesRegistry;
    }

    /**
     * Is used to create self-wrapping implementations
     */
    RouterMachineManager(RouterMachineManager manager) {
        this(manager.snapshotDao,
             manager.machineRegistry,
             manager.machineInstanceProviders,
             manager.machineLogsDir,
             manager.eventService,
             manager.defaultMachineMemorySizeMB,
             manager.apiEndpoint,
             manager.routerRulesRegistry);
    }

    private Map<String, ServerImpl> rewriteServersUrls(String machineId, Map<String, ? extends Server> servers) {
        Map<String, ServerImpl> serversWithRewrittenUrls = new HashMap<>();
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

            serversWithRewrittenUrls.put(exposedPort, new ServerImpl(serverWithRealAddress.getRef(),
                                                                     routingRule.getUri(),
                                                                     routedUrl));
        }

        // add those servers that do not have mappings in RouterRulesRegistry
        servers.entrySet()
               .stream()
               .filter(serverEntry -> !serversWithRewrittenUrls.containsKey(serverEntry.getKey()))
               .forEach(serverEntry -> serversWithRewrittenUrls.put(serverEntry.getKey(), new ServerImpl(serverEntry.getValue())));

        return serversWithRewrittenUrls;
    }

    Instance getMachineWithDirectServersUrls(String machineId) throws NotFoundException, MachineException {
        final Instance machine = super.getMachine(machineId);
        return new Instance() {
            @Override
            public MachineMetadata getMetadata() {
                final MachineMetadata metadata = machine.getMetadata();
                return new MachineMetadata() {
                    @Override
                    public Map<String, ServerImpl> getServers() {
                        return rewriteServersUrls(machineId, metadata.getServers());
                    }

                    /************* Proxy methods ***********/

                    @Override
                    public Map<String, String> getEnvVariables() {
                        return metadata.getEnvVariables();
                    }

                    @Override
                    public Map<String, String> getProperties() {
                        return metadata.getProperties();
                    }

                    @Override
                    public String projectsRoot() {
                        return metadata.projectsRoot();
                    }
                };
            }

            /************* Proxy methods ***********/

            @Override
            public void setStatus(MachineStatus status) {
                machine.setStatus(status);
            }

            @Override
            public LineConsumer getLogger() {
                return machine.getLogger();
            }

            @Override
            public InstanceProcess getProcess(int pid) throws NotFoundException, MachineException {
                return machine.getProcess(pid);
            }

            @Override
            public List<InstanceProcess> getProcesses() throws MachineException {
                return machine.getProcesses();
            }

            @Override
            public InstanceProcess createProcess(String commandName, String commandLine) throws MachineException {
                return machine.createProcess(commandName, commandLine);
            }

            @Override
            public InstanceKey saveToSnapshot(String owner) throws MachineException {
                return machine.saveToSnapshot(owner);
            }

            @Override
            public void destroy() throws MachineException {
                machine.destroy();
            }

            @Override
            public InstanceNode getNode() {
                return machine.getNode();
            }

            @Override
            public String readFileContent(String filePath, int startFrom, int limit) throws MachineException {
                return machine.readFileContent(filePath, startFrom, limit);
            }

            @Override
            public void copy(Instance instance, String sourcePath, String targetPath, boolean overwrite) throws MachineException {
                machine.copy(instance, sourcePath, targetPath, overwrite);
            }

            @Override
            public String getId() {
                return machine.getId();
            }

            @Override
            public String getType() {
                return machine.getType();
            }

            @Override
            public String getOwner() {
                return machine.getOwner();
            }

            @Override
            public MachineStatus getStatus() {
                return machine.getStatus();
            }

            @Override
            public String getName() {
                return machine.getName();
            }

            @Override
            public String getWorkspaceId() {
                return machine.getWorkspaceId();
            }

            @Override
            public String getEnvName() {
                return machine.getEnvName();
            }

            @Override
            public boolean isDev() {
                return machine.isDev();
            }

            @Override
            public Channels getChannels() {
                return machine.getChannels();
            }

            @Override
            public MachineSource getSource() {
                return machine.getSource();
            }

            @Override
            public Limits getLimits() {
                return machine.getLimits();
            }
        };
    }
}
