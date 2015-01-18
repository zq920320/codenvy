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
package com.codenvy.subscription.service.util;

import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.server.dao.PlanDao;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.account.server.subscription.PaymentService;
import com.codenvy.api.account.server.subscription.SubscriptionService;
import com.codenvy.api.account.shared.dto.Plan;
import com.codenvy.api.account.shared.dto.SubscriptionState;
import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.ServerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.mail.MessagingException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Charges for subscription and sends emails to users on successful or unsuccessful charge
 *
 * @author Alexander Garagatyi
 */
public class SubscriptionCharger {
    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionCharger.class);

    @Inject
    private AccountDao accountDao;

    @Inject
    private PaymentService paymentService;

    @Inject
    private PlanDao planDao;

    @Inject
    private SubscriptionMailSender mailUtil;

    public void charge(SubscriptionService service) {
        List<Subscription> subscriptions;
        try {
            subscriptions = accountDao.getSubscriptionQueryBuilder().getChargeQuery(service.getServiceId()).execute();
        } catch (ServerException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return;
        }

        for (Subscription subscription : subscriptions) {
            try {
                Plan plan = planDao.getPlanById(subscription.getPlanId());

                if (plan.isPaid() && Boolean.TRUE.equals(subscription.getUsePaymentSystem())) {
                    try {
                        paymentService.charge(subscription);

                        Calendar nextBillingDate = Calendar.getInstance();
                        nextBillingDate.setTime(new Date());
                        nextBillingDate.add(Calendar.MONTH, subscription.getBillingCycle());
                        subscription.setNextBillingDate(nextBillingDate.getTime());
                        accountDao.updateSubscription(subscription);

                        List<String> accountOwnersEmails = mailUtil.getAccountOwnersEmails(subscription.getAccountId());
                        LOG.info("Send email about subscription charging to {}", accountOwnersEmails);
                        mailUtil.sendEmail("Send email about subscription charging", accountOwnersEmails);
                    } catch (Exception e) {
                        LOG.error(e.getLocalizedMessage(), e);
                        accountDao.updateSubscription(subscription.withState(SubscriptionState.INACTIVE));

                        service.onRemoveSubscription(subscription);

                        List<String> accountOwnersEmails = mailUtil.getAccountOwnersEmails(subscription.getAccountId());
                        LOG.info("Send email about unsuccessful subscription charging to {}", accountOwnersEmails);
                        mailUtil.sendEmail("Send email about unsuccessful subscription charging", accountOwnersEmails);
                    }
                }
            } catch (ApiException | IOException | MessagingException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }
}
