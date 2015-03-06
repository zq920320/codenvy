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
package com.codenvy.api.account.subscription.service.util;

import com.codenvy.api.account.PaymentService;
import com.codenvy.api.account.billing.CreditCardDao;
import com.codenvy.api.account.impl.shared.dto.CreditCard;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.ServerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static java.lang.String.format;

/**
 * @author Sergii Leschenko
 */
public class SubscriptionServiceHelper {
    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionServiceHelper.class);

    private final AccountDao     accountDao;
    private final PaymentService paymentService;
    private final CreditCardDao  creditCardDao;

    @Inject
    public SubscriptionServiceHelper(AccountDao accountDao,
                                     PaymentService paymentService,
                                     CreditCardDao creditCardDao) {
        this.accountDao = accountDao;
        this.paymentService = paymentService;
        this.creditCardDao = creditCardDao;
    }

    public void chargeSubscriptionIfNeed(Subscription subscription) throws ApiException {
        if (subscription.getUsePaymentSystem() && !isPresentTrial(subscription)) {
            try {
                paymentService.charge(subscription);

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(calendar.getTime());
                calendar.add(Calendar.MONTH, subscription.getBillingCycle());
                subscription.setNextBillingDate(calendar.getTime());
            } catch (ApiException e) {
                LOG.error(format("Can't charge subscription with id %s. %s", subscription.getId(), e.getLocalizedMessage()), e);
                safeRemoveSubscription(subscription);
                throw e;
            }
        }
    }

    //TODO Rename it
    public void setDates(Subscription subscription) {
        final boolean presentTrial = isPresentTrial(subscription);

        Calendar calendar = Calendar.getInstance();
        if (subscription.getUsePaymentSystem() || presentTrial) {
            Calendar billingCalendar = Calendar.getInstance();
            billingCalendar.setTime(calendar.getTime());

            subscription.setBillingStartDate(billingCalendar.getTime());
            billingCalendar.add(Calendar.YEAR, 1);//TODO Fix it. Why 1 year?
            subscription.setBillingEndDate(billingCalendar.getTime());
        }

        if (presentTrial) {
            subscription.setNextBillingDate(subscription.getTrialEndDate());
        }

        // subscription without trial is paid unless another stated above
        subscription.setStartDate(calendar.getTime());

        calendar.add(Calendar.YEAR, 1);//TODO Fix it. Why 1 year?
        subscription.setEndDate(calendar.getTime());
    }

    private void safeRemoveSubscription(Subscription subscription) {
        try {
            accountDao.removeSubscription(subscription.getId());
        } catch (Exception e) {
            LOG.error(format("Can't remove subscription %s. %s", subscription.getId(), e.getLocalizedMessage()), e);
        }
    }

    public void checkCreditCard(Subscription subscription) throws ServerException, ForbiddenException, ConflictException {
        if (!isPresentTrial(subscription) && subscription.getUsePaymentSystem()) {
            final List<CreditCard> cards = creditCardDao.getCards(subscription.getAccountId());
            if (cards.isEmpty()) {
                throw new ConflictException("You can't add subscription without credit card");
            }
        }
    }

    private boolean isPresentTrial(Subscription subscription) {
        Date startTrial = subscription.getTrialStartDate();
        Date endTrial = subscription.getTrialEndDate();
        return startTrial != null && endTrial != null && (endTrial.getTime() - startTrial.getTime() > 0);
    }
}
