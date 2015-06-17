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

import com.codenvy.api.creditcard.server.CreditCardDao;
import com.codenvy.api.creditcard.shared.dto.CreditCard;
import com.codenvy.api.subscription.saas.server.AccountLocker;
import com.codenvy.api.subscription.saas.server.InvoicePaymentService;
import com.codenvy.api.subscription.saas.shared.dto.Invoice;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Charges invoice
 *
 * @author Sergii Leschenko
 */
@Singleton
public class InvoiceCharger {
    private static final Logger LOG = LoggerFactory.getLogger(InvoiceCharger.class);

    private final BillingService        billingService;
    private final InvoicePaymentService invoicePaymentService;
    private final CreditCardDao         creditCardDao;
    private final AccountLocker         accountLocker;

    @Inject
    public InvoiceCharger(InvoicePaymentService invoicePaymentService,
                          BillingService billingService,
                          CreditCardDao creditCardDao,
                          AccountLocker accountLocker) {
        this.billingService = billingService;
        this.invoicePaymentService = invoicePaymentService;
        this.creditCardDao = creditCardDao;
        this.accountLocker = accountLocker;
    }

    public void charge(Invoice invoice) throws ApiException {
        final String ccToken = getCreditCardToken(invoice.getAccountId());
        if (ccToken == null) {
            setPaymentState(invoice.getId(), PaymentState.CREDIT_CARD_MISSING);
            accountLocker.setPaymentLock(invoice.getAccountId());

            throw new ConflictException("Account with id " + invoice.getAccountId() + " does not have credit card");
        }

        try {
            invoicePaymentService.charge(invoice.withCreditCardId(ccToken));
            setPaymentState(invoice.getId(), PaymentState.PAID_SUCCESSFULLY, ccToken);
        } catch (ApiException e) {
            setPaymentState(invoice.getId(), PaymentState.PAYMENT_FAIL, ccToken);
            accountLocker.setPaymentLock(invoice.getAccountId());
            throw e;
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
