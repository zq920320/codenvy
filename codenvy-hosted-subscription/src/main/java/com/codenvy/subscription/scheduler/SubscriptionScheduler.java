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
package com.codenvy.subscription.scheduler;

import com.codenvy.api.account.server.subscription.SubscriptionService;
import com.codenvy.api.account.server.subscription.SubscriptionServiceRegistry;
import com.codenvy.commons.lang.NamedThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Run checking of the subscriptions periodically
 *
 * @author Alexander Garagatyi
 */
// must be eager singleton
@Singleton
public class SubscriptionScheduler {
    public static final String SUBSCRIPTION_SCHEDULER_INITIAL_DELAY_MINUTES = "subscription.scheduler.initDelay.minutes";
    public static final String SUBSCRIPTION_SCHEDULER_PERIOD_MINUTES        = "subscription.scheduler.period.minutes";

    private final ScheduledExecutorService scheduler;
    private final SubscriptionCheckerTask  task;
    private final Integer                  subscriptionSchedulerInitDelay;
    private final Integer                  subscriptionSchedulerPeriod;

    @Inject
    public SubscriptionScheduler(SubscriptionCheckerTask task,
                                 @Named(SUBSCRIPTION_SCHEDULER_INITIAL_DELAY_MINUTES) Integer subscriptionSchedulerInitDelay,
                                 @Named(SUBSCRIPTION_SCHEDULER_PERIOD_MINUTES) Integer subscriptionSchedulerPeriod) {
        this.task = task;
        this.subscriptionSchedulerInitDelay = subscriptionSchedulerInitDelay;
        this.subscriptionSchedulerPeriod = subscriptionSchedulerPeriod;
        this.scheduler = Executors.newScheduledThreadPool(1, new NamedThreadFactory("SubscriptionCheckerTask", false));
    }

    @PostConstruct
    private void startScheduling() {
        scheduler.scheduleAtFixedRate(task, subscriptionSchedulerInitDelay, subscriptionSchedulerPeriod, TimeUnit.MINUTES);
    }

    @PreDestroy
    private void destroy() {
        scheduler.shutdownNow();
    }

    private static class SubscriptionCheckerTask implements Runnable {
        private static final Logger LOG = LoggerFactory.getLogger(SubscriptionCheckerTask.class);

        private final SubscriptionServiceRegistry registry;

        @Inject
        public SubscriptionCheckerTask(SubscriptionServiceRegistry registry) {
            this.registry = registry;
        }

        @Override
        public void run() {
            LOG.info("Subscription scheduler task is started");
            try {
                for (SubscriptionService subscriptionService : registry.getAll()) {
                    subscriptionService.onCheckSubscriptions();
                }
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            LOG.info("Subscription scheduler task is finished");
        }
    }
}
