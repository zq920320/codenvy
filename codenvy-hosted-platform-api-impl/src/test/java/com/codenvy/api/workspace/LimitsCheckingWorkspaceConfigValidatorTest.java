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
package com.codenvy.api.workspace;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.testng.annotations.Test;

import static com.codenvy.api.workspace.TestObjects.createConfig;

/**
 * Tests of {@link LimitsCheckingWorkspaceConfigValidator}.
 *
 * @author Yevhenii Voevodin
 */
public class LimitsCheckingWorkspaceConfigValidatorTest {

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "The maximum RAM per workspace is set to '2048mb' and you requested '3072mb'. " +
                                            "This value is set by your admin with the 'limits.workspace.env.ram' property")
    public void shouldNotBeAbleToCreateWorkspaceWhichExceedsRamLimit() throws BadRequestException {
        final WorkspaceConfig config = createConfig("3gb");

        new LimitsCheckingWorkspaceConfigValidator("2gb").validate(config);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "The maximum RAM per workspace is set to '2048mb' and you requested '2304mb'. " +
                                            "This value is set by your admin with the 'limits.workspace.env.ram' property")
    public void shouldNotBeAbleToCreateWorkspaceWithMultipleMachinesWhichExceedsRamLimit() throws BadRequestException {
        final WorkspaceConfig config = createConfig("1gb", "1gb", "256mb");

        new LimitsCheckingWorkspaceConfigValidator("2gb").validate(config);
    }

    @Test
    public void shouldBeAbleToCreateWorkspaceWithMultipleMachinesWhichDoesNotExceedRamLimit() throws BadRequestException {
        final WorkspaceConfig config = createConfig("1gb", "1gb", "256mb");

        new LimitsCheckingWorkspaceConfigValidator("3gb").validate(config);
    }
}
