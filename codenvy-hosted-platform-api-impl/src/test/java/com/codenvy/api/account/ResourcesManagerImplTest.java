/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
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
import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link com.codenvy.api.account.ResourcesManagerImpl}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class ResourcesManagerImplTest {
    private static final String ACCOUNT_ID = "accountId";

    private static final String PRIMARY_WORKSPACE_ID = "primaryWorkspace";
    private static final String EXTRA_WORKSPACE_ID   = "extraWorkspace";

    @Mock
    WorkspaceDao workspaceDao;

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

        resourcesManager = new ResourcesManagerImpl(workspaceDao);
    }

    @Test(expectedExceptions = ForbiddenException.class,
          expectedExceptionsMessageRegExp = "Workspace \\w* is not related to account \\w*")
    public void shouldThrowConflictExceptionIfAccountIsNotOwnerOfWorkspace() throws Exception {
        resourcesManager.redistributeResources(ACCOUNT_ID,
                                               Arrays.asList(DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                       .withResources(Collections.EMPTY_MAP)
                                                                       .withWorkspaceId(PRIMARY_WORKSPACE_ID),
                                                             DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                       .withResources(Collections.EMPTY_MAP)
                                                                       .withWorkspaceId("another_workspace")));
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Missed description of resources for workspace \\w*")
    public void shouldThrowConflictExceptionIfMissedResourcesForWorkspace() throws Exception {
        Map<String, String> primaryResources = new HashMap<>();
        primaryResources.put("RAM", "256");

        resourcesManager.redistributeResources(ACCOUNT_ID,
                                               Arrays.asList(DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                       .withWorkspaceId(PRIMARY_WORKSPACE_ID)
                                                                       .withResources(primaryResources)));
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Missed size of RAM in resources description for workspace \\w*")
    public void shouldThrowConflictExceptionIfMissedRAMInResources() throws Exception {
        resourcesManager.redistributeResources(ACCOUNT_ID,
                                               Arrays.asList(DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                       .withWorkspaceId(PRIMARY_WORKSPACE_ID),
                                                             DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                       .withWorkspaceId(EXTRA_WORKSPACE_ID)));
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Missed description of resources for workspace \\w*")
    public void shouldThrowConflictExceptionIfMissedResources() throws Exception {
        final UpdateResourcesDescriptor mock = Mockito.mock(UpdateResourcesDescriptor.class);
        when(mock.getWorkspaceId()).thenReturn(PRIMARY_WORKSPACE_ID);
        when(mock.getResources()).thenReturn(null);
        resourcesManager.redistributeResources(ACCOUNT_ID,
                                               Arrays.asList(mock,
                                                             DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                       .withWorkspaceId(EXTRA_WORKSPACE_ID)));
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Invalid size of RAM for workspace \\w*")
    public void shouldThrowConflictExceptionIfSizeOfRAMIsInvalid() throws Exception {
        Map<String, String> resources = new HashMap<>();
        resources.put("RAM", "215qw");

        resourcesManager.redistributeResources(ACCOUNT_ID,
                                               Arrays.asList(DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                       .withWorkspaceId(PRIMARY_WORKSPACE_ID)
                                                                       .withResources(resources),
                                                             DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                       .withWorkspaceId(EXTRA_WORKSPACE_ID)
                                                                       .withResources(resources)));
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Size of RAM for workspace \\w* is a negative number")
    public void shouldThrowConflictExceptionIfSizeOfRAMIsNegativeNumber() throws Exception {
        Map<String, String> primaryResources = new HashMap<>();
        primaryResources.put("RAM", "125");

        Map<String, String> extraResources = new HashMap<>();
        extraResources.put("RAM", "-203");

        resourcesManager.redistributeResources(ACCOUNT_ID, Arrays.asList(DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                                   .withWorkspaceId(PRIMARY_WORKSPACE_ID)
                                                                                   .withResources(primaryResources),
                                                                         DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                                   .withWorkspaceId(EXTRA_WORKSPACE_ID)
                                                                                   .withResources(extraResources)));
    }

    @Test
    public void shouldRedistributeResources() throws Exception {
        Map<String, String> resources = new HashMap<>();
        resources.put("RAM", "512");

        resourcesManager.redistributeResources(ACCOUNT_ID,
                                               Arrays.asList(DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                       .withWorkspaceId(PRIMARY_WORKSPACE_ID)
                                                                       .withResources(resources),
                                                             DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                       .withWorkspaceId(EXTRA_WORKSPACE_ID)
                                                                       .withResources(resources)));

        ArgumentCaptor<Workspace> workspaceArgumentCaptor = ArgumentCaptor.forClass(Workspace.class);
        verify(workspaceDao, times(2)).update(workspaceArgumentCaptor.capture());

        for (Workspace workspace : workspaceArgumentCaptor.getAllValues()) {
            assertEquals(workspace.getAttributes().get(Constants.RUNNER_MAX_MEMORY_SIZE), "512");
        }
    }

    @Test
    public void shouldBeAbleToAddMemoryWithoutLimitation() throws Exception {
        Map<String, String> primaryResources = new HashMap<>();
        primaryResources.put("RAM", String.valueOf(Integer.MAX_VALUE));

        Map<String, String> extraResources = new HashMap<>();
        extraResources.put("RAM", "0");

        resourcesManager.redistributeResources(ACCOUNT_ID, Arrays.asList(DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                                   .withWorkspaceId(PRIMARY_WORKSPACE_ID)
                                                                                   .withResources(primaryResources),
                                                                         DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                                   .withWorkspaceId(EXTRA_WORKSPACE_ID)
                                                                                   .withResources(extraResources)));

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
