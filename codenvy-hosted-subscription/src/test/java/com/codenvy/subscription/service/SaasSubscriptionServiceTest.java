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
package com.codenvy.subscription.service;

import com.codenvy.api.account.server.SubscriptionService;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.workspace.server.dao.Workspace;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.codenvy.subscription.service.saas.SaasResourceManager;

import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link com.codenvy.subscription.service.SaasSubscriptionService}
 *
 * @author Eugene Voevodin
 * @author Alexander Garagatyi
 * @author Sergii Leschenko
 */
@Listeners(value = {MockitoTestNGListener.class})
public class SaasSubscriptionServiceTest {
    private static final String ACCOUNT_ID   = "accountID";
    private static final String WORKSPACE_ID = "wsID";

    private SubscriptionService service;
    @Mock
    private WorkspaceDao        workspaceDao;
    @Mock
    private AccountDao          accountDao;
    @Mock
    private SaasResourceManager saasResourceManager;

    @BeforeClass
    public void initialize() {
        service = new SaasSubscriptionService(workspaceDao, accountDao, saasResourceManager);
    }

    @Test
    public void shouldSetResourcesWhenOnCreateInvoked() throws ApiException {
        final Subscription subscription = new Subscription().withAccountId(ACCOUNT_ID);

        service.afterCreateSubscription(subscription);

        verify(saasResourceManager).setResources(eq(subscription));
    }

    @Test
    public void shouldSetResourcesWhenOnCheckInvoked() throws ApiException {
        final Subscription subscription = new Subscription().withAccountId(ACCOUNT_ID);

        service.onCheckSubscription(subscription);

        verify(saasResourceManager).setResources(eq(subscription));
    }

    @Test
    public void shouldRemoveWorkspaceAttributesWhenOnRemoveInvoked() throws ApiException {
        final Subscription subscription = new Subscription().withAccountId(ACCOUNT_ID);

        service.onRemoveSubscription(subscription);

        verify(saasResourceManager).resetResources(eq(subscription));
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = SubscriptionService.SUBSCRIPTION_LIMIT_EXHAUSTED_MESSAGE)
    public void shouldThrowExceptionIfAccountHasSubscriptionOnBeforeCreateSubscription() throws ApiException {
        final List<Subscription> existedSubscriptions = new ArrayList<>(1);
        existedSubscriptions.add(new Subscription().withServiceId(service.getServiceId()));
        when(accountDao.getSubscriptions(ACCOUNT_ID, service.getServiceId())).thenReturn(existedSubscriptions);
        final Map<String, String> properties = new HashMap<>(2);
        properties.put("Package", "team");
        properties.put("RAM", "1gb");
        final Subscription newSubscription = new Subscription().withAccountId(ACCOUNT_ID).withProperties(properties);

        service.beforeCreateSubscription(newSubscription);
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Subscription property 'Package' required")
    public void shouldThrowExceptionIfMissedSubscriptionPackageOnBeforeCreateSubscription() throws ApiException {
        final List<Subscription> existedSubscriptions = new ArrayList<>(1);
        existedSubscriptions.add(new Subscription().withServiceId(service.getServiceId()));
        when(accountDao.getSubscriptions(ACCOUNT_ID, service.getServiceId())).thenReturn(existedSubscriptions);
        final Map<String, String> properties = new HashMap<>(2);
        properties.put("RAM", "1gb");
        final Subscription newSubscription = new Subscription().withAccountId(ACCOUNT_ID).withProperties(properties);

        service.beforeCreateSubscription(newSubscription);
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Subscription property 'RAM' required")
    public void shouldThrowExceptionIfMissedSubscriptionRAMOnBeforeCreateSubscription() throws ApiException {
        final List<Subscription> existedSubscriptions = new ArrayList<>(1);
        existedSubscriptions.add(new Subscription().withServiceId(service.getServiceId()));
        when(accountDao.getSubscriptions(ACCOUNT_ID, service.getServiceId())).thenReturn(existedSubscriptions);
        final Map<String, String> properties = new HashMap<>(2);
        properties.put("Package", "developer");
        final Subscription newSubscription = new Subscription().withAccountId(ACCOUNT_ID).withProperties(properties);

        service.beforeCreateSubscription(newSubscription);
    }

    @Test
    public void shouldNotThrowExceptionOnBeforeCreateSubscriptionWhenAccountHasCommunitySubscription() throws ApiException {
        final List<Subscription> existingSubscriptions = new ArrayList<>(1);
        existingSubscriptions.add(new Subscription().withServiceId(service.getServiceId())
                                                    .withPlanId("sas-community"));
        when(accountDao.getSubscriptions(ACCOUNT_ID, service.getServiceId())).thenReturn(existingSubscriptions);
        final Workspace workspace = new Workspace().withId(WORKSPACE_ID);
        when(workspaceDao.getByAccount(ACCOUNT_ID)).thenReturn(Arrays.asList(workspace));
        final Map<String, String> properties = new HashMap<>(2);
        properties.put("Package", "team");
        properties.put("RAM", "1gb");
        final Subscription newSubscription = new Subscription().withAccountId(ACCOUNT_ID)
                                                               .withProperties(properties);

        service.beforeCreateSubscription(newSubscription);
    }

    @Test
    public void shouldBeAbleToCheckBeforeCreateSubscription() throws Exception {
        final Workspace workspace = new Workspace().withId(WORKSPACE_ID);
        when(accountDao.getSubscriptions(ACCOUNT_ID, service.getServiceId())).thenReturn(Collections.<Subscription>emptyList());
        when(workspaceDao.getByAccount(ACCOUNT_ID)).thenReturn(Collections.singletonList(workspace));
        final Map<String, String> properties = new HashMap<>(3);
        properties.put("Package", "team");
        properties.put("RAM", "1gb");
        properties.put("TariffPlan", "monthly");
        final Subscription newSubscription = new Subscription().withAccountId(ACCOUNT_ID)
                                                               .withProperties(properties);
        service.beforeCreateSubscription(newSubscription);
    }
}
