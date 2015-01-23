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
package com.codenvy.analytics.metrics.workspaces;

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
public class WorkspacesStatisticsList extends AbstractListValueResulted implements ReadBasedSummariziable {

    public static final String JOINED_USERS = "joined_users";

    public WorkspacesStatisticsList() {
        super(MetricType.WORKSPACES_STATISTICS_LIST);
    }

    @Override
    public String getDescription() {
        return "Workspaces' statistics data";
    }

    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.USERS_STATISTICS);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{WS,
                            PROJECTS,
                            SESSIONS,
                            TIME,
                            BUILDS,
                            BUILD_TIME,
                            BUILD_WAITING_TIME,
                            RUNS,
                            RUN_TIME,
                            RUN_WAITING_TIME,
                            DEBUGS,
                            DEBUG_TIME,
                            DEPLOYS,
                            FACTORIES,
                            INVITES,
                            JOINED_USERS};
    }

    @Override
    public DBObject[] getSpecificDBOperations(Context clauses) {
        DBObject group = new BasicDBObject();
        group.put(ID, "$" + WS);
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
        group.put(RUN_TIME, new BasicDBObject("$sum", "$" + RUN_TIME));
        group.put(BUILD_TIME, new BasicDBObject("$sum", "$" + BUILD_TIME));
        group.put(RUN_WAITING_TIME, new BasicDBObject("$sum", "$" + RUN_WAITING_TIME));
        group.put(BUILD_WAITING_TIME, new BasicDBObject("$sum", "$" + BUILD_WAITING_TIME));
        group.put(JOINED_USERS, new BasicDBObject("$sum", "$" + JOINED_USERS));

        DBObject project = new BasicDBObject();
        project.put(WS, "$" + ID);
        project.put(PROJECTS, "$" + PROJECTS);
        project.put(RUNS, "$" + RUNS);
        project.put(DEBUGS, "$" + DEBUGS);
        project.put(DEBUG_TIME, "$" + DEBUG_TIME);
        project.put(DEPLOYS, "$" + DEPLOYS);
        project.put(BUILDS, "$" + BUILDS);
        project.put(FACTORIES, "$" + FACTORIES);
        project.put(TIME, "$" + TIME);
        project.put(SESSIONS, "$" + SESSIONS);
        project.put(INVITES, "$" + INVITES);
        project.put(RUN_TIME, "$" + RUN_TIME);
        project.put(BUILD_TIME, "$" + BUILD_TIME);
        project.put(RUN_WAITING_TIME, "$" + RUN_WAITING_TIME);
        project.put(BUILD_WAITING_TIME, "$" + BUILD_WAITING_TIME);
        project.put(JOINED_USERS, "$" + JOINED_USERS);

        return new DBObject[]{new BasicDBObject("$group", group),
                              new BasicDBObject("$project", project)};
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
            Map<String, ValueData> profile = getWorkspaceProfile(newItems.get(WS).getAsString());
            putNotNull(newItems, profile, WS_NAME);

            value.add(new MapValueData(newItems));
        }

        return new ListValueData(value);
    }

    private void putNotNull(Map<String, ValueData> newItems, Map<String, ValueData> profile, String key) {
        newItems.put(key, profile.containsKey(key) ? profile.get(key) : StringValueData.DEFAULT);
    }

    private Map<String, ValueData> getWorkspaceProfile(String wsName) throws IOException {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.WS, wsName);

        Metric metric = MetricFactory.getMetric(MetricType.WORKSPACES_PROFILES_LIST);
        List<ValueData> workspaces = ValueDataUtil.getAsList(metric, builder.build()).getAll();

        if (workspaces.size() == 1) {
            MapValueData profile = (MapValueData)workspaces.get(0);
            return profile.getAll();
        } else {
            return Collections.emptyMap();
        }
    }

    @Override
    public DBObject[] getSpecificSummarizedDBOperations(Context clauses) {
        DBObject[] dbOperations = getSpecificDBOperations(clauses);
        ((DBObject)(dbOperations[0].get("$group"))).put(ID, null);

        DBObject project = (DBObject)dbOperations[1].get("$project");
        project.removeField(WS);
        project.removeField(JOINED_USERS);
        project.removeField(INVITES);
        project.removeField(FACTORIES);

        return dbOperations;
    }

    public Context applySpecificFilter(Context context) throws IOException {
        // Return only persistence workspaces for expanded cumulative metrics
        if (context.exists(Parameters.EXPANDED_METRIC_NAME)) {
            Metric expandable = context.getExpandedMetric();

            if (expandable != null && (expandable instanceof CumulativeMetric)) {
                Context.Builder builder = new Context.Builder(context);
                builder.put(MetricFilter.PERSISTENT_WS, 1);
                context = builder.build();
            }
        }

        return context;
    }
}

