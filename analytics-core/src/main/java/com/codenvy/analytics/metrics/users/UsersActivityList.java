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

import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.metrics.AbstractListValueResulted;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.sessions.ProductUsageSessionsList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class UsersActivityList extends AbstractListValueResulted {

    public static final String MESSAGE = "message";
    public static final String EVENT   = "event";
    public static final String USER    = "user";
    public static final String WS      = "ws";

    private static final String TIME = ProductUsageSessionsList.TIME;

    public UsersActivityList() {
        super(MetricType.USERS_ACTIVITY_LIST);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{DATE, MESSAGE, EVENT, WS, USER};
    }

    @Override
    public String getDescription() {
        return "Users' actions";
    }

    @Override
    public DBObject getFilter(Map<String, String> clauses) throws ParseException, IOException {
        DBObject initialFilter = super.getFilter(clauses);
        DBObject match = (DBObject)initialFilter.get("$match");

        replaceFilterSessionIdWithUserAndDate(clauses, match);

        return initialFilter;
    }

    /**
     * Update "user", "from_date", "to_date", "ws", "event" fields in match object due to info of session with
     * session_id from clauses. Exclude "session-started" and "session-finished" events.
     */
    protected static void replaceFilterSessionIdWithUserAndDate(Map<String, String> clauses,
                                                                DBObject match) throws IOException {

        if (MetricFilter.SESSION_ID.exists(clauses)) {
            MapValueData sessionData = getSessionData(MetricFilter.SESSION_ID.get(clauses));

            if (sessionData == null) {
                match.put(USER, null);
                match.put(DATE, null);

            } else {
                // don't replace filter on logged user
                if (!match.containsField(USER)) {
                    match.put(USER, getSessionUser(sessionData));
                }

                match.put(DATE, getSessionStartAndFinishDateRange(sessionData));
                match.put(WS, getSessionWorkspace(sessionData));
                match.put(EVENT, getAllEventsExceptSessions());
            }

            match.removeField(ProductUsageSessionsList.SESSION_ID);
        }
    }

    private static BasicDBObject getAllEventsExceptSessions() {
        return new BasicDBObject("$nin", new String[]{"session-started", "session-finished"});
    }

    private static String getSessionWorkspace(MapValueData sessionData) {
        return sessionData.getAll().get(ProductUsageSessionsList.WS).toString();
    }

    private static String getSessionUser(MapValueData sessionData) {
        return sessionData.getAll().get(ProductUsageSessionsList.USER).toString();
    }

    private static DBObject getSessionStartAndFinishDateRange(MapValueData sessionData) {
        long fromDateInMillis = Long.parseLong(sessionData.getAll().get(DATE).getAsString());
        long toDateInMillis = fromDateInMillis + Long.parseLong(sessionData.getAll().get(TIME).getAsString());

        DBObject range = new BasicDBObject();
        range.put("$gte", fromDateInMillis);
        range.put("$lt", toDateInMillis);

        return range;
    }

    private static MapValueData getSessionData(String sessionId) throws IOException {
        Map<String, String> context = new HashMap<>();
        context.put(MetricFilter.SESSION_ID.toString(), sessionId);

        ListValueData value =
                (ListValueData)MetricFactory.getMetric(MetricType.PRODUCT_USAGE_SESSIONS_LIST).getValue(context);

        if (value.size() == 0) {
            return null;
        }

        return (MapValueData)value.getAll().get(0);
    }
}
