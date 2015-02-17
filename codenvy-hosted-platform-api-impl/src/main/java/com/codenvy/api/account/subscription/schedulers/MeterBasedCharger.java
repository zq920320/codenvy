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
import com.codenvy.api.core.ForbiddenException;
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

    @Inject
    public MeterBasedCharger(PaymentService paymentService, BillingService billingService) {
        this.billingService = billingService;
        this.paymentService = paymentService;
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

        final String ccToken = invoice.getCreditCardId();
        if (ccToken == null) {
            setPaymentState(invoice.getId(), PaymentState.CREDIT_CARD_MISSING);
            return;
        }

        LOG.info("PAYMENTS# Saas #Start# accountId#{}#", invoice.getAccountId());
        try {
            paymentService.charge(ccToken, invoice.getTotal(), invoice.getAccountId(), "invoice: " + invoice.getId());//TODO Fix description
            setPaymentState(invoice.getId(), PaymentState.PAID_SUCCESSFULLY);
        } catch (ForbiddenException | ServerException e) {
            //TODO
            setPaymentState(invoice.getId(), PaymentState.PAYMENT_FAIL);
        }
    }

    private void setPaymentState(Long invoiceId, PaymentState paymentState) {
        try {
            billingService.setPaymentState(invoiceId, PaymentState.PAYMENT_FAIL, "creditCard"); //TODO Fix credit card token
        } catch (ServerException e) {
            LOG.error("Can't change state for invoice " + invoiceId + " to " + paymentState.getState());
        }
    }
}
