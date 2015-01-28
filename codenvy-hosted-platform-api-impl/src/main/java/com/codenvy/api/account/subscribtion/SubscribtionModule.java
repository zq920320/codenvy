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
package com.codenvy.api.account.subscribtion;

import com.codenvy.api.account.server.subscription.SubscriptionService;
import com.codenvy.api.account.subscribtion.factory.FactorySubscriptionService;
import com.codenvy.api.account.subscribtion.onpremises.OnPremisesSubscriptionService;
import com.codenvy.api.account.subscribtion.saas.RefillJob;
import com.codenvy.api.account.subscribtion.saas.SaasBillingRestService;
import com.codenvy.api.account.subscribtion.saas.SaasBillingScheduler;
import com.codenvy.api.account.subscribtion.saas.SaasBillingService;
import com.codenvy.api.account.subscribtion.saas.limit.ActiveRunHolder;
import com.codenvy.api.account.subscribtion.saas.limit.ActiveRunRemainResourcesChecker;
import com.codenvy.api.account.subscribtion.saas.limit.CheckRemainResourcesOnStopSubscriber;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

/**
 * @author Sergii Kabashniuk
 */
public class SubscribtionModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder<SubscriptionService> subscriptionServiceBinder =
                Multibinder.newSetBinder(binder(), com.codenvy.api.account.server.subscription.SubscriptionService.class);
        subscriptionServiceBinder.addBinding().to(FactorySubscriptionService.class);
        subscriptionServiceBinder.addBinding().to(OnPremisesSubscriptionService.class);

        bindConstant().annotatedWith(Names.named(ActiveRunRemainResourcesChecker.RUN_ACTIVITY_CHECKING_PERIOD)).to(60);
        bind(ActiveRunHolder.class);
        bind(ActiveRunRemainResourcesChecker.class).asEagerSingleton();
        bind(CheckRemainResourcesOnStopSubscriber.class).asEagerSingleton();
        bind(RefillJob.class);

        bind(SubscriptionScheduler.class).asEagerSingleton();
        bindConstant().annotatedWith(
                Names.named(SubscriptionScheduler.SUBSCRIPTION_SCHEDULER_INITIAL_DELAY_MINUTES)).to(6);
        bindConstant()
                .annotatedWith(Names.named(SubscriptionScheduler.SUBSCRIPTION_SCHEDULER_PERIOD_MINUTES))
                .to(6 * 60);


        bind(SaasBillingService.class).asEagerSingleton();
        bind(SaasBillingRestService.class).asEagerSingleton();
        bind(SaasBillingScheduler.class).asEagerSingleton();
    }
}
