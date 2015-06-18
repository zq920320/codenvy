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
package com.codenvy.api.subscription.server;

import com.codenvy.api.subscription.server.dao.PlanDao;
import com.codenvy.api.subscription.server.dao.Subscription;
import com.codenvy.api.subscription.server.dao.SubscriptionDao;
import com.codenvy.api.subscription.shared.dto.BillingCycleType;
import com.codenvy.api.subscription.shared.dto.NewSubscription;
import com.codenvy.api.subscription.shared.dto.Plan;
import com.codenvy.api.subscription.shared.dto.SubscriptionDescriptor;
import com.codenvy.api.subscription.shared.dto.SubscriptionState;

import org.eclipse.che.api.account.server.ResourcesManager;
import org.eclipse.che.api.account.server.dao.Account;
import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.account.server.dao.Member;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.user.server.dao.User;
import org.eclipse.che.api.user.server.dao.UserDao;
import org.eclipse.che.commons.json.JsonHelper;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.core.impl.ApplicationContextImpl;
import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.EnvironmentContext;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.impl.ProviderBinder;
import org.everrest.core.impl.ResourceBinderImpl;
import org.everrest.core.tools.DependencySupplierImpl;
import org.everrest.core.tools.ResourceLauncher;
import org.everrest.core.tools.SimplePrincipal;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link SubscriptionService}
 *
 * @author Sergii Leschenko
 * @author Alexander Garagatyi
 */
@Listeners(value = {MockitoTestNGListener.class})
public class SubscriptionServiceTest {
    private final String BASE_URI        = "http://localhost/service";
    private final String SERVICE_PATH    = BASE_URI + "/subscription";
    private final String USER_ID         = "user123abc456def";
    private final String ACCOUNT_ID      = "account0xffffffffff";
    private final String SUBSCRIPTION_ID = "subscription0xffffffffff";
    private final String ACCOUNT_NAME    = "codenvy";
    private final String SERVICE_ID      = "IDE_SERVICE";
    private final String USER_EMAIL      = "account@mail.com";
    private final String PLAN_ID         = "planId";
    private final User   user            = new User().withId(USER_ID).withEmail(USER_EMAIL);

    @Mock
    private AccountDao                  accountDao;
    @Mock
    private SubscriptionDao             subscriptionDao;
    @Mock
    private UserDao                     userDao;
    @Mock
    private PlanDao                     planDao;
    @Mock
    private ResourcesManager            resourcesManager;
    @Mock
    private SecurityContext             securityContext;
    @Mock
    private SubscriptionServiceRegistry serviceRegistry;
    @Mock
    private AbstractSubscriptionService abstractSubscriptionService;
    @Mock
    private EnvironmentContext          environmentContext;

    private Account           account;
    private Plan              plan;
    private ArrayList<Member> memberships;
    private NewSubscription   newSubscription;

    protected ProviderBinder     providers;
    protected ResourceBinderImpl resources;
    protected ResourceLauncher   launcher;

    @BeforeMethod
    public void setUp() throws Exception {
        resources = new ResourceBinderImpl();
        providers = new ApplicationProviderBinder();

        DependencySupplierImpl dependencies = new DependencySupplierImpl();
        dependencies.addComponent(SubscriptionDao.class, subscriptionDao);
        dependencies.addComponent(PlanDao.class, planDao);
        dependencies.addComponent(AccountDao.class, accountDao);
        dependencies.addComponent(SubscriptionServiceRegistry.class, serviceRegistry);
        resources.addResource(SubscriptionService.class, null);
        EverrestProcessor processor = new EverrestProcessor(resources, providers, dependencies, new EverrestConfiguration(), null);
        launcher = new ResourceLauncher(processor);
        ProviderBinder providerBinder = ProviderBinder.getInstance();
        providerBinder.addExceptionMapper(new ApiExceptionMapper());
        final ApplicationContextImpl contextImpl = new ApplicationContextImpl(null, null, providerBinder);
        contextImpl.setDependencySupplier(dependencies);
        ApplicationContextImpl.setCurrent(contextImpl);
        Map<String, String> attributes = new HashMap<>();
        attributes.put("secret", "bit secret");
        account = new Account().withId(ACCOUNT_ID)
                               .withName(ACCOUNT_NAME)
                               .withAttributes(attributes);

        plan = DtoFactory.getInstance().createDto(Plan.class)
                         .withId(PLAN_ID)
                         .withPaid(true)
                         .withSalesOnly(false)
                         .withServiceId(SERVICE_ID)
                         .withProperties(Collections.singletonMap("key", "value"))
                         .withBillingContractTerm(12)
                         .withBillingCycle(1)
                         .withBillingCycleType(BillingCycleType.AutoRenew)
                         .withDescription("description");

        memberships = new ArrayList<>(1);
        Member ownerMembership = new Member();
        ownerMembership.setAccountId(account.getId());
        ownerMembership.setUserId(USER_ID);
        ownerMembership.setRoles(Arrays.asList("account/owner"));
        memberships.add(ownerMembership);

        newSubscription = DtoFactory.getInstance().createDto(NewSubscription.class)
                                    .withAccountId(ACCOUNT_ID)
                                    .withPlanId(PLAN_ID)
                                    .withUsePaymentSystem(true);

        when(environmentContext.get(SecurityContext.class)).thenReturn(securityContext);
        when(securityContext.getUserPrincipal()).thenReturn(new SimplePrincipal(USER_EMAIL));

        org.eclipse.che.commons.env.EnvironmentContext.getCurrent().setUser(new org.eclipse.che.commons.user.User() {
            @Override
            public String getName() {
                return user.getEmail();
            }

            @Override
            public boolean isMemberOf(String role) {
                return false;
            }

            @Override
            public String getToken() {
                return "token";
            }

            @Override
            public String getId() {
                return user.getId();
            }

            @Override
            public boolean isTemporary() {
                return false;
            }
        });
    }

