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
package com.codenvy.subscription.service.saas;

import com.codenvy.api.account.server.dao.Account;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.core.notification.EventService;
import com.codenvy.api.workspace.server.dao.Workspace;
import com.codenvy.workspace.event.CreateWorkspaceEvent;

import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link com.codenvy.subscription.service.saas.SaasWorkspaceResourcesProvider}
 *
 * @author Sergii Leschenko
 */
@Listeners(value = {MockitoTestNGListener.class})
public class SaasWorkspaceResourcesProviderTest {
    @Mock
    private AccountDao          accountDao;
    @Mock
    private EventService        eventService;
    @Mock
    private SaasResourceManager saasResourceManager;

    private SaasWorkspaceResourcesProvider resourceProvider;

    private final String WORKSPACE_ID = "workspace_id";
    private final String ACCOUNT_ID   = "account_id";


    @BeforeMethod
    public void setUp() throws Exception {
        resourceProvider = new SaasWorkspaceResourcesProvider(eventService,
                                                              accountDao,
                                                              saasResourceManager,
                                                              false);
    }

    @Test
    public void shouldDoNothingWhenOnPremisesPackaging() throws Exception {
        resourceProvider = new SaasWorkspaceResourcesProvider(eventService,
                                                              accountDao,
                                                              saasResourceManager,
                                                              true);

        resourceProvider.onEvent(new CreateWorkspaceEvent(new Workspace()));

        verify(saasResourceManager, times(0)).setResources((Workspace)anyObject(), (Subscription)anyObject());
    }

    @Test
    public void shouldDoNothingWhenCreateWorkspaceIsTemporary() throws Exception {
        final Workspace temporaryWorkspace = new Workspace().withTemporary(true);
        resourceProvider.onEvent(new CreateWorkspaceEvent(temporaryWorkspace));

        verify(saasResourceManager, times(0)).setResources((Workspace)anyObject(), (Subscription)anyObject());
    }

    @Test
    public void shouldDoNothingWhenAccountHasNotAttributeMultiWs() throws Exception {
        Workspace workspace = new Workspace().withId(WORKSPACE_ID)
                                             .withAccountId(ACCOUNT_ID)
                                             .withTemporary(false);
        final Account account = new Account().withId(ACCOUNT_ID);
        when(accountDao.getById(ACCOUNT_ID)).thenReturn(account);

        resourceProvider.onEvent(new CreateWorkspaceEvent(workspace));

        verify(saasResourceManager, times(0)).setResources((Workspace)anyObject(), (Subscription)anyObject());
    }

    @Test
    public void shouldDoNothingWhenAccountHasCommunitySubscription() throws Exception {
        Workspace workspace = new Workspace().withId(WORKSPACE_ID)
                                             .withAccountId(ACCOUNT_ID)
                                             .withTemporary(false);
        Map<String, String> accountAttributes = new HashMap<>();
        accountAttributes.put("codenvy:multi-ws", "true");
        final Account account = new Account().withId(ACCOUNT_ID)
                                             .withAttributes(accountAttributes);
        when(accountDao.getById(ACCOUNT_ID)).thenReturn(account);

        resourceProvider.onEvent(new CreateWorkspaceEvent(workspace));

        verify(saasResourceManager, times(0)).setResources((Workspace)anyObject(), (Subscription)anyObject());
    }

    @Test
    public void shouldSetResourcesForPrimaryWorkspace() throws Exception {
        Workspace workspace = new Workspace().withId(WORKSPACE_ID)
                                             .withAccountId(ACCOUNT_ID)
                                             .withTemporary(false);
        Map<String, String> accountAttributes = new HashMap<>();
        accountAttributes.put("codenvy:multi-ws", "true");
        final Account account = new Account().withId(ACCOUNT_ID)
                                             .withAttributes(accountAttributes);
        when(accountDao.getById(ACCOUNT_ID)).thenReturn(account);

        Map<String, String> subscriptionAttributes = new HashMap<>();
        subscriptionAttributes.put("Package", "Developer");
        subscriptionAttributes.put("RAM", "2GB");
        final Subscription subscription = new Subscription().withServiceId("Saas")
                                                            .withProperties(subscriptionAttributes);
        when(accountDao.getSubscriptions(ACCOUNT_ID, "Saas")).thenReturn(Arrays.asList(subscription));

        resourceProvider.onEvent(new CreateWorkspaceEvent(workspace));

        verify(saasResourceManager).setResources(eq(workspace), eq(subscription));
    }

    @Test
    public void shouldSetResourcesForSecondCreatedWorkspace() throws Exception {
        Workspace workspace = new Workspace().withId(WORKSPACE_ID)
                                             .withAccountId(ACCOUNT_ID)
                                             .withTemporary(false);
        Map<String, String> accountAttributes = new HashMap<>();
        accountAttributes.put("codenvy:multi-ws", "true");
        final Account account = new Account().withId(ACCOUNT_ID)
                                             .withAttributes(accountAttributes);
        when(accountDao.getById(ACCOUNT_ID)).thenReturn(account);

        Map<String, String> subscriptionAttributes = new HashMap<>();
        subscriptionAttributes.put("Package", "Developer");
        final Subscription subscription = new Subscription().withServiceId("Saas")
                                                            .withProperties(subscriptionAttributes);
        when(accountDao.getSubscriptions(ACCOUNT_ID, "Saas")).thenReturn(Arrays.asList(subscription));

        resourceProvider.onEvent(new CreateWorkspaceEvent(workspace));

        verify(saasResourceManager, times(1)).setResources(eq(workspace), eq(subscription));
    }
}
