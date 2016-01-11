/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2016] Codenvy, S.A.
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
package com.codenvy.api.subscription.onpremises.server;

import com.codenvy.api.subscription.server.AbstractSubscriptionService;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

/**
 * @author Sergii Leschenko
 */
public class OnPremisesSubscriptionModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder<AbstractSubscriptionService> subscriptionServiceBinder =
                Multibinder.newSetBinder(binder(), AbstractSubscriptionService.class);
        subscriptionServiceBinder.addBinding().to(OnPremisesSubscriptionService.class);
        bind(OnPremisesSubscriptionSubscriber.class);
    }
}
