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
package com.codenvy.api.subscription.saas.server;

import com.codenvy.api.subscription.saas.server.billing.BillingPeriod;
import com.codenvy.api.subscription.saas.server.billing.BillingService;
import com.codenvy.api.subscription.saas.server.billing.Period;
import com.codenvy.api.subscription.saas.server.dao.MeterBasedStorage;
import com.codenvy.api.subscription.saas.server.service.util.SubscriptionMailSender;
import com.codenvy.api.subscription.server.util.SubscriptionServiceHelper;
import com.codenvy.api.subscription.server.dao.Subscription;
import com.codenvy.api.subscription.server.dao.SubscriptionDao;

import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.core.notification.EventService;
//import org.eclipse.che.api.runner.internal.Constants;
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
import java.util.Map;

import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link SaasSubscriptionService}
 *
 * @author Sergii Leschenko
 */
@Listeners(value = {MockitoTestNGListener.class})
public class SaasSubscriptionServiceTest {
    /*private static final int FREE_MAX_LIMIT = 4096;
    @Mock
    WorkspaceDao              workspaceDao;
    @Mock
    AccountDao                accountDao;
    @Mock
    SubscriptionDao           subscriptionDao;
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
                                              subscriptionDao,
                                              accountLocker,
                                              eventService,
                                              billingService,
                                              subscriptionServiceHelper,
                                              resourcesChangesNotifier);

        when(billingPeriod.getCurrent()).thenReturn(period);
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
    }*/
}
