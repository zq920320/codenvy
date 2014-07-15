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
import com.codenvy.api.account.shared.dto.Subscription;
import com.codenvy.api.core.ApiException;
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
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.codenvy.scheduler.SubscriptionScheduler.CheckState.ABORT_CHECK;

/**
 * Periodically checks all stored subscriptions with list of defined {@link SubscriptionSchedulerHandler}
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class SubscriptionScheduler {
    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionScheduler.class);

    public enum CheckState {
        CONTINUE_CHECK, ABORT_CHECK
    }

    /** Period for subscription scheduling. In minutes */
    private static final int SCHEDULE_PERIOD = 60;
    private final AtomicBoolean                     started;
    private final Set<SubscriptionSchedulerHandler> handlers;
    private final AccountDao                        accountDao;
    private       ScheduledExecutorService          scheduler;

    @Inject
    public SubscriptionScheduler(Set<SubscriptionSchedulerHandler> handlers, AccountDao accountDao) {
        this.started = new AtomicBoolean(false);
        this.handlers = new TreeSet<>(handlers);
        this.accountDao = accountDao;
    }

    @PostConstruct
    protected void start() {
        if (started.compareAndSet(false, true)) {
            // this thread SHOULD NOT be a daemon
            scheduler = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("SubscriptionScheduler", false));
            scheduler.scheduleAtFixedRate(new CheckSubscriptionsTask(), 5, SCHEDULE_PERIOD, TimeUnit.MINUTES);
        } else {
            throw new IllegalStateException("Already started");
        }
    }

    class CheckSubscriptionsTask implements Runnable {
        @Override
        public void run() {
            try {
                Iterator<Subscription> allSubscriptionsIterator = accountDao.getAllSubscriptions().iterator();
                while (!Thread.currentThread().isInterrupted() && allSubscriptionsIterator.hasNext()) {
                    Subscription subscription = allSubscriptionsIterator.next();

                    for (SubscriptionSchedulerHandler handler : handlers) {
                        try {
                            if (ABORT_CHECK == handler.checkSubscription(subscription)) {
                                break;
                            }
                        } catch (ApiException e) {
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
        if (started.compareAndSet(true, false)) {
            scheduler.shutdownNow();
            try {
                if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                    LOG.warn("Unable terminate scheduler");
                }
            } catch (InterruptedException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        } else {
            throw new IllegalStateException("Is not started yet.");
        }
    }
}
