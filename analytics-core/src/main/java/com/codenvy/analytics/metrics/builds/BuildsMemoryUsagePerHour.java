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
package com.codenvy.analytics.metrics.builds;

import com.codenvy.analytics.datamodel.DoubleValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.ReadBasedExpandable;
import com.codenvy.analytics.metrics.ReadBasedMetric;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import javax.annotation.security.RolesAllowed;

/** @author Dmytro Nochevnov */
@RolesAllowed(value = {"user", "system/admin", "system/manager"})
public class BuildsMemoryUsagePerHour extends ReadBasedMetric implements ReadBasedExpandable {

    public BuildsMemoryUsagePerHour() {
        super(MetricType.BUILDS_MEMORY_USAGE_PER_HOUR);
    }

    public static final int BUILDER_MEMORY_USAGE_IN_MB = 1536;  // TODO (dnochevnov) temporary constant 1.5GB until the issue IDEX-1760 will be resolved.

    /** {@inheritDoc} */
    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.BUILDS_FINISHED);
    }

    /** {@inheritDoc} */
    @Override
    public String[] getTrackedFields() {
        return new String[]{VALUE};
    }

    /** {@inheritDoc} */
    @Override
    public DBObject[] getSpecificDBOperations(Context clauses) {
        DBObject project1 = new BasicDBObject("x", new BasicDBObject("$multiply", new Object[]{BUILDER_MEMORY_USAGE_IN_MB, "$" + USAGE_TIME}));
        DBObject project2 = new BasicDBObject("y", new BasicDBObject("$divide", new Object[]{"$x", 3686400000L}));
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
        return PROJECT_ID;
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return DoubleValueData.class;
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "The memory usage in GB per hour";
    }

}
