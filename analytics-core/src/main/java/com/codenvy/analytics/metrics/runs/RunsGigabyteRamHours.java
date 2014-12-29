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
package com.codenvy.analytics.metrics.runs;

import com.codenvy.analytics.datamodel.DoubleValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.ReadBasedExpandable;
import com.codenvy.analytics.metrics.ReadBasedMetric;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import javax.annotation.security.RolesAllowed;

import static com.codenvy.analytics.pig.udf.CalculateGigabyteRamHours.GRH_DEVIDER;

/** @author Anatoliy Bazko */
@RolesAllowed(value = {"user", "system/admin", "system/manager"})
public class RunsGigabyteRamHours extends ReadBasedMetric implements ReadBasedExpandable {
    public RunsGigabyteRamHours() {
        super(MetricType.RUNS_GIGABYTE_RAM_HOURS);
    }

    /** {@inheritDoc} */
    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.RUNS_FINISHED);
    }

    /** {@inheritDoc} */
    @Override
    public String[] getTrackedFields() {
        return new String[]{VALUE};
    }

    /** {@inheritDoc} */
    @Override
    public DBObject[] getSpecificDBOperations(Context clauses) {
        DBObject project1 = new BasicDBObject("x", new BasicDBObject("$multiply", new Object[]{"$" + MEMORY, "$" + USAGE_TIME}));
        DBObject project2 = new BasicDBObject("y", new BasicDBObject("$divide", new Object[]{"$x", GRH_DEVIDER}));
        DBObject group = new BasicDBObject(ID, null).append(VALUE, new BasicDBObject("$sum", "$y"));

        return new DBObject[]{new BasicDBObject("$project", project1),
                              new BasicDBObject("$project", project2),
                              new BasicDBObject("$group", group)};
    }

    /** {@inheritDoc} */
    @Override
    public DBObject[] getSpecificExpandedDBOperations(Context clauses) {
        DBObject group = new BasicDBObject();
        group.put(ID, "$" + getExpandedField());

        DBObject project = new BasicDBObject(getExpandedField(), "$_id");

        return new DBObject[]{new BasicDBObject("$group", group),
                              new BasicDBObject("$project", project)};
    }

    /** {@inheritDoc} */
    @Override
    public String getExpandedField() {
        return TASK_ID;
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return DoubleValueData.class;
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "The memory usage in GB RAM on hour";
    }

}
