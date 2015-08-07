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
package com.codenvy.analytics.metrics.tasks;

import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.mongodb.BasicDBObject;

import java.io.IOException;

/** @author Dmytro Nochevnov */
public class TasksStoppedNormally extends TasksStopped {
    public enum NormalShutdownType {
        NORMAL,
        USER;

        public static String[] names() {
            NormalShutdownType[] states = values();
            String[] names = new String[states.length];

            for (int i = 0; i < states.length; i++) {
                names[i] = states[i].name().toLowerCase();
            }

            return names;
        }
    }

    public TasksStoppedNormally() {
        this(MetricType.TASKS_STOPPED_NORMALLY);
    }

    public TasksStoppedNormally(MetricType metricType) {
        super(metricType);
    }

    /** {@inheritDoc} */
    @Override
    public Context applySpecificFilter(Context context) throws IOException {
        Context.Builder builder = new Context.Builder(super.applySpecificFilter(context));
        builder.put(MetricFilter.SHUTDOWN_TYPE, new BasicDBObject("$in", NormalShutdownType.names()));
        return builder.build();
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "The number of tasks stopped normally";
    }
}
