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
package com.codenvy.api.account.subscription.onpremises;


import com.codenvy.api.account.subscription.SubscriptionEvent;
import com.codenvy.api.account.subscription.service.util.SubscriptionCharger;
import com.codenvy.api.account.subscription.service.util.SubscriptionServiceHelper;
import com.codenvy.api.account.subscription.service.util.SubscriptionTrialRemover;

import org.eclipse.che.api.account.server.SubscriptionService;
import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.account.server.dao.Subscription;
import org.eclipse.che.api.account.shared.dto.UsedAccountResources;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.codenvy.api.account.subscription.ServiceId.ONPREMISES;

/**
 * Service provide functionality of On-premises subscription.
 *
 * @author Sergii Leschenko
 * @author Alexander Garagatyi
 */
@Singleton
public class OnPremisesSubscriptionService extends SubscriptionService {
    private static final Logger LOG = LoggerFactory.getLogger(OnPremisesSubscriptionService.class);
    private final AccountDao                accountDao;
    private final SubscriptionCharger       chargeUtil;
    private final SubscriptionTrialRemover  removeUtil;
    private final EventService              eventService;
    private final SubscriptionServiceHelper subscriptionServiceHelper;

    @Inject
    public OnPremisesSubscriptionService(AccountDao accountDao,
                                         SubscriptionCharger chargeUtil,
                                         SubscriptionTrialRemover removeUtil,
                                         EventService eventService,
                                         SubscriptionServiceHelper subscriptionServiceHelper) {
        super(ONPREMISES, ONPREMISES);
        this.accountDao = accountDao;
        this.chargeUtil = chargeUtil;
        this.removeUtil = removeUtil;
        this.eventService = eventService;
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
            if (accountDao.getActiveSubscription(subscription.getAccountId(), getServiceId()) != null) {
                throw new ConflictException(SUBSCRIPTION_LIMIT_EXHAUSTED_MESSAGE);
            }
        } catch (NotFoundException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException(e.getLocalizedMessage());
        }
        subscriptionServiceHelper.checkTrial(subscription);
        subscriptionServiceHelper.checkCreditCard(subscription);
        subscriptionServiceHelper.fillDates(subscription);
    }

    @Override
    public void afterCreateSubscription(Subscription subscription) throws ApiException {
        subscriptionServiceHelper.chargeSubscriptionIfNeed(subscription);
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
        removeUtil.removeExpiredTrial(this);
        chargeUtil.charge(this);

//        TODO It is need to send emails about trial expiration?
//        expirationUtil.sendEmailAboutExpiringTrial(getServiceId(), 2);
//        expirationUtil.sendEmailAboutExpiredTrial(getServiceId(), 2);
//        expirationUtil.sendEmailAboutExpiredTrial(getServiceId(), 7);
    }

    @Override
    public UsedAccountResources getAccountResources(Subscription subscription) throws ServerException {
        return DtoFactory.getInstance().createDto(UsedAccountResources.class);
    }
}
