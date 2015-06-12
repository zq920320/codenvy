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
package com.codenvy.api.subscription.saas.server.schedulers;

import com.codenvy.api.subscription.saas.server.AccountLocker;
import com.codenvy.api.subscription.saas.server.InvoicePaymentService;
import com.codenvy.api.subscription.saas.server.billing.BillingService;
import com.codenvy.api.creditcard.server.CreditCardDao;
import com.codenvy.api.subscription.saas.server.billing.InvoiceFilter;
import com.codenvy.api.subscription.saas.server.billing.PaymentState;
import com.codenvy.api.creditcard.shared.dto.CreditCard;
import com.codenvy.api.subscription.saas.server.schedulers.MeterBasedCharger;
import com.codenvy.api.subscription.saas.shared.dto.Invoice;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link MeterBasedCharger}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class MeterBasedChargerTest {
    private static final int INVOICES_LIMIT = 20;

    final DtoFactory dto = DtoFactory.getInstance();

    @Mock
    BillingService        billingService;
    @Mock
    InvoicePaymentService invoicePaymentService;
    @Mock
    CreditCardDao         creditCardDao;
    @Mock
    AccountLocker         accountLocker;

    MeterBasedCharger meterBasedCharger;

    @BeforeMethod
    public void setUp() {
        meterBasedCharger = new MeterBasedCharger(invoicePaymentService, billingService, creditCardDao, accountLocker, INVOICES_LIMIT);
    }

    @Test
    public void shouldPaymentStateToNotRequiredForInvoiceWithTotal0() throws Exception {
        final Invoice invoice = dto.createDto(Invoice.class)
                                   .withTotal(0D)
                                   .withId(1L);
        when(billingService.getInvoices((InvoiceFilter)anyObject())).thenReturn(Arrays.asList(invoice));

        meterBasedCharger.chargeInvoices();

        verify(billingService).getInvoices(argThat(new ArgumentMatcher<InvoiceFilter>() {
            @Override
            public boolean matches(Object o) {
                final InvoiceFilter invoiceFilter = (InvoiceFilter)o;

                final String[] states = invoiceFilter.getStates();
                return states.length == 1
                       && PaymentState.WAITING_EXECUTOR.getState().equals(states[0])
                       && INVOICES_LIMIT == invoiceFilter.getMaxItems()
                       && invoiceFilter.getSkipCount() == null;
            }
        }));

        verify(billingService).setPaymentState(eq(1L), eq(PaymentState.NOT_REQUIRED), (String)isNull());
    }

    @Test
    public void shouldPaymentStateToCreditCardMissingForInvoiceWithTotalMoreThan0ButAccountHasNotCreditCard() throws Exception {
        when(billingService.getInvoices((InvoiceFilter)anyObject())).thenReturn(Arrays.asList(dto.createDto(Invoice.class)
                                                                                                 .withTotal(100D)
                                                                                                 .withId(1L)));
        when(creditCardDao.getCards(anyString())).thenReturn(Collections.<CreditCard>emptyList());

        meterBasedCharger.chargeInvoices();

        verify(billingService).getInvoices(argThat(new ArgumentMatcher<InvoiceFilter>() {
            @Override
            public boolean matches(Object o) {
                final InvoiceFilter invoiceFilter = (InvoiceFilter)o;

                final String[] states = invoiceFilter.getStates();
                return states.length == 1
                       && PaymentState.WAITING_EXECUTOR.getState().equals(states[0])
                       && INVOICES_LIMIT == invoiceFilter.getMaxItems()
                       && invoiceFilter.getSkipCount() == null;
            }
        }));

        verify(billingService).setPaymentState(eq(1L), eq(PaymentState.CREDIT_CARD_MISSING), (String)isNull());
    }

    @Test
    public void shouldPayInvoiceWithTotalMoreThan0() throws Exception {
        when(billingService.getInvoices((InvoiceFilter)anyObject())).thenReturn(Collections.singletonList(dto.createDto(Invoice.class)
                                                                                                             .withTotal(100D)
                                                                                                             .withId(1L)));
        when(creditCardDao.getCards(anyString())).thenReturn(Collections.singletonList(dto.createDto(CreditCard.class)
                                                                                          .withToken("ccToken")));

        meterBasedCharger.chargeInvoices();

        verify(billingService).getInvoices(argThat(new ArgumentMatcher<InvoiceFilter>() {
            @Override
            public boolean matches(Object o) {
                final InvoiceFilter invoiceFilter = (InvoiceFilter)o;

                final String[] states = invoiceFilter.getStates();
                return states.length == 1
                       && PaymentState.WAITING_EXECUTOR.getState().equals(states[0])
                       && INVOICES_LIMIT == invoiceFilter.getMaxItems()
                       && invoiceFilter.getSkipCount() == null;
            }
        }));

        verify(invoicePaymentService).charge(argThat(new ArgumentMatcher<Invoice>() {
            @Override
            public boolean matches(Object o) {
                return ((Invoice)o).getId() == 1L;
            }
        }));
        verify(billingService).setPaymentState(eq(1L), eq(PaymentState.PAID_SUCCESSFULLY), eq("ccToken"));
    }

    @Test
    public void shouldSetPaymentStateToPaymentFailedIfWereSomeChargingTroubles() throws Exception {
        when(billingService.getInvoices((InvoiceFilter)anyObject())).thenReturn(Arrays.asList(dto.createDto(Invoice.class)
                                                                                                 .withTotal(100D)
                                                                                                 .withId(1L)));
        when(creditCardDao.getCards(anyString())).thenReturn(Arrays.asList(dto.createDto(CreditCard.class)
                                                                              .withToken("ccToken")));

        doThrow(new ServerException("Exception")).when(invoicePaymentService).charge((Invoice)anyObject());

        meterBasedCharger.chargeInvoices();

        verify(billingService).getInvoices(argThat(new ArgumentMatcher<InvoiceFilter>() {
            @Override
            public boolean matches(Object o) {
                final InvoiceFilter invoiceFilter = (InvoiceFilter)o;

                final String[] states = invoiceFilter.getStates();
                return states.length == 1
                       && PaymentState.WAITING_EXECUTOR.getState().equals(states[0])
                       && INVOICES_LIMIT == invoiceFilter.getMaxItems()
                       && invoiceFilter.getSkipCount() == null;
            }
        }));

        verify(invoicePaymentService).charge(argThat(new ArgumentMatcher<Invoice>() {
            @Override
            public boolean matches(Object o) {
                return ((Invoice)o).getId() == 1L;
            }
        }));
        verify(billingService).setPaymentState(eq(1L), eq(PaymentState.PAYMENT_FAIL), eq("ccToken"));
    }

}