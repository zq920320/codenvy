/*
 * CODENVY CONFIDENTIAL
 * __________________
 * 
 *  [2012] - [2014] Codenvy, S.A.
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
package com.codenvy.scheduler;

import com.codenvy.api.account.server.Constants;
import com.codenvy.api.account.server.PaymentService;
import com.codenvy.api.account.server.SubscriptionService;
import com.codenvy.api.account.server.SubscriptionServiceRegistry;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.account.server.dao.SubscriptionHistoryEvent;
import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.ServerException;
import com.codenvy.commons.lang.NameGenerator;

import javax.inject.Inject;
import java.util.Calendar;
import java.util.List;

import static com.codenvy.scheduler.SubscriptionScheduler.EVENTS_INITIATOR_SCHEDULER;

/**
 * Proceed expired trial subscriptions.
 *
 * @author Alexander Garagatyi
 */
public class TrialSubscriptionSchedulerHandler implements SubscriptionSchedulerHandler {
    private final SubscriptionServiceRegistry registry;
    private final PaymentService              paymentService;
    private final AccountDao                  accountDao;

    @Inject
    public TrialSubscriptionSchedulerHandler(SubscriptionServiceRegistry registry, PaymentService paymentService, AccountDao accountDao) {
        this.registry = registry;
        this.paymentService = paymentService;
        this.accountDao = accountDao;
    }

    @Override
    public void checkSubscription(Subscription subscription) throws ApiException {
        if (!"true".equals(subscription.getProperties().get("codenvy:trial"))) {
            return;
        }
        SubscriptionService service = registry.get(subscription.getServiceId());
        if (service == null) {
            throw new ConflictException("Subscription service not found " + subscription.getServiceId());
        }
        if (subscription.getEndDate() < System.currentTimeMillis()) {
            final List<SubscriptionHistoryEvent> subscriptionHistoryEvents;
            try {
                subscriptionHistoryEvents = accountDao.getSubscriptionHistoryEvents(
                        new SubscriptionHistoryEvent().withType(SubscriptionHistoryEvent.Type.CREATE).withSubscription(
                                new Subscription().withId(subscription.getId()))
                                                                                   );
            } catch (ApiException e) {
                throw new ServerException(
                        "Error on purchasing trial subscription " + subscription.getId() + ". Message: " + e.getLocalizedMessage(), e);
            }
            if (subscriptionHistoryEvents.size() == 0) {
                throw new ConflictException("Can't find creation event of subscription " + subscription.getId());
            }

            try {
                final Subscription newSubscription = new Subscription(subscription).withState(Subscription.State.WAIT_FOR_PAYMENT);

                final Calendar calendar = Calendar.getInstance();
                newSubscription.setStartDate(calendar.getTimeInMillis());
                final String tariffPlan = subscription.getProperties().get("TariffPlan");
                switch (tariffPlan) {
                    case "yearly":
                        calendar.add(Calendar.YEAR, 1);
                        break;
                    case "monthly":
                        calendar.add(Calendar.MONTH, 1);
                        break;
                }
                newSubscription.setEndDate(calendar.getTimeInMillis());

                accountDao.updateSubscription(newSubscription);

                SubscriptionHistoryEvent event = new SubscriptionHistoryEvent();
                event.setId(NameGenerator.generate(SubscriptionHistoryEvent.class.getSimpleName().toLowerCase(), Constants.ID_LENGTH));
                event.setType(SubscriptionHistoryEvent.Type.UPDATE);
                event.setUserId(EVENTS_INITIATOR_SCHEDULER);
                event.setTime(System.currentTimeMillis());
                event.setSubscription(newSubscription);
                accountDao.addSubscriptionHistoryEvent(event);

                service.onUpdateSubscription(subscription, newSubscription);

                paymentService.purchase(subscriptionHistoryEvents.get(0).getUserId(), subscription.getId());
            } catch (ApiException e) {
                throw new ServerException(
                        "Error on purchasing trial subscription " + subscription.getId() + ". Message: " + e.getLocalizedMessage(), e);
            }
        }
    }
}
