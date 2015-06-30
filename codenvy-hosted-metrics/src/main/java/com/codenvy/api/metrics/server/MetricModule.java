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
package com.codenvy.api.metrics.server;

import com.codenvy.api.metrics.server.builds.BuildStatusSubscriber;
import com.codenvy.api.metrics.server.builds.BuildTasksActivityChecker;
import com.codenvy.api.metrics.server.dao.MeterBasedStorage;
import com.codenvy.api.metrics.server.dao.sql.SqlMeterBasedStorage;
import com.codenvy.api.metrics.server.limit.ResourcesUsageLimitProvider;
import com.codenvy.api.metrics.server.limit.ResourcesWatchdogProvider;
import com.codenvy.api.metrics.server.limit.WorkspaceCapsResourcesWatchdogProvider;
import com.codenvy.api.metrics.server.limit.subscriber.BuildEventSubscriber;
import com.codenvy.api.metrics.server.limit.subscriber.ChangeResourceUsageLimitSubscriber;
import com.codenvy.api.metrics.server.limit.subscriber.RunEventSubscriber;
import com.codenvy.api.metrics.server.period.MetricPeriod;
import com.codenvy.api.metrics.server.period.MonthlyMetricPeriod;
import com.codenvy.api.metrics.server.runs.RunStatusSubscriber;
import com.codenvy.api.metrics.server.runs.RunTasksActivityChecker;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

/**
 * @author Sergii Kabashniuk
 */
public class MetricModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(MetricPeriod.class).to(MonthlyMetricPeriod.class);
        bind(BuildTasksActivityChecker.class).asEagerSingleton();
        bind(RunTasksActivityChecker.class).asEagerSingleton();
        bind(MeterBasedStorage.class).to(SqlMeterBasedStorage.class);
        bind(BuildStatusSubscriber.class);
        bind(RunStatusSubscriber.class);
        bind(WorkspaceLockWebSocketMessenger.class);

        bind(BuildEventSubscriber.class).asEagerSingleton();
        bind(RunEventSubscriber.class).asEagerSingleton();
        bind(ChangeResourceUsageLimitSubscriber.class).asEagerSingleton();
        bind(ResourcesUsageLimitProvider.class);
        Multibinder<ResourcesWatchdogProvider> watchdogProviders = Multibinder.newSetBinder(binder(),
                                                                                            ResourcesWatchdogProvider.class);
        watchdogProviders.addBinding().to(WorkspaceCapsResourcesWatchdogProvider.class);
    }
}
