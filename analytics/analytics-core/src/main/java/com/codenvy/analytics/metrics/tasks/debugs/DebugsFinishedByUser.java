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
package com.codenvy.analytics.metrics.tasks.debugs;

import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.tasks.TasksStoppedNormally;

import java.io.IOException;

/** @author Anatoliy Bazko */
public class DebugsFinishedByUser extends TasksStoppedNormally {

    public DebugsFinishedByUser() {
        super(MetricType.DEBUGS_FINISHED_BY_USER);
    }

    /** {@inheritDoc} */
    @Override
    public Context applySpecificFilter(Context context) throws IOException {
        Context.Builder builder = new Context.Builder(super.applySpecificFilter(context));
        builder.put(MetricFilter.TASK_TYPE, DEBUGGER);
        builder.put(MetricFilter.SHUTDOWN_TYPE, NormalShutdownType.USER.name().toLowerCase());
        return builder.build();
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "The number of debugs stopped by user";
    }
}
