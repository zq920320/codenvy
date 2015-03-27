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
package com.codenvy.api.account.subscription.saas;

import com.codenvy.api.account.AccountLocker;
import com.codenvy.api.account.billing.BillingPeriod;
import com.codenvy.api.account.billing.BillingService;
import com.codenvy.api.account.billing.Period;
import com.codenvy.api.account.metrics.MeterBasedStorage;
import com.codenvy.api.account.server.ResourcesChangesNotifier;
import com.codenvy.api.account.subscription.service.util.SubscriptionMailSender;
import com.codenvy.api.account.subscription.service.util.SubscriptionServiceHelper;

import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.account.server.dao.Subscription;
import org.eclipse.che.api.account.shared.dto.UsedAccountResources;
import org.eclipse.che.api.account.shared.dto.WorkspaceResources;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.runner.internal.Constants;
import org.eclipse.che.api.workspace.server.dao.Workspace;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link com.codenvy.api.account.subscription.saas.SaasSubscriptionService}
 *
 * @author Sergii Leschenko
 */
@Listeners(value = {MockitoTestNGListener.class})
public class SaasSubscriptionServiceTest {
    private static final int FREE_MAX_LIMIT = 4096;
    @Mock
    WorkspaceDao              workspaceDao;
    @Mock
    AccountDao                accountDao;
    @Mock
    MeterBasedStorage         meterBasedStorage;
    @Mock
    BillingPeriod             billingPeriod;
    @Mock
    AccountLocker             accountLocker;
    @Mock
    EventService              eventService;
    @Mock
    SubscriptionMailSender    mailSender;
    @Mock
    BillingService            billingService;
    @Mock
    SubscriptionServiceHelper subscriptionServiceHelper;
    @Mock
    ResourcesChangesNotifier  resourcesChangesNotifier;
    @Mock
    Period                    period;

    private SaasSubscriptionService service;

    @BeforeMethod
    public void setUp() {
        service = new SaasSubscriptionService(FREE_MAX_LIMIT,
                                              workspaceDao,
                                              accountDao,
                                              meterBasedStorage,
                                              billingPeriod,
                                              accountLocker,
                                              eventService,
                                              mailSender,
                                              billingService,
                                              subscriptionServiceHelper,
                                              resourcesChangesNotifier);

        when(billingPeriod.getCurrent()).thenReturn(period);
    }

    @Test
    public void shouldReturnAccountResources() throws ServerException {
        when(period.getStartDate()).thenReturn(new Date());
        Workspace workspace = new Workspace().withId("workspaceID");
        when(workspaceDao.getByAccount(anyString())).thenReturn(Arrays.asList(workspace));

        Map<String, Double> usedReport = new HashMap<>();
        usedReport.put("workspaceID", 1024D);
        when(meterBasedStorage.getMemoryUsedReport(anyString(), anyLong(), anyLong())).thenReturn(usedReport);

        final UsedAccountResources usedAccountResources = service.getAccountResources(new Subscription());

        final List<WorkspaceResources> used = usedAccountResources.getUsed();
        assertEquals(used.size(), 1);
        assertEquals(used.get(0).getWorkspaceId(), "workspaceID");
        assertEquals(used.get(0).getMemory(), 1024D);
    }

    @Test
    public void shouldReturnInformationAboutUsedResourcesForWorkspaceThatHasNotUsedResources() throws ServerException {
        when(period.getStartDate()).thenReturn(new Date());
        Workspace workspace = new Workspace().withId("workspaceID");
        when(workspaceDao.getByAccount(anyString())).thenReturn(Arrays.asList(workspace));

        when(meterBasedStorage.getMemoryUsedReport(anyString(), anyLong(), anyLong())).thenReturn(new HashMap<String, Double>());

        final UsedAccountResources usedAccountResources = service.getAccountResources(new Subscription());

        final List<WorkspaceResources> used = usedAccountResources.getUsed();
        assertEquals(used.size(), 1);
        assertEquals(used.get(0).getWorkspaceId(), "workspaceID");
        assertEquals(used.get(0).getMemory(), 0D);
    }

    @Test
    public void shouldResetSizeOfRunnerRamToMaxAllowedValueIfItNecessaryOnRemoveSubscription() throws Exception {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(Constants.RUNNER_MAX_MEMORY_SIZE, "512");
        Workspace workspace = new Workspace().withId("workspace")
                                             .withAttributes(attributes);

        Map<String, String> otherAttributes = new HashMap<>();
        otherAttributes.put(Constants.RUNNER_MAX_MEMORY_SIZE, "5120");
        Workspace otherWorkspace = new Workspace().withId("otherWorkspace")
                                                  .withAttributes(otherAttributes);
        when(workspaceDao.getByAccount(eq("accountId"))).thenReturn(Arrays.asList(workspace, otherWorkspace));

        service.onRemoveSubscription(new Subscription().withAccountId("accountId")
                                                       .withEndDate(new Date()));

        verify(workspaceDao).update(argThat(new ArgumentMatcher<Workspace>() {
            @Override
            public boolean matches(Object o) {
                final Workspace workspace = (Workspace)o;
                return workspace.getId().equals("otherWorkspace")
                       && workspace.getAttributes().get(Constants.RUNNER_MAX_MEMORY_SIZE).equals(String.valueOf(FREE_MAX_LIMIT));
            }
        }));
        verify(resourcesChangesNotifier).publishTotalMemoryChangedEvent(eq("otherWorkspace"), eq(String.valueOf(FREE_MAX_LIMIT)));
    }
}
