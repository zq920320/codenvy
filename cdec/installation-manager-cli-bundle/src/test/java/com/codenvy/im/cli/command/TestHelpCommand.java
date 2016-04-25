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
package com.codenvy.im.cli.command;

import com.codenvy.cli.command.builtin.MultiRemoteCodenvy;
import com.codenvy.im.facade.IMArtifactLabeledFacade;

import org.apache.felix.service.command.CommandSession;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertTrue;

/** @author Anatoliy Bazko */
public class TestHelpCommand extends AbstractTestCommand {
    private AbstractIMCommand spyCommand;

    @Mock
    private IMArtifactLabeledFacade service;
    @Mock
    private CommandSession          commandSession;
    @Mock
    private MultiRemoteCodenvy      multiRemoteCodenvy;

    @BeforeMethod
    public void initMocks() throws IOException {
        MockitoAnnotations.initMocks(this);

        spyCommand = spy(new HelpCommand());
        spyCommand.facade = service;
        doReturn(multiRemoteCodenvy).when(spyCommand).getMultiRemoteCodenvy();
        doReturn("").when(multiRemoteCodenvy).listRemotes();

        performBaseMocks(spyCommand, true);
    }

    @Test
    public void testHelp() throws Exception {
        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, commandSession);
        CommandInvoker.Result result = commandInvoker.invoke();
        String output = removeAnsi(result.getOutputStream());

        assertTrue(output.contains("nullCOMMANDS\n"
                                         + "build          Build a project \n"
                                         + "clone-local    Clone a remote Codenvy project to a local directory \n"
                                         + "create-factory Create a factory \n"
                                         + "create-project Create a project \n"
                                         + "delete-factory Delete a factory \n"
                                         + "delete-project Delete a project \n"
                                         + "exit           Exit of the shell \n"
                                         + "im-add-node    Add new Codenvy node such as builder or runner\n"
                                         + "im-backup      Backup all Codenvy data\n"
                                         + "im-config      Get installation manager configuration\n"
                                         + "im-download    Download artifacts or print the list of installed ones\n"
                                         + "im-install     Install, update artifact or print the list of already installed ones\n"
                                         + "im-password    Change Codenvy admin password\n"
                                         + "im-remove-node Remove Codenvy node\n"
                                         + "im-restore     Restore Codenvy data\n"
                                         + "im-version     Print the list of available latest versions and installed ones\n"
                                         + "info           Display information for a project, runner, or builder \n"
                                         + "list           List workspaces, projects and processes \n"
                                         + "list-factories List factories \n"
                                         + "login          Login to a remote Codenvy cloud \n"
                                         + "logout         Logout to a remote Codenvy cloud \n"
                                         + "logs           Display output logs for a runner or builder \n"
                                         + "open           Starts a browser session to access a project, builder or runner \n"
                                         + "privacy        Set privacy of a project \n"
                                         + "pull           Update project sync point directory created by clone-local \n"
                                         + "push           Push local project changes back to Codenvy \n"
                                         + "remote         Add or remove remote Codenvy cloud references \n"
                                         + "run            Run a project \n"
                                         + "stop           Stop one or more runner processes \n"
                                         + "update-factory Update a factory \n"), "Actual output: " + output);
    }
}
