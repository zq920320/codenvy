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
package com.codenvy.analytics.api;

import com.codenvy.analytics.impl.FileBasedMetricHandler;
import com.codenvy.api.analytics.MetricHandler;
import com.codenvy.inject.DynaModule;
import com.google.inject.AbstractModule;

/** @author Anatoliy Bazko */
@DynaModule
public class ApiModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(MetricHandler.class).to(FileBasedMetricHandler.class);
        bind(AnalyticsPrivate.class);
        bind(View.class);
        bind(Service.class);
    }
}
