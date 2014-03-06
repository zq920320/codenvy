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
package com.codenvy.analytics.metrics.workspaces;

import static com.codenvy.analytics.metrics.users.UsersStatisticsList.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;

import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.AbstractListValueResulted;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.users.UsersStatisticsList;
import com.codenvy.organization.client.WorkspaceManager;
import com.codenvy.organization.exception.OrganizationServiceException;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
@RolesAllowed({"system/admin", "system/manager"})
public class WorkspacesStatisticsList extends AbstractListValueResulted {
    
    public static final String USERS = "users";
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
        return getStorageCollectionName(MetricType.USERS_STATISTICS_LIST);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{WS,
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
                            JOINED_USERS};
    }

    @Override
    public DBObject getFilter(Map<String, String> clauses) throws ParseException, IOException {
        DBObject filter = super.getFilter(clauses);

        DBObject match = (DBObject)filter.get("$match");
        if (match.get(WS) == null) {
            match.put(WS, PERSISTENT_WS);
        }

        return filter;
    }

    @Override
    public DBObject[] getSpecificDBOperations(Map<String, String> clauses) {
        DBObject group = new BasicDBObject();
        group.put(ID, "$" + WS);
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
        group.put(JOINED_USERS, new BasicDBObject("$sum", "$" + JOINED_USERS));

        DBObject project = new BasicDBObject();
        project.put(WS, "$" + ID);
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
        project.put(JOINED_USERS, "$" + JOINED_USERS);

        return new DBObject[]{new BasicDBObject("$group", group),
                              new BasicDBObject("$project", project)};
    }

//    @Override
//    protected ValueData postEvaluation(ValueData valueData, Map<String, String> clauses) throws IOException {
//        String wsName = MetricFilter.WS.get(clauses);
//        
//        if (wsName != null) {
//            int wsMemberCount = getWsUserCount(wsName);
//            
//            List<ValueData> value = new ArrayList<>();
//            ListValueData listValueData = (ListValueData)valueData;
//            
//            for (ValueData items : listValueData.getAll()) {
//                MapValueData prevItems = (MapValueData)items;
//                Map<String, ValueData> newItems = new HashMap<>(prevItems.getAll());
//
//                // add workspace user number
//                newItems.put(USERS, LongValueData.valueOf(wsMemberCount));
//
//                value.add(new MapValueData(newItems));
//            }
//
//            return new ListValueData(value); 
//            
//        } else {
//            return valueData;
//        }
//    }
//
//    
//    private int getWsUserCount(String wsName) throws IOException {
//        try {
//            WorkspaceManager workspaceManager = organizationClient.getWorkspaceManager();
//            return workspaceManager.getWorkspaceMembers(wsName).size();
//        } catch (OrganizationServiceException e) {
//            throw new IOException(e);
//        }
//    }
}