    @AfterMethod
    public void tearDown() throws Exception {
        org.eclipse.che.commons.env.EnvironmentContext.reset();
    }


    @Test
    public void shouldBeAbleToGetSubscriptionsOfSpecificAccount() throws Exception {
        Subscription expectedSubscription = createSubscription();
        when(subscriptionDao.getActive(ACCOUNT_ID)).thenReturn(Arrays.asList(expectedSubscription));
        prepareSecurityContext("system/admin");

        ContainerResponse response = makeRequest(HttpMethod.GET, SERVICE_PATH + "/find/account?id=" + ACCOUNT_ID, null, null);

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        @SuppressWarnings("unchecked") List<SubscriptionDescriptor> subscriptions = (List<SubscriptionDescriptor>)response.getEntity();
        for (SubscriptionDescriptor subscription : subscriptions) {
            subscription.setLinks(null);
        }
        assertEquals(subscriptions, Collections.singletonList(convertToDescriptor(expectedSubscription)));
        verify(subscriptionDao).getActive(ACCOUNT_ID);
    }

    @Test
    public void shouldBeAbleToGetSubscriptionsOfSpecificAccountWithSpecifiedServiceId() throws Exception {
        Subscription subscription = createSubscription();
        when(subscriptionDao.getActiveByServiceId(ACCOUNT_ID, SERVICE_ID)).thenReturn(subscription);
        prepareSecurityContext("system/admin");

        ContainerResponse response =
                makeRequest(HttpMethod.GET, SERVICE_PATH + "/find/account?id=" + ACCOUNT_ID + "&service=" + SERVICE_ID, null, null);

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        @SuppressWarnings("unchecked") List<SubscriptionDescriptor> subscriptions = (List<SubscriptionDescriptor>)response.getEntity();
        for (SubscriptionDescriptor subscriptionDescriptor : subscriptions) {
            subscriptionDescriptor.setLinks(null);
        }
        assertEquals(subscriptions, Collections.singletonList(convertToDescriptor(subscription)));
        verify(subscriptionDao).getActiveByServiceId(ACCOUNT_ID, SERVICE_ID);
    }

    @Test
    public void shouldReturnNoSubscriptionIfThereIsNoSubscriptionWithGivenServiceIdOnGetSubscriptions() throws Exception {
        when(subscriptionDao.getActiveByServiceId(ACCOUNT_ID, SERVICE_ID)).thenReturn(null);
        prepareSecurityContext("system/admin");

        ContainerResponse response =
                makeRequest(HttpMethod.GET, SERVICE_PATH + "/find/account/?id=" + ACCOUNT_ID + "&service=" + SERVICE_ID, null, null);

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        @SuppressWarnings("unchecked") List<SubscriptionDescriptor> subscriptions = (List<SubscriptionDescriptor>)response.getEntity();
        assertEquals(subscriptions.size(), 0);
        verify(subscriptionDao).getActiveByServiceId(ACCOUNT_ID, SERVICE_ID);
    }

    @Test
    public void shouldBeAbleToGetSpecificSubscriptionBySystemAdmin() throws Exception {
        Subscription expectedSubscription = createSubscription();
        when(subscriptionDao.getById(SUBSCRIPTION_ID)).thenReturn(expectedSubscription);
        prepareSecurityContext("system/admin");

        ContainerResponse response = makeRequest(HttpMethod.GET, SERVICE_PATH + "/" + SUBSCRIPTION_ID, null, null);

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        SubscriptionDescriptor subscription = (SubscriptionDescriptor)response.getEntity();
        assertEquals(subscription.withLinks(null), convertToDescriptor(expectedSubscription));
        verify(subscriptionDao).getById(SUBSCRIPTION_ID);
    }

