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

package com.codenvy.api.account.subscription;

import com.codenvy.api.account.billing.BillingService;
import com.codenvy.api.account.billing.PaymentState;
import com.codenvy.api.account.impl.shared.dto.Invoice;
import com.codenvy.api.account.server.subscription.PaymentService;
import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.ServerException;
import com.codenvy.commons.schedule.ScheduleCron;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.List;

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

    public MeterBasedCharger(PaymentService paymentService, BillingService billingService) {
        this.billingService = billingService;
        this.paymentService = paymentService;
    }

    //TODO configure it
    // 0sec 0min 07hour 1st day of every month
    @ScheduleCron(cron = "0 0 7 1 * ?")
    @Override
    public void run() {
        try {
            List<Invoice> invoices = billingService.getInvoices(PaymentState.EXECUTING, INVOICES_LIMIT);

            while ((billingService.getInvoices(PaymentState.EXECUTING, INVOICES_LIMIT)).size() != 0) {
                for (Invoice invoice : invoices) {
                    doCharge(invoice);
                }
            }
        } catch (ServerException e) {
            LOG.error("Can't get receipts", e);
        }
    }

    public void doCharge(Invoice invoice) {
        final String accountId = invoice.getAccountId();
        LOG.info("PAYMENTS# Saas #Start# accountId#{}#", accountId);

        if (invoice.getTotal() > 0) {
            final String ccToken = invoice.getCreditCardId();
            if (ccToken == null) {
                setPaymentState(invoice.getId(), PaymentState.CREDIT_CARD_MISSING);
                return;
            }

            try {
                paymentService.charge(ccToken, invoice.getTotal(), invoice.getAccountId(),
                                      "invoice: " + invoice.getId());//TODO Fix description

                setPaymentState(invoice.getId(), PaymentState.PAID_SUCCESSFULLY);
            } catch (ForbiddenException | ServerException e) {
                //TODO
                setPaymentState(invoice.getId(), PaymentState.PAYMENT_FAIL);
            }
        }
    }

    private void setPaymentState(Long invoiceId, PaymentState paymentState) {
        try {
            billingService.setPaymentState(invoiceId, PaymentState.PAYMENT_FAIL);
        } catch (ServerException e) {
            LOG.error("Can't change state for invoice " + invoiceId + " to " + paymentState);
        }
    }
}
