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
import com.codenvy.api.account.shared.dto.Subscription;
import com.codenvy.api.account.shared.dto.SubscriptionHistoryEvent;
import com.codenvy.api.core.ApiException;
import com.codenvy.commons.lang.NameGenerator;
import com.codenvy.dto.server.DtoFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static com.codenvy.api.account.shared.dto.SubscriptionHistoryEvent.Type.DELETE;

/**
 * Checks subscriptions using their service. Also removes expired subscription.
 *
 * @author Alexander Garagatyi
 */
public class DefaultSubscriptionSchedulerHandler extends SubscriptionSchedulerHandler {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultSubscriptionSchedulerHandler.class);
    private final AccountDao                  accountDao;
    private final SubscriptionServiceRegistry registry;

    @Inject
    public DefaultSubscriptionSchedulerHandler(AccountDao accountDao, SubscriptionServiceRegistry registry) {
        super();
        this.accountDao = accountDao;
        this.registry = registry;
    }

    /**
     * Has priority 100
     *
     * @see SubscriptionSchedulerHandler#getPriority()
     */
    @Override
    protected int getPriority() {
        return 100;
    }

    @Override
    public SubscriptionScheduler.CheckState checkSubscription(Subscription subscription) throws ApiException {
        SubscriptionService service = registry.get(subscription.getServiceId());
        if (service == null) {
            LOG.error("Subscription service not found {}", subscription.getServiceId());
            return SubscriptionScheduler.CheckState.ABORT_CHECK;
        }

        if (subscription.getEndDate() < System.currentTimeMillis()) {
            accountDao.removeSubscription(subscription.getId());

            try {
                service.onRemoveSubscription(subscription);

                SubscriptionHistoryEvent event = DtoFactory.getInstance().createDto(SubscriptionHistoryEvent.class);
                event.setId(NameGenerator.generate(SubscriptionHistoryEvent.class.getSimpleName().toLowerCase(), Constants.ID_LENGTH));
                event.setType(DELETE);
                event.setUserId(EVENTS_INITIATOR_SCHEDULER);
                event.setTime(System.currentTimeMillis());
                event.setSubscription(subscription);

                accountDao.addSubscriptionHistoryEvent(event);
            } catch (ApiException e) {
                LOG.error("Error on removing subscription " + subscription.getId() + ". Message: " + e.getLocalizedMessage(), e);
            } finally {
                return SubscriptionScheduler.CheckState.ABORT_CHECK;
            }
        } else {
            service.onCheckSubscription(subscription);
        }

        return SubscriptionScheduler.CheckState.CONTINUE_CHECK;
    }
}
