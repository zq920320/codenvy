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

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.workspace.server.DefaultWorkspaceConfigValidator;
import org.eclipse.che.commons.lang.Size;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import static java.lang.String.format;

/**
 * Checks workspace configuration object limitations,
 * in the case of limitation violation throws {@link BadRequestException}.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class LimitsCheckingWorkspaceConfigValidator extends DefaultWorkspaceConfigValidator {

    private final long maxRamPerWorkspaceEnv;

    @Inject
    public LimitsCheckingWorkspaceConfigValidator(@Named("limits.workspace.env.ram") String maxRam) {
        maxRamPerWorkspaceEnv = Size.parseSizeToMegabytes(maxRam);
    }

    @Override
    public void validate(WorkspaceConfig config) throws BadRequestException {
        super.validate(config);
        for (Environment environment : config.getEnvironments()) {
            final long workspaceRam = environment.getMachineConfigs()
                                                 .stream()
                                                 .filter(machineCfg -> machineCfg.getLimits() != null)
                                                 .mapToInt(machineCfg -> machineCfg.getLimits().getRam())
                                                 .sum();
            if (workspaceRam > maxRamPerWorkspaceEnv) {
                throw new BadRequestException(format("The environment '%s' of the workspace configuration '%s' "
                                                     + "exceeds maximum available RAM per workspace. The maximum available value is '%dmb' "
                                                     + "but the received value is '%dmb'.",
                                                     environment.getName(),
                                                     config.getName(),
                                                     maxRamPerWorkspaceEnv,
                                                     workspaceRam));
            }
        }
    }
}
