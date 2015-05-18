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
package com.codenvy.api.account.billing;

import com.codenvy.api.account.AccountLocker;
import com.google.inject.Inject;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;

/**
 * @author Sergii Leschenko
 */
public class CreateBonusInterceptor implements MethodInterceptor {
    private static final Logger LOG = LoggerFactory.getLogger(CreateBonusInterceptor.class);

    @Inject
    BillingService billingService;
    @Inject
    BillingPeriod  billingPeriod;
    @Inject
    AccountLocker  accountLocker;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object result = invocation.proceed();
        Bonus createdBonus = (Bonus)result;
        String accountId = createdBonus.getAccountId();
        try {
            final Period current = billingPeriod.getCurrent();
            if (billingService.hasAvailableResources(accountId, current.getStartDate().getTime(), System.currentTimeMillis())) {
                accountLocker.removeResourcesLock(accountId);
            }
        } catch (Exception e) {
            LOG.error(format("Can't unlock account %s by adding of bonus", accountId), e);
        }
        return result;
    }
}
