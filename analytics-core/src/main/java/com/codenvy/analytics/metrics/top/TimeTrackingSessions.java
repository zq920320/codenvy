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
package com.codenvy.analytics.metrics.top;

import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.mongodb.DBObject;

import java.io.IOException;

/**
 * @author Alexander Reshetnyak
 */
public class TimeTrackingSessions extends AbstractTimeTracking {

    public TimeTrackingSessions() {
        super(MetricType.TIME_TRACKING_SESSIONS);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{SESSION_ID,
                            USER,
                            WS,
                            TIME};
    }

    @Override
    public DBObject[] getSpecificDBOperations(Context clauses) {
        return new DBObject[0];
    }

    @Override
    public Context applySpecificFilter(Context context) throws IOException {
        Context.Builder builder = new Context.Builder();
        builder.putAll(context);
        builder.put(Parameters.USER, Parameters.USER_TYPES.REGISTERED.toString());
        builder.put(Parameters.WS, Parameters.WS_TYPES.PERSISTENT.toString());

        return super.applySpecificFilter(builder.build());
    }

    @Override
    public String getDescription() {
        return "Top 100 sessions by time working in product during specific period";
    }
}