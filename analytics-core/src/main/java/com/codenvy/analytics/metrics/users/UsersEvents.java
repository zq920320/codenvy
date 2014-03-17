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
package com.codenvy.analytics.metrics.users;

import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.ReadBasedMetric;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import javax.annotation.security.RolesAllowed;

/**
 * @author Alexander Reshetnyak
 * @author Anatoliy Bazko
 */
@RolesAllowed({"system/admin", "system/manager"})
public class UsersEvents extends ReadBasedMetric {

    public UsersEvents() {
        super(MetricType.USERS_EVENTS);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{ACTION, COUNT};
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListValueData.class;
    }

    @Override
    public DBObject[] getSpecificDBOperations(Context clauses) {

        DBObject group = new BasicDBObject();
        group.put(ID, "$" + ACTION);
        group.put(COUNT, new BasicDBObject("$sum", 1));

        DBObject project = new BasicDBObject();
        project.put(ACTION, "$_id");
        project.put(COUNT, "$" + COUNT);

        return new DBObject[]{new BasicDBObject("$group", group),
                              new BasicDBObject("$project", project)};
    }

    @Override
    public String getDescription() {
        return "The number of user's events per action";
    }
}
