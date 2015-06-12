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
package com.codenvy.api.subscription.server.util;

import com.codenvy.api.creditcard.server.CreditCardDao;
import com.codenvy.api.creditcard.shared.dto.CreditCard;
import com.codenvy.api.subscription.server.dao.Subscription;
import com.codenvy.api.subscription.server.dao.mongo.SubscriptionDaoImpl;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Calendar;
import java.util.List;

import static java.lang.String.format;

/**
 * @author Sergii Leschenko
 */
public class SubscriptionServiceHelper {
    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionServiceHelper.class);

    private final SubscriptionDaoImpl subscriptionDao;
    private final CreditCardDao       creditCardDao;

    @Inject
    public SubscriptionServiceHelper(SubscriptionDaoImpl subscriptionDao,
                                     CreditCardDao creditCardDao) {
        this.subscriptionDao = subscriptionDao;
        this.creditCardDao = creditCardDao;
    }

    public void fillDates(Subscription subscription) {
        Calendar calendar = Calendar.getInstance();
        if (subscription.getUsePaymentSystem()) {
            Calendar billingCalendar = Calendar.getInstance();
            billingCalendar.setTime(calendar.getTime());

            subscription.setBillingStartDate(billingCalendar.getTime());
            billingCalendar.add(Calendar.YEAR, 1);
            subscription.setBillingEndDate(billingCalendar.getTime());
        }

        //TODO Set next billing date or remove this field from subscription
        // subscription.setNextBillingDate(subscription.getTrialEndDate());


        // subscription without trial is paid unless another stated above
        subscription.setStartDate(calendar.getTime());

        calendar.add(Calendar.YEAR, 1);//TODO Fix it. Why 1 year?
        subscription.setEndDate(calendar.getTime());
    }

    public void checkCreditCard(Subscription subscription) throws ServerException, ForbiddenException, ConflictException {
        if (subscription.getUsePaymentSystem()) {
            final List<CreditCard> cards = creditCardDao.getCards(subscription.getAccountId());
            if (cards.isEmpty()) {
                throw new ConflictException("You can't add subscription without credit card");
            }
        }
    }

    private void safeRemoveSubscription(Subscription subscription) {
        try {
            subscriptionDao.remove(subscription.getId());
        } catch (Exception e) {
            LOG.error(format("Can't remove subscription %s. %s", subscription.getId(), e.getLocalizedMessage()), e);
        }
    }
}
