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
import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.workspace.server.dao.Workspace;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;

import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link com.codenvy.subscription.service.saas.SaasResourceManager}
 *
 * @author Sergii Leschenko
 * @author Alexander Garagatyi
 */
@Listeners(value = {MockitoTestNGListener.class})
public class SaasResourceManagerTest {
    private static final String ACCOUNT_ID             = "accountID";
    private static final String WORKSPACE_ID           = "wsID";
    private static final String RUNNER_LIFE_TIME       = "3600";
    private static final String BUILDER_EXECUTION_TIME = "600";

    private SaasResourceManager resourceManager;
    @Mock
    private WorkspaceDao        workspaceDao;
    @Mock
    private AccountDao          accountDao;

    @BeforeClass
    public void initialize() {
        resourceManager = new SaasResourceManager(workspaceDao,
                                                  accountDao,
                                                  RUNNER_LIFE_TIME,
                                                  BUILDER_EXECUTION_TIME);
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Given account doesn't have any workspaces.")
    public void shouldThrowExceptionWhenAccountDoesNotHaveAnyWorkspaces() throws ApiException {
        final Account account = new Account().withId(ACCOUNT_ID);
        when(accountDao.getById(ACCOUNT_ID)).thenReturn(account);
        final Subscription subscription = new Subscription().withAccountId(ACCOUNT_ID)
                                                            .withProperties(Collections.singletonMap("Package", "team"));
        when(workspaceDao.getByAccount(ACCOUNT_ID)).thenReturn(Collections.<Workspace>emptyList());
        try {
            resourceManager.setResources(subscription);
        } finally {
            verify(accountDao, times(0)).update((Account)anyObject());
            verify(workspaceDao, times(0)).update((Workspace)anyObject());
        }
    }

    @Test(expectedExceptions = NotFoundException.class,
          expectedExceptionsMessageRegExp = "not found message")
    public void shouldThrowExceptionWhenAccountNotFound() throws ApiException {
        final Subscription subscription = new Subscription().withProperties(Collections.singletonMap("Package", "team"));
        when(accountDao.getById(anyString())).thenThrow(new NotFoundException("not found message"));
        resourceManager.setResources(subscription);
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Subscription with such plan can't be added",
          dataProvider = "badRamProvider")
    public void shouldThrowConflictExceptionWhenSubscriptionHasBadRAM(String ram) throws ApiException {
        final Workspace workspace = new Workspace().withId(WORKSPACE_ID).withAttributes(new HashMap<String, String>());
        when(workspaceDao.getByAccount(ACCOUNT_ID)).thenReturn(Arrays.asList(workspace));
        final Map<String, String> properties = new HashMap<>(3);
        properties.put("RAM", ram);
        properties.put("Package", "developer");
        final Subscription subscription = new Subscription().withAccountId(ACCOUNT_ID).withProperties(properties);

        resourceManager.setResources(subscription);
    }

    @DataProvider(name = "badRamProvider")
    public String[][] badRamProvider() {
        return new String[][]{
                {"0xAGB"},
                {"0xAMB"},
                {"1gigabyte"},
                {"4gigabytes"},
                {"1536megabytes"},
                {"4Gbytes"},
                {"4GBytes"},
        };
    }

    @Test
    public void shouldSetDeveloperResources() throws ApiException {
        final Workspace workspace = new Workspace().withId(WORKSPACE_ID);
        when(workspaceDao.getByAccount(ACCOUNT_ID)).thenReturn(Arrays.asList(workspace));
        when(accountDao.getById(ACCOUNT_ID)).thenReturn(new Account());
        final Map<String, String> properties = new HashMap<>(2);
        properties.put("Package", "developer");
        properties.put("RAM", "1GB");
        final Subscription subscription = new Subscription().withAccountId(ACCOUNT_ID)
                                                            .withProperties(properties);

        resourceManager.setResources(subscription);

        assertEquals(workspace.getAttributes().size(), 4);
        assertEquals(workspace.getAttributes().get("codenvy:runner_ram"), "1024");
        assertEquals(workspace.getAttributes().get("codenvy:runner_lifetime"), RUNNER_LIFE_TIME);
        assertEquals(workspace.getAttributes().get("codenvy:builder_execution_time"), BUILDER_EXECUTION_TIME);
        assertEquals(workspace.getAttributes().get("codenvy:runner_infra"), "paid");
        verify(accountDao, never()).update(any(Account.class));
    }

    @Test
    public void shouldSetTeamResources() throws ApiException {
        final Workspace workspace = new Workspace().withId(WORKSPACE_ID);
        when(workspaceDao.getByAccount(ACCOUNT_ID)).thenReturn(Arrays.asList(workspace));
        when(accountDao.getById(ACCOUNT_ID)).thenReturn(new Account());
        final Map<String, String> properties = new HashMap<>(2);
        properties.put("Package", "Team");
        properties.put("RAM", "1GB");
        final Subscription subscription = new Subscription().withAccountId(ACCOUNT_ID)
                                                            .withProperties(properties);

        resourceManager.setResources(subscription);

        assertEquals(workspace.getAttributes().size(), 4);
        assertEquals(workspace.getAttributes().get("codenvy:runner_ram"), "1024");
        assertEquals(workspace.getAttributes().get("codenvy:runner_lifetime"), RUNNER_LIFE_TIME);
        assertEquals(workspace.getAttributes().get("codenvy:builder_execution_time"), BUILDER_EXECUTION_TIME);
        assertEquals(workspace.getAttributes().get("codenvy:runner_infra"), "paid");
        verify(accountDao).update(argThat(new ArgumentMatcher<Account>() {
            @Override
            public boolean matches(Object argument) {
                Account actual = (Account)argument;
                return Collections.singletonMap("codenvy:multi-ws", "true").equals(actual.getAttributes());
            }
        }));
    }

    @Test
    public void shouldSetProjectResources() throws ApiException {
        final Workspace workspace = new Workspace().withId(WORKSPACE_ID);
        when(workspaceDao.getByAccount(ACCOUNT_ID)).thenReturn(Arrays.asList(workspace));
        when(accountDao.getById(ACCOUNT_ID)).thenReturn(new Account());
        final Map<String, String> properties = new HashMap<>(2);
        properties.put("Package", "project");
        properties.put("RAM", "1GB");
        final Subscription subscription = new Subscription().withAccountId(ACCOUNT_ID)
                                                            .withProperties(properties);

        resourceManager.setResources(subscription);

        assertEquals(workspace.getAttributes().size(), 4);
        assertEquals(workspace.getAttributes().get("codenvy:runner_ram"), "1024");
        assertEquals(workspace.getAttributes().get("codenvy:runner_lifetime"), "-1");
        assertEquals(workspace.getAttributes().get("codenvy:builder_execution_time"), BUILDER_EXECUTION_TIME);
        assertEquals(workspace.getAttributes().get("codenvy:runner_infra"), "always_on");
        verify(accountDao, never()).update(any(Account.class));
    }

    @Test
    public void shouldSetEnterpriseResources() throws ApiException {
        final Workspace workspace = new Workspace().withId(WORKSPACE_ID);
        when(workspaceDao.getByAccount(ACCOUNT_ID)).thenReturn(Arrays.asList(workspace));
        when(accountDao.getById(ACCOUNT_ID)).thenReturn(new Account());
        final Map<String, String> properties = new HashMap<>(2);
        properties.put("Package", "enterprise");
        properties.put("RAM", "1GB");
        final Subscription subscription = new Subscription().withAccountId(ACCOUNT_ID)
                                                            .withProperties(properties);

        resourceManager.setResources(subscription);

        assertEquals(workspace.getAttributes().size(), 4);
        assertEquals(workspace.getAttributes().get("codenvy:runner_ram"), "1024");
        assertEquals(workspace.getAttributes().get("codenvy:runner_lifetime"), "-1");
        assertEquals(workspace.getAttributes().get("codenvy:builder_execution_time"), BUILDER_EXECUTION_TIME);
        assertEquals(workspace.getAttributes().get("codenvy:runner_infra"), "always_on");
        verify(accountDao).update(argThat(new ArgumentMatcher<Account>() {
            @Override
            public boolean matches(Object argument) {
                Account actual = (Account)argument;
                return Collections.singletonMap("codenvy:multi-ws", "true").equals(actual.getAttributes());
            }
        }));
    }

    @Test
    public void shouldRemoveResources() throws ApiException {
        final Map<String, String> attributes = new HashMap<>(4);
        attributes.put("codenvy:runner_ram", "fake");
        attributes.put("codenvy:runner_lifetime", "fake");
        attributes.put("codenvy:builder_execution_time", "fake");
        attributes.put("codenvy:runner_infra", "fake");
        final Workspace workspace = new Workspace().withId(WORKSPACE_ID)
                                                   .withAttributes(attributes);
        final Subscription subscription = new Subscription().withAccountId(ACCOUNT_ID)
                                                            .withProperties(Collections.singletonMap("Package", "team"));
        when(workspaceDao.getByAccount(ACCOUNT_ID)).thenReturn(Arrays.asList(workspace));
        HashMap<String, String> accountAttributes = new HashMap<>();
        accountAttributes.put("codenvy:multi-ws", "true");
        when(accountDao.getById(ACCOUNT_ID)).thenReturn(new Account().withAttributes(accountAttributes));

        resourceManager.resetResources(subscription);

        assertEquals(workspace.getAttributes().size(), 0);
        verify(accountDao).update(argThat(new ArgumentMatcher<Account>() {
            @Override
            public boolean matches(Object argument) {
                Account actual = (Account)argument;
                return actual.getAttributes().isEmpty();
            }
        }));
    }

    @Test
    public void shouldSetDefaultRamForOneWorkspaceOnlyOnUnsetResources() throws Exception {
        final Subscription subscription = new Subscription().withAccountId(ACCOUNT_ID)
                                                            .withProperties(Collections.singletonMap("Package", "team"));
        final Map<String, String> resourcesAttributes = new HashMap<>(4);
        resourcesAttributes.put("codenvy:runner_ram", "fake");
        resourcesAttributes.put("codenvy:runner_lifetime", "fake");
        resourcesAttributes.put("codenvy:builder_execution_time", "fake");
        resourcesAttributes.put("codenvy:runner_infra", "fake");
        final Workspace workspace = new Workspace().withId(WORKSPACE_ID)
                                                   .withAttributes(new HashMap<>(resourcesAttributes));
        final Map<String, String> extraAttributes = new HashMap<>(resourcesAttributes);
        extraAttributes.put("codenvy:role", "extra");
        final Workspace workspace2 = new Workspace().withId("ANOTHER_WORKSPACE_ID")
                                                    .withAttributes(new HashMap<>(extraAttributes));
        final Workspace workspace3 = new Workspace().withId("YET_ANOTHER_WORKSPACE_ID")
                                                    .withAttributes(new HashMap<>(extraAttributes));
        when(workspaceDao.getByAccount(ACCOUNT_ID)).thenReturn(Arrays.asList(workspace, workspace2, workspace3));
        HashMap<String, String> accountAttributes = new HashMap<>();
        accountAttributes.put("codenvy:multi-ws", "true");
        when(accountDao.getById(ACCOUNT_ID)).thenReturn(new Account().withAttributes(accountAttributes));

        resourceManager.resetResources(subscription);

        verify(workspaceDao).update(argThat(new ArgumentMatcher<Workspace>() {
            @Override
            public boolean matches(Object argument) {
                Workspace actual = (Workspace)argument;
                return actual.getAttributes().isEmpty();
            }
        }));
        verify(workspaceDao, times(2)).update(argThat(new ArgumentMatcher<Workspace>() {
            @Override
            public boolean matches(Object argument) {
                Workspace actual = (Workspace)argument;
                return actual.getAttributes().size() == 2 && "0".equals(actual.getAttributes().get("codenvy:runner_ram"));
            }
        }));
        verify(accountDao).update(argThat(new ArgumentMatcher<Account>() {
            @Override
            public boolean matches(Object argument) {
                Account actual = (Account)argument;
                return actual.getAttributes().isEmpty();
            }
        }));
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

        resourceManager.setResources(subscription);

        assertEquals(workspace.getAttributes().size(), 4);
        assertEquals(workspace.getAttributes().get("codenvy:runner_ram"), expectedRam);
        assertEquals(workspace.getAttributes().get("codenvy:runner_lifetime"), RUNNER_LIFE_TIME);
        assertEquals(workspace.getAttributes().get("codenvy:builder_execution_time"), BUILDER_EXECUTION_TIME);
        assertEquals(workspace.getAttributes().get("codenvy:runner_infra"), "paid");
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
                {"1536mb", "1536"},
                {"1Gb", "1024"},
                {"1gB", "1024"}
        };
    }

