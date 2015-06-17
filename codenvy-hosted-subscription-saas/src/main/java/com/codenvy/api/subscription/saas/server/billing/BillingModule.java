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
package com.codenvy.api.subscription.saas.server.billing;

import com.codenvy.api.subscription.saas.server.dao.BonusDao;
import com.codenvy.api.subscription.server.payment.GuiceBraintreeGateway;
import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;

import static org.eclipse.che.inject.Matchers.names;

/**
 * @author Sergii Kabashniuk
 */
public class BillingModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(com.braintreegateway.BraintreeGateway.class).to(GuiceBraintreeGateway.class).asEagerSingleton();
        bind(BillingPeriod.class).to(MonthlyBillingPeriod.class);

        bind(InvoiceTemplateProcessor.class);

        bind(InvoiceCharger.class);
        bind(InvoiceRecharger.class).asEagerSingleton();

        bind(InvoiceService.class);

        CreateBonusInterceptor createBonusInterceptor = new CreateBonusInterceptor();
        requestInjection(createBonusInterceptor);
        bindInterceptor(Matchers.subclassesOf(BonusDao.class),
                        names("create"),
                        createBonusInterceptor);
    }
}