    @Test
    public void shouldBeAbleToGetSpecificSubscriptionByAccountOwner() throws Exception {
        Subscription expectedSubscription = createSubscription().withAccountId("ANOTHER_ACCOUNT_ID");
        when(subscriptionDao.getById(SUBSCRIPTION_ID)).thenReturn(expectedSubscription);
        when(accountDao.getByMember(USER_ID)).thenReturn(Arrays.asList(new Member().withRoles(Arrays.asList("account/owner"))
                                                                                   .withAccountId("ANOTHER_ACCOUNT_ID")
                                                                                   .withUserId(USER_ID)));
        prepareSecurityContext("user");

        ContainerResponse response = makeRequest(HttpMethod.GET, SERVICE_PATH + "/" + SUBSCRIPTION_ID, null, null);

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        SubscriptionDescriptor subscription = (SubscriptionDescriptor)response.getEntity();
        assertEquals(subscription.withLinks(null), convertToDescriptor(expectedSubscription));
        verify(subscriptionDao).getById(SUBSCRIPTION_ID);
    }

    @Test
    public void shouldBeAbleToGetSpecificSubscriptionByAccountMember() throws Exception {
        Subscription expectedSubscription = createSubscription().withAccountId("ANOTHER_ACCOUNT_ID");
        when(subscriptionDao.getById(SUBSCRIPTION_ID)).thenReturn(expectedSubscription);
        when(accountDao.getByMember(USER_ID)).thenReturn(Arrays.asList(new Member().withRoles(Arrays.asList("account/member"))
                                                                                   .withAccountId("ANOTHER_ACCOUNT_ID")
                                                                                   .withUserId(USER_ID)));
        prepareSecurityContext("user");

        ContainerResponse response = makeRequest(HttpMethod.GET, SERVICE_PATH + "/" + SUBSCRIPTION_ID, null, null);

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        SubscriptionDescriptor subscription = (SubscriptionDescriptor)response.getEntity();
        assertEquals(subscription.withLinks(null), convertToDescriptor(expectedSubscription));
        verify(subscriptionDao).getById(SUBSCRIPTION_ID);
    }

    @Test
    public void shouldRespondForbiddenIfUserIsNotMemberOrOwnerOfAccountOnGetSubscriptionById() throws Exception {
        ArrayList<Member> memberships = new ArrayList<>();
        Member am = new Member();
        am.withRoles(Arrays.asList("account/owner")).withAccountId("fake_id");
        memberships.add(am);

        when(accountDao.getByMember(USER_ID)).thenReturn(memberships);
        when(subscriptionDao.getById(SUBSCRIPTION_ID)).thenReturn(createSubscription());
        prepareSecurityContext("user");

        ContainerResponse response = makeRequest(HttpMethod.GET, SERVICE_PATH + "/" + SUBSCRIPTION_ID, null, null);

        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
        assertEquals(getErrorMessage(response.getEntity()), "Access denied");
    }

    @Test
    public void shouldNotAddSubscriptionIfBeforeAddSubscriptionValidationFails() throws Exception {
        prepareSuccessfulSubscriptionAddition();

        doThrow(new ConflictException("conflict")).when(abstractSubscriptionService).beforeCreateSubscription(
                Matchers.any(Subscription.class));

        ContainerResponse response =
                makeRequest(HttpMethod.POST, SERVICE_PATH, MediaType.APPLICATION_JSON, newSubscription);

        assertNotEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
        assertEquals(getErrorMessage(response.getEntity()), "conflict");
        verify(subscriptionDao, never()).create(Matchers.any(Subscription.class));
        verify(abstractSubscriptionService, never()).afterCreateSubscription(Matchers.any(Subscription.class));
    }

