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
import com.codenvy.api.subscription.saas.server.billing.BillingService;
import com.codenvy.api.subscription.saas.server.billing.invoice.InvoiceCharger;
import com.codenvy.api.subscription.saas.server.billing.invoice.InvoiceFilter;
import com.codenvy.api.subscription.saas.server.billing.invoice.InvoiceRecharger;
import com.codenvy.api.subscription.saas.server.billing.PaymentState;
import com.codenvy.api.subscription.saas.shared.dto.Invoice;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class InvoiceChargingSchedulerTest {
    private static final int INVOICES_LIMIT = 20;

    final DtoFactory dto = DtoFactory.getInstance();
    @Mock
    private BillingService   billingService;
    @Mock
    private InvoiceRecharger invoiceRecharger;
    @Mock
    private InvoiceCharger   invoiceCharger;
    @Mock
    private AccountLocker    accountLocker;


    InvoiceChargingScheduler invoiceChargingScheduler;

    @BeforeMethod
    public void setUp() {
        invoiceChargingScheduler = new InvoiceChargingScheduler(billingService, invoiceRecharger, invoiceCharger, accountLocker,
                                                                INVOICES_LIMIT);
    }

    @Test
    public void shouldChargeNotPaidInvoices() throws Exception {
        final Invoice invoice = dto.createDto(Invoice.class)
                                   .withId(1L);
        when(billingService.getInvoices((InvoiceFilter)anyObject())).thenReturn(Collections.singletonList(invoice));

        invoiceChargingScheduler.chargeInvoices();

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

        verify(invoiceCharger).charge(argThat(new ArgumentMatcher<Invoice>() {
            @Override
            public boolean matches(Object o) {
                return ((Invoice)o).getId() == 1;
            }
        }));
    }

    @Test
    public void shouldRunLazyChargingIfErrorOccursOnChargeInvoice() throws Exception {
        final Invoice invoice = dto.createDto(Invoice.class)
                                   .withId(1L);
        when(billingService.getInvoices((InvoiceFilter)anyObject())).thenReturn(Collections.singletonList(invoice));
        doThrow(ApiException.class).when(invoiceCharger).charge((Invoice)anyObject());

        invoiceChargingScheduler.chargeInvoices();

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

        verify(invoiceCharger).charge(argThat(new ArgumentMatcher<Invoice>() {
            @Override
            public boolean matches(Object o) {
                return ((Invoice)o).getId() == 1;
            }
        }));
        verify(invoiceRecharger).scheduleCharging(1L);
    }
}
