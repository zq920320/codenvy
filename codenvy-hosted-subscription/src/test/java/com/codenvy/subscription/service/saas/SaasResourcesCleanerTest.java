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
package com.codenvy.subscription.service.saas;

import com.codenvy.api.core.notification.EventService;
import com.codenvy.api.workspace.server.dao.Workspace;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.codenvy.workspace.event.DeleteWorkspaceEvent;

import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link com.codenvy.subscription.service.saas.SaasResourcesCleaner}
 *
 * @author Sergii Leschenko
 */
@Listeners(value = {MockitoTestNGListener.class})
public class SaasResourcesCleanerTest {
    @Mock
    private WorkspaceDao workspaceDao;
    @Mock
    private EventService eventService;

    private SaasResourcesCleaner resourcesCleaner;

    private final String PRIMARY_WORKSPACE_ID = "primaryWsId";
    private final String EXTRA_WORKSPACE_ID   = "extraWsId";
    private final String ACCOUNT_ID           = "accId";


    @BeforeMethod
    public void setUp() throws Exception {
        resourcesCleaner = new SaasResourcesCleaner(workspaceDao,
                                                    eventService,
                                                    false);
    }

    @Test
    public void shouldDoNothingWhenOnPremisesPackaging() throws Exception {
        Workspace primaryWorkspace = createPrimaryWorkspace();

        resourcesCleaner = new SaasResourcesCleaner(workspaceDao,
                                                    eventService,
                                                    true);

        resourcesCleaner.onEvent(new DeleteWorkspaceEvent(primaryWorkspace));

        verify(workspaceDao, times(0)).update((Workspace)anyObject());
    }

    @Test
    public void shouldDoNothingWhenDeletedPrimaryWorkspace() throws Exception {
        Workspace primaryWorkspace = createPrimaryWorkspace();

        resourcesCleaner.onEvent(new DeleteWorkspaceEvent(primaryWorkspace));

        verify(workspaceDao, times(0)).update((Workspace)anyObject());
    }

    @Test
    public void shouldMoveRAMToPrimaryWorkspaceWhenDeletedExtraWorkspace() throws Exception {
        Workspace primaryWorkspace = createPrimaryWorkspace();
        primaryWorkspace.getAttributes().put("codenvy:runner_ram", "256");

        Workspace extraWorkspace = createExtraWorkspace();
        extraWorkspace.getAttributes().put("codenvy:runner_ram", "256");

        when(workspaceDao.getByAccount(ACCOUNT_ID)).thenReturn(Arrays.asList(primaryWorkspace, extraWorkspace));

        resourcesCleaner.onEvent(new DeleteWorkspaceEvent(extraWorkspace));

        verify(workspaceDao, times(1)).update(argThat(new ArgumentMatcher<Workspace>() {
            @Override
            public boolean matches(Object argument) {
                Workspace workspace = (Workspace)argument;
                return PRIMARY_WORKSPACE_ID.equals(workspace.getId())
                       && "512".equals(workspace.getAttributes().get("codenvy:runner_ram"));
            }
        }));
    }

    @Test
    public void shouldSetRAMToPrimaryWorkspaceWhenDeletedExtraWorkspace() throws Exception {
        Workspace primaryWorkspace = createPrimaryWorkspace();

        Workspace extraWorkspace = createExtraWorkspace();
        extraWorkspace.getAttributes().put("codenvy:runner_ram", "256");

        when(workspaceDao.getByAccount(ACCOUNT_ID)).thenReturn(Arrays.asList(primaryWorkspace, extraWorkspace));

        resourcesCleaner.onEvent(new DeleteWorkspaceEvent(extraWorkspace));

        verify(workspaceDao, times(1)).update(argThat(new ArgumentMatcher<Workspace>() {
            @Override
            public boolean matches(Object argument) {
                Workspace workspace = (Workspace)argument;
                return PRIMARY_WORKSPACE_ID.equals(workspace.getId())
                       && "256".equals(workspace.getAttributes().get("codenvy:runner_ram"));
            }
        }));
    }

    @Test
    public void shouldThrowExceptionWhenPrimaryWorkspaceIsAbsent() throws Exception {
        Workspace extraWorkspace = createExtraWorkspace();
        extraWorkspace.getAttributes().put("codenvy:runner_ram", "256");

        when(workspaceDao.getByAccount(ACCOUNT_ID)).thenReturn(Arrays.asList(extraWorkspace));

        resourcesCleaner.onEvent(new DeleteWorkspaceEvent(extraWorkspace));

        verify(workspaceDao, times(0)).update((Workspace)anyObject());
    }

    @Test
    public void shouldThrowExceptionWhenExtraWorkspaceHasInvalidRAM() throws Exception {
        Workspace primaryWorkspace = createPrimaryWorkspace();

        Workspace extraWorkspace = createExtraWorkspace();
        extraWorkspace.getAttributes().put("codenvy:runner_ram", "256x");

        when(workspaceDao.getByAccount(ACCOUNT_ID)).thenReturn(Arrays.asList(primaryWorkspace, extraWorkspace));

        resourcesCleaner.onEvent(new DeleteWorkspaceEvent(extraWorkspace));

        verify(workspaceDao, times(0)).update((Workspace)anyObject());
    }

    private Workspace createPrimaryWorkspace() {
        return new Workspace().withId(PRIMARY_WORKSPACE_ID)
                              .withAccountId(ACCOUNT_ID)
                              .withAttributes(new HashMap<String, String>())
                              .withTemporary(false);
    }

    private Workspace createExtraWorkspace() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("codenvy:role", "extra");
        attributes.put("codenvy:runner_ram", "256");
        return new Workspace().withId(EXTRA_WORKSPACE_ID)
                              .withAccountId(ACCOUNT_ID)
                              .withAttributes(attributes)
                              .withTemporary(false);
    }
}