    @Test
    public void shouldBeAbleToAddSubscriptionWithoutCharge() throws Exception {
        prepareSuccessfulSubscriptionAddition();

        ContainerResponse response =
                makeRequest(HttpMethod.POST, SERVICE_PATH, MediaType.APPLICATION_JSON, newSubscription);

        assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
        SubscriptionDescriptor subscription = (SubscriptionDescriptor)response.getEntity();
        assertEquals(subscription.getAccountId(), ACCOUNT_ID);
        assertEquals(subscription.getPlanId(), PLAN_ID);
        assertEquals(subscription.getServiceId(), SERVICE_ID);
        assertEquals(subscription.getState(), SubscriptionState.ACTIVE);
        assertEquals(subscription.getBillingCycleType(), plan.getBillingCycleType());
        assertEquals(subscription.getBillingCycle(), plan.getBillingCycle());
        assertEquals(subscription.getBillingContractTerm(), plan.getBillingContractTerm());
        assertEquals(subscription.getDescription(), plan.getDescription());
        assertEquals(subscription.getProperties(), plan.getProperties());
        assertTrue(subscription.getUsePaymentSystem());

        assertNotNull(subscription.getId());
        assertNull(subscription.getTrialStartDate());
        assertNull(subscription.getTrialEndDate());

        verify(subscriptionDao).create(Matchers.argThat(new ArgumentMatcher<Subscription>() {
            @Override
            public boolean matches(Object argument) {
                Subscription actual = (Subscription)argument;

                assertEquals(actual.getAccountId(), ACCOUNT_ID);
                assertEquals(actual.getPlanId(), PLAN_ID);
                assertEquals(actual.getServiceId(), SERVICE_ID);
                assertEquals(actual.getState(), SubscriptionState.ACTIVE);
                assertEquals(actual.getBillingCycleType(), plan.getBillingCycleType());
                assertEquals(actual.getBillingCycle(), plan.getBillingCycle());
                assertEquals(actual.getBillingContractTerm(), plan.getBillingContractTerm());
                assertEquals(actual.getDescription(), plan.getDescription());
                assertEquals(actual.getProperties(), plan.getProperties());
                assertTrue(actual.getUsePaymentSystem());

                assertNotNull(actual.getId());
                return true;
            }
        }));
        verify(serviceRegistry).get(SERVICE_ID);
        verify(abstractSubscriptionService).beforeCreateSubscription(Matchers.any(Subscription.class));
        verify(abstractSubscriptionService).afterCreateSubscription(Matchers.any(Subscription.class));
    }

    @Test
    public void shouldBeAbleToAddSubscriptionWithoutChargingIfUsePaymentSystemSetToFalseAndUserIsSystemAdmin() throws Exception {
        prepareSuccessfulSubscriptionAddition();

        newSubscription.setUsePaymentSystem(false);

        prepareSecurityContext("system/admin");

        ContainerResponse response =
                makeRequest(HttpMethod.POST, SERVICE_PATH, MediaType.APPLICATION_JSON, newSubscription);

        assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
        verify(subscriptionDao).create(Matchers.any(Subscription.class));
    }

    @Test
    public void shouldBeAbleToAddSubscriptionWithoutChargingIfSubscriptionIsNotPaid() throws Exception {
        prepareSuccessfulSubscriptionAddition();
        plan.setPaid(false);
        newSubscription.setUsePaymentSystem(false);

        ContainerResponse response =
                makeRequest(HttpMethod.POST, SERVICE_PATH, MediaType.APPLICATION_JSON, newSubscription);

        assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
        verify(subscriptionDao).create(Matchers.any(Subscription.class));
    }

    @Test
    public void shouldRespondNotFoundIfSubscriptionIsNotFoundOnRemoveSubscription() throws Exception {
        when(subscriptionDao.getById(SUBSCRIPTION_ID)).thenThrow(new NotFoundException("subscription not found"));

        prepareSecurityContext("system/admin");

        ContainerResponse response =
                makeRequest(HttpMethod.DELETE, SERVICE_PATH + "/" + SUBSCRIPTION_ID, null, null);

        assertNotEquals(response.getStatus(), Response.Status.OK);
        assertEquals(getErrorMessage(response.getEntity()), "subscription not found");
        verify(subscriptionDao, never()).remove(Matchers.anyString());
        verify(subscriptionDao, never()).update(Matchers.any(Subscription.class));
    }

    @Test
    public void shouldRespondAccessDeniedIfUserIsNotAccountOwnerOnDeactivateSubscription() throws Exception {
        ArrayList<Member> memberships = new ArrayList<>(2);
        Member am = new Member().withRoles(Arrays.asList("account/owner"))
                                .withAccountId("fake_id");
        memberships.add(am);
        Member am2 = new Member().withRoles(Arrays.asList("account/member"))
                                 .withAccountId(ACCOUNT_ID);
        memberships.add(am2);

        when(accountDao.getByMember(USER_ID)).thenReturn(memberships);
        when(serviceRegistry.get(SERVICE_ID)).thenReturn(abstractSubscriptionService);
        when(subscriptionDao.getById(SUBSCRIPTION_ID)).thenReturn(createSubscription());
        prepareSecurityContext("user");

        ContainerResponse response =
                makeRequest(HttpMethod.DELETE, SERVICE_PATH + "/" + SUBSCRIPTION_ID, null, null);

        assertNotEquals(response.getStatus(), Response.Status.OK);
        assertEquals(getErrorMessage(response.getEntity()), "Access denied");
        verify(subscriptionDao, never()).deactivate(Matchers.anyString());
    }

