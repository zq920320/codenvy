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

import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.account.server.subscription.SubscriptionService;
import com.codenvy.api.account.shared.dto.SubscriptionState;
import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.ServerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;


/**
 * Remove subscription after expiration
 *
 * @author Alexander Garagatyi
 */
public class SubscriptionTrialRemover {
    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionTrialRemover.class);

    @Inject
    private AccountDao accountDao;

    @Inject
    private SubscriptionMailSender mailUtil;

    public void removeExpiredTrial(SubscriptionService service) {
        try {
            List<Subscription> subscriptions =
                    accountDao.getSubscriptionQueryBuilder().getTrialExpiredQuery(service.getServiceId()).execute();

            for (Subscription subscription : subscriptions) {
                try {
                    accountDao.updateSubscription(subscription.withState(SubscriptionState.INACTIVE));

                    service.onRemoveSubscription(subscription);

                    List<String> accountOwnersEmails = mailUtil.getAccountOwnersEmails(subscription.getAccountId());
                    LOG.info("Send email about trial removing to {}", accountOwnersEmails);
                    mailUtil.sendEmail("Send email about trial removing", accountOwnersEmails);
                } catch (ApiException | IOException | MessagingException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        } catch (ServerException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }
}
