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


package com.codenvy.analytics.metrics.users;

import com.codenvy.analytics.datamodel.SetValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.ReadBasedExpandable;
import com.codenvy.analytics.metrics.ReadBasedMetric;
import com.codenvy.analytics.metrics.tasks.TasksGigabyteRamHours;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import javax.annotation.security.RolesAllowed;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import static com.codenvy.analytics.Utils.getFilterAsString;
import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsSet;

/** @author Anatoliy Bazko */
@RolesAllowed({"system/admin", "system/manager"})
public abstract class AbstractNewUsersAnalysis extends ReadBasedMetric implements ReadBasedExpandable {

    private final Metric basedMetric;

    public AbstractNewUsersAnalysis(MetricType metricType, MetricType basedMetricType) {
        super(metricType);
        this.basedMetric = MetricFactory.getMetric(basedMetricType);
    }

    /** {@inheritDoc} */
    @Override
    public String[] getTrackedFields() {
        return new String[]{VALUE};
    }

    /** {@inheritDoc} */
    @Override
    public Context applySpecificFilter(Context context) throws IOException {
        Context filter = constructFilter(context);
        return ((ReadBasedMetric)basedMetric).applySpecificFilter(filter);
    }

    /** {@inheritDoc} */
    @Override
    public DBObject[] getSpecificDBOperations(Context clauses) {
        if (basedMetric instanceof TasksGigabyteRamHours) {
            String field = ((ReadBasedMetric)basedMetric).getTrackedFields()[0];

            DBObject group = new BasicDBObject();

            group.put(ID, null);
            group.put(VALUE, new BasicDBObject("$sum", "$" + field));

            return new DBObject[]{new BasicDBObject("$group", group)};

        } else {
            DBObject group = new BasicDBObject();
            group.put(ID, "$" + USER);

            DBObject count = new BasicDBObject();
            count.put(ID, null);
            count.put(VALUE, new BasicDBObject("$sum", 1));

            return new DBObject[]{new BasicDBObject("$group", group),
                                  new BasicDBObject("$group", count)};
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getStorageCollectionName() {
        return ((ReadBasedMetric)basedMetric).getStorageCollectionName();
    }

    /** {@inheritDoc} */
    @Override
    public DBObject[] getSpecificExpandedDBOperations(Context clauses) {
        DBObject group = new BasicDBObject();
        group.put(ID, "$" + getExpandedField());

        DBObject projection = new BasicDBObject(getExpandedField(), "$_id");

        return new DBObject[]{new BasicDBObject("$group", group),
                              new BasicDBObject("$project", projection)};
    }

    /** {@inheritDoc} */
    @Override
    public String getExpandedField() {
        return USER;
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return basedMetric.getValueDataClass();
    }

    protected Context constructFilter(Context context) throws IOException {
        Context.Builder builder = new Context.Builder(context);

        String userFilter = getNewUsers(context);
        setNewUsersAsFilter(builder, userFilter);

        setSpecificFilter(builder);

        return builder.build();
    }

    protected String getNewUsers(Context context) throws IOException {
        SetValueData svd = getAsSet(getCreatedUsersMetric(), context);
        Object[] newUsers = svd.getAll().toArray();
        return getFilterAsString(new HashSet<>(Arrays.asList(newUsers)));
    }

    protected void setNewUsersAsFilter(Context.Builder builder, String userFilter) {
        builder.put(MetricFilter.USER, userFilter);
    }

    protected Metric getCreatedUsersMetric() {
        return MetricFactory.getMetric(MetricType.CREATED_USERS_SET);
    }

    abstract protected void setSpecificFilter(Context.Builder builder);
}


