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
package com.codenvy.api.subscription.saas.server;

import com.codenvy.api.subscription.saas.server.dao.BonusDao;
import com.codenvy.api.subscription.saas.server.billing.SaasBraintreePaymentService;
import com.codenvy.api.subscription.saas.server.dao.sql.SqlBonusDao;
import com.codenvy.api.subscription.saas.server.job.RefillJob;
import com.codenvy.api.subscription.saas.server.limit.ActiveTasksHolder;
import com.codenvy.api.subscription.saas.server.limit.ResourcesUsageLimitProvider;
import com.codenvy.api.subscription.saas.server.schedulers.GenerateInvoicesJob;
import com.codenvy.api.subscription.saas.server.schedulers.InvoiceChargingScheduler;
import com.codenvy.api.subscription.saas.server.schedulers.MailScheduler;
import com.codenvy.api.subscription.saas.server.schedulers.SubscriptionScheduler;
import com.codenvy.api.subscription.server.AbstractSubscriptionService;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

/**
 * @author Sergii Kabashniuk
 */
public class SaasSubscriptionModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(InvoicePaymentService.class).to(SaasBraintreePaymentService.class).asEagerSingleton();
        bind(BonusDao.class).to(SqlBonusDao.class);
        bind(SaasService.class);

        Multibinder<AbstractSubscriptionService> subscriptionServiceBinder = Multibinder.newSetBinder(binder(),
                                                                                                      AbstractSubscriptionService.class);
        subscriptionServiceBinder.addBinding().to(SaasSubscriptionService.class);

        bind(ActiveTasksHolder.class).asEagerSingleton();
        bind(ResourcesUsageLimitProvider.class).asEagerSingleton();
        bind(InvoiceChargingScheduler.class);
        bind(RefillJob.class);
        bind(MailScheduler.class);
        bind(GenerateInvoicesJob.class);
        bind(SubscriptionScheduler.class).asEagerSingleton();
        bind(CreditCardRegistrationSubscriber.class);
    }
}
