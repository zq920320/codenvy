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
import com.codenvy.api.subscription.saas.server.billing.Bonus;
import com.codenvy.api.subscription.saas.server.billing.BonusFilter;
import com.codenvy.api.subscription.saas.server.billing.Period;
import com.codenvy.api.subscription.saas.server.dao.BonusDao;
import com.codenvy.api.subscription.saas.server.dao.MeterBasedStorage;
import com.codenvy.api.subscription.saas.server.service.util.SubscriptionMailSender;
import com.codenvy.api.subscription.saas.shared.dto.BonusDescriptor;
import com.codenvy.api.subscription.saas.shared.dto.NewBonus;
import com.codenvy.api.subscription.saas.shared.dto.Resources;
import com.codenvy.api.subscription.server.dao.Subscription;
import com.codenvy.api.subscription.server.dao.SubscriptionDao;
import com.codenvy.api.subscription.shared.dto.ProvidedResourcesDescriptor;
import com.codenvy.api.subscription.shared.dto.WorkspaceResources;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.jayway.restassured.response.Response;

import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.api.user.server.dao.PreferenceDao;
import org.eclipse.che.api.user.server.dao.UserDao;
import org.eclipse.che.api.workspace.server.dao.Workspace;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.user.UserImpl;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.assured.EverrestJetty;
import org.everrest.assured.JettyHttpServer;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.everrest.core.impl.uri.UriBuilderImpl;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.ws.rs.core.UriInfo;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link SaasService}
 *
 * @author Sergii Leschenko
 */
@Listeners({EverrestJetty.class, MockitoTestNGListener.class})
public class SaasServiceTest {
    private final static double PROMOTION_RESOURCES_SIZE = 20;

    private static final String ACCOUNT_ID = "accountId";

    @SuppressWarnings("unused")
    private final ApiExceptionMapper exceptionMapper = new ApiExceptionMapper();
    @SuppressWarnings("unused")
    private final EnvironmentFilter  filter          = new EnvironmentFilter();

    @Mock
    private BillingService         billingService;
    @Mock
    private BillingPeriod          billingPeriod;
    @Mock
    private Period                 period;
    @Mock
    private AccountDao             accountDao;
    @Mock
    private SubscriptionDao        subscriptionDao;
    @Mock
    private BonusDao               bonusDao;
    @Mock
    private PreferenceDao          preferenceDao;
    @Mock
    private UserDao                userDao;
    @Mock
    private SubscriptionMailSender mailSender;
    @Mock
    private MeterBasedStorage      meterBasedStorage;
    @Mock
    private WorkspaceDao           workspaceDao;
    @Mock
    private UriInfo                uriInfo;

    @SuppressWarnings("unused")
    private SaasService saasService;

    @BeforeMethod
    public void setUp() throws Exception {
        saasService = new SaasService(PROMOTION_RESOURCES_SIZE,
                                      billingService,
                                      billingPeriod,
                                      accountDao,
                                      subscriptionDao,
                                      bonusDao,
                                      preferenceDao,
                                      userDao,
                                      mailSender,
                                      meterBasedStorage,
                                      workspaceDao);

        when(uriInfo.getBaseUriBuilder()).thenReturn(new UriBuilderImpl());
        final Field uriField = saasService.getClass()
                                          .getSuperclass()
                                          .getDeclaredField("uriInfo");
        uriField.setAccessible(true);
        uriField.set(saasService, uriInfo);

        when(billingPeriod.getCurrent()).thenReturn(period);
    }

    @Filter
    private class EnvironmentFilter implements RequestFilter {
        public void doFilter(GenericContainerRequest request) {
            EnvironmentContext context = EnvironmentContext.getCurrent();
            context.setUser(new UserImpl(JettyHttpServer.ADMIN_USER_NAME, "id-2314", "token-2323",
                                         Collections.<String>emptyList(), false));
        }
    }

