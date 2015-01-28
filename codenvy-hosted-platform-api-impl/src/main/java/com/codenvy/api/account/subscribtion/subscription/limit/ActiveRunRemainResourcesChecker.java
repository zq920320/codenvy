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
package com.codenvy.api.account.subscribtion.subscription.limit;

import com.codenvy.api.account.billing.BillingPeriod;
import com.codenvy.api.account.server.MeterBasedStorage;
import com.codenvy.api.account.server.dao.Account;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.runner.RunQueue;
import com.codenvy.api.runner.RunQueueTask;
import com.codenvy.commons.schedule.ScheduleRate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Set;

/**
 * Periodically check the active runs to make sure they not exceeded the free RAM limit.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com) on 1/16/15.
 */
@Singleton
public class ActiveRunRemainResourcesChecker implements Runnable {

    private static final Logger LOG                          = LoggerFactory.getLogger(ActiveRunRemainResourcesChecker.class);
    public static final  String RUN_ACTIVITY_CHECKING_PERIOD = "resources.run_activity_checking.period_sec";

    private final ActiveRunHolder   activeRunHolder;
    private final AccountDao        accountDao;
    private final MeterBasedStorage storage;
    private final RunQueue          runQueue;
    private final BillingPeriod     billingPeriod;
    private final long freeUsageLimit;

    @Inject
    public ActiveRunRemainResourcesChecker(ActiveRunHolder activeRunHolder, AccountDao accountDao,
                                           MeterBasedStorage storage, RunQueue runQueue, BillingPeriod billingPeriod,
                                           @Named("subscription.saas.usage.free.mb_minutes") long freeUsage) {
        this.activeRunHolder = activeRunHolder;
        this.accountDao = accountDao;
        this.storage = storage;
        this.runQueue = runQueue;
        this.billingPeriod = billingPeriod;
        this.freeUsageLimit = freeUsage;
    }

    @ScheduleRate(periodParameterName = RUN_ACTIVITY_CHECKING_PERIOD)
    @Override
    public void run() {
        for (Map.Entry<String, Set<Long>> accountRuns : activeRunHolder.getActiveRuns().entrySet()) {
            try {
                final Account account = accountDao.getById(accountRuns.getKey());
                if (account.getAttributes().containsKey("codenvy:paid")) {
                    return;
                }
                Long used =
                        storage.getMemoryUsed(accountRuns.getKey(), billingPeriod.getCurrentPeriodStartDate().getTime(),
                                              System.currentTimeMillis());
                if (used >= freeUsageLimit) {
                    for (Long processId : accountRuns.getValue()) {
                        try {
                            final RunQueueTask task = runQueue.getTask(processId);
                            task.stop();
                        } catch (Exception e) {
                            LOG.error("Unable to terminate process id: {} ", processId);
                        }
                    }
                }
            } catch (NotFoundException | ServerException e) {
                LOG.error("Error check remaining resources  in account {} .", accountRuns.getKey());
            }
        }
    }
}
