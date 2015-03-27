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

import javax.inject.Inject;
import java.util.List;

/**
 * Checks if account has available resources
 *
 * @author Sergii Leschenko
 */
public class ResourcesChecker {
    private final AccountDao     accountDao;
    private final BillingService service;
    private final BillingPeriod  billingPeriod;

    @Inject
    public ResourcesChecker(AccountDao accountDao, BillingService service, BillingPeriod billingPeriod) {
        this.accountDao = accountDao;
        this.service = service;
        this.billingPeriod = billingPeriod;
    }

    public boolean hasAvailableResources(String accountId) throws NotFoundException, ServerException {
        final Subscription activeSaasSubscription = accountDao.getActiveSubscription(accountId, ServiceId.SAAS);
        if (activeSaasSubscription != null && !"sas-community".equals(activeSaasSubscription.getPlanId())) {
            return true;
        }

        List<AccountResources> usedMemory = service.getEstimatedUsageByAccount(ResourcesFilter.builder()
                                                                                              .withAccountId(accountId)
                                                                                              .withFromDate(billingPeriod.getCurrent()
                                                                                                                         .getStartDate()
                                                                                                                         .getTime())
                                                                                              .withTillDate(System.currentTimeMillis())
                                                                                              .withPaidGbHMoreThan(0)
                                                                                              .build());
        return usedMemory.isEmpty();
    }
}