    @Test
    public void shouldBeAbleToCreateBonus() throws Exception {
        when(bonusDao.create((Bonus)anyObject())).thenReturn(new Bonus().withId(1)
                                                                        .withAccountId(ACCOUNT_ID));

        NewBonus newBonus = newDto(NewBonus.class).withAccountId(ACCOUNT_ID)
                                                  .withResources(20D)
                                                  .withFromDate(1)
                                                  .withTillDate(120)
                                                  .withCause("VIP");

        Response response = given().auth()
                                   .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                   .contentType("application/json")
                                   .body(newBonus)
                                   .when()
                                   .post(SECURE_PATH + "/saas/bonus");

        assertEquals(response.getStatusCode(), 200);

        BonusDescriptor bonus = unwrapDto(response, BonusDescriptor.class);
        assertEquals(bonus.getId(), new Long(1L));

        verify(bonusDao).create(argThat(new ArgumentMatcher<Bonus>() {
            @Override
            public boolean matches(Object o) {
                return true;
            }
        }));
        verify(mailSender).sendBonusNotification((Bonus)anyObject());
    }

    @Test
    public void shouldThrowForbiddenOnCreateBonusWithNullBody() throws Exception {
        when(period.getStartDate()).thenReturn(new Date());
        Response response = given().auth()
                                   .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                   .contentType("application/json")
                                   .when()
                                   .post(SECURE_PATH + "/saas/bonus");

        assertEquals(response.getStatusCode(), 403);
        assertEquals("Bonus required", unwrapDto(response, ServiceError.class).getMessage());
    }

    @Test
    public void shouldThrowForbiddenOnCreateBonusWithNewBonusWhichDoesNotHaveAccountId() throws Exception {
        NewBonus newBonus = newDto(NewBonus.class).withResources(20D)
                                                  .withFromDate(1)
                                                  .withTillDate(120)
                                                  .withCause("VIP");

        Response response = given().auth()
                                   .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                   .contentType("application/json")
                                   .body(newBonus)
                                   .when()
                                   .post(SECURE_PATH + "/saas/bonus");

        assertEquals(response.getStatusCode(), 403);
        assertEquals("Account id required", unwrapDto(response, ServiceError.class).getMessage());
    }

    @Test
    public void shouldReturnsBonusesByCurrentBillingPeriodIfStartAndEndPeriodParametersAreMissed() throws Exception {
        when(period.getStartDate()).thenReturn(new Date(1));
        when(period.getEndDate()).thenReturn(new Date(10));

        Response response = given().auth()
                                   .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                   .when()
                                   .get(SECURE_PATH + "/saas/bonus");
        assertEquals(response.getStatusCode(), 200);

        verify(billingPeriod, times(2)).getCurrent();
        verify(bonusDao).getBonuses(argThat(new ArgumentMatcher<BonusFilter>() {
            @Override
            public boolean matches(Object o) {
                BonusFilter filter = (BonusFilter)o;
                return filter.getFromDate() == 1
                       && filter.getTillDate() == 10;
            }
        }));
    }

    @Test
    public void shouldUseFilterWithFieldsWhichAreFilledFromQueryParametersForLoadingBonusesFromBonusDao() throws Exception {
        Response response = given().auth()
                                   .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                   .when()
                                   .get(SECURE_PATH + "/saas/bonus?accountId=someAccount" +
                                        "&startPeriod=123" +
                                        "&endPeriod=234" +
                                        "&maxItems=10" +
                                        "&skipCount=15" +
                                        "&cause=promotion");
        assertEquals(response.getStatusCode(), 200);

        verify(bonusDao).getBonuses(argThat(new ArgumentMatcher<BonusFilter>() {
            @Override
            public boolean matches(Object o) {
                BonusFilter filter = (BonusFilter)o;
                return "someAccount".equals(filter.getAccountId())
                       && "promotion".equals(filter.getCause())
                       && filter.getMaxItems() == 10
                       && filter.getSkipCount() == 15
                       && filter.getFromDate() == 123
                       && filter.getTillDate() == 234;
            }
        }));
    }

    @Test
    public void shouldBeAbleToRemoveBonus() throws Exception {
        Response response = given().auth()
                                   .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                   .when()
                                   .delete(SECURE_PATH + "/saas/bonus/123");

        assertEquals(response.getStatusCode(), 204);

        verify(bonusDao).remove(eq(123L), anyLong());
    }

