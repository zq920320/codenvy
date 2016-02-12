/*
 *  [2012] - [2016] Codenvy, S.A.
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
package com.codenvy.api.subscription.onpremises.server;


import com.codenvy.api.subscription.server.AbstractSubscriptionService;
import com.codenvy.api.subscription.server.SubscriptionEvent;
import com.codenvy.api.subscription.server.dao.Subscription;
import com.codenvy.api.subscription.server.dao.SubscriptionDao;
import com.codenvy.api.subscription.server.util.SubscriptionServiceHelper;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Service provide functionality of On-premises subscription.
 *
 * @author Sergii Leschenko
 * @author Alexander Garagatyi
 */
@Singleton
public class OnPremisesSubscriptionService extends AbstractSubscriptionService {
    private static final Logger LOG = LoggerFactory.getLogger(OnPremisesSubscriptionService.class);

    public static final String ONPREMISES_SERVICE_ID = "OnPremises";

    private final SubscriptionDao           subscriptionDao;
    private final EventService              eventService;
    private final SubscriptionMailSender    mailSender;
    private final SubscriptionServiceHelper subscriptionServiceHelper;

    @Inject
    public OnPremisesSubscriptionService(SubscriptionDao subscriptionDao,
                                         EventService eventService,
                                         SubscriptionMailSender mailSender,
                                         SubscriptionServiceHelper subscriptionServiceHelper) {
        super(ONPREMISES_SERVICE_ID, "OnPremises");
        this.subscriptionDao = subscriptionDao;
        this.eventService = eventService;
        this.mailSender = mailSender;
        this.subscriptionServiceHelper = subscriptionServiceHelper;
    }

    @Override
    public void beforeCreateSubscription(Subscription subscription) throws ConflictException, ServerException, ForbiddenException {
        if (subscription.getProperties().get("Package") == null) {
            throw new ConflictException("Subscription property 'Package' required");
        }
        if (subscription.getProperties().get("Users") == null) {
            throw new ConflictException("Subscription property 'Users' required");
        }
        try {
            if (subscriptionDao.getActiveByServiceId(subscription.getAccountId(), getServiceId()) != null) {
                throw new ConflictException(SUBSCRIPTION_LIMIT_EXHAUSTED_MESSAGE);
            }
        } catch (NotFoundException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException(e.getLocalizedMessage());
        }
        subscriptionServiceHelper.checkCreditCard(subscription);
        subscriptionServiceHelper.fillDates(subscription);
    }

    @Override
    public void afterCreateSubscription(Subscription subscription) throws ApiException {
        eventService.publish(SubscriptionEvent.subscriptionAddedEvent(subscription));
    }

    @Override
    public void onRemoveSubscription(Subscription subscription) throws ApiException {
        eventService.publish(SubscriptionEvent.subscriptionRemovedEvent(subscription));
    }

    @Override
    public void onUpdateSubscription(Subscription oldSubscription, Subscription newSubscription) throws ApiException {

    }

    @Override
    public void onCheckSubscriptions() throws ApiException {
        mailSender.sendEmailAboutExpiring(getServiceId(), 14);
    }
}
