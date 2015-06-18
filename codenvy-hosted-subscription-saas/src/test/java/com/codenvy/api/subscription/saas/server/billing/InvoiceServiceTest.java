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
package com.codenvy.api.subscription.saas.server.billing;

import com.codenvy.api.subscription.saas.server.AccountLocker;
import com.codenvy.api.subscription.saas.shared.dto.Invoice;
import com.codenvy.api.subscription.saas.shared.dto.InvoiceDescriptor;
import com.google.common.collect.FluentIterable;
import com.jayway.restassured.response.Response;

import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.account.server.dao.Member;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.user.UserImpl;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.everrest.core.impl.uri.UriBuilderImpl;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.ws.rs.core.UriInfo;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.codenvy.api.subscription.saas.server.billing.PaymentState.PAID_SUCCESSFULLY;
import static com.jayway.restassured.RestAssured.given;
import static java.util.Collections.singletonList;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link InvoiceService}
 *
 * @author Sergii Leschenko
 */
@Listeners({EverrestJetty.class, MockitoTestNGListener.class})
public class InvoiceServiceTest {
    @SuppressWarnings("unused")
    private final ApiExceptionMapper exceptionMapper = new ApiExceptionMapper();
    @SuppressWarnings("unused")
    private final EnvironmentFilter  filter          = new EnvironmentFilter();

    static final String       USER_ID = "user123";
    static final List<String> ROLES   = new ArrayList<>();


    @Mock
    private BillingService           billingService;
    @Mock
    private InvoiceTemplateProcessor invoiceTemplateProcessor;
    @Mock
    private BillingPeriod            billingPeriod;
    @Mock
    private Period                   period;
    @Mock
    private AccountDao               accountDao;
    @Mock
    private UriInfo                  uriInfo;
    @Mock
    private AccountLocker            accountLocker;
    @Mock
    private InvoiceCharger           invoiceCharger;

    @InjectMocks
    InvoiceService invoiceService;

    @BeforeMethod
    public void setUp(ITestContext context) throws Exception {
        prepareRoles(singletonList("user"));

        when(uriInfo.getBaseUriBuilder()).thenReturn(new UriBuilderImpl());
        final Field uriField = invoiceService.getClass()
                                             .getSuperclass()
                                             .getDeclaredField("uriInfo");
        uriField.setAccessible(true);
        uriField.set(invoiceService, uriInfo);

        when(billingPeriod.getCurrent()).thenReturn(period);
    }

    @Filter
    private class EnvironmentFilter implements RequestFilter {
        public void doFilter(GenericContainerRequest request) {
            EnvironmentContext context = EnvironmentContext.getCurrent();
            context.setUser(new UserImpl(ADMIN_USER_NAME, USER_ID, ADMIN_USER_PASSWORD, ROLES, false));
        }
    }

    @Test
    public void shouldBeAbleToGetInvoiceById() throws Exception {
        when(accountDao.getByMember(anyString())).thenReturn(singletonList(new Member().withUserId("userId")
                                                                                       .withAccountId("accountId")
                                                                                       .withRoles(singletonList("account/owner"))));

        Invoice invoice = newDto(Invoice.class).withId(123L)
                                               .withAccountId("accountId");
        when(billingService.getInvoice(123L)).thenReturn(invoice);

        Response response = given().auth()
                                   .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                   .accept("application/json")
                                   .when()
                                   .get(SECURE_PATH + "/invoice/123");

        assertEquals(response.getStatusCode(), 200);
        InvoiceDescriptor invoiceDescriptor = unwrapDto(response, InvoiceDescriptor.class);
        assertEquals(invoiceDescriptor.getId(), new Long(123));
        assertEquals(invoiceDescriptor.getAccountId(), "accountId");
        assertEquals(invoiceDescriptor.getLinks().size(), 2);
    }

