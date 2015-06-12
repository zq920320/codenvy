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
package com.codenvy.api.subscription.saas.server.service.util;

import com.codenvy.api.subscription.saas.server.InvoicePaymentService;
import com.codenvy.api.subscription.server.AbstractSubscriptionService;
import com.codenvy.api.subscription.server.dao.PlanDao;
import com.codenvy.api.subscription.server.dao.Subscription;
import com.codenvy.api.subscription.server.dao.SubscriptionDao;
import com.codenvy.api.subscription.shared.dto.Plan;

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

    private final SubscriptionDao       subscriptionDao;
    private final InvoicePaymentService invoicePaymentService;
    private final PlanDao               planDao;

    @Inject
    public SubscriptionCharger(SubscriptionDao subscriptionDao,
                               InvoicePaymentService invoicePaymentService,
                               PlanDao planDao) {
        this.subscriptionDao = subscriptionDao;
        this.invoicePaymentService = invoicePaymentService;
        this.planDao = planDao;
    }

    public void charge(AbstractSubscriptionService service) {
        List<Subscription> subscriptions;
        try {
            subscriptions = subscriptionDao.getSubscriptionQueryBuilder()
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
                        invoicePaymentService.charge(subscription);

                        Calendar nextBillingDate = Calendar.getInstance();
                        nextBillingDate.add(Calendar.MONTH, subscription.getBillingCycle());
                        subscription.setNextBillingDate(nextBillingDate.getTime());

                        subscriptionDao.update(subscription);
                        //mailUtil.sendSubscriptionChargedNotification(subscription.getAccountId());
                    } catch (Exception e) {
                        LOG.error(format("Can't charge subscription %s. %s", subscription.getId(), e.getLocalizedMessage()), e);
                        subscriptionDao.deactivate(subscription.getId());
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
