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
package com.codenvy.api.account.subscription.saas.limit;

import com.codenvy.api.account.billing.BillingPeriod;
import com.codenvy.api.account.billing.BillingService;
import com.codenvy.api.account.billing.ResourcesFilter;
import com.codenvy.api.account.impl.shared.dto.AccountResources;
import com.codenvy.api.account.subscription.ServiceId;

import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.account.server.dao.Subscription;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.runner.RunQueue;
import org.eclipse.che.api.runner.RunQueueTask;
import org.eclipse.che.commons.schedule.ScheduleRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Periodically check the active runs to make sure they not exceeded the free RAM limit.
 *
 * @author Max Shaposhnik.
 */
@Singleton
public class ActiveRunRemainResourcesChecker implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(ActiveRunRemainResourcesChecker.class);

    private final ActiveRunHolder activeRunHolder;
    private final AccountDao      accountDao;
    private final BillingService  service;
    private final RunQueue        runQueue;
    private final BillingPeriod   billingPeriod;

    @Inject
    public ActiveRunRemainResourcesChecker(ActiveRunHolder activeRunHolder,
                                           AccountDao accountDao,
                                           BillingService service,
                                           RunQueue runQueue,
                                           BillingPeriod billingPeriod) {
        this.activeRunHolder = activeRunHolder;
        this.accountDao = accountDao;
        this.service = service;
        this.runQueue = runQueue;
        this.billingPeriod = billingPeriod;
    }

    @ScheduleRate(period = 60)
    @Override
    public void run() {
        for (Map.Entry<String, Set<Long>> accountRuns : activeRunHolder.getActiveRuns().entrySet()) {
            try {
                final Subscription activeSaasSubscription = accountDao.getActiveSubscription(accountRuns.getKey(), ServiceId.SAAS);
                if (activeSaasSubscription != null && !"sas-community".equals(activeSaasSubscription.getPlanId())) {
                    return;
                }

                long startBillingPeriod = billingPeriod.getCurrent().getStartDate().getTime();
                final List<AccountResources> usedMemory = service.getEstimatedUsageByAccount(ResourcesFilter.builder()
                                                                                                            .withAccountId(
                                                                                                                    accountRuns.getKey())
                                                                                                            .withFromDate(
                                                                                                                    startBillingPeriod)
                                                                                                            .withTillDate(
                                                                                                                    System.currentTimeMillis())
                                                                                                            .withPaidGbHMoreThan(0)
                                                                                                            .build());
                if (!usedMemory.isEmpty()) {
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
