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

import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.datamodel.ValueDataUtil;
import com.codenvy.analytics.metrics.AbstractListValueResulted;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.CumulativeMetric;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.ReadBasedSummariziable;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import javax.annotation.security.RolesAllowed;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
@RolesAllowed({"system/admin", "system/manager"})
public class UsersStatisticsList extends AbstractListValueResulted implements ReadBasedSummariziable {

    public UsersStatisticsList() {
        super(MetricType.USERS_STATISTICS_LIST);
    }

    @Override
    public String getDescription() {
        return "Users' statistics data";
    }

    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.USERS_STATISTICS);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{USER,
                            PROJECTS,
                            SESSIONS,
                            TIME,
                            BUILDS,
                            BUILD_TIME,
                            RUNS,
                            RUN_TIME,
                            DEBUGS,
                            DEBUG_TIME,
                            DEPLOYS,
                            FACTORIES,
                            INVITES,
                            LOGINS};
    }

    @Override
    public DBObject[] getSpecificDBOperations(Context clauses) {
        DBObject group = new BasicDBObject();
        group.put(ID, "$" + USER);
        group.put(PROJECTS, new BasicDBObject("$sum", "$" + PROJECTS));
        group.put(RUNS, new BasicDBObject("$sum", "$" + RUNS));
        group.put(DEBUGS, new BasicDBObject("$sum", "$" + DEBUGS));
        group.put(DEBUG_TIME, new BasicDBObject("$sum", "$" + DEBUG_TIME));
        group.put(DEPLOYS, new BasicDBObject("$sum", "$" + DEPLOYS));
        group.put(BUILDS, new BasicDBObject("$sum", "$" + BUILDS));
        group.put(FACTORIES, new BasicDBObject("$sum", "$" + FACTORIES));
        group.put(TIME, new BasicDBObject("$sum", "$" + TIME));
        group.put(SESSIONS, new BasicDBObject("$sum", "$" + SESSIONS));
        group.put(INVITES, new BasicDBObject("$sum", "$" + INVITES));
        group.put(LOGINS, new BasicDBObject("$sum", "$" + LOGINS));
        group.put(RUN_TIME, new BasicDBObject("$sum", "$" + RUN_TIME));
        group.put(BUILD_TIME, new BasicDBObject("$sum", "$" + BUILD_TIME));

        DBObject project = new BasicDBObject();
        project.put(USER, "$" + ID);
        project.put(PROJECTS, "$" + PROJECTS);
        project.put(RUNS, "$" + RUNS);
        project.put(RUN_TIME, "$" + RUN_TIME);
        project.put(DEBUGS, "$" + DEBUGS);
        project.put(DEBUG_TIME, "$" + DEBUG_TIME);
        project.put(DEPLOYS, "$" + DEPLOYS);
        project.put(BUILDS, "$" + BUILDS);
        project.put(BUILD_TIME, "$" + BUILD_TIME);
        project.put(FACTORIES, "$" + FACTORIES);
        project.put(TIME, "$" + TIME);
        project.put(SESSIONS, "$" + SESSIONS);
        project.put(INVITES, "$" + INVITES);
        project.put(LOGINS, "$" + LOGINS);

        return new DBObject[]{new BasicDBObject("$group", group),
                              new BasicDBObject("$project", project)};
    }

    @Override
    public DBObject[] getSpecificSummarizedDBOperations(Context clauses) {
        DBObject[] dbOperations = getSpecificDBOperations(clauses);
        ((DBObject)(dbOperations[0].get("$group"))).put(ID, null);
        ((DBObject)(dbOperations[1].get("$project"))).removeField(USER);
        ((DBObject)(dbOperations[1].get("$project"))).removeField(INVITES);
        ((DBObject)(dbOperations[1].get("$project"))).removeField(LOGINS);

        return dbOperations;
    }

    /** To add user profile data. */
    @Override
    public ValueData postComputation(ValueData valueData, Context clauses) throws IOException {
        List<ValueData> value = new ArrayList<>();
        ListValueData listValueData = (ListValueData)valueData;

        for (ValueData items : listValueData.getAll()) {
            MapValueData prevItems = (MapValueData)items;
            Map<String, ValueData> newItems = new HashMap<>(prevItems.getAll());

            // add user profile data
            Map<String, ValueData> profile = getUserProfile(newItems.get(USER).getAsString());
            putNotNull(newItems, profile, USER_FIRST_NAME);
            putNotNull(newItems, profile, USER_LAST_NAME);
            putNotNull(newItems, profile, USER_COMPANY);
            putNotNull(newItems, profile, ALIASES);

            value.add(new MapValueData(newItems));
        }

        return new ListValueData(value);
    }

    private void putNotNull(Map<String, ValueData> newItems, Map<String, ValueData> profile, String key) {
        newItems.put(key, profile.containsKey(key) ? profile.get(key) : StringValueData.DEFAULT);
    }

    /**
     * Get user profile by using USERS_PROFILES_LIST metric.
     * Returns null if USERS_PROFILES_LIST metric returns empty list on certain user.
     */
    private Map<String, ValueData> getUserProfile(String user) throws IOException {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.USER, user);

        Metric metric = MetricFactory.getMetric(MetricType.USERS_PROFILES_LIST);
        List<ValueData> users = ValueDataUtil.getAsList(metric, builder.build()).getAll();

        if (users.size() == 1) {
            MapValueData userProfile = (MapValueData)users.get(0);
            return userProfile.getAll();
        } else {
            return Collections.emptyMap();
        }
    }

    public Context applySpecificFilter(Context context) throws IOException {
        // Return only registered users for expanded cumulative metrics
        if (context.exists(Parameters.EXPANDED_METRIC_NAME)) {
            Metric expandable = context.getExpandedMetric();

            if (expandable != null && (expandable instanceof CumulativeMetric)) {
                Context.Builder builder = new Context.Builder(context);
                builder.put(MetricFilter.REGISTERED_USER, 1);
                context = builder.build();
            }
        }

        return context;
    }
}
