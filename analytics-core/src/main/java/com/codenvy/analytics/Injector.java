/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2014] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.analytics;

import com.codenvy.analytics.metrics.accounts.DummyHTTPMetricTransport;
import com.codenvy.analytics.metrics.accounts.HTTPMetricTransport;
import com.codenvy.analytics.metrics.accounts.MetricTransport;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;

/** @author Anatoliy Bazko */
public class Injector {

    private static final com.google.inject.Injector injector;

    static {
        final com.google.inject.Injector parent = Guice.createInjector();
        injector = parent.createChildInjector(new AbstractModule() {
            @Override
            protected void configure() {
                    Configurator configurator = parent.getInstance(Configurator.class);
                if (configurator.exists(HTTPMetricTransport.API_ENDPOINT)) {
                    bind(MetricTransport.class).to(HTTPMetricTransport.class);
                } else {
                    bind(MetricTransport.class).to(DummyHTTPMetricTransport.class);
                }
            }
        });
    }

    public static <T> T getInstance(Class<T> clazz) {
        return injector.getInstance(clazz);
    }
}