    @Test
    public void shouldAddAttributesExceptRamToAllWorkspaces() throws Exception {
        final Workspace workspace = new Workspace().withId(WORKSPACE_ID);
        Map<String, String> attributes = new HashMap<>();
        attributes.put("codenvy:role", "extra");
        final Workspace workspace2 = new Workspace().withId("ANOTHER_WORKSPACE_ID")
                                                    .withAttributes(attributes);
        final Workspace workspace3 = new Workspace().withId("YET_ANOTHER_WORKSPACE_ID")
                                                    .withAttributes(new HashMap<>(attributes));
        when(accountDao.getSubscriptions(ACCOUNT_ID, "Saas")).thenReturn(Collections.<Subscription>emptyList());
        when(workspaceDao.getByAccount(ACCOUNT_ID)).thenReturn(Arrays.asList(workspace, workspace2, workspace3));
        when(accountDao.getById(ACCOUNT_ID)).thenReturn(new Account());
        final Map<String, String> properties = new HashMap<>(4);
        properties.put("Package", "team");
        properties.put("TariffPlan", "monthly");
        properties.put("RAM", "2GB");
        final Subscription newSubscription = new Subscription().withAccountId(ACCOUNT_ID).withProperties(properties);

        resourceManager.setResources(newSubscription);

        verify(workspaceDao).update(argThat(new ArgumentMatcher<Workspace>() {
            @Override
            public boolean matches(Object argument) {
                Workspace actual = (Workspace)argument;
                return actual.getAttributes().size() == 4 &&
                       actual.getAttributes().get("codenvy:builder_execution_time") != null &&
                       actual.getAttributes().get("codenvy:runner_lifetime") != null &&
                       actual.getAttributes().get("codenvy:runner_infra") != null &&
                       "2048".equals(actual.getAttributes().get("codenvy:runner_ram"));
            }
        }));
        verify(workspaceDao, times(2)).update(argThat(new ArgumentMatcher<Workspace>() {
            @Override
            public boolean matches(Object argument) {
                Workspace actual = (Workspace)argument;
                return actual.getAttributes().size() == 5 &&
                       actual.getAttributes().get("codenvy:builder_execution_time") != null &&
                       actual.getAttributes().get("codenvy:runner_lifetime") != null &&
                       actual.getAttributes().get("codenvy:runner_infra") != null &&
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
