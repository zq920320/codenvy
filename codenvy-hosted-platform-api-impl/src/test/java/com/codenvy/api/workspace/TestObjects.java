/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
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
package com.codenvy.api.workspace;

import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.machine.server.model.impl.LimitsImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineSourceImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeWorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.UsersWorkspaceImpl;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.lang.Size;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

/**
 * Test util class, helps to create test objects.
 *
 * @author Yevhenii Voevodin
 */
public final class TestObjects {

    private static final String DEFAULT_USER_NAME = "user123456";

    /** Creates users workspace object based on the owner and machines RAM. */
    public static UsersWorkspaceImpl createWorkspace(String owner, String devMachineRam, String... machineRams) {
        final List<MachineConfigImpl> machineConfigs = new ArrayList<>(1 + machineRams.length);
        machineConfigs.add(createMachineConfig(true, (int)Size.parseSizeToMegabytes(devMachineRam)));
        for (String machineRam : machineRams) {
            machineConfigs.add(createMachineConfig(false, (int)Size.parseSizeToMegabytes(machineRam)));
        }
        return UsersWorkspaceImpl.builder()
                                 .generateId()
                                 .setName(NameGenerator.generate("workspace", 2))
                                 .setOwner(owner)
                                 .setTemporary(false)
                                 .setStatus(WorkspaceStatus.STOPPED)
                                 .setEnvironments(singletonList(new EnvironmentImpl("dev-env", null, machineConfigs)))
                                 .setDefaultEnv("dev-env")
                                 .build();
    }

    /** Creates workspace config object based on the machines RAM. */
    public static WorkspaceConfig createConfig(String devMachineRam, String... machineRams) {
        return createWorkspace(DEFAULT_USER_NAME, devMachineRam, machineRams);
    }

    /** Creates runtime workspace object based on the machines RAM. */
    public static RuntimeWorkspaceImpl createRuntime(String devMachineRam, String... machineRams) {
        final UsersWorkspaceImpl workspace = createWorkspace(DEFAULT_USER_NAME, devMachineRam, machineRams);
        return RuntimeWorkspaceImpl.builder()
                                   .fromWorkspace(workspace)
                                   .setActiveEnvName(workspace.getDefaultEnv())
                                   .build();
    }

    /** Creates machine config object based on ram and dev flag */
    public static MachineConfigImpl createMachineConfig(boolean isDev, int ramLimit) {
        return new MachineConfigImpl(isDev,
                                     NameGenerator.generate("machine", 2),
                                     "docker",
                                     new MachineSourceImpl("recipe",
                                                           "recipe-location"),
                                     new LimitsImpl(ramLimit));
    }

    private TestObjects() {}
}
