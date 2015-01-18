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
package com.codenvy.api.account;

import com.codenvy.api.account.server.ResourcesManager;
import com.codenvy.api.account.server.dao.Account;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.shared.dto.UpdateResourcesDescriptor;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.runner.internal.Constants;
import com.codenvy.api.workspace.server.dao.Workspace;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.codenvy.dto.server.DtoFactory;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link com.codenvy.api.account.ResourcesManagerImpl}
 *
 * @author Sergii Leschenko
 * @author Max Shaposhnik
 */
@Listeners(MockitoTestNGListener.class)
public class ResourcesManagerImplTest {
    private static final String ACCOUNT_ID = "accountId";

    private static final String PRIMARY_WORKSPACE_ID = "primaryWorkspace";
    private static final String EXTRA_WORKSPACE_ID   = "extraWorkspace";

    @Mock
    WorkspaceDao workspaceDao;

    @Mock
    AccountDao accountDao;

    ResourcesManager resourcesManager;

    @BeforeMethod
    public void setUp() throws NotFoundException, ServerException {
        Workspace primaryWorkspace = new Workspace().withAccountId(ACCOUNT_ID)
                                                    .withId(PRIMARY_WORKSPACE_ID);

        Map<String, String> attributesForExtraWs = new HashMap<>();
        attributesForExtraWs.put("codenvy:role", "extra");
        Workspace extraWorkspace = new Workspace().withAccountId(ACCOUNT_ID)
                                                  .withId(EXTRA_WORKSPACE_ID)
                                                  .withAttributes(attributesForExtraWs);

        when(workspaceDao.getByAccount(ACCOUNT_ID)).thenReturn(Arrays.asList(primaryWorkspace, extraWorkspace));
        when(accountDao.getById(anyString())).thenReturn(new Account().withId(ACCOUNT_ID).withName("testName"));

        resourcesManager = new ResourcesManagerImpl(accountDao, workspaceDao);
    }

    @Test(expectedExceptions = ForbiddenException.class,
          expectedExceptionsMessageRegExp = "Workspace \\w* is not related to account \\w*")
    public void shouldThrowConflictExceptionIfAccountIsNotOwnerOfWorkspace() throws Exception {
        resourcesManager.redistributeResources(ACCOUNT_ID,
                                               Arrays.asList(DtoFactory.getInstance().createDto(
                                                                     UpdateResourcesDescriptor.class)
                                                                       .withWorkspaceId(PRIMARY_WORKSPACE_ID),
                                                             DtoFactory.getInstance().createDto(
                                                                     UpdateResourcesDescriptor.class)
                                                                       .withWorkspaceId("another_workspace")));
    }

    @Test(expectedExceptions = ConflictException.class,
            expectedExceptionsMessageRegExp = "Missed description of resources for workspace \\w*")
    public void shouldThrowConflictExceptionIfMissedResources() throws Exception {
        resourcesManager.redistributeResources(ACCOUNT_ID,
                                               Arrays.asList(DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                       .withWorkspaceId(PRIMARY_WORKSPACE_ID),
                                                             DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                       .withWorkspaceId(EXTRA_WORKSPACE_ID)));
    }



    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Size of RAM for workspace \\w* is a negative number")
    public void shouldThrowConflictExceptionIfSizeOfRAMIsNegativeNumber() throws Exception {
        resourcesManager.redistributeResources(ACCOUNT_ID, Arrays.asList(DtoFactory.getInstance().createDto(
                UpdateResourcesDescriptor.class)
                                                                                   .withWorkspaceId(
                                                                                           EXTRA_WORKSPACE_ID)
                                                                                   .withRunnerRam(-256)));
    }

    @Test(expectedExceptions = ConflictException.class,
            expectedExceptionsMessageRegExp = "Builder timeout for workspace \\w* is a negative number")
    public void shouldThrowConflictExceptionIfSizeOfBuilderTimeoutIsNegativeNumber() throws Exception {
        resourcesManager.redistributeResources(ACCOUNT_ID, Arrays.asList(DtoFactory.getInstance().createDto(
                                                                     UpdateResourcesDescriptor.class)
                                                                       .withWorkspaceId(
                                                                               EXTRA_WORKSPACE_ID)
                                                                       .withBuilderTimeout(-5)));
    }

    @Test(expectedExceptions = ConflictException.class,
            expectedExceptionsMessageRegExp = "Runner timeout for workspace \\w* is a negative number")
    public void shouldThrowConflictExceptionIfSizeOfRunnerTimeoutIsNegativeNumber() throws Exception {
        resourcesManager.redistributeResources(ACCOUNT_ID,

                                               Arrays.asList(DtoFactory.getInstance().createDto(
                                                                     UpdateResourcesDescriptor.class)
                                                                       .withWorkspaceId(
                                                                               PRIMARY_WORKSPACE_ID).
                                                                               withRunnerTimeout(-1), //ok
                                                             DtoFactory.getInstance().createDto(
                                                                     UpdateResourcesDescriptor.class)
                                                                       .withWorkspaceId(
                                                                               EXTRA_WORKSPACE_ID)
                                                                       .withRunnerTimeout(-5))); // not ok
    }


