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
import com.codenvy.api.subscription.saas.shared.dto.Invoice;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
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
public class MeterBasedCharger {
    private static final Logger LOG = LoggerFactory.getLogger(MeterBasedCharger.class);

    private static final String INVOICE_FETCH_LIMIT = "billing.invoice.fetch.limit";

    private final BillingService        billingService;
    private final InvoicePaymentService invoicePaymentService;
    private final CreditCardDao         creditCardDao;
    private final AccountLocker         accountLocker;
    private final int                   invoices_limit;

    @Inject
    public MeterBasedCharger(InvoicePaymentService invoicePaymentService,
                             BillingService billingService,
                             CreditCardDao creditCardDao,
                             AccountLocker accountLocker,
                             @Named(INVOICE_FETCH_LIMIT) int invoices_limit) {
        this.billingService = billingService;
        this.invoicePaymentService = invoicePaymentService;
        this.creditCardDao = creditCardDao;
        this.accountLocker = accountLocker;
        this.invoices_limit = invoices_limit;
    }

    @ScheduleDelay(delay = 60)
    public void chargeInvoices() {
        try {
            List<Invoice> notPaidInvoices = billingService.getInvoices(InvoiceFilter.builder()
                                                                                    .withPaymentStates(PaymentState.WAITING_EXECUTOR)
                                                                                    .withMaxItems(invoices_limit)
                                                                                    .build());
            for (Invoice invoice : notPaidInvoices) {
                doCharge(invoice);
            }
        } catch (Exception e) {
            LOG.error("Error of invoices charging. " + e.getLocalizedMessage(), e);
        }
    }

    private void doCharge(Invoice invoice) {
        if (invoice.getTotal() <= 0) {
            setPaymentState(invoice.getId(), PaymentState.NOT_REQUIRED);
            return;
        }

        final String ccToken = getCreditCardToken(invoice.getAccountId());
        if (ccToken == null) {
            setPaymentState(invoice.getId(), PaymentState.CREDIT_CARD_MISSING);
            accountLocker.setPaymentLock(invoice.getAccountId());
            return;
        }

        LOG.info("PAYMENTS# Saas #Start# accountId#{}#", invoice.getAccountId());
        try {
            invoicePaymentService.charge(invoice.withCreditCardId(ccToken));
            setPaymentState(invoice.getId(), PaymentState.PAID_SUCCESSFULLY, ccToken);
        } catch (ApiException e) {
            LOG.error("Can't pay invoice " + invoice.getId(), e);
            setPaymentState(invoice.getId(), PaymentState.PAYMENT_FAIL, ccToken);
            accountLocker.setPaymentLock(invoice.getAccountId());
        }
    }

    private String getCreditCardToken(String accountId) {
        try {
            final List<CreditCard> cards = creditCardDao.getCards(accountId);

            if (!cards.isEmpty()) {
                //Now user can have only one credit card
                return cards.get(0).getToken();
            }
        } catch (ServerException | ForbiddenException e) {
            LOG.error("Can't get credit card of account " + accountId, e);
            return null;
        }

        return null;
    }

    private void setPaymentState(Long invoiceId, PaymentState paymentState) {
        setPaymentState(invoiceId, paymentState, null);
    }

    private void setPaymentState(Long invoiceId, PaymentState paymentState, String creditCardToken) {
        try {
            billingService.setPaymentState(invoiceId, paymentState, creditCardToken);
        } catch (ServerException e) {
            LOG.error("Can't change state for invoice " + invoiceId + " to " + paymentState.getState(), e);
        }
    }
}
