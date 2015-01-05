/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
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
package com.codenvy.analytics.metrics.debugs;

import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.ide_usage.AbstractTimeSpentInAction;
import com.codenvy.analytics.metrics.tasks.AbstractTasksMetric;

import javax.annotation.security.RolesAllowed;
import java.io.IOException;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
@RolesAllowed({"system/admin", "system/manager"})
public class DebugsTime extends AbstractTimeSpentInAction {

    public DebugsTime() {
        super(MetricType.DEBUGS_TIME, TASK_ID);
    }

    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.TASKS);
    }

    @Override public Context applySpecificFilter(Context context) throws IOException {
        Context.Builder builder = new Context.Builder(super.applySpecificFilter(context));
        builder.put(MetricFilter.TASK_TYPE, AbstractTasksMetric.DEBUGGER);

        return builder.build();
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{USAGE_TIME};
    }

    @Override
    public String getDescription() {
        return "The total time of all debugs in minutes";
    }
}