    @Test(expectedExceptions = ConflictException.class,
            expectedExceptionsMessageRegExp = "Size of RAM for workspace \\w* has a 4096 MB limit.")
    public void shouldThrowConflictExceptionIfSizeOfRAMIsTooBigForCommunityAccountNumber() throws Exception {
        resourcesManager.redistributeResources(ACCOUNT_ID, Arrays.asList(DtoFactory.getInstance().createDto(
                                                                                 UpdateResourcesDescriptor.class)
                                                                                   .withWorkspaceId(
                                                                                           EXTRA_WORKSPACE_ID)
                                                                                   .withRunnerRam(5000)));
    }


    @Test
    public void shouldRedistributeRAMResources() throws Exception {
        resourcesManager.redistributeResources(ACCOUNT_ID,
                                               Arrays.asList(DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                       .withWorkspaceId(PRIMARY_WORKSPACE_ID)
                                                                       .withRunnerRam(512),
                                                             DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                       .withWorkspaceId(EXTRA_WORKSPACE_ID)
                                                                       .withRunnerRam(512)));

        ArgumentCaptor<Workspace> workspaceArgumentCaptor = ArgumentCaptor.forClass(Workspace.class);
        verify(workspaceDao, times(2)).update(workspaceArgumentCaptor.capture());

        for (Workspace workspace : workspaceArgumentCaptor.getAllValues()) {
            assertEquals(workspace.getAttributes().get(Constants.RUNNER_MAX_MEMORY_SIZE), "512");
        }
    }

    @Test
    public void shouldRedistributeRunnerLimitResources() throws Exception {
        resourcesManager.redistributeResources(ACCOUNT_ID,
                                               Arrays.asList(DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                       .withWorkspaceId(PRIMARY_WORKSPACE_ID)
                                                                       .withRunnerTimeout(20),
                                                             DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                       .withWorkspaceId(EXTRA_WORKSPACE_ID)
                                                                       .withRunnerTimeout(20)));

        ArgumentCaptor<Workspace> workspaceArgumentCaptor = ArgumentCaptor.forClass(Workspace.class);
        verify(workspaceDao, times(2)).update(workspaceArgumentCaptor.capture());

        for (Workspace workspace : workspaceArgumentCaptor.getAllValues()) {
            assertEquals(workspace.getAttributes().get(Constants.RUNNER_LIFETIME), "20");
        }
    }

    @Test
    public void shouldRedistributeBuilderLimitResources() throws Exception {
        resourcesManager.redistributeResources(ACCOUNT_ID,
                                               Arrays.asList(DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                       .withWorkspaceId(PRIMARY_WORKSPACE_ID)
                                                                       .withBuilderTimeout(10),
                                                             DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                       .withWorkspaceId(EXTRA_WORKSPACE_ID)
                                                                       .withBuilderTimeout(10)));

        ArgumentCaptor<Workspace> workspaceArgumentCaptor = ArgumentCaptor.forClass(Workspace.class);
        verify(workspaceDao, times(2)).update(workspaceArgumentCaptor.capture());

        for (Workspace workspace : workspaceArgumentCaptor.getAllValues()) {
            assertEquals(workspace.getAttributes().get(com.codenvy.api.builder.internal.Constants.BUILDER_EXECUTION_TIME), "10");
        }
    }

    @Test
    public void shouldBeAbleToAddMemoryWithoutLimitation() throws Exception {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("codenvy:paid", "true");
        when(accountDao.getById(anyString())).thenReturn(
                new Account().withId(ACCOUNT_ID).withName("testName").withAttributes(attributes));

        resourcesManager.redistributeResources(ACCOUNT_ID, Arrays.asList(DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                                   .withWorkspaceId(PRIMARY_WORKSPACE_ID)
                                                                                   .withRunnerRam(Integer.MAX_VALUE),
                                                                         DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                                   .withWorkspaceId(EXTRA_WORKSPACE_ID)
                                                                                   .withRunnerRam(0)));

        ArgumentCaptor<Workspace> workspaceArgumentCaptor = ArgumentCaptor.forClass(Workspace.class);
        verify(workspaceDao, times(2)).update(workspaceArgumentCaptor.capture());

        for (Workspace workspace : workspaceArgumentCaptor.getAllValues()) {
            switch (workspace.getId()) {
                case PRIMARY_WORKSPACE_ID:
                    assertEquals(workspace.getAttributes().get(Constants.RUNNER_MAX_MEMORY_SIZE), String.valueOf(Integer.MAX_VALUE));
                    break;
                case EXTRA_WORKSPACE_ID:
                    assertEquals(workspace.getAttributes().get(Constants.RUNNER_MAX_MEMORY_SIZE), "0");
                    break;
            }
        }
    }
}
