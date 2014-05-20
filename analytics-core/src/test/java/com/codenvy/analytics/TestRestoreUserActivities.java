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
package com.codenvy.analytics;

import com.codenvy.analytics.metrics.AbstractMetric;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.users.ActiveUsersSet;
import com.codenvy.analytics.metrics.workspaces.ActiveWorkspacesSet;
import com.codenvy.analytics.persistent.MongoDataLoader;
import com.mongodb.*;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Modify analytics.properties:
 * analytics.mongodb.embedded=false
 * analytics.mongodb.url=mongodb://localhost:27017/analytics_data
 *
 * @author Anatoliy Bazko
 */
public class TestRestoreUserActivities extends BaseTest {
    private static final String LDAP_DUMP = "/home/tolusha/ldap.diff";

    private static final String TO_DATE_BEFORE = "20140402";

    private static final String FROM_DATE = "20140403";
    private static final String TO_DATE   = "20140403"; //24


    @BeforeClass
    @Override
    public void clearDatabase() {
    }

    @Test
    public void restore() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, FROM_DATE);
        builder.put(Parameters.TO_DATE, TO_DATE);
        Context oneDayContext = builder.build();

        builder = new Context.Builder();
        builder.putDefaultValue(Parameters.FROM_DATE);
        builder.put(Parameters.TO_DATE, TO_DATE_BEFORE);
        Context daysBeforeContext = builder.build();

        doRestore(oneDayContext, daysBeforeContext);
    }

    private void doRestore(Context oneDayContext, Context daysBeforeContext) throws IOException, ParseException {
        Set<String> activeUsersForDay = new HashSet<>();
        Set<String> activeWsForDay = new HashSet<>();
        getActiveEntities(oneDayContext, activeUsersForDay, activeWsForDay);

        Set<String> activeUsersBefore = new HashSet<>();
        Set<String> activeWsBefore = new HashSet<>();
        getActiveEntities(daysBeforeContext, activeUsersBefore, activeWsBefore);

        Set<String> diffUsers = new HashSet<>(activeUsersForDay);
        diffUsers.removeAll(activeUsersBefore);

        Set<String> diffWs = new HashSet<>(activeWsForDay);
        diffWs.removeAll(activeWsBefore);

        Map<String, String> ids = readUsersIds();
        Set<Entry> activePairs = getActivePairs(oneDayContext);

        createLog(oneDayContext, diffUsers, diffWs, activePairs, ids);
    }

    private void getActiveEntities(Context context,
                                   Set<String> activeUsers,
                                   Set<String> activeWs) throws ParseException {
        DBObject dateFilter = getDateFilter(context);
        DBCollection collection = mongoDb.getCollection("product_usage_sessions_list");
        DBCursor cursor = collection.find(dateFilter);
        while (cursor.hasNext()) {
            DBObject next = cursor.next();
            if (next.containsField(AbstractMetric.USER)) {
                activeUsers.add((String)next.get(AbstractMetric.USER));
            }

            if (next.containsField(AbstractMetric.WS)) {
                activeWs.add((String)next.get(AbstractMetric.WS));
            }
        }
    }

    private void createLog(Context oneDayContext,
                           Set<String> newUsers,
                           Set<String> newWs,
                           Set<Entry> activePairs,
                           Map<String, String> ids) throws IOException, ParseException {

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        File file = new File(BASE_DIR, oneDayContext.getAsString(Parameters.FROM_DATE) + File.separator + "restored_messages");
        file.getParentFile().mkdirs();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String newUser : newUsers) {
                String email = newUser;
                String id = ids.get(email);

                if (id == null) {
                    if (email.toLowerCase().startsWith("anonymoususer")) {
                        id = "unknown_id";
                    } else {
                        LOG.warn("There is no id for " + email);
                        continue;
                    }
                }

                writer.write("127.0.0.1 " + df.format(oneDayContext.getAsDate(Parameters.TO_DATE).getTime()) +
                             " 00:00:01,000[l-4-thread-8211]  [INFO ] [Main 224]  [][][] - ");
                writer.write("EVENT#user-created# " +
                             "ALIASES#" + email + "# " +
                             "USER-ID#" + id + "#");
                writer.newLine();
            }

            for (String newW : newWs) {
                Set<String> usersForWs = getUsersForWs(newW, activePairs);
                if (usersForWs.size() == 1) {
                    writer.write("127.0.0.1 " + df.format(oneDayContext.getAsDate(Parameters.TO_DATE).getTime()) +
                                 " 00:00:01,000[l-4-thread-8211]  [INFO ] [Main 224]  [][][] - ");
                    writer.write("EVENT#workspace-created# " +
                                 "WS#" + newW + "# " +
                                 "USER#" + usersForWs.iterator().next() + "#");
                    writer.newLine();
                }

                for (String usersForW : usersForWs) {
                    writer.write("127.0.0.1 " + df.format(oneDayContext.getAsDate(Parameters.TO_DATE).getTime()) +
                                 " 00:00:01,000[l-4-thread-8211]  [INFO ] [Main 224]  [][][] - ");
                    writer.write("EVENT#user-added-to-ws# " +
                                 "WS#" + newW + "# " +
                                 "USER#" + usersForW + "# " +
                                 "FROM#website#");
                    writer.newLine();
                }
            }
        }
    }

    private Set<String> getUsersForWs(String ws, Set<Entry> activePairs) {
        Set<String> users = new HashSet<>();
        for (Entry activePair : activePairs) {
            if (activePair.ws.equals(ws)) {
                users.add(activePair.user);
            }
        }

        return users;
    }

    private Set<Entry> getActivePairs(Context oneDayContext) throws ParseException {
        Set<Entry> result = new HashSet<>();

        DBCollection collection = mongoDb.getCollection("users_activity_list");
        DBObject filter = getDateFilter(oneDayContext);
        BasicDBObject[] dbOps = getDbOps();

        AggregationOutput aggregate = collection.aggregate(new BasicDBObject("$match", filter), dbOps);
        Iterator<DBObject> iterator = aggregate.results().iterator();
        while (iterator.hasNext()) {
            DBObject next = iterator.next();

            Entry entry = new Entry();
            entry.user = (String)next.get("user");
            entry.ws = (String)next.get("ws");

            if (!entry.user.equals("default") && !entry.ws.equals("default")) {
                result.add(entry);
            }
        }

        return result;
    }

    private BasicDBObject[] getDbOps() {
        Map<Object, Object> groupBy = new HashMap<>();
        groupBy.put(AbstractMetric.WS, "$" + AbstractMetric.WS);
        groupBy.put(AbstractMetric.USER, "$" + AbstractMetric.USER);
        DBObject group = new BasicDBObject(AbstractMetric.ID, groupBy);

        DBObject project = new BasicDBObject();
        project.put(AbstractMetric.USER, "$" + AbstractMetric.ID + "." + AbstractMetric.USER);
        project.put(AbstractMetric.WS, "$" + AbstractMetric.ID + "." + AbstractMetric.WS);

        return new BasicDBObject[]{new BasicDBObject("$group", group), new BasicDBObject("$project", project)};
    }

    private DBObject getDateFilter(Context clauses) throws ParseException {
        DBObject dateFilter = new BasicDBObject();

        String fromDate = clauses.getAsString(Parameters.FROM_DATE);
        if (fromDate != null) {
            if (Utils.isDateFormat(fromDate)) {
                dateFilter.put("$gte", clauses.getAsDate(Parameters.FROM_DATE).getTimeInMillis());
            } else {
                dateFilter.put("$gte", clauses.getAsLong(Parameters.FROM_DATE));
            }
        }

        String toDate = clauses.getAsString(Parameters.TO_DATE);
        if (toDate != null) {
            if (Utils.isDateFormat(toDate)) {
                dateFilter.put("$lt", clauses.getAsDate(Parameters.TO_DATE).getTimeInMillis() + MongoDataLoader.DAY_IN_MILLISECONDS);
            } else {
                dateFilter.put("$lte", clauses.getAsLong(Parameters.TO_DATE));
            }
        }

        return new BasicDBObject(AbstractMetric.DATE, dateFilter);
    }

    private Map<String, String> readUsersIds() throws IOException {
        Map<String, String> result = new HashMap<>();

        Email email = new Email();
        try (BufferedReader reader = new BufferedReader(new FileReader(LDAP_DUMP))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("dn: ")) {
                    result.put(email.email, email.userId);
                    email = new Email();
                } else if (line.startsWith("mail: ")) {
                    email.email = line.substring(6);
                } else if (line.startsWith("uid: ")) {
                    email.userId = line.substring(5);
                }
            }
        }

        return result;
    }

    private static class Entry {
        String user;
        String ws;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Entry entry = (Entry)o;

            if (user != null ? !user.equals(entry.user) : entry.user != null) return false;
            if (ws != null ? !ws.equals(entry.ws) : entry.ws != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = user != null ? user.hashCode() : 0;
            result = 31 * result + (ws != null ? ws.hashCode() : 0);
            return result;
        }
    }


    private static class Email {
        String email  = "";
        String userId = "";
    }

    private static class TestedActiveUsersSet extends ActiveUsersSet {
        private final MetricType metricType;

        private TestedActiveUsersSet(MetricType metricType) {
            this.metricType = metricType;
        }

        @Override
        public String getStorageCollectionName() {
            return getStorageCollectionName(metricType);
        }

        @Override
        public Context applySpecificFilter(Context clauses) {
            return clauses;
        }
    }


    private static class TestedActiveWsSet extends ActiveWorkspacesSet {
        private final MetricType metricType;

        private TestedActiveWsSet(MetricType metricType) {
            this.metricType = metricType;
        }

        @Override
        public Context applySpecificFilter(Context clauses) {
            return clauses;
        }

        @Override
        public String getStorageCollectionName() {
            return getStorageCollectionName(metricType);
        }
    }
}
