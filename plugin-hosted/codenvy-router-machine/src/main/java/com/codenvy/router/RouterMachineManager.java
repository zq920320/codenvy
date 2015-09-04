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
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.machine.server.MachineInstanceProviders;
import org.eclipse.che.api.machine.server.MachineManager;
import org.eclipse.che.api.machine.server.MachineRegistry;
import org.eclipse.che.api.machine.server.dao.SnapshotDao;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceKey;
import org.eclipse.che.api.machine.server.spi.InstanceMetadata;
import org.eclipse.che.api.machine.server.spi.InstanceNode;
import org.eclipse.che.api.machine.server.spi.InstanceProcess;
import org.eclipse.che.api.machine.shared.MachineStatus;
import org.eclipse.che.api.machine.shared.ProjectBinding;
import org.eclipse.che.api.machine.shared.Recipe;
import org.eclipse.che.api.machine.shared.Server;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
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

    @Inject
    public RouterMachineManager(SnapshotDao snapshotDao,
                                MachineRegistry machineRegistry,
                                MachineInstanceProviders machineInstanceProviders,
                                @Named("machine.logs.location") String machineLogsDir,
                                EventService eventService,
                                @Named("machine.default_mem_size_mb") int defaultMachineMemorySizeMB) {
        super(snapshotDao, machineRegistry, machineInstanceProviders, machineLogsDir, eventService, defaultMachineMemorySizeMB);
        this.snapshotDao = snapshotDao;
        this.machineRegistry = machineRegistry;
        this.machineInstanceProviders = machineInstanceProviders;
        this.machineLogsDir = machineLogsDir;
        this.eventService = eventService;
        this.defaultMachineMemorySizeMB = defaultMachineMemorySizeMB;
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
             manager.defaultMachineMemorySizeMB);
    }

    Instance getMachineWithDirectServersUrls(String machineId) throws NotFoundException, MachineException {
        final PredictableMachineServerUrlInstance machine = (PredictableMachineServerUrlInstance)super.getMachine(machineId);
        return new Instance() {
            @Override
            public Map<String, Server> getServers() throws MachineException {
                return machine.getServersWithRealAddress();
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
            public InstanceMetadata getMetadata() throws MachineException {
                return machine.getMetadata();
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
            public InstanceProcess createProcess(String commandLine) throws MachineException {
                return machine.createProcess(commandLine);
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
            public void bindProject(ProjectBinding project) throws MachineException {
                machine.bindProject(project);
            }

            @Override
            public void unbindProject(ProjectBinding project) throws MachineException, NotFoundException {
                machine.unbindProject(project);
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
            public String getId() {
                return machine.getId();
            }

            @Override
            public String getType() {
                return machine.getType();
            }

            @Override
            public Recipe getRecipe() {
                return machine.getRecipe();
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
            public List<? extends ProjectBinding> getProjects() {
                return machine.getProjects();
            }

            @Override
            public String getWorkspaceId() {
                return machine.getWorkspaceId();
            }

            @Override
            public boolean isDev() {
                return machine.isDev();
            }

            @Override
            public String getDisplayName() {
                return machine.getDisplayName();
            }

            @Override
            public int getMemorySize() {
                return machine.getMemorySize();
            }
        };
    }
}
