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

import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.core.ApiException;

/**
 * Checks subscription and does something with it if it's needed
 *
 * @author Alexander Garagatyi
 */
public interface SubscriptionSchedulerHandler {
    /**
     * Checks subscription
     *
     * @param subscription
     *         subscription to check
     */
    void checkSubscription(Subscription subscription) throws ApiException;
}
