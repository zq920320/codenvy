/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
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

import static com.mongodb.util.MyAsserts.assertEquals;

import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.AbstractListValueResulted;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import javax.annotation.security.RolesAllowed;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
@RolesAllowed({"system/admin", "system/manager"})
public class UsersStatisticsList extends AbstractListValueResulted {

    public static final String PROJECTS     = "projects";
    public static final String BUILDS       = "builds";
    public static final String DEPLOYS      = "deploys";
    public static final String RUNS         = "runs";
    public static final String DEBUGS       = "debugs";
    public static final String FACTORIES    = "factories";
    public static final String INVITES      = "invites";
    public static final String LOGINS       = "logins";
    public static final String RUN_TIME     = "run_time";
    public static final String BUILD_TIME   = "build_time";
    public static final String PAAS_DEPLOYS = "paas_deploys";
    public static final String JOINED_USERS = "joined-users";

    public UsersStatisticsList() {
        super(MetricType.USERS_STATISTICS_LIST);
    }

    @Override
    public String getDescription() {
        return "Users' statistics data";
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{USER,
                            PROJECTS,
                            RUNS,
                            DEBUGS,
                            BUILDS,
                            DEPLOYS,
                            FACTORIES,
                            TIME,
                            SESSIONS,
                            INVITES,
                            LOGINS,
                            RUN_TIME,
                            BUILD_TIME,
                            PAAS_DEPLOYS,
                            USER_FIRST_NAME,
                            USER_LAST_NAME,
                            USER_COMPANY,
                            USER_JOB,
                            USER_PHONE};
    }

    @Override
    public DBObject[] getSpecificDBOperations(Map<String, String> clauses) {       
        DBObject group = new BasicDBObject();
        group.put(ID, "$" + USER);
        group.put(PROJECTS, new BasicDBObject("$sum", "$" + PROJECTS));
        group.put(RUNS, new BasicDBObject("$sum", "$" + RUNS));
        group.put(DEBUGS, new BasicDBObject("$sum", "$" + DEBUGS));
        group.put(DEPLOYS, new BasicDBObject("$sum", "$" + DEPLOYS));
        group.put(BUILDS, new BasicDBObject("$sum", "$" + BUILDS));
        group.put(FACTORIES, new BasicDBObject("$sum", "$" + FACTORIES));
        group.put(TIME, new BasicDBObject("$sum", "$" + TIME));
        group.put(SESSIONS, new BasicDBObject("$sum", "$" + SESSIONS));
        group.put(INVITES, new BasicDBObject("$sum", "$" + INVITES));
        group.put(LOGINS, new BasicDBObject("$sum", "$" + LOGINS));
        group.put(RUN_TIME, new BasicDBObject("$sum", "$" + RUN_TIME));
        group.put(BUILD_TIME, new BasicDBObject("$sum", "$" + BUILD_TIME));
        group.put(PAAS_DEPLOYS, new BasicDBObject("$sum", "$" + PAAS_DEPLOYS));

        DBObject project = new BasicDBObject();
        project.put(USER, "$" + ID);
        project.put(PROJECTS, "$" + PROJECTS);
        project.put(RUNS, "$" + RUNS);
        project.put(DEBUGS, "$" + DEBUGS);
        project.put(DEPLOYS, "$" + DEPLOYS);
        project.put(BUILDS, "$" + BUILDS);
        project.put(FACTORIES, "$" + FACTORIES);
        project.put(TIME, "$" + TIME);
        project.put(SESSIONS, "$" + SESSIONS);
        project.put(INVITES, "$" + INVITES);
        project.put(LOGINS, "$" + LOGINS);
        project.put(RUN_TIME, "$" + RUN_TIME);
        project.put(BUILD_TIME, "$" + BUILD_TIME);
        project.put(PAAS_DEPLOYS, "$" + PAAS_DEPLOYS);

        return new DBObject[]{new BasicDBObject("$group", group),
                              new BasicDBObject("$project", project)};
    }
    
    /**
     * To add user profile data.
     */
    @Override
    protected ValueData postEvaluation(ValueData valueData, Map<String, String> clauses) throws IOException {
        List<ValueData> value = new ArrayList<>();
        ListValueData listValueData = (ListValueData)valueData;

        for (ValueData items : listValueData.getAll()) {
            MapValueData prevItems = (MapValueData)items;
            Map<String, ValueData> newItems = new HashMap<>(prevItems.getAll());

            // add user profile data
            Map<String, ValueData> profile = getUserProfile(newItems.get(USER).getAsString(), clauses);
            if (profile != null) {
                newItems.put(USER_FIRST_NAME, profile.get(USER_FIRST_NAME));
                newItems.put(USER_LAST_NAME, profile.get(USER_LAST_NAME));
                newItems.put(USER_COMPANY, profile.get(USER_COMPANY));
                newItems.put(USER_JOB, profile.get(USER_JOB));
            } else {
                newItems.put(USER_FIRST_NAME, StringValueData.DEFAULT);
                newItems.put(USER_LAST_NAME, StringValueData.DEFAULT);
                newItems.put(USER_COMPANY, StringValueData.DEFAULT);
                newItems.put(USER_JOB, StringValueData.DEFAULT);
            }

            value.add(new MapValueData(newItems));
        }

        return new ListValueData(value);       
    }

    /**
     * Get user profile by using USERS_PROFILES_LIST metric.
     * Returns null if USERS_PROFILES_LIST metric returns empty list on certain user.
     */
    private Map<String, ValueData> getUserProfile(String user, Map<String, String> clauses) throws IOException {
        Map<String, String> context = Utils.clone(clauses);
        Parameters.USER.put(context, user);
        
        Metric metric = MetricFactory.getMetric(MetricType.USERS_PROFILES_LIST);
        List<ValueData> users = ((ListValueData) metric.getValue(context)).getAll();

        if (users.size() > 0) { 
            MapValueData userProfile = (MapValueData)users.get(0);
            return userProfile.getAll();
        } else {
            return null;
        }
    }
    
    
}
