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
import com.codenvy.api.account.server.dao.Account;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.server.dao.Subscription;
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

import java.util.ArrayList;
import java.util.Arrays;
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
    AccountDao   accountDao;
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

        Account account = new Account().withId(ACCOUNT_ID);
        when(accountDao.getById(ACCOUNT_ID)).thenReturn(account);

        Map<String, String> accountSubscription = new HashMap<>();
        accountSubscription.put("RAM", "1GB");
        Subscription subscription = new Subscription().withAccountId(ACCOUNT_ID)
                                                      .withServiceId("Saas")
                                                      .withProperties(accountSubscription);

        when(accountDao.getSubscriptions(ACCOUNT_ID, "Saas")).thenReturn(Arrays.asList(subscription));

        resourcesManager = new ResourcesManagerImpl(accountDao, workspaceDao);
    }

    @Test(expectedExceptions = ForbiddenException.class,
          expectedExceptionsMessageRegExp = "Workspace \\w* is not related to account \\w*")
    public void shouldThrowConflictExceptionIfAccountIsNotOwnerOfWorkspace() throws Exception {
        Map<String, String> resources = new HashMap<>();
        resources.put("RAM", "2048");

        resourcesManager.redistributeResources(ACCOUNT_ID, Arrays.asList(DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                                   .withResources(resources)
                                                                                   .withWorkspaceId(PRIMARY_WORKSPACE_ID),
                                                                         DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                                   .withResources(resources)
                                                                                   .withWorkspaceId("another_workspace")));
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Failed to allocate \\d*mb of RAM. Your account is provisioned with \\d*mb of RAM")
    public void shouldThrowConflictExceptionIfUseRamMoreThanAllowed() throws Exception {
        Map<String, String> resources = new HashMap<>();
        resources.put("RAM", "1024");

        resourcesManager.redistributeResources(ACCOUNT_ID, Arrays.asList(DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                                   .withWorkspaceId(PRIMARY_WORKSPACE_ID)
                                                                                   .withResources(resources),
                                                                         DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                                   .withWorkspaceId(EXTRA_WORKSPACE_ID)
                                                                                   .withResources(resources)));
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Users who have community subscription can't distribute resources")
    public void shouldThrowConflictExceptionIfUserHasCommunitySubscription() throws Exception {
        Map<String, String> accountSubscription = new HashMap<>();
        accountSubscription.put("RAM", "256MB");
        accountSubscription.put("Package", "Community");
        Subscription subscription = new Subscription().withAccountId(ACCOUNT_ID)
                                                      .withServiceId("Saas")
                                                      .withProperties(accountSubscription);

        when(accountDao.getSubscriptions(ACCOUNT_ID, "Saas")).thenReturn(Arrays.asList(subscription));

        Map<String, String> resources = new HashMap<>();
        resources.put("RAM", "2048");

        resourcesManager.redistributeResources(ACCOUNT_ID, Arrays.asList(DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                                   .withWorkspaceId(PRIMARY_WORKSPACE_ID)
                                                                                   .withResources(resources),
                                                                         DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                                   .withWorkspaceId(EXTRA_WORKSPACE_ID)
                                                                                   .withResources(resources)));
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Account hasn't Saas subscription")
    public void shouldThrowConflictExceptionIfUserHasNotSaasSubscription() throws Exception {
        when(accountDao.getSubscriptions(ACCOUNT_ID, "Saas")).thenReturn(new ArrayList<Subscription>());

        Map<String, String> resources = new HashMap<>();
        resources.put("RAM", "2048");

        resourcesManager.redistributeResources(ACCOUNT_ID, Arrays.asList(DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                                   .withWorkspaceId(PRIMARY_WORKSPACE_ID)
                                                                                   .withResources(resources),
                                                                         DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                                   .withWorkspaceId(EXTRA_WORKSPACE_ID)
                                                                                   .withResources(resources)));
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Missed description of resources for workspace \\w*")
    public void shouldThrowConflictExceptionIfMissedResourcesForWorkspace() throws Exception {
        Map<String, String> primaryResources = new HashMap<>();
        primaryResources.put("RAM", "256");

        resourcesManager.redistributeResources(ACCOUNT_ID, Arrays.asList(DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                                   .withWorkspaceId(PRIMARY_WORKSPACE_ID)
                                                                                   .withResources(primaryResources)));
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Missed size of RAM in resources description for workspace \\w*")
    public void shouldThrowConflictExceptionIfMissedResources() throws Exception {
        resourcesManager.redistributeResources(ACCOUNT_ID, Arrays.asList(DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                                   .withWorkspaceId(PRIMARY_WORKSPACE_ID),
                                                                         DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                                   .withWorkspaceId(EXTRA_WORKSPACE_ID)));
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Account has more than 1 Saas subscription")
    public void shouldThrowConflictExceptionIfUserHasMore1SaasSubscription() throws Exception {
        Map<String, String> accountSubscription = new HashMap<>();
        accountSubscription.put("RAM", "256MB");
        accountSubscription.put("Package", "Community");
        Subscription subscription = new Subscription().withAccountId(ACCOUNT_ID)
                                                      .withServiceId("Saas")
                                                      .withProperties(accountSubscription);
        when(accountDao.getSubscriptions(ACCOUNT_ID, "Saas")).thenReturn(Arrays.asList(subscription, subscription));

        Map<String, String> resources = new HashMap<>();
        resources.put("RAM", "2048");

        resourcesManager.redistributeResources(ACCOUNT_ID, Arrays.asList(DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                                   .withWorkspaceId(PRIMARY_WORKSPACE_ID)
                                                                                   .withResources(resources),
                                                                         DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                                   .withWorkspaceId(EXTRA_WORKSPACE_ID)
                                                                                   .withResources(resources)));
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Invalid size of RAM for workspace \\w*")
    public void shouldThrowConflictExceptionIfSizeOfRAMIsInvalid() throws Exception {
        Map<String, String> resources = new HashMap<>();
        resources.put("RAM", "215qw");

        resourcesManager.redistributeResources(ACCOUNT_ID, Arrays.asList(DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                                   .withWorkspaceId(PRIMARY_WORKSPACE_ID)
                                                                                   .withResources(resources),
                                                                         DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                                   .withWorkspaceId(EXTRA_WORKSPACE_ID)
                                                                                   .withResources(resources)));
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Primary workspace is not found for distribution of the remaining RAM")
    public void shouldThrowConflictExceptionIfPrimaryWorkspaceDoesNotExist() throws Exception {
        Map<String, String> resources = new HashMap<>();
        resources.put("RAM", "512");

        Map<String, String> attributesForExtraWs = new HashMap<>();
        attributesForExtraWs.put("codenvy:role", "extra");

        Workspace workspace1 = new Workspace().withAccountId(ACCOUNT_ID)
                                              .withId(PRIMARY_WORKSPACE_ID)
                                              .withAttributes(attributesForExtraWs);
        when(workspaceDao.getByAccount(ACCOUNT_ID)).thenReturn(Arrays.asList(workspace1));

        resourcesManager.redistributeResources(ACCOUNT_ID, Arrays.asList(DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                                   .withWorkspaceId(PRIMARY_WORKSPACE_ID)
                                                                                   .withResources(resources)));
    }

    @Test
    public void shouldRedistributeResources() throws Exception {
        Map<String, String> resources = new HashMap<>();
        resources.put("RAM", "512");

        resourcesManager.redistributeResources(ACCOUNT_ID, Arrays.asList(DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
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
    public void shouldRedistributeResourcesAndAddedRemainingRAMToPrimaryWorkspace() throws Exception {
        Map<String, String> primaryResources = new HashMap<>();
        primaryResources.put("RAM", "256");

        Map<String, String> extraResources = new HashMap<>();
        extraResources.put("RAM", "256");

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
                    assertEquals(workspace.getAttributes().get(Constants.RUNNER_MAX_MEMORY_SIZE), "768");
                    break;
                case EXTRA_WORKSPACE_ID:
                    assertEquals(workspace.getAttributes().get(Constants.RUNNER_MAX_MEMORY_SIZE), "256");
                    break;
            }
        }
    }
}
