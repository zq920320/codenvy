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
import com.codenvy.api.account.shared.dto.AccountMetrics;
import com.codenvy.api.account.shared.dto.UpdateResourcesDescriptor;
import com.codenvy.api.account.shared.dto.WorkspaceMetrics;
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

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
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

    private static final Long    FREE_MEMORY         = 61440L;
    private static final Integer MAX_LIMIT           = 4096;
    private static final String  FIRST_WORKSPACE_ID  = "firstWorkspace";
    private static final String  SECOND_WORKSPACE_ID = "secondWorkspace";

    @Mock
    WorkspaceDao workspaceDao;

    @Mock
    AccountDao accountDao;

    @Mock
    MeterBasedStorage meterBasedStorage;

    ResourcesManager resourcesManager;

    @BeforeMethod
    public void setUp() throws NotFoundException, ServerException {
        Map<String, String> firstAttributes = new HashMap<>();
        firstAttributes.put(Constants.RUNNER_MAX_MEMORY_SIZE, "1024");
        Workspace firstWorkspace = new Workspace().withAccountId(ACCOUNT_ID)
                                                  .withId(FIRST_WORKSPACE_ID)
                                                  .withAttributes(firstAttributes);

        Map<String, String> secondAttributes = new HashMap<>();
        secondAttributes.put(Constants.RUNNER_MAX_MEMORY_SIZE, "2048");
        Workspace secondWorkspace = new Workspace().withAccountId(ACCOUNT_ID)
                                                   .withId(SECOND_WORKSPACE_ID)
                                                   .withAttributes(secondAttributes);

        when(workspaceDao.getByAccount(ACCOUNT_ID)).thenReturn(Arrays.asList(firstWorkspace, secondWorkspace));
        when(accountDao.getById(anyString())).thenReturn(new Account().withId(ACCOUNT_ID).withName("accountName"));

        resourcesManager = new ResourcesManagerImpl(FREE_MEMORY, MAX_LIMIT, accountDao, workspaceDao, meterBasedStorage);
    }

    @Test(expectedExceptions = ForbiddenException.class,
          expectedExceptionsMessageRegExp = "Workspace \\w* is not related to account \\w*")
    public void shouldThrowConflictExceptionIfAccountIsNotOwnerOfWorkspace() throws Exception {
        resourcesManager.redistributeResources(ACCOUNT_ID,
                                               Arrays.asList(DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                       .withWorkspaceId(FIRST_WORKSPACE_ID)
                                                                       .withRunnerRam(1024),
                                                             DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                       .withWorkspaceId("another_workspace")
                                                                       .withRunnerRam(1024)));
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Missed description of resources for workspace \\w*")
    public void shouldThrowConflictExceptionIfMissedResources() throws Exception {
        resourcesManager.redistributeResources(ACCOUNT_ID,
                                               Arrays.asList(DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                       .withWorkspaceId(FIRST_WORKSPACE_ID),
                                                             DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                       .withWorkspaceId(SECOND_WORKSPACE_ID)));
    }


    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Size of RAM for workspace \\w* is a negative number")
    public void shouldThrowConflictExceptionIfSizeOfRAMIsNegativeNumber() throws Exception {
        resourcesManager.redistributeResources(ACCOUNT_ID,
                                               Arrays.asList(DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                       .withWorkspaceId(SECOND_WORKSPACE_ID)
                                                                       .withRunnerRam(-256)));
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Builder timeout for workspace \\w* is a negative number")
    public void shouldThrowConflictExceptionIfSizeOfBuilderTimeoutIsNegativeNumber() throws Exception {
        resourcesManager.redistributeResources(ACCOUNT_ID, Arrays.asList(DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                                   .withWorkspaceId(SECOND_WORKSPACE_ID)
                                                                                   .withBuilderTimeout(-5)));
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Runner timeout for workspace \\w* is a negative number")
    public void shouldThrowConflictExceptionIfSizeOfRunnerTimeoutIsNegativeNumber() throws Exception {
        resourcesManager.redistributeResources(ACCOUNT_ID,
                                               Arrays.asList(DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                       .withWorkspaceId(FIRST_WORKSPACE_ID)
                                                                       .withRunnerTimeout(-1), //ok
                                                             DtoFactory.getInstance().createDto(
                                                                     UpdateResourcesDescriptor.class)
                                                                       .withWorkspaceId(SECOND_WORKSPACE_ID)
                                                                       .withRunnerTimeout(-5))); // not ok
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Size of RAM for workspace \\w* has a 4096 MB limit.")
    public void shouldThrowConflictExceptionIfSizeOfRAMIsTooBigForCommunityAccountNumber() throws Exception {
        resourcesManager.redistributeResources(ACCOUNT_ID, Arrays.asList(DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                                   .withWorkspaceId(SECOND_WORKSPACE_ID)
                                                                                   .withRunnerRam(5000)));
    }

    @Test
    public void shouldRedistributeRAMResources() throws Exception {
        resourcesManager.redistributeResources(ACCOUNT_ID,
                                               Arrays.asList(DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                       .withWorkspaceId(FIRST_WORKSPACE_ID)
                                                                       .withRunnerRam(512),
                                                             DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                       .withWorkspaceId(SECOND_WORKSPACE_ID)
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
                                                                       .withWorkspaceId(FIRST_WORKSPACE_ID)
                                                                       .withRunnerTimeout(20),
                                                             DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                       .withWorkspaceId(SECOND_WORKSPACE_ID)
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
                                                                       .withWorkspaceId(FIRST_WORKSPACE_ID)
                                                                       .withBuilderTimeout(10),
                                                             DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                       .withWorkspaceId(SECOND_WORKSPACE_ID)
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
        when(accountDao.getById(anyString())).thenReturn(new Account().withId(ACCOUNT_ID)
                                                                      .withName("testName")
                                                                      .withAttributes(attributes));

        resourcesManager.redistributeResources(ACCOUNT_ID, Arrays.asList(DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                                   .withWorkspaceId(FIRST_WORKSPACE_ID)
                                                                                   .withRunnerRam(Integer.MAX_VALUE),
                                                                         DtoFactory.getInstance().createDto(UpdateResourcesDescriptor.class)
                                                                                   .withWorkspaceId(SECOND_WORKSPACE_ID)
                                                                                   .withRunnerRam(0)));

        ArgumentCaptor<Workspace> workspaceArgumentCaptor = ArgumentCaptor.forClass(Workspace.class);
        verify(workspaceDao, times(2)).update(workspaceArgumentCaptor.capture());

        for (Workspace workspace : workspaceArgumentCaptor.getAllValues()) {
            switch (workspace.getId()) {
                case FIRST_WORKSPACE_ID:
                    assertEquals(workspace.getAttributes().get(Constants.RUNNER_MAX_MEMORY_SIZE), String.valueOf(Integer.MAX_VALUE));
                    break;
                case SECOND_WORKSPACE_ID:
                    assertEquals(workspace.getAttributes().get(Constants.RUNNER_MAX_MEMORY_SIZE), "0");
                    break;
            }
        }
    }

    @Test
    public void shouldBeAbleToGetAccountMetrics() throws Exception {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("codenvy:paid", "false");
        when(accountDao.getById(anyString())).thenReturn(new Account().withId(ACCOUNT_ID)
                                                                      .withName("testName")
                                                                      .withAttributes(attributes));

        Map<String, Long> usedReport = new HashMap<>();
        usedReport.put(FIRST_WORKSPACE_ID, 1024L);
        usedReport.put(SECOND_WORKSPACE_ID, 512L);
        when(meterBasedStorage.getMemoryUsedReport(eq(ACCOUNT_ID), anyLong(), anyLong())).thenReturn(usedReport);

        final AccountMetrics accountMetrics = resourcesManager.getAccountMetrics(ACCOUNT_ID);

        assertEquals(accountMetrics.isPremium(), Boolean.FALSE);
        assertEquals(accountMetrics.getMaxWorkspaceMemoryLimit(), MAX_LIMIT);

        assertEquals(accountMetrics.getFreeMemory(), FREE_MEMORY);
        assertEquals(accountMetrics.getUsedMemoryInCurrentBillingPeriod(), new Long(1536));
        assertEquals(accountMetrics.getWorkspaceMetrics().size(), 2);
        for (WorkspaceMetrics workspaceMetrics : accountMetrics.getWorkspaceMetrics()) {
            switch (workspaceMetrics.getWorkspaceId()) {
                case FIRST_WORKSPACE_ID:
                    assertEquals(workspaceMetrics.getUsedMemoryInCurrentBillingPeriod(), new Long(1024));
                    assertEquals(workspaceMetrics.getWorkspaceMemoryLimit(), new Integer(1024));
                    break;
                case SECOND_WORKSPACE_ID:
                    assertEquals(workspaceMetrics.getUsedMemoryInCurrentBillingPeriod(), new Long(512));
                    assertEquals(workspaceMetrics.getWorkspaceMemoryLimit(), new Integer(2048));
                    break;
            }
        }
    }

    @Test
    public void shouldBeAbleToGetAccountMetricsForPremiumAccount() throws Exception {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("codenvy:paid", "true");
        when(accountDao.getById(anyString())).thenReturn(new Account().withId(ACCOUNT_ID)
                                                                      .withName("testName")
                                                                      .withAttributes(attributes));

        final AccountMetrics accountMetrics = resourcesManager.getAccountMetrics(ACCOUNT_ID);

        assertEquals(accountMetrics.getMaxWorkspaceMemoryLimit(), new Integer(-1));
        assertEquals(accountMetrics.isPremium(), Boolean.TRUE);
    }
}
