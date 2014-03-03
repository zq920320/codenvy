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

import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.*;
import com.mongodb.DBObject;

import javax.annotation.security.RolesAllowed;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
@RolesAllowed({"system/admin", "system/manager"})
public class UsersActivityList extends AbstractListValueResulted {

    public static final String TIME_FROM_BEGINNING = "time_from_beginning";

    public UsersActivityList() {
        super(MetricType.USERS_ACTIVITY_LIST);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{DATE,
                            MESSAGE,
                            EVENT,
                            WS,
                            USER};
    }

    @Override
    public String getDescription() {
        return "Users' actions";
    }

    /**
     * Calculate <time from beginning of session_with_SESSION_ID>=(activityDate-startSessionTime), or 0,
     * if there is SESSION_ID value in the clauses.
     * Then add it value into separate column TIME_FROM_BEGINNING
     */
    @Override
    protected ValueData postEvaluation(ValueData valueData, Map<String, String> clauses) throws IOException {
        long startSessionTime = 0;

        ListValueData listValueData = (ListValueData)valueData;
        List<ValueData> list2Return = new ArrayList<>(listValueData.size());

        // get start session time
        if (MetricFilter.SESSION_ID.exists(clauses)) {
            MapValueData sessionData = getSessionData(MetricFilter.SESSION_ID.get(clauses));
            startSessionTime = Long.parseLong(sessionData.getAll().get(DATE).getAsString());
        }

        Iterator<ValueData> iterator = listValueData.getAll().iterator();
        while (iterator.hasNext()) {
            Map<String, ValueData> items = ((MapValueData)iterator.next()).getAll();

            Map<String, ValueData> items2Return = new HashMap<>();
            items2Return.putAll(items);

            if (startSessionTime != 0) {
                long activityDate = ((LongValueData)items2Return.get(DATE)).getAsLong();
                long delta = activityDate - startSessionTime;

                items2Return.put(TIME_FROM_BEGINNING, LongValueData.valueOf(delta));
            } else {
                items2Return.put(TIME_FROM_BEGINNING, LongValueData.DEFAULT);
            }

            list2Return.add(new MapValueData(items2Return));
        }

        return new ListValueData(list2Return);
    }


    @Override
    public DBObject getFilter(Map<String, String> clauses) throws ParseException, IOException {
        clauses = Utils.clone(clauses);

        excludeFactorySessions(clauses);

        if (MetricFilter.SESSION_ID.exists(clauses)) {
            replaceSessionIdWithUserAndDate(clauses);
        }

        return super.getFilter(clauses);
    }

    private void excludeFactorySessions(Map<String, String> clauses) {
        String eventFilter = MetricFilter.EVENT.get(clauses);

        if (eventFilter != null) {
            MetricFilter.EVENT.put(clauses, eventFilter + ",~session-factory-stopped,~session-factory-started");
        } else {
            MetricFilter.EVENT.put(clauses, "~session-factory-stopped,~session-factory-started");
        }
    }

    /**
     * Update "user", "from_date", "to_date", "ws", "event" fields in match object due to info of session with
     * session_id from clauses. Exclude "session-started" and "session-finished" events.
     */
    private void replaceSessionIdWithUserAndDate(Map<String, String> clauses) throws IOException {
        MapValueData sessionData = getSessionData(MetricFilter.SESSION_ID.get(clauses));

        if (sessionData != null) {
            // don't replace filter on logged user
            if (!MetricFilter.USER.exists(clauses)) {
                MetricFilter.USER.put(clauses, getSessionUser(sessionData));
            }

            MetricFilter.WS.put(clauses, getSessionWorkspace(sessionData));
            Parameters.FROM_DATE.put(clauses, getSessionFromDate(sessionData));
            Parameters.TO_DATE.put(clauses, getSessionToDate(sessionData));
        }

        MetricFilter.SESSION_ID.remove(clauses);
    }

    private String getSessionToDate(MapValueData sessionData) {
        String sessionStartDate = getSessionFromDate(sessionData);
        String sessionTime = getSessionTime(sessionData);

        return "" + (Long.parseLong(sessionStartDate) + Long.parseLong(sessionTime));
    }

    private String getSessionWorkspace(MapValueData sessionData) {
        return getSessionParameter(sessionData, WS);
    }

    private String getSessionUser(MapValueData sessionData) {
        return getSessionParameter(sessionData, USER);
    }

    private String getSessionFromDate(MapValueData sessionData) {
        return getSessionParameter(sessionData, DATE);
    }

    private String getSessionTime(MapValueData sessionData) {
        return getSessionParameter(sessionData, TIME);
    }

    private String getSessionParameter(MapValueData sessionData, String param) {
        return sessionData.getAll().get(param).getAsString();
    }

    private MapValueData getSessionData(String sessionId) throws IOException {
        Map<String, String> context = Utils.newContext();
        MetricFilter.SESSION_ID.put(context, sessionId);

        Metric metric = MetricFactory.getMetric(MetricType.PRODUCT_USAGE_SESSIONS_LIST);
        ListValueData valueData = (ListValueData)metric.getValue(context);

        return valueData.size() == 0 ? null : (MapValueData)valueData.getAll().get(0);
    }
}
