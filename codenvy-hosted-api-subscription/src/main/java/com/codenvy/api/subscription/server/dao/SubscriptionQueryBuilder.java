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
package com.codenvy.api.subscription.server.dao;

import org.eclipse.che.api.core.ServerException;

import java.util.List;

/**
 * @author Alexander Garagatyi
 */
public interface SubscriptionQueryBuilder {
    interface SubscriptionQuery {
        List<Subscription> execute() throws ServerException;
    }

    SubscriptionQuery getTrialQuery(String service, String accountId);

    SubscriptionQuery getChargeQuery(String service);

    /**
     * Get list of subscriptions that will be expired in specified period.
     *
     * @param service service ID
     * @param days count of days until subscription expires
     * @return
     */
    SubscriptionQuery getExpiringQuery(String service, int days);

    SubscriptionQuery getTrialExpiringQuery(String service, int days);

    SubscriptionQuery getTrialExpiredQuery(String service, int days);

    SubscriptionQuery getTrialExpiredQuery(String service);
}