    @Test
    public void shouldBeAbleToGetProvidedResources() throws Exception {
        when(period.getStartDate()).thenReturn(new Date(1));
        when(period.getEndDate()).thenReturn(new Date(10));

        Response response = given().auth()
                                   .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                   .when()
                                   .get(SECURE_PATH + "/saas/resources/" + ACCOUNT_ID + "/provided");
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void shouldReturnProvidedResourcesForAccountWhichDoesNotHaveAnySubscriptions() throws Exception {
        when(period.getStartDate()).thenReturn(new Date(1));
        when(period.getEndDate()).thenReturn(new Date(10));
        when(billingService.getProvidedFreeResources(eq(ACCOUNT_ID), anyLong(), anyLong())).thenReturn(10D);

        Response response = given().auth()
                                   .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                   .when()
                                   .get(SECURE_PATH + "/saas/resources/" + ACCOUNT_ID + "/provided");
        assertEquals(response.getStatusCode(), 200);
        ProvidedResourcesDescriptor providedResources = unwrapDto(response, ProvidedResourcesDescriptor.class);
        assertEquals(providedResources.getFreeAmount(), 10D);
        assertEquals(providedResources.getPrepaidAmount(), 0D);
        verify(subscriptionDao).getActiveByServiceId(ACCOUNT_ID, "Saas");
        verify(billingService).getProvidedFreeResources(ACCOUNT_ID, 1L, 10L);
    }

    @Test
    public void shouldReturnProvidedResourcesForAccountWhichHasPrepaidSubscription() throws Exception {
        when(period.getStartDate()).thenReturn(new Date(1));
        when(period.getEndDate()).thenReturn(new Date(10));
        when(billingService.getProvidedFreeResources(eq(ACCOUNT_ID), anyLong(), anyLong())).thenReturn(10D);
        when(subscriptionDao.getActiveByServiceId(anyString(), anyString()))
                .thenReturn(new Subscription().withPlanId("prepaid")
                                              .withUsePaymentSystem(false)
                                              .withProperties(ImmutableMap.of("PrepaidGbH", "100")));

        Response response = given().auth()
                                   .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                   .when()
                                   .get(SECURE_PATH + "/saas/resources/" + ACCOUNT_ID + "/provided");
        assertEquals(response.getStatusCode(), 200);
        ProvidedResourcesDescriptor providedResources = unwrapDto(response, ProvidedResourcesDescriptor.class);
        assertEquals(providedResources.getFreeAmount(), 10D);
        assertEquals(providedResources.getPrepaidAmount(), 100D);
        verify(subscriptionDao).getActiveByServiceId(ACCOUNT_ID, "Saas");
        verify(billingService).getProvidedFreeResources(ACCOUNT_ID, 1L, 10L);
    }

    @Test
    public void shouldReturnProvidedResourcesForAccountWhichHasPayAsYouGoSubscription() throws Exception {
        when(period.getStartDate()).thenReturn(new Date(1));
        when(period.getEndDate()).thenReturn(new Date(10));
        when(billingService.getProvidedFreeResources(eq(ACCOUNT_ID), anyLong(), anyLong())).thenReturn(10D);
        when(subscriptionDao.getActiveByServiceId(anyString(), anyString())).thenReturn(new Subscription().withPlanId("pay-as-you-go"));

        Response response = given().auth()
                                   .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                   .when()
                                   .get(SECURE_PATH + "/saas/resources/" + ACCOUNT_ID + "/provided");
        assertEquals(response.getStatusCode(), 200);
        ProvidedResourcesDescriptor providedResources = unwrapDto(response, ProvidedResourcesDescriptor.class);
        assertEquals(providedResources.getFreeAmount(), 10D);
        assertEquals(providedResources.getPrepaidAmount(), 0D);
        verify(subscriptionDao).getActiveByServiceId(ACCOUNT_ID, "Saas");
        verify(billingService).getProvidedFreeResources(ACCOUNT_ID, 1L, 10L);
    }

    @Test
    public void shouldBeAbleToGetAccountResources() throws Exception {
        when(period.getStartDate()).thenReturn(new Date());
        when(workspaceDao.getByAccount(ACCOUNT_ID)).thenReturn(Collections.singletonList(new Workspace().withId("ws_id")
                                                                                                        .withAccountId(ACCOUNT_ID)));

        when(meterBasedStorage.getMemoryUsedReport(eq(ACCOUNT_ID), anyLong(), anyLong())).thenReturn(new HashMap<String, Double>());

        Response response = given().auth()
                                   .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                   .when()
                                   .get(SECURE_PATH + "/saas/resources/" + ACCOUNT_ID + "/used");

        assertEquals(response.getStatusCode(), 200);

        List<WorkspaceResources> result = unwrapDtoList(response, WorkspaceResources.class);

        assertEquals(result.size(), 1);
    }

    @Test
    public void shouldReturnUsedResourcesByWorkspacesOnGetUsedAccountResources() throws Exception {
        when(period.getStartDate()).thenReturn(new Date());
        when(workspaceDao.getByAccount(ACCOUNT_ID)).thenReturn(Collections.singletonList(new Workspace().withId("ws_id")
                                                                                                        .withAccountId(ACCOUNT_ID)));
        when(meterBasedStorage.getMemoryUsedReport(eq(ACCOUNT_ID), anyLong(), anyLong())).thenReturn(ImmutableMap.of("ws_id", 123D));

        Response response = given().auth()
                                   .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                   .when()
                                   .get(SECURE_PATH + "/saas/resources/" + ACCOUNT_ID + "/used");

        assertEquals(response.getStatusCode(), 200);

        List<WorkspaceResources> result = unwrapDtoList(response, WorkspaceResources.class);
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getWorkspaceId(), "ws_id");
        assertEquals(result.get(0).getMemory(), 123D);
    }

