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

import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.core.ServerException;
import com.codenvy.commons.lang.NamedThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Periodically checks all stored subscriptions with list of defined {@link SubscriptionSchedulerHandler}
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class SubscriptionScheduler {
    private static final Logger LOG                        = LoggerFactory.getLogger(SubscriptionScheduler.class);
    public static final  String EVENTS_INITIATOR_SCHEDULER = "scheduler";

    /** Period for subscription scheduling. In minutes */
    private static final int SCHEDULE_PERIOD = 60;
    private final Set<SubscriptionSchedulerHandler> handlers;
    private final AccountDao                        accountDao;
    private       ScheduledExecutorService          scheduler;

    @Inject
    public SubscriptionScheduler(Set<SubscriptionSchedulerHandler> handlers, AccountDao accountDao) {
        this.handlers = handlers;
        this.accountDao = accountDao;
    }

    @PostConstruct
    protected void start() {
        // this thread SHOULD NOT be a daemon
        scheduler = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("SubscriptionScheduler", false));
        scheduler.scheduleAtFixedRate(new CheckSubscriptionsTask(), 5, SCHEDULE_PERIOD, TimeUnit.MINUTES);
    }

    class CheckSubscriptionsTask implements Runnable {
        @Override
        public void run() {
            try {
                Iterator<Subscription> allSubscriptionsIterator = accountDao.getSubscriptions().iterator();
                while (!Thread.currentThread().isInterrupted() && allSubscriptionsIterator.hasNext()) {
                    Subscription subscription = allSubscriptionsIterator.next();

                    for (SubscriptionSchedulerHandler handler : handlers) {
                        try {
                            handler.checkSubscription(subscription);
                        } catch (Exception e) {
                            LOG.error(e.getLocalizedMessage(), e);
                        }
                    }
                }
            } catch (ServerException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }

    @PreDestroy
    protected void cleanup() {
        scheduler.shutdownNow();
        try {
            if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                LOG.warn("Unable terminate scheduler");
            }
        } catch (InterruptedException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }
}
