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
package com.codenvy.api.dao.mongo;

import org.eclipse.che.api.account.server.dao.Subscription;
import org.eclipse.che.api.core.ServerException;

import java.util.List;

/**
 * @author Alexander Garagatyi
 */
public interface SubscriptionQueryBuilder {
    public static interface SubscriptionQuery {
        List<Subscription> execute() throws ServerException;
    }

    SubscriptionQuery getTrialQuery(String service, String accountId);

    SubscriptionQuery getChargeQuery(String service);

    SubscriptionQuery getTrialExpiringQuery(String service, int days);

    SubscriptionQuery getTrialExpiredQuery(String service, int days);

    SubscriptionQuery getTrialExpiredQuery(String service);
}
