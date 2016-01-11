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
package com.codenvy.api.subscription.server;

import com.braintreegateway.BraintreeGateway;
import com.codenvy.api.subscription.server.dao.PlanDao;
import com.codenvy.api.subscription.server.dao.RemoveAccountEventSubscriber;
import com.codenvy.api.subscription.server.dao.SubscriptionDao;
import com.codenvy.api.subscription.server.dao.SubscriptionQueryBuilder;
import com.codenvy.api.subscription.server.dao.mongo.MongoSubscriptionQueryBuilder;
import com.codenvy.api.subscription.server.dao.mongo.PlanDaoImpl;
import com.codenvy.api.subscription.server.dao.mongo.SubscriptionDaoImpl;
import com.codenvy.api.subscription.server.payment.GuiceBraintreeGateway;
import com.google.inject.AbstractModule;

/**
 * @author Sergii Leschenko
 */
public class SubscriptionModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(PlanDao.class).to(PlanDaoImpl.class);
        bind(SubscriptionDao.class).to(SubscriptionDaoImpl.class);
        bind(SubscriptionQueryBuilder.class).to(MongoSubscriptionQueryBuilder.class);
        bind(BraintreeGateway.class).to(GuiceBraintreeGateway.class).asEagerSingleton();
        bind(SubscriptionService.class);
        bind(RemoveAccountEventSubscriber.class).asEagerSingleton();
    }
}
