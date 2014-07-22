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

import static com.codenvy.api.account.server.dao.SubscriptionHistoryEvent.Type.DELETE;
import static com.codenvy.scheduler.SubscriptionScheduler.EVENTS_INITIATOR_SCHEDULER;

/**
 * Removes expired subscriptions
 *
 * @author Alexander Garagatyi
 */
public class RemoveExpiredSubscriptionSchedulerHandler implements SubscriptionSchedulerHandler {
    private final AccountDao                  accountDao;
    private final SubscriptionServiceRegistry registry;

    @Inject
    public RemoveExpiredSubscriptionSchedulerHandler(AccountDao accountDao, SubscriptionServiceRegistry registry) {
        this.accountDao = accountDao;
        this.registry = registry;
    }

    @Override
    public void checkSubscription(Subscription subscription) throws ApiException {
        if ("true".equals(subscription.getProperties().get("codenvy:trial"))) {
            return;
        }

        SubscriptionService service = registry.get(subscription.getServiceId());
        if (service == null) {
            throw new ConflictException("Subscription service not found " + subscription.getServiceId());
        }

        if (subscription.getEndDate() < System.currentTimeMillis()) {
            try {
                accountDao.removeSubscription(subscription.getId());

                service.onRemoveSubscription(subscription);

                SubscriptionHistoryEvent event = new SubscriptionHistoryEvent();
                event.setId(NameGenerator.generate(SubscriptionHistoryEvent.class.getSimpleName().toLowerCase(), Constants.ID_LENGTH));
                event.setType(DELETE);
                event.setUserId(EVENTS_INITIATOR_SCHEDULER);
                event.setTime(System.currentTimeMillis());
                event.setSubscription(subscription);

                accountDao.addSubscriptionHistoryEvent(event);
            } catch (ApiException e) {
                throw new ServerException(
                        "Error on removing subscription " + subscription.getId() + ". Message: " + e.getLocalizedMessage(), e);
            }
        }
    }
}