    @Test
    public void shouldReturnUsedResourcesByWorkspaceWhichDoesNotUseResourcesOnGetUsedAccountResources() throws Exception {
        when(period.getStartDate()).thenReturn(new Date());
        when(workspaceDao.getByAccount(ACCOUNT_ID)).thenReturn(Collections.singletonList(new Workspace().withId("workspaceID")
                                                                                                        .withAccountId(ACCOUNT_ID)));

        Response response = given().auth()
                                   .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                   .when()
                                   .get(SECURE_PATH + "/saas/resources/" + ACCOUNT_ID + "/used");

        assertEquals(response.getStatusCode(), 200);

        List<WorkspaceResources> result = unwrapDtoList(response, WorkspaceResources.class);
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getWorkspaceId(), "workspaceID");
        assertEquals(result.get(0).getMemory(), 0D);
    }

    @Test
    public void shouldReturnUsedResourcesByWorkspaceWhichNotReturnsByWorkspaceDaoOnGetUsedAccountResources() throws Exception {
        when(period.getStartDate()).thenReturn(new Date());
        when(meterBasedStorage.getMemoryUsedReport(eq(ACCOUNT_ID), anyLong(), anyLong())).thenReturn(ImmutableMap.of("ws_id", 123D));

        Response response = given().auth()
                                   .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                   .when()
                                   .get(SECURE_PATH + "/saas/resources/" + ACCOUNT_ID + "/used");

        assertEquals(response.getStatusCode(), 200);

        List<WorkspaceResources> result = unwrapDtoList(response, WorkspaceResources.class);
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getWorkspaceId(), "ws_id");
        assertEquals(result.get(0).getMemory(), 123D);
    }

    @Test
    public void shouldBeAbleToGetEstimatedResources() throws Exception {
        when(billingService.getEstimatedUsage(anyLong(), anyLong())).thenReturn(newDto(Resources.class));

        Response response = given().auth()
                                   .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                   .when()
                                   .get(SECURE_PATH + "/saas/resources?startPeriod=10&endPeriod=20");

        assertEquals(response.getStatusCode(), 200);
        verify(billingService).getEstimatedUsage(10, 20);
    }

    @Test
    public void shouldReturnEstimatedResourcesFromStartOfCurrentBillingPeriodToNowWhenStartPeriodAndEndPeriodAreMissed() throws Exception {
        when(billingService.getEstimatedUsage(anyLong(), anyLong())).thenReturn(newDto(Resources.class));
        when(period.getStartDate()).thenReturn(new Date(10));
        when(period.getEndDate()).thenReturn(new Date(20));

        Response response = given().auth()
                                   .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                   .when()
                                   .get(SECURE_PATH + "/saas/resources");

        assertEquals(response.getStatusCode(), 200);
        verify(billingService).getEstimatedUsage(10, 20);
    }

    private static <T> T newDto(Class<T> clazz) {
        return DtoFactory.getInstance().createDto(clazz);
    }

    private static <T> T unwrapDto(Response response, Class<T> dtoClass) {
        return DtoFactory.getInstance().createDtoFromJson(response.body().print(), dtoClass);
    }

    private static <T> List<T> unwrapDtoList(Response response, Class<T> dtoClass) {
        return FluentIterable.from(DtoFactory.getInstance().createListDtoFromJson(response.body().print(), dtoClass)).toList();
    }
}
