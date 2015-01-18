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
package com.codenvy.saas;

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
 * @author Alexander Garagatyi
 */
// must be eager singleton
@Singleton
public class SaasBillingScheduler {
    private static final Logger LOG = LoggerFactory.getLogger(SaasBillingScheduler.class);

    private final int                      schedulerDelay;
    private final int                      schedulerPeriod;
    private final SaasBillingService       saasBillingService;
    private final ScheduledExecutorService scheduler;
    private final SaasChargeTask           chargeTask;

    @Inject
    public SaasBillingScheduler(@Named("subscription.saas.scheduler.delay.minutes") int schedulerDelay,
                                @Named("subscription.saas.scheduler.period.minutes") int schedulerPeriod,
                                SaasBillingService saasBillingService) {
        this.schedulerDelay = schedulerDelay;
        this.schedulerPeriod = schedulerPeriod;
        this.saasBillingService = saasBillingService;
        this.scheduler = Executors.newScheduledThreadPool(1, new NamedThreadFactory("SaasChargeTask", false));
        this.chargeTask = new SaasChargeTask();
    }

    @PostConstruct
    public void scheduleBilling() {
        scheduler.scheduleAtFixedRate(chargeTask, schedulerDelay, schedulerPeriod, TimeUnit.MINUTES);
    }

    @PreDestroy
    private void destroy() {
        scheduler.shutdownNow();
    }

    private class SaasChargeTask implements Runnable {
        @Override
        public void run() {
            try {
                saasBillingService.chargeAccounts();
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }
}
