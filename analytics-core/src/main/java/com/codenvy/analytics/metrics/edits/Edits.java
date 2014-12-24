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
package com.codenvy.analytics.metrics.edits;

import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.ReadBasedExpandable;
import com.codenvy.analytics.metrics.ReadBasedMetric;
import com.codenvy.analytics.metrics.tasks.AbstractTasksMetric;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import javax.annotation.security.RolesAllowed;

/** @author Dmytro Nochevnov */
@RolesAllowed({"system/admin", "system/manager"})
public class Edits extends ReadBasedMetric implements ReadBasedExpandable {
    public Edits() {
        super(MetricType.EDITS);
    }

    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.TASKS);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{VALUE};
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    public DBObject[] getSpecificDBOperations(Context clauses) {
        DBObject match = new BasicDBObject(TASK_TYPE, new BasicDBObject("$in", new String[] {AbstractTasksMetric.EDITOR}));

        DBObject group = new BasicDBObject();
        group.put(ID, null);
        group.put(VALUE, new BasicDBObject("$sum", 1));

        return new DBObject[]{new BasicDBObject("$match", match),
                              new BasicDBObject("$group", group)};
    }

    @Override
    public DBObject[] getSpecificExpandedDBOperations(Context clauses) {
        DBObject match = new BasicDBObject(TASK_TYPE, new BasicDBObject("$in", new String[] {AbstractTasksMetric.EDITOR}));

        DBObject group = new BasicDBObject();
        group.put(ID, "$" + getExpandedField());

        DBObject projection = new BasicDBObject(getExpandedField(), "$_id");

        return new DBObject[]{new BasicDBObject("$match", match),
                              new BasicDBObject("$group", group),
                              new BasicDBObject("$project", projection)};
    }

    /** {@inheritDoc} */
    @Override
    public String getExpandedField() {
        return TASK_ID;
    }

    @Override
    public String getDescription() {
        return "The total number of edits";
    }
}
