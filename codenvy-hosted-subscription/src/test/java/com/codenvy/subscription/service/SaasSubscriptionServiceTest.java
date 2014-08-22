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
package com.codenvy.subscription.service;

import com.codenvy.api.account.server.SubscriptionService;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.workspace.server.dao.Workspace;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.codenvy.subscription.service.SaasSubscriptionService;

import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.when;

/**
 * Tests for {@link com.codenvy.subscription.service.SaasSubscriptionService}
 *
 * @author Eugene Voevodin
 * @author Alexander Garagatyi
 */
@Listeners(value = {MockitoTestNGListener.class})
public class SaasSubscriptionServiceTest {

    private SubscriptionService service;
    @Mock
    private WorkspaceDao        workspaceDao;
    @Mock
    private AccountDao          accountDao;

    @BeforeClass
    public void initialize() {
        service = new SaasSubscriptionService(workspaceDao, accountDao);
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Given account don't have any workspaces.")
    public void testOnCreateSubscriptionWithoutAccountId() throws ApiException {
        final Subscription subscription = new Subscription();
        service.afterCreateSubscription(subscription);
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Given account don't have any workspaces.")
    public void testRemoveSubscriptionWithoutAccountIdProperty() throws ApiException {
        final Subscription subscription = new Subscription();
        service.onRemoveSubscription(subscription);
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Given account don't have any workspaces.")
    public void testUpdateSubscriptionWithoutAccountIdProperty() throws ApiException {
        final Subscription subscription = new Subscription();
        service.onUpdateSubscription(subscription, subscription);
    }


    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Bad RAM value")
    public void testOnCreateSubscriptionWithBadSubscriptionRAM() throws ApiException {
        final String workspaceId = "ws1";
        final String accountId = "ws1";
        final Workspace workspace = new Workspace().withId(workspaceId)
                                                   .withAttributes(new HashMap<String, String>());
        when(workspaceDao.getByAccount(accountId)).thenReturn(Arrays.asList(workspace));
        final Map<String, String> properties = new HashMap<>(3);
        properties.put("codenvy:workspace_id", workspaceId);
        properties.put("RAM", "0xAGB");
        properties.put("Package", "developer");
        final Subscription subscription = new Subscription().withAccountId(accountId)
                                                            .withProperties(properties);

        service.afterCreateSubscription(subscription);
    }

    @Test
    public void testWorkspaceAttributesAddedWhenOnCreateInvoked() throws ApiException {
        final String workspaceId = "ws1";
        final String accountId = "acc1";
        final Workspace workspace = new Workspace().withId(workspaceId);
        when(workspaceDao.getByAccount(accountId)).thenReturn(Arrays.asList(workspace));
        final Map<String, String> properties = new HashMap<>(2);
        properties.put("Package", "developer");
        properties.put("RAM", "1GB");
        final Subscription subscription = new Subscription().withAccountId(accountId)
                                                            .withProperties(properties);

        service.afterCreateSubscription(subscription);

        Assert.assertEquals(workspace.getAttributes().size(), 3);
        Assert.assertEquals(workspace.getAttributes().get("codenvy:runner_ram"), "1024");
        Assert.assertEquals(workspace.getAttributes().get("codenvy:runner_lifetime"), String.valueOf(TimeUnit.HOURS.toSeconds(1)));
        Assert.assertEquals(workspace.getAttributes().get("codenvy:builder_execution_time"),
                            String.valueOf(TimeUnit.MINUTES.toSeconds(10)));
    }

    @Test
    public void testWorkspaceAttributesAddedWhenOnCheckInvoked() throws ApiException {
        final String workspaceId = "ws1";
        final String accountId = "acc1";
        final Workspace workspace = new Workspace().withId(workspaceId);
        when(workspaceDao.getByAccount(accountId)).thenReturn(Arrays.asList(workspace));
        final Map<String, String> properties = new HashMap<>(2);
        properties.put("Package", "developer");
        properties.put("RAM", "1GB");
        final Subscription subscription = new Subscription().withAccountId(accountId)
                                                            .withProperties(properties);
        service.onCheckSubscription(subscription);

        Assert.assertEquals(workspace.getAttributes().size(), 3);
        Assert.assertEquals(workspace.getAttributes().get("codenvy:runner_ram"), "1024");
        Assert.assertEquals(workspace.getAttributes().get("codenvy:runner_lifetime"), String.valueOf(TimeUnit.HOURS.toSeconds(1)));
        Assert.assertEquals(workspace.getAttributes().get("codenvy:builder_execution_time"),
                            String.valueOf(TimeUnit.MINUTES.toSeconds(10)));
    }

    @Test
    public void testWorkspaceAttributesReplacedOrAddedWhenOnUpdateInvoked() throws ApiException {
        final String workspaceId = "ws1";
        final String accountId = "acc1";
        final Workspace workspace = new Workspace().withId(workspaceId);
        when(workspaceDao.getByAccount(accountId)).thenReturn(Arrays.asList(workspace));
        final Map<String, String> properties = new HashMap<>(3);
        properties.put("Package", "developer");
        properties.put("RAM", "1GB");
        final Subscription subscription = new Subscription().withAccountId(accountId)
                                                            .withProperties(properties);

        service.onUpdateSubscription(subscription, subscription);

        Assert.assertEquals(workspace.getAttributes().size(), 3);
        Assert.assertEquals(workspace.getAttributes().get("codenvy:runner_ram"), "1024");
        Assert.assertEquals(workspace.getAttributes().get("codenvy:runner_lifetime"), String.valueOf(TimeUnit.HOURS.toSeconds(1)));
        Assert.assertEquals(workspace.getAttributes().get("codenvy:builder_execution_time"),
                            String.valueOf(TimeUnit.MINUTES.toSeconds(10)));
    }

    @Test
    public void testRemoveWorkspaceAttributesWhenOnRemoveInvoked() throws ApiException {
        final String workspaceId = "ws1";
        final String accountId = "acc1";
        final Map<String, String> attributes = new HashMap<>(2);
        attributes.put("codenvy:runner_ram", "fake");
        attributes.put("codenvy:runner_lifetime", "fake");
        final Workspace workspace = new Workspace().withId(workspaceId)
                                                   .withAttributes(attributes);
        final Subscription subscription = new Subscription().withAccountId(accountId)
                                                            .withProperties(Collections.singletonMap("codenvy:workspace_id", workspaceId));
        when(workspaceDao.getByAccount(accountId)).thenReturn(Arrays.asList(workspace));

        service.onRemoveSubscription(subscription);

        Assert.assertEquals(workspace.getAttributes().size(), 0);
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Subscriptions limit exhausted")
    public void testBeforeCreateSubscriptionWhenExistsActiveState() throws ApiException {
        final String workspaceId = "ws1";
        final String accountId = "acc1";
        final List<Subscription> existedSubscriptions = new ArrayList<>(1);
        existedSubscriptions.add(new Subscription().withServiceId(service.getServiceId())
                                                   .withProperties(Collections.singletonMap("codenvy:workspace_id", workspaceId)));
        when(accountDao.getSubscriptions(accountId)).thenReturn(existedSubscriptions);
        final Map<String, String> properties = new HashMap<>(4);
        properties.put("codenvy:workspace_id", workspaceId);
        properties.put("Package", "team");
        properties.put("TariffPlan", "monthly");
        properties.put("RAM", "2GB");
        final Subscription newSubscription = new Subscription().withAccountId(accountId)
                                                               .withProperties(properties);
        service.beforeCreateSubscription(newSubscription);
    }
}
