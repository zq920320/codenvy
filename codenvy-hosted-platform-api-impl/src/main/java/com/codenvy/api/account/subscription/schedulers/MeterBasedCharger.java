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

import com.codenvy.api.account.PaymentService;
import com.codenvy.api.account.billing.BillingService;
import com.codenvy.api.account.billing.PaymentState;
import com.codenvy.api.account.impl.shared.dto.Invoice;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.commons.schedule.ScheduleDelay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * TODO
 *
 * @author Sergii Leschenko
 */
@Singleton
public class MeterBasedCharger implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(MeterBasedCharger.class);

    private static final int INVOICES_LIMIT = 50;

    private final BillingService billingService;
    private final PaymentService paymentService;
    private final AccountDao     accountDao;

    @Inject
    public MeterBasedCharger(PaymentService paymentService,
                             BillingService billingService,
                             AccountDao accountDao) {
        this.billingService = billingService;
        this.paymentService = paymentService;
        this.accountDao = accountDao;
    }

    //TODO configure it
    @ScheduleDelay(initialDelay = 6,
                   delay = 60,
                   unit = TimeUnit.SECONDS)
    @Override
    public void run() {
        try {
            List<Invoice> invoices;

            while ((invoices = billingService.getInvoices(PaymentState.EXECUTING, INVOICES_LIMIT, 0)).size() != 0) {
                for (Invoice invoice : invoices) {
                    doCharge(invoice);
                }
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

        final String ccToken = getSaasCreditCardToken(invoice.getAccountId());
        if (ccToken == null) {
            setPaymentState(invoice.getId(), PaymentState.CREDIT_CARD_MISSING);
            return;
        }

        LOG.info("PAYMENTS# Saas #Start# accountId#{}#", invoice.getAccountId());
        try {
            paymentService.charge(ccToken, invoice.getTotal(), invoice.getAccountId(), "invoice: " + invoice.getId());//TODO Fix description
            setPaymentState(invoice.getId(), PaymentState.PAID_SUCCESSFULLY);
        } catch (ForbiddenException | ServerException e) {
            LOG.error("Can't pay invoice " + invoice.getAccountId(), e);
            setPaymentState(invoice.getId(), PaymentState.PAYMENT_FAIL);
        }
    }

    //TODO Get credit card from account or use different credit card for all charges
    private String getSaasCreditCardToken(String accountId) {
        final List<Subscription> saas;
        try {
            saas = accountDao.getActiveSubscriptions(accountId, "Saas");
        } catch (NotFoundException | ServerException e) {
            return null;
        }
        if (!saas.isEmpty() && !"sas-community".equals(saas.get(0).getPlanId())) {
            return saas.get(0).getPaymentToken();
        }

        return null;
    }

    private void setPaymentState(Long invoiceId, PaymentState paymentState) {
        setPaymentState(invoiceId, paymentState, null);
    }

    private void setPaymentState(Long invoiceId, PaymentState paymentState, String creditCardToken) {
        try {
            billingService.setPaymentState(invoiceId, PaymentState.PAYMENT_FAIL, creditCardToken); //TODO Fix credit card token
        } catch (ServerException e) {
            LOG.error("Can't change state for invoice " + invoiceId + " to " + paymentState.getState());
        }
    }
}
