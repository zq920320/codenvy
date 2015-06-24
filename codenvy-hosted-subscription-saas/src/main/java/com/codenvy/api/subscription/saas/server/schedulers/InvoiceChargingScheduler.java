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
import org.eclipse.che.commons.schedule.ScheduleDelay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;

/**
 * Charges all unpaid invoices
 *
 * @author Sergii Leschenko
 */
@Singleton
public class InvoiceChargingScheduler {
    private static final Logger LOG = LoggerFactory.getLogger(InvoiceChargingScheduler.class);

    private static final String INVOICE_FETCH_LIMIT = "billing.invoice.fetch.limit";

    private final BillingService   billingService;
    private final InvoiceRecharger invoiceRecharger;
    private final InvoiceCharger   invoiceCharger;
    private final AccountLocker    accountLocker;
    private final int              invoices_limit;

    @Inject
    public InvoiceChargingScheduler(BillingService billingService,
                                    InvoiceRecharger invoiceRecharger,
                                    InvoiceCharger invoiceCharger,
                                    AccountLocker accountLocker,
                                    @Named(INVOICE_FETCH_LIMIT) int invoices_limit) {
        this.billingService = billingService;
        this.invoiceRecharger = invoiceRecharger;
        this.invoiceCharger = invoiceCharger;
        this.accountLocker = accountLocker;
        this.invoices_limit = invoices_limit;
    }

    @ScheduleDelay(delay = 60)
    void chargeInvoices() {
        try {
            List<Invoice> notPaidInvoices = billingService.getInvoices(InvoiceFilter.builder()
                                                                                    .withPaymentStates(PaymentState.WAITING_EXECUTOR)
                                                                                    .withMaxItems(invoices_limit)
                                                                                    .build());
            for (Invoice invoice : notPaidInvoices) {
                try {
                    invoiceCharger.charge(invoice);
                    accountLocker.removePaymentLock(invoice.getAccountId());
                } catch (ApiException e) {
                    invoiceRecharger.scheduleCharging(invoice.getId());
                    LOG.error("Can't pay invoice " + invoice.getId(), e);
                }
            }
        } catch (Exception e) {
            LOG.error("Error of invoices charging. " + e.getLocalizedMessage(), e);
        }
    }
}
