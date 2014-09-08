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
import com.codenvy.api.account.server.dao.Account;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.workspace.server.dao.Workspace;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;

import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link com.codenvy.subscription.service.SaasSubscriptionService}
 *
 * @author Eugene Voevodin
 * @author Alexander Garagatyi
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

    @BeforeClass
    public void initialize() {
        service = new SaasSubscriptionService(workspaceDao, accountDao);
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Given account doesn't have any workspaces.")
    public void testOnCreateSubscriptionWithoutAccountId() throws ApiException {
        final Subscription subscription = new Subscription();
        service.afterCreateSubscription(subscription);
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Given account doesn't have any workspaces.")
    public void testUpdateSubscriptionWithoutAccountIdProperty() throws ApiException {
        final Subscription subscription = new Subscription();
        service.onUpdateSubscription(subscription, subscription);
    }


    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Subscription with such plan can't be added",
          dataProvider = "badRamProvider")
    public void shouldThrowConflictExceptionOnAfterCreateSubscriptionWithBadSubscriptionRAM(String ram) throws ApiException {
        final Workspace workspace = new Workspace().withId(WORKSPACE_ID).withAttributes(new HashMap<String, String>());
        when(workspaceDao.getByAccount(ACCOUNT_ID)).thenReturn(Arrays.asList(workspace));
        final Map<String, String> properties = new HashMap<>(3);
        properties.put("RAM", ram);
        properties.put("Package", "developer");
        final Subscription subscription = new Subscription().withAccountId(ACCOUNT_ID).withProperties(properties);

        service.afterCreateSubscription(subscription);
    }

    @DataProvider(name = "badRamProvider")
    public String[][] badRamProvider() {
        return new String[][]{
                {"0xAGB"},
                {"0xAMB"},
                {"1024"},
                {"2TB"},
                {"1gb"},
                {"1536mb"},
                {"1Gb"},
                {"1gB"},
                {"1536mB"},
                {"1gigabyte"},
                {"4gigabytes"},
                {"1536megabytes"},
                {"4Gbytes"},
                {"4GBytes"},
        };
    }

    @Test
    public void testWorkspaceAttributesAddedWhenOnCreateInvoked() throws ApiException {
        final Workspace workspace = new Workspace().withId(WORKSPACE_ID);
        when(workspaceDao.getByAccount(ACCOUNT_ID)).thenReturn(Arrays.asList(workspace));
        when(accountDao.getById(ACCOUNT_ID)).thenReturn(new Account());
        final Map<String, String> properties = new HashMap<>(2);
        properties.put("Package", "developer");
        properties.put("RAM", "1GB");
        final Subscription subscription = new Subscription().withAccountId(ACCOUNT_ID)
                                                            .withProperties(properties);

        service.afterCreateSubscription(subscription);

        Assert.assertEquals(workspace.getAttributes().size(), 3);
        Assert.assertEquals(workspace.getAttributes().get("codenvy:runner_ram"), "1024");
        Assert.assertEquals(workspace.getAttributes().get("codenvy:runner_lifetime"), String.valueOf(TimeUnit.HOURS.toSeconds(1)));
        Assert.assertEquals(workspace.getAttributes().get("codenvy:builder_execution_time"),
                            String.valueOf(TimeUnit.MINUTES.toSeconds(20)));
        verify(accountDao).update(argThat(new ArgumentMatcher<Account>() {
            @Override
            public boolean matches(Object argument) {
                Account actual = (Account) argument;
                return Collections.singletonMap("codenvy:multi-ws", "true").equals(actual.getAttributes());
            }
        }));
    }

    @Test
    public void testWorkspaceAttributesAddedWhenOnCheckInvoked() throws ApiException {
        final Workspace workspace = new Workspace().withId(WORKSPACE_ID);
        when(workspaceDao.getByAccount(ACCOUNT_ID)).thenReturn(Arrays.asList(workspace));
        when(accountDao.getById(ACCOUNT_ID)).thenReturn(new Account());
        final Map<String, String> properties = new HashMap<>(2);
        properties.put("Package", "developer");
        properties.put("RAM", "1GB");
        final Subscription subscription = new Subscription().withAccountId(ACCOUNT_ID)
                                                            .withProperties(properties);
        service.onCheckSubscription(subscription);

        Assert.assertEquals(workspace.getAttributes().size(), 3);
        Assert.assertEquals(workspace.getAttributes().get("codenvy:runner_ram"), "1024");
        Assert.assertEquals(workspace.getAttributes().get("codenvy:runner_lifetime"), String.valueOf(TimeUnit.HOURS.toSeconds(1)));
        Assert.assertEquals(workspace.getAttributes().get("codenvy:builder_execution_time"),
                            String.valueOf(TimeUnit.MINUTES.toSeconds(20)));
        verify(accountDao).update(argThat(new ArgumentMatcher<Account>() {
            @Override
            public boolean matches(Object argument) {
                Account actual = (Account) argument;
                return Collections.singletonMap("codenvy:multi-ws", "true").equals(actual.getAttributes());
            }
        }));
    }

    @Test
    public void testWorkspaceAttributesReplacedOrAddedWhenOnUpdateInvoked() throws ApiException {
        final Workspace workspace = new Workspace().withId(WORKSPACE_ID);
        when(workspaceDao.getByAccount(ACCOUNT_ID)).thenReturn(Arrays.asList(workspace));
        when(accountDao.getById(ACCOUNT_ID)).thenReturn(new Account());
        final Map<String, String> properties = new HashMap<>(3);
        properties.put("Package", "developer");
        properties.put("RAM", "1GB");
        final Subscription subscription = new Subscription().withAccountId(ACCOUNT_ID)
                                                            .withProperties(properties);

        service.onUpdateSubscription(subscription, subscription);

        Assert.assertEquals(workspace.getAttributes().size(), 3);
        Assert.assertEquals(workspace.getAttributes().get("codenvy:runner_ram"), "1024");
        Assert.assertEquals(workspace.getAttributes().get("codenvy:runner_lifetime"), String.valueOf(TimeUnit.HOURS.toSeconds(1)));
        Assert.assertEquals(workspace.getAttributes().get("codenvy:builder_execution_time"),
                            String.valueOf(TimeUnit.MINUTES.toSeconds(20)));
        verify(accountDao).update(argThat(new ArgumentMatcher<Account>() {
            @Override
            public boolean matches(Object argument) {
                Account actual = (Account) argument;
                return Collections.singletonMap("codenvy:multi-ws", "true").equals(actual.getAttributes());
            }
        }));
    }

    @Test
    public void testRemoveWorkspaceAttributesWhenOnRemoveInvoked() throws ApiException {
        final Map<String, String> attributes = new HashMap<>(2);
        attributes.put("codenvy:runner_ram", "fake");
        attributes.put("codenvy:runner_lifetime", "fake");
        final Workspace workspace = new Workspace().withId(WORKSPACE_ID)
                                                   .withAttributes(attributes);
        final Subscription subscription = new Subscription().withAccountId(ACCOUNT_ID);
        when(workspaceDao.getByAccount(ACCOUNT_ID)).thenReturn(Arrays.asList(workspace));
        HashMap<String, String> accountAttributes = new HashMap<>();
        accountAttributes.put("codenvy:multi-ws", "true");
        when(accountDao.getById(ACCOUNT_ID)).thenReturn(new Account().withAttributes(accountAttributes));

        service.onRemoveSubscription(subscription);

        Assert.assertEquals(workspace.getAttributes().size(), 0);
        verify(accountDao).update(argThat(new ArgumentMatcher<Account>() {
            @Override
            public boolean matches(Object argument) {
                Account actual = (Account) argument;
                return actual.getAttributes().isEmpty();
            }
        }));
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = SubscriptionService.SUBSCRIPTION_LIMIT_EXHAUSTED_MESSAGE)
    public void testBeforeCreateSubscriptionWhenExistsActiveState() throws ApiException {
        final List<Subscription> existedSubscriptions = new ArrayList<>(1);
        existedSubscriptions.add(new Subscription().withServiceId(service.getServiceId()));
        when(accountDao.getSubscriptions(ACCOUNT_ID, service.getServiceId())).thenReturn(existedSubscriptions);
        final Map<String, String> properties = new HashMap<>(4);
        properties.put("Package", "team");
        properties.put("TariffPlan", "monthly");
        properties.put("RAM", "2GB");
        final Subscription newSubscription = new Subscription().withAccountId(ACCOUNT_ID).withProperties(properties);
        service.beforeCreateSubscription(newSubscription);
    }

    @Test
    public void shouldSetDefaultRamForOneWorkspaceOnlyOnRemoveSubscription() throws Exception {
        final Subscription subscription = new Subscription().withAccountId(ACCOUNT_ID);
        final Map<String, String> attributes = new HashMap<>(2);
        attributes.put("codenvy:runner_ram", "fake");
        attributes.put("codenvy:runner_lifetime", "fake");
        attributes.put("codenvy:builder_execution_time", "fake");
        final Workspace workspace = new Workspace().withId(WORKSPACE_ID)
                                                   .withAttributes(new HashMap<>(attributes));
        final Workspace workspace2 = new Workspace().withId("ANOTHER_WORKSPACE_ID")
                                                    .withAttributes(new HashMap<>(attributes));
        final Workspace workspace3 = new Workspace().withId("YET_ANOTHER_WORKSPACE_ID")
                                                    .withAttributes(new HashMap<>(attributes));
        when(workspaceDao.getByAccount(ACCOUNT_ID)).thenReturn(Arrays.asList(workspace, workspace2, workspace3));
        HashMap<String, String> accountAttributes = new HashMap<>();
        accountAttributes.put("codenvy:multi-ws", "true");
        when(accountDao.getById(ACCOUNT_ID)).thenReturn(new Account().withAttributes(accountAttributes));

        service.onRemoveSubscription(subscription);

        verify(workspaceDao).update(argThat(new ArgumentMatcher<Workspace>() {
            @Override
            public boolean matches(Object argument) {
                Workspace actual = (Workspace)argument;
                return actual.getAttributes().get("codenvy:builder_execution_time") == null &&
                       actual.getAttributes().get("codenvy:runner_lifetime") == null &&
                       actual.getAttributes().get("codenvy:runner_ram") == null;
            }
        }));
        verify(workspaceDao, times(2)).update(argThat(new ArgumentMatcher<Workspace>() {
            @Override
            public boolean matches(Object argument) {
                Workspace actual = (Workspace)argument;
                return actual.getAttributes().get("codenvy:builder_execution_time") == null &&
                       actual.getAttributes().get("codenvy:runner_lifetime") == null &&
                       "0".equals(actual.getAttributes().get("codenvy:runner_ram"));
            }
        }));
        verify(accountDao).update(argThat(new ArgumentMatcher<Account>() {
            @Override
            public boolean matches(Object argument) {
                Account actual = (Account) argument;
                return actual.getAttributes().isEmpty();
            }
        }));
    }

    @Test
    public void shouldBeAbleToCheckBeforeCreateSubscription() throws Exception {
        final Workspace workspace = new Workspace().withId(WORKSPACE_ID);
        when(accountDao.getSubscriptions(ACCOUNT_ID, service.getServiceId())).thenReturn(Collections.<Subscription>emptyList());
        when(workspaceDao.getByAccount(ACCOUNT_ID)).thenReturn(Collections.singletonList(workspace));
        final Map<String, String> properties = new HashMap<>(4);
        properties.put("Package", "team");
        properties.put("TariffPlan", "monthly");
        properties.put("RAM", "2GB");
        final Subscription newSubscription = new Subscription().withAccountId(ACCOUNT_ID).withProperties(properties);
        service.beforeCreateSubscription(newSubscription);
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Given account doesn't have any workspaces.")
    public void shouldThrowConflictExceptionOnBeforeCreateSubscriptionIfAccountDoesNotHaveWs() throws Exception {
        when(accountDao.getSubscriptions(ACCOUNT_ID, service.getServiceId())).thenReturn(Collections.<Subscription>emptyList());
        when(workspaceDao.getByAccount(ACCOUNT_ID)).thenReturn(Collections.<Workspace>emptyList());
        final Map<String, String> properties = new HashMap<>(4);
        properties.put("Package", "team");
        properties.put("TariffPlan", "monthly");
        properties.put("RAM", "2GB");
        final Subscription newSubscription = new Subscription().withAccountId(ACCOUNT_ID).withProperties(properties);
        service.beforeCreateSubscription(newSubscription);
    }

    @Test(dataProvider = "goodRamValuesProvider")
    public void shouldBeAbleToUseMBOrGBOnAfterCreateSubscription(String actualRam, String expectedRam) throws Exception {
        final Workspace workspace = new Workspace().withId(WORKSPACE_ID);
        when(workspaceDao.getByAccount(ACCOUNT_ID)).thenReturn(Arrays.asList(workspace));
        when(accountDao.getById(ACCOUNT_ID)).thenReturn(new Account());
        final Map<String, String> properties = new HashMap<>(2);
        properties.put("Package", "developer");
        properties.put("RAM", actualRam);
        final Subscription subscription = new Subscription().withAccountId(ACCOUNT_ID)
                                                            .withProperties(properties);

        service.afterCreateSubscription(subscription);

        Assert.assertEquals(workspace.getAttributes().size(), 3);
        Assert.assertEquals(workspace.getAttributes().get("codenvy:runner_ram"), expectedRam);
        Assert.assertEquals(workspace.getAttributes().get("codenvy:runner_lifetime"), String.valueOf(TimeUnit.HOURS.toSeconds(1)));
        Assert.assertEquals(workspace.getAttributes().get("codenvy:builder_execution_time"),
                            String.valueOf(TimeUnit.MINUTES.toSeconds(20)));
    }

    @DataProvider(name = "goodRamValuesProvider")
    public String[][] goodRamValuesProvider() {
        return new String[][]{
                {"1GB", String.valueOf(1024)},
                {"2GB", String.valueOf(2 * 1024)},
                {"32GB", String.valueOf(32 * 1024)},
                {"1536MB", "1536"},
                {"512MB", "512"},
                {"105MB", "105"},
        };
    }

    @Test
    public void shouldAddAttributesExceptRamToAllWorkspacesOnAfterCreateSubscription() throws Exception {
        final Workspace workspace = new Workspace().withId(WORKSPACE_ID);
        final Workspace workspace2 = new Workspace().withId("ANOTHER_WORKSPACE_ID");
        final Workspace workspace3 = new Workspace().withId("YET_ANOTHER_WORKSPACE_ID");
        when(accountDao.getSubscriptions(ACCOUNT_ID, service.getServiceId())).thenReturn(Collections.<Subscription>emptyList());
        when(workspaceDao.getByAccount(ACCOUNT_ID)).thenReturn(Arrays.asList(workspace, workspace2, workspace3));
        when(accountDao.getById(ACCOUNT_ID)).thenReturn(new Account());
        final Map<String, String> properties = new HashMap<>(4);
        properties.put("Package", "team");
        properties.put("TariffPlan", "monthly");
        properties.put("RAM", "2GB");
        final Subscription newSubscription = new Subscription().withAccountId(ACCOUNT_ID).withProperties(properties);

        service.afterCreateSubscription(newSubscription);

        verify(workspaceDao).update(argThat(new ArgumentMatcher<Workspace>() {
            @Override
            public boolean matches(Object argument) {
                Workspace actual = (Workspace)argument;
                return actual.getAttributes().size() == 3 && actual.getAttributes().get("codenvy:builder_execution_time") != null &&
                       actual.getAttributes().get("codenvy:runner_lifetime") != null &&
                       "2048".equals(actual.getAttributes().get("codenvy:runner_ram"));
            }
        }));
        verify(workspaceDao, times(2)).update(argThat(new ArgumentMatcher<Workspace>() {
            @Override
            public boolean matches(Object argument) {
                Workspace actual = (Workspace)argument;
                return actual.getAttributes().size() == 3 && actual.getAttributes().get("codenvy:builder_execution_time") != null &&
                       actual.getAttributes().get("codenvy:runner_lifetime") != null &&
                       "0".equals(actual.getAttributes().get("codenvy:runner_ram"));
            }
        }));
        verify(accountDao).update(argThat(new ArgumentMatcher<Account>() {
            @Override
            public boolean matches(Object argument) {
                Account actual = (Account)argument;
                return Collections.singletonMap("codenvy:multi-ws", "true").equals(actual.getAttributes());
            }
        }));
    }
}
