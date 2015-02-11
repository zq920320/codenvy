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
package com.codenvy.api.account.subscription.schedulers;

import com.codenvy.api.account.billing.BillingPeriod;
import com.codenvy.api.account.billing.BillingService;
import com.codenvy.api.account.billing.Period;
import com.codenvy.api.core.ServerException;
import com.codenvy.commons.schedule.ScheduleDelay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

/**
 * @author Sergii Leschenko
 */
@Singleton
public class GenerateInvoicesJob implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(GenerateInvoicesJob.class);

    @Inject
    BillingService billingService;

    @Inject
    BillingPeriod billingPeriod;

    // 0sec 0min 07hour 1st day of every month
//    @ScheduleCron(cron = "30 * * * ? ?")
    @ScheduleDelay(initialDelay = 10,
                   delay = 30,
                   unit = TimeUnit.SECONDS)
    @Override
    public void run() {
        final Period current = billingPeriod.getCurrent();

        try {
            billingService.generateInvoices(current.getStartDate().getTime(), current.getEndDate().getTime());
        } catch (ServerException e) {
            e.printStackTrace();
        }
    }
}