    @Test
    public void shouldBeAbleToRemoveSubscriptionBySystemAdmin() throws Exception {
        when(serviceRegistry.get(SERVICE_ID)).thenReturn(abstractSubscriptionService);
        final Subscription subscription = createSubscription();
        when(subscriptionDao.getById(SUBSCRIPTION_ID)).thenReturn(subscription);
        prepareSecurityContext("system/admin");

        ContainerResponse response =
                makeRequest(HttpMethod.DELETE, SERVICE_PATH + "/" + SUBSCRIPTION_ID, null, null);

        assertEquals(response.getStatus(), Response.Status.NO_CONTENT.getStatusCode());
        verify(serviceRegistry).get(SERVICE_ID);
        verify(abstractSubscriptionService).onRemoveSubscription(Matchers.any(Subscription.class));
        verify(subscriptionDao).deactivate(SUBSCRIPTION_ID);
    }

    @Test
    public void shouldBeAbleToRemoveSubscriptionByAccountOwner() throws Exception {
        when(accountDao.getByMember(USER_ID)).thenReturn(memberships);
        when(serviceRegistry.get(SERVICE_ID)).thenReturn(abstractSubscriptionService);
        final Subscription subscription = createSubscription();
        when(subscriptionDao.getById(SUBSCRIPTION_ID)).thenReturn(subscription);
        when(planDao.getPlanById(PLAN_ID)).thenReturn(plan);
        prepareSecurityContext("user");

        ContainerResponse response =
                makeRequest(HttpMethod.DELETE, SERVICE_PATH + "/" + SUBSCRIPTION_ID, null, null);

        assertEquals(response.getStatus(), Response.Status.NO_CONTENT.getStatusCode());
        verify(serviceRegistry).get(SERVICE_ID);
        verify(abstractSubscriptionService).onRemoveSubscription(Matchers.any(Subscription.class));
        verify(subscriptionDao).deactivate(SUBSCRIPTION_ID);
    }

    @Test
    public void shouldRespondForbiddenIfSubscriptionIsInactiveOnDeactivateSubscription() throws Exception {
        when(accountDao.getByMember(USER_ID)).thenReturn(memberships);
        when(serviceRegistry.get(SERVICE_ID)).thenReturn(abstractSubscriptionService);
        Subscription subscription = createSubscription().withState(SubscriptionState.INACTIVE);

        when(subscriptionDao.getById(SUBSCRIPTION_ID)).thenReturn(subscription);
        prepareSecurityContext("user");

        ContainerResponse response =
                makeRequest(HttpMethod.DELETE, SERVICE_PATH + "/" + SUBSCRIPTION_ID, null, null);

        assertEquals(response.getStatus(), Response.Status.CONFLICT.getStatusCode());
        assertEquals(getErrorMessage(response.getEntity()), "Subscription with id " + subscription.getId() + " is inactive already");

        verify(subscriptionDao, never()).deactivate(Matchers.anyString());
    }

    @Test
    public void shouldBeAbleToConvertSubscriptionToDescriptor() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("codenvy:property", "value");
        properties.put("someproperty", "value");
        properties.put("codenvyProperty", "value");
        properties.put("codenvy:", "value");
        Subscription subscription = createSubscription().withProperties(properties);
        SubscriptionDescriptor expectedDescriptor = convertToDescriptor(subscription);
        Link[] expectedLinks = new Link[2];
        expectedLinks[0] = (DtoFactory.getInstance().createDto(Link.class)
                                      .withRel(Constants.LINK_REL_DEACTIVATE_SUBSCRIPTION)
                                      .withMethod(HttpMethod.DELETE)
                                      .withHref(SERVICE_PATH + "/" + SUBSCRIPTION_ID));
        expectedLinks[1] = (DtoFactory.getInstance().createDto(Link.class)
                                      .withRel(Constants.LINK_REL_GET_SUBSCRIPTION)
                                      .withMethod(HttpMethod.GET)
                                      .withHref(SERVICE_PATH + "/" + SUBSCRIPTION_ID)
                                      .withProduces(MediaType.APPLICATION_JSON));

        prepareSecurityContext("system/admin");

        SubscriptionDescriptor descriptor = getDescriptor(subscription);

