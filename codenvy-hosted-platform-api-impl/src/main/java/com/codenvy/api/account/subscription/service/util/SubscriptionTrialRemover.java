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

import org.eclipse.che.api.account.server.SubscriptionService;
import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.account.server.dao.Subscription;
import org.eclipse.che.api.account.shared.dto.SubscriptionState;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;


/**
 * Remove subscription after expiration
 *
 * @author Alexander Garagatyi
 */
public class SubscriptionTrialRemover {
    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionTrialRemover.class);

    private final AccountDao accountDao;

    @Inject
    public SubscriptionTrialRemover(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    public void removeExpiredTrial(SubscriptionService service) {
        try {
            List<Subscription> subscriptions = accountDao.getSubscriptionQueryBuilder()
                                                         .getTrialExpiredQuery(service.getServiceId())
                                                         .execute();
            for (Subscription subscription : subscriptions) {
                try {
                    //TODO Rework this. When trial is expired then try to charge subscription
                    accountDao.updateSubscription(subscription.withState(SubscriptionState.INACTIVE));
                    service.onRemoveSubscription(subscription);
                    //TODO Is it need to send email?
                    // mailUtil.sendTrialExpiredNotification(subscription.getAccountId());
                } catch (ApiException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        } catch (ServerException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }
}
