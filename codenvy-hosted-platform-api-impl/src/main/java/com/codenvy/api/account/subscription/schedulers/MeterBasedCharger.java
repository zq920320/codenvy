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

package com.codenvy.api.account.subscription.schedulers;

import com.codenvy.api.account.AccountLocker;
import com.codenvy.api.account.PaymentService;
import com.codenvy.api.account.billing.BillingService;
import com.codenvy.api.account.billing.CreditCardDao;
import com.codenvy.api.account.billing.PaymentState;
import com.codenvy.api.account.impl.shared.dto.CreditCard;
import com.codenvy.api.account.impl.shared.dto.Invoice;
import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.ServerException;
import com.codenvy.commons.schedule.ScheduleDelay;

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
public class MeterBasedCharger implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(MeterBasedCharger.class);

    private static final String INVOICE_FETCH_LIMIT = "billing.invoice.fetch.limit";

    private final BillingService billingService;
    private final PaymentService paymentService;
    private final CreditCardDao  creditCardDao;
    private final AccountLocker  accountLocker;
    private final int            invoices_limit;

    @Inject
    public MeterBasedCharger(PaymentService paymentService,
                             BillingService billingService,
                             CreditCardDao creditCardDao,
                             AccountLocker accountLocker,
                             @Named(INVOICE_FETCH_LIMIT) int invoices_limit) {
        this.billingService = billingService;
        this.paymentService = paymentService;
        this.creditCardDao = creditCardDao;
        this.accountLocker = accountLocker;
        this.invoices_limit = invoices_limit;
    }

    @ScheduleDelay(delay = 5)
    @Override
    public void run() {
        try {
            for (Invoice invoice : billingService.getInvoices(PaymentState.WAITING_EXECUTOR, invoices_limit, 0)) {
                doCharge(invoice);
            }
        } catch (ServerException e) {
            LOG.error("Can't get receipts", e);
        }
    }

    public void doCharge(Invoice invoice) {
        if (invoice.getTotal() <= 0) {
            setPaymentState(invoice.getId(), PaymentState.NOT_REQUIRED);
            return;
        }

        final String ccToken = getCreditCardToken(invoice.getAccountId());
        if (ccToken == null) {
            setPaymentState(invoice.getId(), PaymentState.CREDIT_CARD_MISSING);
            return;
        }

        LOG.info("PAYMENTS# Saas #Start# accountId#{}#", invoice.getAccountId());
        try {
            paymentService.charge(invoice.withCreditCardId(ccToken));
            setPaymentState(invoice.getId(), PaymentState.PAID_SUCCESSFULLY, ccToken);
        } catch (ForbiddenException | ServerException e) {
            LOG.error("Can't pay invoice " + invoice.getAccountId(), e);
            setPaymentState(invoice.getId(), PaymentState.PAYMENT_FAIL, ccToken);
            accountLocker.lockAccount(invoice.getAccountId());
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
