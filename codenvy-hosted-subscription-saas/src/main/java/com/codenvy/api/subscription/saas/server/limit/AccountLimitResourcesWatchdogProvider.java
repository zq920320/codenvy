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
package com.codenvy.api.subscription.saas.server.limit;

import com.codenvy.api.metrics.server.limit.MeteredTask;
import com.codenvy.api.metrics.server.limit.ResourcesWatchdog;
import com.codenvy.api.metrics.server.limit.ResourcesWatchdogProvider;
import com.codenvy.api.metrics.server.period.MetricPeriod;
import com.codenvy.api.subscription.saas.server.AccountLocker;
import com.codenvy.api.subscription.saas.server.billing.BillingService;
import com.codenvy.api.subscription.server.dao.Subscription;
import com.codenvy.api.subscription.server.dao.SubscriptionDao;
import com.codenvy.service.http.WorkspaceInfoCache;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static com.codenvy.api.subscription.saas.server.SaasSubscriptionService.SAAS_SUBSCRIPTION_ID;

/**
 * @author Sergii Leschenko
 */
public class AccountLimitResourcesWatchdogProvider implements ResourcesWatchdogProvider {
    private static final Logger LOG = LoggerFactory.getLogger(AccountLimitResourcesWatchdogProvider.class);

    private final WorkspaceInfoCache workspaceInfoCache;
    private final BillingService     billingService;
    private final MetricPeriod       metricPeriod;
    private final SubscriptionDao    subscriptionDao;
    private final AccountLocker      accountLocker;

    @Inject
    public AccountLimitResourcesWatchdogProvider(WorkspaceInfoCache workspaceInfoCache,
                                                 BillingService billingService,
                                                 MetricPeriod metricPeriod,
                                                 SubscriptionDao subscriptionDao,
                                                 AccountLocker accountLocker) {
        this.workspaceInfoCache = workspaceInfoCache;
        this.billingService = billingService;
        this.metricPeriod = metricPeriod;
        this.subscriptionDao = subscriptionDao;
        this.accountLocker = accountLocker;
    }

    @Override
    public String getId(MeteredTask meteredTask) {
        final String workspaceId = meteredTask.getWorkspaceId();
        try {
            return workspaceInfoCache.getById(workspaceId).getAccountId();
        } catch (ServerException e) {
            LOG.error("Can't calculate account id for workspace " + workspaceId + " for tracking resources usage", e);
        } catch (NotFoundException e) {
            //do nothing
        }

        return null;
    }

    @Override
    public ResourcesWatchdog createWatchdog(MeteredTask meteredTask) {
        AccountResourcesWatchdog accountResourcesWatchdog = new AccountResourcesWatchdog(getId(meteredTask));
        accountResourcesWatchdog.checkLimit();
        return accountResourcesWatchdog;
    }

    class AccountResourcesWatchdog implements ResourcesWatchdog {
        private final String  accountId;
        private       boolean hasLimit;

        public AccountResourcesWatchdog(String accountId) {
            this.accountId = accountId;
        }

        @Override
        public String getId() {
            return accountId;
        }

        @Override
        public boolean isLimitedReached() {
            if (!hasLimit) {
                return false;
            }

            try {
                return !billingService.hasAvailableResources(accountId,
                                                             metricPeriod.getCurrent()
                                                                         .getStartDate()
                                                                         .getTime(),
                                                             System.currentTimeMillis());
            } catch (ServerException e) {
                LOG.error("Can't check resources consuming in account " + accountId, e);
            }

            return false;
        }

        @Override
        public void checkLimit() {
            try {
                final Subscription activeSaasSubscription = subscriptionDao.getActiveByServiceId(accountId, SAAS_SUBSCRIPTION_ID);
                if (activeSaasSubscription != null) {
                    hasLimit = false;
                    return;
                }
            } catch (ServerException | NotFoundException e) {
                LOG.error("Can't check resources usage limit in account " + accountId, e);
            }
            hasLimit = true;
        }

        @Override
        public void lock() {
            accountLocker.setResourcesLock(accountId);
        }
    }
}
