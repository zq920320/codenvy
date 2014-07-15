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

import com.codenvy.api.account.shared.dto.Subscription;
import com.codenvy.api.core.ApiException;

/**
 * Checks subscription and does something with it if it's needed
 *
 * @author Alexander Garagatyi
 */
public abstract class SubscriptionSchedulerHandler implements Comparable<SubscriptionSchedulerHandler> {
    public static final String EVENTS_INITIATOR_SCHEDULER = "scheduler";

    /** Get priority of a handler. Finally all handlers are used in ascending order of priority. Less value of priority means earlier
     *  usage it in cycle of check. */
    protected abstract int getPriority();

    /**
     * Checks subscription and indicates whether there can be other checks are made.
     *
     * @param subscription
     *         subscription to check
     * @return {@link SubscriptionScheduler.CheckState#CONTINUE_CHECK} if another checks can be performed,
     * {@link SubscriptionScheduler.CheckState#ABORT_CHECK} otherwise
     */
    public abstract SubscriptionScheduler.CheckState checkSubscription(Subscription subscription) throws ApiException;

    @Override
    public int compareTo(SubscriptionSchedulerHandler o) {
        final int priority = getPriority();
        final int anotherPriority = o.getPriority();

        return (priority < anotherPriority) ? -1 : ((priority == anotherPriority) ? 0 : 1);
    }
}
