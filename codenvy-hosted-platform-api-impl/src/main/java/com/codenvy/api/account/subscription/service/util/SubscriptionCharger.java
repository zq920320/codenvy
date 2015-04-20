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
import com.codenvy.api.dao.mongo.AccountDaoImpl;

import org.eclipse.che.api.account.server.SubscriptionService;
import org.eclipse.che.api.account.server.dao.PlanDao;
import org.eclipse.che.api.account.server.dao.Subscription;
import org.eclipse.che.api.account.shared.dto.Plan;
import org.eclipse.che.api.account.shared.dto.SubscriptionState;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Calendar;
import java.util.List;

import static java.lang.String.format;

/**
 * Charges for subscription and sends emails to users on successful or unsuccessful charge
 *
 * @author Alexander Garagatyi
 */
public class SubscriptionCharger {
    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionCharger.class);

    private final AccountDaoImpl accountDao;
    private final PaymentService paymentService;
    private final PlanDao        planDao;

    @Inject
    public SubscriptionCharger(AccountDaoImpl accountDao, PaymentService paymentService, PlanDao planDao) {
        this.accountDao = accountDao;
        this.paymentService = paymentService;
        this.planDao = planDao;
    }

    public void charge(SubscriptionService service) {
        List<Subscription> subscriptions;
        try {
            subscriptions = accountDao.getSubscriptionQueryBuilder()
                                      .getChargeQuery(service.getServiceId())
                                      .execute();
        } catch (ServerException e) {
            LOG.error(format("Can't get subscription for payment. %s", e.getLocalizedMessage()), e);
            return;
        }

        for (Subscription subscription : subscriptions) {
            try {
                Plan plan = planDao.getPlanById(subscription.getPlanId());

                if (plan.isPaid()) {
                    try {
                        paymentService.charge(subscription);

                        Calendar nextBillingDate = Calendar.getInstance();
                        nextBillingDate.add(Calendar.MONTH, subscription.getBillingCycle());
                        subscription.setNextBillingDate(nextBillingDate.getTime());

                        accountDao.updateSubscription(subscription);
                        //mailUtil.sendSubscriptionChargedNotification(subscription.getAccountId());
                    } catch (Exception e) {
                        LOG.error(format("Can't charge subscription %s. %s", subscription.getId(), e.getLocalizedMessage()), e);
                        accountDao.updateSubscription(subscription.withState(SubscriptionState.INACTIVE));
                        service.onRemoveSubscription(subscription);
                        //mailUtil.sendSubscriptionChargeFailNotification(subscription.getAccountId());
                    }
                }
            } catch (ApiException e) {
                LOG.error(format("Can't charge subscription %s. %s", subscription.getId(), e.getLocalizedMessage()), e);
            }
        }
    }
}
