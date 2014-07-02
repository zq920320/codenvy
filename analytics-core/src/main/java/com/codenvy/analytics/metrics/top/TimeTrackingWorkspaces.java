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
package com.codenvy.analytics.metrics.top;

import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.io.IOException;

/**
 * @author Alexander Reshetnyak
 */
public class TimeTrackingWorkspaces extends AbstractTimeTracking {

    public TimeTrackingWorkspaces() {
        super(MetricType.TIME_TRACKING_WORKSPACES);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{WS,
                            TIME};
    }

    @Override
    public DBObject[] getSpecificDBOperations(Context clauses) {
        DBObject group = new BasicDBObject();
        group.put(ID, "$" + WS);
        group.put(TIME, new BasicDBObject("$sum", "$" + TIME));

        DBObject project = new BasicDBObject();
        project.put(WS, "$" + ID);
        project.put(TIME, "$" + TIME);

        return new DBObject[]{new BasicDBObject("$group", group),
                              new BasicDBObject("$project", project)};
    }

    @Override
    public Context applySpecificFilter(Context context) throws IOException {
        Context.Builder builder = new Context.Builder();
        builder.putAll(context);
        builder.put(Parameters.WS, Parameters.WS_TYPES.PERSISTENT.toString());

        return super.applySpecificFilter(builder.build());
    }

    @Override
    public String getDescription() {
        return "Top 100 workspaces by time working in product during specific period";
    }
}
