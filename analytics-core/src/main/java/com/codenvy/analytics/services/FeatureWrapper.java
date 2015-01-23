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
package com.codenvy.analytics.services;

import com.codenvy.analytics.Injector;
import com.codenvy.analytics.metrics.Context;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * @author Dmytro Nochevnov
 * @author Anatoliy Bazko
 */
public abstract class FeatureWrapper extends Feature {
    private final Feature delegated;

    public FeatureWrapper(Class<? extends Feature> featureClass) {
        delegated = Injector.getInstance(featureClass);
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        delegated.execute(jobExecutionContext);
    }

    @Override
    public void forceExecute(Context context) throws JobExecutionException {
        delegated.forceExecute(context);
    }

    @Override
    public boolean isAvailable() {
        return delegated.isAvailable();
    }

    @Override
    protected void doExecute(Context context) throws Exception {
        throw new UnsupportedOperationException();
    }
}
