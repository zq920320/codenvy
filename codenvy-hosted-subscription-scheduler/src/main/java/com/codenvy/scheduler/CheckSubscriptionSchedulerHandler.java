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

import com.codenvy.api.account.server.SubscriptionService;
import com.codenvy.api.account.server.SubscriptionServiceRegistry;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.ConflictException;

import javax.inject.Inject;

/**
 * Checks subscriptions using their service.
 *
 * @author Alexander Garagatyi
 */
public class CheckSubscriptionSchedulerHandler implements SubscriptionSchedulerHandler {
    private final SubscriptionServiceRegistry registry;

    @Inject
    public CheckSubscriptionSchedulerHandler(SubscriptionServiceRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void checkSubscription(Subscription subscription) throws ApiException {
        SubscriptionService service = registry.get(subscription.getServiceId());
        if (service == null) {
            throw new ConflictException("Subscription service not found " + subscription.getServiceId());
        }

        service.onCheckSubscription(subscription);
    }
}