        assertEqualsNoOrder(descriptor.getLinks().toArray(), expectedLinks);
        assertEquals(descriptor.withLinks(null), expectedDescriptor);
    }

    @Test
    public void shouldNotBeAbleToAddSubscriptionIfNoDataSent() throws Exception {
        prepareSuccessfulSubscriptionAddition();

        ContainerResponse response =
                makeRequest(HttpMethod.POST, SERVICE_PATH, MediaType.APPLICATION_JSON, null);

        assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
        assertEquals(getErrorMessage(response.getEntity()), "New subscription required");
    }

    @Test
    public void shouldNotBeAbleToAddSubscriptionIfAccountIdIsNotSent() throws Exception {
        prepareSuccessfulSubscriptionAddition();

        ContainerResponse response =
                makeRequest(HttpMethod.POST, SERVICE_PATH, MediaType.APPLICATION_JSON,
                            newSubscription.withAccountId(null));

        assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
        assertEquals(getErrorMessage(response.getEntity()), "Account identifier required");
    }

    @Test
    public void shouldNotBeAbleToAddSubscriptionIfPlanIdIsNotSent() throws Exception {
        prepareSuccessfulSubscriptionAddition();

        ContainerResponse response =
                makeRequest(HttpMethod.POST, SERVICE_PATH, MediaType.APPLICATION_JSON, newSubscription.withPlanId(null));

        assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
        assertEquals(getErrorMessage(response.getEntity()), "Plan identifier required");
    }

    @Test
    public void shouldNotBeAbleToRemoveSubscriptionWithSalesOnlyPlan() throws Exception {
        when(subscriptionDao.getById(SUBSCRIPTION_ID)).thenReturn(createSubscription());
        when(planDao.getPlanById(PLAN_ID)).thenReturn(plan.withSalesOnly(true));

        ContainerResponse response = makeRequest(HttpMethod.DELETE, SERVICE_PATH + "/" + SUBSCRIPTION_ID, null, null);

        assertEquals(response.getStatus(), Response.Status.FORBIDDEN.getStatusCode());
        assertEquals(getErrorMessage(response.getEntity()), "User not authorized to remove this subscription, please contact support");
    }

    @Test
    public void shouldRespondAccessDeniedIfUserIsNotAccountOwnerOnAddSubscription() throws Exception {
        prepareSuccessfulSubscriptionAddition();

        ArrayList<Member> memberships = new ArrayList<>(2);
        Member am = new Member();
        am.withRoles(Arrays.asList("account/owner")).withAccountId("fake_id");
        memberships.add(am);
        Member am2 = new Member();
        am2.withRoles(Arrays.asList("account/member")).withAccountId(ACCOUNT_ID);
        memberships.add(am2);

        when(accountDao.getByMember(USER_ID)).thenReturn(memberships);

        ContainerResponse response =
                makeRequest(HttpMethod.POST, SERVICE_PATH, MediaType.APPLICATION_JSON, newSubscription);

        assertNotEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
        assertEquals(getErrorMessage(response.getEntity()), "Access denied");
    }

    @Test
    public void shouldRespondForbiddenIfUserTryOverrideSubscriptionAttributes() throws Exception {
        prepareSuccessfulSubscriptionAddition();

        newSubscription.getProperties().put("key", "123");
        ContainerResponse response =
                makeRequest(HttpMethod.POST, SERVICE_PATH, MediaType.APPLICATION_JSON, newSubscription);

        assertEquals(response.getStatus(), Response.Status.FORBIDDEN.getStatusCode());
        assertEquals(getErrorMessage(response.getEntity()),
                     "User not authorized to add subscription with custom properties, please contact support");
    }

    @Test
    public void shouldRespondForbiddenIfAdminTryOverrideNonExistentSubscriptionAttributes() throws Exception {
        prepareSuccessfulSubscriptionAddition();

        prepareSecurityContext("system/admin");

        newSubscription.getProperties().put("nonExistentKey", "123");
        ContainerResponse response =
                makeRequest(HttpMethod.POST, SERVICE_PATH, MediaType.APPLICATION_JSON, newSubscription);

        assertEquals(response.getStatus(), Response.Status.FORBIDDEN.getStatusCode());
        assertEquals(getErrorMessage(response.getEntity()), "Forbidden overriding of non-existent plan properties");
    }

    @Test
    public void shouldBeAbleToAddSubscriptionWithCustomAttributes() throws Exception {
        prepareSuccessfulSubscriptionAddition();

        prepareSecurityContext("system/admin");

        Map<String, String> properties = new HashMap<>();
        properties.put("custom", "0");
        plan.setProperties(properties);
        when(planDao.getPlanById(PLAN_ID)).thenReturn(plan);

        newSubscription.getProperties().put("custom", "123");
        ContainerResponse response =
                makeRequest(HttpMethod.POST, SERVICE_PATH, MediaType.APPLICATION_JSON, newSubscription);

        assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
        SubscriptionDescriptor subscription = (SubscriptionDescriptor)response.getEntity();
        assertEquals(subscription.getProperties().get("custom"), "123");
    }

    @Test
    public void shouldRespondNotFoundIfPlanNotFoundOnAddSubscription() throws Exception {
        prepareSuccessfulSubscriptionAddition();

        when(planDao.getPlanById(PLAN_ID)).thenThrow(new NotFoundException("Plan not found"));

        ContainerResponse response =
                makeRequest(HttpMethod.POST, SERVICE_PATH, MediaType.APPLICATION_JSON, newSubscription);

        assertNotEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
        assertEquals(getErrorMessage(response.getEntity()), "Plan not found");
    }

    @Test
    public void shouldRespondConflictIfServiceIdIsUnknownOnAddSubscription() throws Exception {
        prepareSuccessfulSubscriptionAddition();

        when(serviceRegistry.get(SERVICE_ID)).thenReturn(null);

        ContainerResponse response =
                makeRequest(HttpMethod.POST, SERVICE_PATH, MediaType.APPLICATION_JSON, newSubscription);

        assertNotEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
        assertEquals(getErrorMessage(response.getEntity()), "Unknown serviceId is used");
    }

    @Test
    public void shouldRespondConflictIfIfUsePaymentSystemSetToFalseAndUserIsNotSystemAdminOnAddSubscription() throws Exception {
        prepareSuccessfulSubscriptionAddition();

        newSubscription.setUsePaymentSystem(false);

        ContainerResponse response =
                makeRequest(HttpMethod.POST, SERVICE_PATH, MediaType.APPLICATION_JSON, newSubscription);

        assertNotEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
        assertEquals(getErrorMessage(response.getEntity()), "Given value of attribute usePaymentSystem is not allowed");
        verify(accountDao).getByMember(USER_ID);
        verifyNoMoreInteractions(accountDao);
    }

    @Test
    public void shouldRespondConflictIfPlanIsForSalesOnlyAndUserIsNotSystemAdminOnAddSubscription() throws Exception {
        prepareSuccessfulSubscriptionAddition();

        plan.setSalesOnly(true);

        ContainerResponse response =
                makeRequest(HttpMethod.POST, SERVICE_PATH, MediaType.APPLICATION_JSON, newSubscription);

        assertNotEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
        assertEquals(getErrorMessage(response.getEntity()), "User not authorized to add this subscription, please contact support");
        verify(accountDao).getByMember(USER_ID);
        verifyNoMoreInteractions(accountDao);
    }

    @Test
    public void shouldAddGetByIdLinkOnlyToSubscriptionDescriptorIfSubscriptionIsInactive() throws Exception {
        List<Link> expectedLinks = new ArrayList<>();
        expectedLinks.add(DtoFactory.getInstance().createDto(Link.class)
                                    .withRel(Constants.LINK_REL_GET_SUBSCRIPTION)
                                    .withMethod(HttpMethod.GET)
                                    .withHref(SERVICE_PATH + "/" + SUBSCRIPTION_ID)
                                    .withProduces(MediaType.APPLICATION_JSON));
        Subscription subscription = createSubscription().withState(SubscriptionState.INACTIVE);

        prepareSecurityContext("system/admin");

        SubscriptionDescriptor descriptor = getDescriptor(subscription);

        assertEquals(descriptor.getLinks(), expectedLinks);
    }

    @Test
    public void shouldNotAddDeleteLinkToSubscriptionDescriptorIfUserHasNotRights() throws Exception {
        List<Link> expectedLinks = new ArrayList<>();
        expectedLinks.add(DtoFactory.getInstance().createDto(Link.class)
                                    .withRel(Constants.LINK_REL_GET_SUBSCRIPTION)
                                    .withMethod(HttpMethod.GET)
                                    .withHref(SERVICE_PATH + "/" + SUBSCRIPTION_ID)
                                    .withProduces(MediaType.APPLICATION_JSON));

        prepareAccountsRoles(Collections.singletonList("account/member"), ACCOUNT_ID);

        SubscriptionDescriptor descriptor = getDescriptor(createSubscription());

        assertEquals(descriptor.getLinks(), expectedLinks);
    }

    @Test
    public void shouldNotAddRestrictedPropertiesInSubscriptionDescriptorIfUserIsNotAccountOwner() throws Exception {
        prepareAccountsRoles(Collections.singletonList("account/member"), ACCOUNT_ID);

        Map<String, String> properties = new HashMap<>();
        properties.put("codenvy:property", "value");
        properties.put("someproperty", "value");
        properties.put("restricted:property", "value");
        Subscription subscription = createSubscription().withProperties(properties);

        prepareSecurityContext("account/member");

        SubscriptionDescriptor descriptor = getDescriptor(subscription);

        for (String property : descriptor.getProperties().keySet()) {
            assertFalse(property.startsWith("restricted:"));
        }
    }

    @Test
    public void shouldBeAbleToReturnDescriptorWithNullDates() throws Exception {
        prepareSecurityContext("system/admin");

        Subscription subscription = createSubscription()
                .withStartDate(null)
                .withEndDate(null)
                .withBillingStartDate(null)
                .withBillingEndDate(null)
                .withNextBillingDate(null);

        SubscriptionDescriptor descriptor = getDescriptor(subscription);

        assertEquals(descriptor.withLinks(null), convertToDescriptor(subscription));
    }

    private void prepareAccountsRoles(List<String> roles, final String accountId) throws NotFoundException, ServerException {
        when(accountDao.getByMember(anyString())).thenReturn(Collections.singletonList(new Member().withRoles(roles)
                                                                                                   .withAccountId(accountId)));
    }

    private Subscription createSubscription() {
        final Map<String, String> properties = new HashMap<>();
        properties.put("RAM", "2048");
        properties.put("Package", "Developer");
        return new Subscription()
                .withId(SUBSCRIPTION_ID)
                .withAccountId(ACCOUNT_ID)
                .withPlanId(PLAN_ID)
                .withServiceId(SERVICE_ID)
                .withProperties(properties)
                .withState(SubscriptionState.ACTIVE)
                .withDescription("description")
                .withUsePaymentSystem(true)
                .withStartDate(new Date())
                .withEndDate(new Date())
                .withBillingStartDate(new Date())
                .withNextBillingDate(new Date())
                .withBillingEndDate(new Date())
                .withBillingContractTerm(12)
                .withBillingCycleType(BillingCycleType.AutoRenew)
                .withBillingCycle(1);
    }

    private void prepareSuccessfulSubscriptionAddition() throws NotFoundException, ServerException {
        when(serviceRegistry.get(SERVICE_ID)).thenReturn(abstractSubscriptionService);
        when(planDao.getPlanById(PLAN_ID)).thenReturn(plan);
        when(accountDao.getByMember(USER_ID)).thenReturn(Arrays.asList(new Member().withRoles(Arrays.asList("account/owner"))
                                                                                   .withAccountId(ACCOUNT_ID)
                                                                                   .withUserId(USER_ID)));
        prepareSecurityContext("user");
    }

    private SubscriptionDescriptor getDescriptor(Subscription subscription) throws Exception {
        when(subscriptionDao.getActive(ACCOUNT_ID)).thenReturn(Arrays.asList(subscription));

        ContainerResponse response = makeRequest(HttpMethod.GET, SERVICE_PATH + "/find/account?id=" + ACCOUNT_ID, null, null);

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        @SuppressWarnings("unchecked") List<SubscriptionDescriptor> subscriptionDescriptors =
                (List<SubscriptionDescriptor>)response.getEntity();
        assertEquals(subscriptionDescriptors.size(), 1);
        return subscriptionDescriptors.get(0);
    }

    protected void prepareSecurityContext(String role) {
        when(securityContext.isUserInRole(Matchers.anyString())).thenReturn(false);
        if (!role.equals("system/admin") && !role.equals("system/manager")) {
            when(securityContext.isUserInRole("user")).thenReturn(true);
        }
        when(securityContext.isUserInRole(role)).thenReturn(true);
    }

    private SubscriptionDescriptor convertToDescriptor(Subscription subscription) {
        return DtoFactory.getInstance().createDto(SubscriptionDescriptor.class)
                         .withId(subscription.getId())
                         .withAccountId(subscription.getAccountId())
                         .withServiceId(subscription.getServiceId())
                         .withPlanId(subscription.getPlanId())
                         .withProperties(subscription.getProperties())
                         .withState(subscription.getState())
                         .withDescription(subscription.getDescription())
                         .withUsePaymentSystem(subscription.getUsePaymentSystem())
                         .withStartDate(dateToString(subscription.getStartDate()))
                         .withEndDate(dateToString(subscription.getEndDate()))
                         .withBillingStartDate(dateToString(subscription.getBillingStartDate()))
                         .withNextBillingDate(dateToString(subscription.getNextBillingDate()))
                         .withBillingEndDate(dateToString(subscription.getBillingEndDate()))
                         .withBillingContractTerm(subscription.getBillingContractTerm())
                         .withBillingCycleType(subscription.getBillingCycleType())
                         .withBillingCycle(subscription.getBillingCycle());
    }

    protected ContainerResponse makeRequest(String method, String path, String contentType, Object toSend) throws Exception {
        Map<String, List<String>> headers = null;
        if (contentType != null) {
            headers = new HashMap<>();
            headers.put("Content-Type", Arrays.asList(contentType));
        }
        byte[] data = null;
        if (toSend != null) {
            data = JsonHelper.toJson(toSend).getBytes();
        }
        return launcher.service(method, path, BASE_URI, headers, data, null, environmentContext);
    }

    private String dateToString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        dateFormat.setLenient(false);

        return null == date ? null : dateFormat.format(date);
    }

    private String getErrorMessage(Object entity) throws Exception {
        return JsonHelper.parseJson((String)entity).getElement("message").getStringValue();
    }
}