    @Test
    public void shouldBeAbleToGetInvoiceByIdAsHtml() throws Exception {
        when(accountDao.getByMember(anyString())).thenReturn(singletonList(new Member().withUserId("userId")
                                                                                       .withAccountId("accountId")
                                                                                       .withRoles(singletonList("account/owner"))));
        final ArgumentCaptor<Writer> writer = ArgumentCaptor.forClass(Writer.class);
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                writer.getValue().write("Invoice template");
                return null;
            }
        }).when(invoiceTemplateProcessor).processTemplate((Invoice)anyObject(), writer.capture());

        Invoice invoice = newDto(Invoice.class).withId(123L)
                                               .withAccountId("accountId");
        when(billingService.getInvoice(123L)).thenReturn(invoice);

        Response response = given().auth()
                                   .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                   .when()
                                   .accept("text/html")
                                   .get(SECURE_PATH + "/invoice/123");

        assertEquals(response.getStatusCode(), 200);
        assertEquals(response.getBody().print(), "Invoice template");
    }

    @Test
    public void shouldNotBeAbleToGetInvoiceByIdIfUserDoesNotHaveAccountOwnerRole() throws Exception {
        Invoice invoice = newDto(Invoice.class).withId(123L)
                                               .withAccountId("accountId");
        when(billingService.getInvoice(123L)).thenReturn(invoice);

        Response response = given().auth()
                                   .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                   .when()
                                   .get(SECURE_PATH + "/invoice/123");

        assertEquals(response.getStatusCode(), 403);
        assertEquals(unwrapDto(response, ServiceError.class).getMessage(), "Access denied");
    }

    @Test
    public void shouldReturn404WhenInvoiceDoesNotExist() throws Exception {
        doThrow(new NotFoundException("Invoice does not exist")).when(billingService).getInvoice(123L);

        Response response = given().auth()
                                   .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                   .accept("text/html")
                                   .when()
                                   .get(SECURE_PATH + "/invoice/123");

        assertEquals(response.getStatusCode(), 404);
    }

    @Test
    public void shouldBeAbleToGetInvoices() throws Exception {
        when(accountDao.getByMember(anyString())).thenReturn(singletonList(new Member().withUserId("userId")
                                                                                       .withAccountId("account123")
                                                                                       .withRoles(singletonList("account/owner"))));
        when(billingService.getInvoices((InvoiceFilter)anyObject()))
                .thenReturn(singletonList(newDto(Invoice.class).withId(123L)));

        Response response = given().auth()
                                   .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                   .when()
                                   .get(SECURE_PATH + "/invoice/find?accountId=account123" +
                                        "&maxItems=10" +
                                        "&skipCount=11" +
                                        "&startPeriod=1" +
                                        "&endPeriod=2");
        assertEquals(response.getStatusCode(), 200);
        final List<Invoice> invoices = unwrapDtoList(response, Invoice.class);
        assertEquals(invoices.size(), 1);
        assertEquals(invoices.get(0).getId(), new Long(123));
        verify(billingService).getInvoices(argThat(new ArgumentMatcher<InvoiceFilter>() {
            @Override
            public boolean matches(Object o) {
                final InvoiceFilter filter = (InvoiceFilter)o;
                return filter.getAccountId().equals("account123")
                       && filter.getMaxItems() == 10
                       && filter.getSkipCount() == 11
                       && filter.getFromDate() == 1
                       && filter.getTillDate() == 2;
            }
        }));
    }

    @Test
    public void shouldNotBeAbleToGetInvoicesOfForeignAccount() throws Exception {
        Response response = given().auth()
                                   .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                   .when()
                                   .get(SECURE_PATH + "/invoice/find?accountId=account123");

        assertEquals(response.getStatusCode(), 403);
        assertEquals(unwrapDto(response, ServiceError.class).getMessage(), "Access denied");
    }

    @Test
    public void shouldBeAbleToGetInvoicesForAdminWhenMissedAccountIdParam() throws Exception {
        prepareRoles(singletonList("system/admin"));
        when(billingService.getInvoices((InvoiceFilter)anyObject()))
                .thenReturn(singletonList(newDto(Invoice.class).withId(123L)));

        Response response = given().auth()
                                   .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                   .when()
                                   .get(SECURE_PATH + "/invoice/find?maxItems=10" +
                                        "&skipCount=11" +
                                        "&startPeriod=1" +
                                        "&endPeriod=2");
        assertEquals(response.getStatusCode(), 200);
        final List<Invoice> invoices = unwrapDtoList(response, Invoice.class);
        assertEquals(invoices.size(), 1);
        assertEquals(invoices.get(0).getId(), new Long(123));
        verify(billingService).getInvoices(argThat(new ArgumentMatcher<InvoiceFilter>() {
            @Override
            public boolean matches(Object o) {
                final InvoiceFilter filter = (InvoiceFilter)o;
                return filter.getAccountId() == null
                       && filter.getMaxItems() == 10
                       && filter.getSkipCount() == 11
                       && filter.getFromDate() == 1
                       && filter.getTillDate() == 2;
            }
        }));
    }

    @Test(dataProvider = "invoiceChargingProvider")
    public void shouldBeAbleToChargerInvoice(Invoice invoice) throws Exception {
        when(accountDao.getByMember(anyString())).thenReturn(singletonList(new Member().withUserId("userId")
                                                                                       .withAccountId("account123")
                                                                                       .withRoles(singletonList("account/owner"))));

        when(billingService.getInvoice(123L)).thenReturn(invoice);

        Response response = given().auth()
                                   .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                   .accept("application/json")
                                   .when()
                                   .post(SECURE_PATH + "/invoice/123/charge");

        assertEquals(response.getStatusCode(), 204);
        verify(invoiceCharger).charge(invoice);
        verify(accountLocker).removePaymentLock("account123");
    }

    @DataProvider(name = "invoiceChargingProvider")
    public Object[][] invoiceChargingProvider() {
        final Invoice invoice = newDto(Invoice.class).withId(123L)
                                                     .withAccountId("account123");
        return new Object[][]{
                {invoice.withPaymentState(PaymentState.PAYMENT_FAIL.getState())},
                {invoice.withPaymentState(PaymentState.CREDIT_CARD_MISSING.getState())}
        };
    }

    @Test
    public void shouldNotBeAbleToChargerInvoiceThatHasPaidState() throws Exception {
        when(accountDao.getByMember(anyString())).thenReturn(singletonList(new Member().withUserId("userId")
                                                                                       .withAccountId("account123")
                                                                                       .withRoles(singletonList("account/owner"))));
        Invoice invoice = newDto(Invoice.class).withId(123L)
                                               .withAccountId("account123")
                                               .withPaymentState(PAID_SUCCESSFULLY.getState());
        when(billingService.getInvoice(123L)).thenReturn(invoice);

        Response response = given().auth()
                                   .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                   .accept("application/json")
                                   .when()
                                   .post(SECURE_PATH + "/invoice/123/charge");

        assertEquals(response.getStatusCode(), 409);
        assertEquals(unwrapDto(response, ServiceError.class).getMessage(), "Payment is not required for invoice with id 123");
    }

    @Test
    public void shouldNotBeAbleToGetInvoicesForUserWhenMissedAccountIdParam() {
        Response response = given().auth()
                                   .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                   .when()
                                   .get(SECURE_PATH + "/invoice/find");

        assertEquals(response.getStatusCode(), 400);
        assertEquals(unwrapDto(response, ServiceError.class).getMessage(), "Missed value of account id parameter");
    }

    @Test
    public void shouldBeAbleToGenerateInvoices() throws Exception {
        when(billingService.generateInvoices(anyLong(), anyLong())).thenReturn(152);

        Response response = given().auth()
                                   .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                   .when()
                                   .post(SECURE_PATH + "/invoice/generate?startPeriod=12&endPeriod=22");

        assertEquals(response.getStatusCode(), 200);
        assertEquals(response.getBody().print(), "152");
        verify(billingService).generateInvoices(12, 22);
    }

    @Test
    public void shouldGenerateInvoicesByPreviousPeriodIfQueryParametersAreMissed() throws Exception {
        when(period.getPreviousPeriod()).thenReturn(period);
        when(period.getStartDate()).thenReturn(new Date(1));
        when(period.getEndDate()).thenReturn(new Date(10));
        when(billingService.generateInvoices(anyLong(), anyLong())).thenReturn(152);

        Response response = given().auth()
                                   .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                   .contentType("application/json")
                                   .when()
                                   .post(SECURE_PATH + "/invoice/generate");

        assertEquals(response.getStatusCode(), 200);
        assertEquals(response.getBody().print(), "152");
        verify(billingService).generateInvoices(1, 10);
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

    private void prepareRoles(List<String> roles) {
        ROLES.clear();
        ROLES.addAll(roles);
    }
}