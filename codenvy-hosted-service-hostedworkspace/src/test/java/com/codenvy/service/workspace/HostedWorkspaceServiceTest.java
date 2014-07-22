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
package com.codenvy.service.workspace;

import com.codenvy.api.account.server.SubscriptionService;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.workspace.server.dao.Workspace;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;

import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link com.codenvy.service.workspace.HostedWorkspaceService}
 *
 * @author Eugene Voevodin
 */
@Listeners(value = {MockitoTestNGListener.class})
public class HostedWorkspaceServiceTest {

    private SubscriptionService service;
    @Mock
    private WorkspaceDao        workspaceDao;
    @Mock
    private AccountDao          accountDao;

    @BeforeClass
    public void initialize() {
        service = new HostedWorkspaceService(workspaceDao, accountDao);
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Subscription property codenvy:workspace_id required")
    public void testOnCreateSubscriptionWithoutWorkspaceIdProperty() throws ApiException {
        final Subscription subscription = new Subscription().withState(Subscription.State.ACTIVE);
        service.afterCreateSubscription(subscription);
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Bad RAM value")
    public void testOnCreateSubscriptionWithBadSubscriptionRAM() throws ApiException {
        final String workspaceId = "ws1";
        final Workspace workspace = new Workspace().withId(workspaceId)
                                                   .withAttributes(new HashMap<String, String>());
        when(workspaceDao.getById(workspaceId)).thenReturn(workspace);
        final Map<String, String> properties = new HashMap<>(3);
        properties.put("codenvy:workspace_id", workspaceId);
        properties.put("RAM", "0xAGB");
        properties.put("Package", "developer");
        final Subscription subscription = new Subscription().withState(Subscription.State.ACTIVE)
                                                            .withProperties(properties);

        service.afterCreateSubscription(subscription);
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Subscription property codenvy:workspace_id required")
    public void testOnUpdateSubscriptionWithoutWorkspaceIdProperty() throws ApiException {
        final Subscription subscription = new Subscription().withState(Subscription.State.ACTIVE);
        service.onUpdateSubscription(subscription, subscription);
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Subscription property codenvy:workspace_id required")
    public void testTarifficateSubscriptionWithoutWorkspaceIdProperty() throws ApiException {
        final Subscription subscription = new Subscription().withState(Subscription.State.ACTIVE);
        service.afterCreateSubscription(subscription);
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Subscription property codenvy:workspace_id required")
    public void testRemoveSubscriptionWithoutWorkspaceIdProperty() throws ApiException {
        final Subscription subscription = new Subscription();
        service.onRemoveSubscription(subscription);
    }

    @Test
    public void testWorkspaceAttributesAddedWhenOnCreateInvoked() throws ApiException {
        final String workspaceId = "ws1";
        final Workspace workspace = new Workspace().withId(workspaceId);
        when(workspaceDao.getById(workspaceId)).thenReturn(workspace);
        final Map<String, String> properties = new HashMap<>(3);
        properties.put("codenvy:workspace_id", workspaceId);
        properties.put("Package", "developer");
        properties.put("RAM", "1GB");
        final Subscription subscription = new Subscription().withState(Subscription.State.ACTIVE)
                                                            .withProperties(properties);

        service.afterCreateSubscription(subscription);

        assertEquals(workspace.getAttributes().size(), 2);
        assertEquals(workspace.getAttributes().get("codenvy:runner_ram"), "1024");
        assertEquals(workspace.getAttributes().get("codenvy:runner_lifetime"), String.valueOf(TimeUnit.HOURS.toSeconds(1)));
    }

    //if checked subscription has start date before current, workspace attributes should be added
    @Test
    public void testWorkspaceAttributesAddedWhenOnCheckInvoked() throws ApiException {
        final String workspaceId = "ws1";
        final Workspace workspace = new Workspace().withId(workspaceId);
        when(workspaceDao.getById(workspaceId)).thenReturn(workspace);
        final Map<String, String> properties = new HashMap<>(3);
        properties.put("codenvy:workspace_id", workspaceId);
        properties.put("Package", "developer");
        properties.put("RAM", "1GB");
        final Subscription subscription = new Subscription().withState(Subscription.State.ACTIVE)
                                                            .withProperties(properties)
                                                            .withStartDate(System.currentTimeMillis())
                                                            .withEndDate(System.currentTimeMillis() + 60_000);
        service.onCheckSubscription(subscription);

        assertEquals(workspace.getAttributes().size(), 2);
        assertEquals(workspace.getAttributes().get("codenvy:runner_ram"), "1024");
        assertEquals(workspace.getAttributes().get("codenvy:runner_lifetime"), String.valueOf(TimeUnit.HOURS.toSeconds(1)));
    }

    //if checked subscription has start date after current date, workspace attributes should not be added
    @Test
    public void testWorkspaceAttributesNotAddedWhenOnCheckInvoked() throws ApiException {
        final String workspaceId = "ws1";
        final Workspace workspace = new Workspace().withId(workspaceId);
        when(workspaceDao.getById(workspaceId)).thenReturn(workspace);
        final Map<String, String> properties = new HashMap<>(3);
        properties.put("codenvy:workspace_id", workspaceId);
        properties.put("Package", "developer");
        properties.put("RAM", "1GB");
        final Subscription subscription = new Subscription().withState(Subscription.State.ACTIVE)
                                                            .withProperties(properties)
                                                            .withStartDate(System.currentTimeMillis() + 60_000);

        service.onCheckSubscription(subscription);

        assertEquals(workspace.getAttributes().size(), 0);
    }

    @Test
    public void testWorkspaceAttributesReplacedOrAddedWhenOnUpdateWithActiveSubscriptionInvoked() throws ApiException {
        final String workspaceId = "ws1";
        final Workspace workspace = new Workspace().withId(workspaceId);
        when(workspaceDao.getById(workspaceId)).thenReturn(workspace);
        final Map<String, String> properties = new HashMap<>(3);
        properties.put("codenvy:workspace_id", workspaceId);
        properties.put("Package", "developer");
        properties.put("RAM", "1GB");
        final Subscription subscription = new Subscription().withState(Subscription.State.ACTIVE)
                                                            .withProperties(properties);

        service.onUpdateSubscription(subscription, subscription);

        assertEquals(workspace.getAttributes().size(), 2);
        assertEquals(workspace.getAttributes().get("codenvy:runner_ram"), "1024");
        assertEquals(workspace.getAttributes().get("codenvy:runner_lifetime"), String.valueOf(TimeUnit.HOURS.toSeconds(1)));
    }

    @Test
    public void testWorkspaceAttributesRemovedWhenOnUpdateWithNotActiveSubscriptionInvoked() throws ApiException {
        final String workspaceId = "ws1";
        final Map<String, String> attributes = new HashMap<>(2);
        attributes.put("codenvy:runner_ram", "fake");
        attributes.put("codenvy:runner_lifetime", "fake");
        final Workspace workspace = new Workspace().withId(workspaceId)
                                                   .withAttributes(attributes);
        final Subscription subscription = new Subscription().withState(Subscription.State.WAIT_FOR_PAYMENT)
                                                            .withProperties(Collections.singletonMap("codenvy:workspace_id", workspaceId));
        when(workspaceDao.getById(workspaceId)).thenReturn(workspace);

        service.onUpdateSubscription(subscription, subscription);

        assertEquals(workspace.getAttributes().size(), 0);
    }

    @Test
    public void testRemoveWorkspaceAttributesWhenOnRemoveInvoked() throws ApiException {
        final String workspaceId = "ws1";
        final Map<String, String> attributes = new HashMap<>(2);
        attributes.put("codenvy:runner_ram", "fake");
        attributes.put("codenvy:runner_lifetime", "fake");
        final Workspace workspace = new Workspace().withId(workspaceId)
                                                   .withAttributes(attributes);
        final Subscription subscription = new Subscription().withProperties(Collections.singletonMap("codenvy:workspace_id", workspaceId));
        when(workspaceDao.getById(workspaceId)).thenReturn(workspace);

        service.onRemoveSubscription(subscription);

        assertEquals(workspace.getAttributes().size(), 0);
    }

    @Test(expectedExceptions = NotFoundException.class,
          expectedExceptionsMessageRegExp = "Tariff not found")
    public void testTarifficateWithNotExistingTariffPlan() throws ApiException {
        final String workspaceId = "ws1";
        final Workspace workspace = new Workspace().withId(workspaceId);
        when(workspaceDao.getById(workspaceId)).thenReturn(workspace);
        final Map<String, String> properties = new HashMap<>(4);
        properties.put("codenvy:workspace_id", workspaceId);
        properties.put("Package", "custom");
        properties.put("TariffPlan", "custom");
        properties.put("RAM", "1GB");
        final Subscription subscription = new Subscription().withState(Subscription.State.ACTIVE)
                                                            .withProperties(properties);

        service.tarifficate(subscription);
    }

    @Test
    public void testBeforeCreateSubscriptionWithNewStateWaitForPayment() throws ApiException {
        final String workspaceId = "ws1";
        final String accountId = "acc1";
        when(accountDao.getSubscriptions(accountId)).thenReturn(Collections.<Subscription>emptyList());
        final Map<String, String> properties = new HashMap<>(4);
        properties.put("codenvy:workspace_id", workspaceId);
        properties.put("Package", "team");
        properties.put("TariffPlan", "monthly");
        properties.put("RAM", "2GB");
        final Subscription newSubscription = new Subscription().withAccountId(accountId)
                                                               .withState(Subscription.State.WAIT_FOR_PAYMENT)
                                                               .withProperties(properties);
        service.beforeCreateSubscription(newSubscription);
    }

    @Test
    public void testBeforeCreateSubscriptionWithActiveState() throws ApiException {
        final String workspaceId = "ws1";
        final String accountId = "acc1";
        when(accountDao.getSubscriptions(accountId)).thenReturn(Collections.<Subscription>emptyList());
        final Map<String, String> properties = new HashMap<>(4);
        properties.put("codenvy:workspace_id", workspaceId);
        properties.put("Package", "team");
        properties.put("TariffPlan", "monthly");
        properties.put("RAM", "2GB");
        final Subscription newSubscription = new Subscription().withAccountId(accountId)
                                                               .withState(Subscription.State.ACTIVE)
                                                               .withProperties(properties);

        service.beforeCreateSubscription(newSubscription);
    }

    @Test
    public void testBeforeCreateSubscriptionWithWaitForPaymentStateWhenExistsActiveState() throws ApiException {
        final String workspaceId = "ws1";
        final String accountId = "acc1";
        final List<Subscription> existedSubscriptions = new ArrayList<>(1);
        existedSubscriptions.add(new Subscription().withServiceId(service.getServiceId())
                                                   .withState(Subscription.State.WAIT_FOR_PAYMENT)
                                                   .withProperties(Collections.singletonMap("codenvy:workspace_id", workspaceId)));
        when(accountDao.getSubscriptions(accountId)).thenReturn(existedSubscriptions);
        final Map<String, String> properties = new HashMap<>(4);
        properties.put("codenvy:workspace_id", workspaceId);
        properties.put("Package", "team");
        properties.put("TariffPlan", "monthly");
        properties.put("RAM", "2GB");
        final Subscription newSubscription = new Subscription().withAccountId(accountId)
                                                               .withState(Subscription.State.ACTIVE)
                                                               .withProperties(properties);
        service.beforeCreateSubscription(newSubscription);
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Subscription with WAIT_FOR_PAYMENT state already exists")
    public void testBeforeCreateSubscriptionWithWaitForPaymentStateWhenExistsWaitForPaymentState() throws ApiException {
        final String workspaceId = "ws1";
        final String accountId = "acc1";
        final List<Subscription> existedSubscriptions = new ArrayList<>(1);
        existedSubscriptions.add(new Subscription().withServiceId(service.getServiceId())
                                                   .withState(Subscription.State.WAIT_FOR_PAYMENT)
                                                   .withProperties(Collections.singletonMap("codenvy:workspace_id", workspaceId)));
        when(accountDao.getSubscriptions(accountId)).thenReturn(existedSubscriptions);
        final Map<String, String> properties = new HashMap<>(4);
        properties.put("codenvy:workspace_id", workspaceId);
        properties.put("Package", "team");
        properties.put("TariffPlan", "monthly");
        properties.put("RAM", "2GB");
        final Subscription newSubscription = new Subscription().withAccountId(accountId)
                                                               .withState(Subscription.State.WAIT_FOR_PAYMENT)
                                                               .withProperties(properties);
        service.beforeCreateSubscription(newSubscription);
    }

    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "Subscriptions limit exhausted")
    public void testBeforeCreateSubscriptionWithAnyStateWhen2SubscriptionsAlreadyExist() throws ApiException {
        final String workspaceId = "ws1";
        final String accountId = "acc1";
        final List<Subscription> existedSubscriptions = new ArrayList<>(2);
        existedSubscriptions.add(new Subscription().withServiceId(service.getServiceId())
                                                   .withProperties(Collections.singletonMap("codenvy:workspace_id", workspaceId)));
        existedSubscriptions.add(new Subscription().withServiceId(service.getServiceId())
                                                   .withProperties(Collections.singletonMap("codenvy:workspace_id", workspaceId)));
        when(accountDao.getSubscriptions(accountId)).thenReturn(existedSubscriptions);
        final Map<String, String> properties = new HashMap<>(4);
        properties.put("codenvy:workspace_id", workspaceId);
        properties.put("Package", "team");
        properties.put("TariffPlan", "monthly");
        properties.put("RAM", "2GB");
        final Subscription newSubscription = new Subscription().withAccountId(accountId)
                                                               .withState(Subscription.State.WAIT_FOR_PAYMENT)
                                                               .withProperties(properties);
        service.beforeCreateSubscription(newSubscription);
    }

    @Test
    public void testBeforeCreateSubscriptionWithActiveStateWhenExistsActiveState() throws ApiException {
        final String workspaceId = "ws1";
        final String accountId = "acc1";
        final List<Subscription> existedSubscriptions = new ArrayList<>(1);
        final Calendar calendar = Calendar.getInstance();
        final long startDate = calendar.getTimeInMillis();
        calendar.add(Calendar.MONTH, 1);
        final long endDate = calendar.getTimeInMillis();
        existedSubscriptions.add(new Subscription().withState(Subscription.State.ACTIVE)
                                                   .withServiceId(service.getServiceId())
                                                   .withStartDate(startDate)
                                                   .withEndDate(endDate)
                                                   .withProperties(Collections.singletonMap("codenvy:workspace_id", workspaceId)));
        when(accountDao.getSubscriptions(accountId)).thenReturn(existedSubscriptions);
        final Map<String, String> properties = new HashMap<>(4);
        properties.put("codenvy:workspace_id", workspaceId);
        properties.put("Package", "team");
        properties.put("TariffPlan", "monthly");
        properties.put("RAM", "2GB");
        final Subscription newSubscription = new Subscription().withAccountId(accountId)
                                                               .withState(Subscription.State.ACTIVE)
                                                               .withProperties(properties);
        service.beforeCreateSubscription(newSubscription);

        assertEquals(newSubscription.getStartDate(), endDate);
    }
}
