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

import com.codenvy.api.creditcard.server.event.CreditCardRegistrationEvent;
import com.codenvy.api.subscription.saas.server.AccountLocker;
import com.codenvy.api.subscription.saas.shared.dto.Invoice;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.codenvy.api.creditcard.server.event.CreditCardRegistrationEvent.EventType.CREDIT_CARD_ADDED;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link InvoiceRecharger}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class InvoiceRechargerTest {
    private static final int ATTEMPT_PERIOD = 50;

    DtoFactory dtoFactory = DtoFactory.getInstance();

    @Mock
    BillingService billingService;
    @Mock
    AccountLocker  accountLocker;
    @Mock
    InvoiceCharger invoiceCharger;
    @Mock
    EventService   eventService;

    InvoiceRecharger invoiceRecharger;

    @BeforeMethod
    public void setUp() {
        InvoiceRecharger.usedTimeUnit = TimeUnit.MILLISECONDS;
        invoiceRecharger = new InvoiceRecharger(billingService,
                                                invoiceCharger,
                                                eventService,
                                                accountLocker,
                                                ATTEMPT_PERIOD,
                                                2);
    }

    @Test
    public void shouldRetryToPayInvoiceTwiceWhenApiExceptionOccurs() throws Exception {
        Invoice invoice = dtoFactory.createDto(Invoice.class).withId(1L)
                                    .withPaymentState(PaymentState.PAYMENT_FAIL.getState());
        when(billingService.getInvoice(1L)).thenReturn(invoice);
        doThrow(ApiException.class).when(invoiceCharger).charge((Invoice)anyObject());

        invoiceRecharger.scheduleCharging(1L);

        Thread.sleep(ATTEMPT_PERIOD * 5);

        verify(invoiceCharger, times(2)).charge(invoice);
    }


    @Test(dataProvider = "invoicesProvider")
    public void shouldRechargeInvoice(Invoice invoice) throws Exception {
        when(billingService.getInvoice(1L)).thenReturn(invoice);

        invoiceRecharger.scheduleCharging(1L);

        Thread.sleep(ATTEMPT_PERIOD * 3);

        verify(invoiceCharger, times(1)).charge(invoice);
        verify(accountLocker).removePaymentLock(invoice.getAccountId());
    }

    @DataProvider(name = "invoicesProvider")
    public Object[][] invoicesProvider() {
        final Invoice invoice = dtoFactory.createDto(Invoice.class).withId(1L)
                                          .withAccountId("account");
        return new Object[][]{
                {invoice.withPaymentState(PaymentState.PAYMENT_FAIL.getState())},
                {invoice.withPaymentState(PaymentState.CREDIT_CARD_MISSING.getState())}
        };
    }


    @Test
    public void shouldPayInvoiceAfterAddingOfCreditCardIfAccountHaveUnpaidInvoice() throws Exception {
        Invoice invoice = dtoFactory.createDto(Invoice.class).withId(1L)
                                    .withPaymentState(PaymentState.PAYMENT_FAIL.getState());
        when(billingService.getInvoices((InvoiceFilter)anyObject())).thenReturn(Collections.singletonList(invoice));

        invoiceRecharger.onEvent(new CreditCardRegistrationEvent(CREDIT_CARD_ADDED, "account", "user", null));

        verify(billingService).getInvoices(argThat(new ArgumentMatcher<InvoiceFilter>() {
            @Override
            public boolean matches(Object o) {
                InvoiceFilter filter = (InvoiceFilter)o;
                List<String> states = Arrays.asList(filter.getStates());
                return "account".equals(filter.getAccountId())
                       && states.size() == 2
                       && states.contains(PaymentState.CREDIT_CARD_MISSING.getState())
                       && states.contains(PaymentState.PAYMENT_FAIL.getState());
            }
        }));
        verify(invoiceCharger).charge(invoice);
    }
}
