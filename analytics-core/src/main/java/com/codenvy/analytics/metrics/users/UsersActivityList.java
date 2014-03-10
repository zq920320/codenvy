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
import com.codenvy.analytics.datamodel.*;
import com.codenvy.analytics.metrics.*;
import com.codenvy.analytics.pig.scripts.EventsHolder;
import com.mongodb.DBObject;

import javax.annotation.security.RolesAllowed;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
@RolesAllowed({"system/admin", "system/manager"})
public class UsersActivityList extends AbstractListValueResulted {

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

    @Override
    public DBObject getFilter(Map<String, String> clauses) throws ParseException, IOException {
        clauses = Utils.clone(clauses);

        overrideSortOrder(clauses);
        excludeStartAndStopFactorySessionsEvents(clauses);

        if (MetricFilter.SESSION_ID.exists(clauses)) {
            setUserWsAndDateFilters(clauses);
            MetricFilter.SESSION_ID.remove(clauses);
        }

        return super.getFilter(clauses);
    }

    /**
     * Adds {@link #CUMULATIVE_TIME} and {@link #TIME} fields to the result. Also adds
     * {@link com.codenvy.analytics.pig.scripts.EventsHolder#IDE_OPENED} and
     * {@link com.codenvy.analytics.pig.scripts.EventsHolder#IDE_CLOSED} events.
     */
    @Override
    protected ValueData postEvaluation(ValueData valueData, Map<String, String> clauses) throws IOException {
        int itemNumber = -1;
        long prevActionStartDate = -1;
        SessionData sessionData = SessionData.init(clauses);
        List<ValueData> list2Return = new ArrayList<>();

        for (ValueData row : ((ListValueData)valueData).getAll()) {
            itemNumber++;
            Map<String, ValueData> items2Return = new HashMap<>(((MapValueData)row).getAll());
            long eventDate = ((LongValueData)items2Return.get(DATE)).getAsLong();

            if (sessionData != null) {
                long eventDurationTime = getDurationTime(eventDate, clauses, prevActionStartDate, itemNumber);
                long eventCumulativeTime = getCumulativeTime(eventDate, sessionData.fromDate);

                items2Return.put(TIME, LongValueData.valueOf(eventDurationTime));
                items2Return.put(CUMULATIVE_TIME, LongValueData.valueOf(eventCumulativeTime));

                prevActionStartDate = eventDate;
            } else {
                items2Return.put(TIME, LongValueData.DEFAULT);
                items2Return.put(CUMULATIVE_TIME, LongValueData.DEFAULT);
            }

            list2Return.add(new MapValueData(items2Return));
        }

        if (sessionData != null) {
            addArtificialActions(clauses, sessionData, list2Return);
        }

        return new ListValueData(list2Return);
    }

    /**
     * Calculate duration of event, in millisec.
     */
    private long getDurationTime(long eventDate,
                                 Map<String, String> clauses,
                                 long prevEventStartDate,
                                 int numberOfEventWithinList2Return) throws IOException {
        long durationTime;

        if (prevEventStartDate < 0) { // calculate duration time of first event on page
            if (isFirstPage(clauses)) {
                durationTime = 0;
            } else {
                // taking into account the date of last event from previous page
                if (Parameters.PAGE.exists(clauses)
                    && Parameters.PER_PAGE.exists(clauses)
                    && Parameters.PER_PAGE.getAsLong(clauses) > 1) // to protect from recursion of method
                                                                   // getDateOfEvent()
                {
                    int eventNumber = (int)((Parameters.PAGE.getAsLong(clauses) - 1) * Parameters.PER_PAGE.getAsLong(clauses) 
                                      + numberOfEventWithinList2Return + 1);
                    
                    long previousEventData = getDateOfEvent(clauses, eventNumber - 1); // get date of previous event by
                                                                                       // requesting to database
                    durationTime = eventDate - previousEventData;
                } else {
                    durationTime = 0;
                }
            }
        } else {
            durationTime = eventDate - prevEventStartDate;
        }

        return durationTime;
    }

    /**
     * Calculate cumulative time which is passed from start of session to start of event, in millisec.
     */
    private long getCumulativeTime(long eventDate, long startSessionDate) {
        return eventDate - startSessionDate;
    }

    /**
     * Returns date of event from USERS_ACTIVITY_LIST
     * 
     * @param eventNumber starting from 0
     */
    private long getDateOfEvent(Map<String, String> clauses, int eventNumber) throws IOException {
        if (eventNumber <= 0) {
            return 0; // first event time = 0
        }

        Map<String, String> context = Utils.clone(clauses);
        // get only one document with number = eventNumber
        Parameters.PAGE.put(context, eventNumber);
        Parameters.PER_PAGE.put(context, 1);

        Metric metric = MetricFactory.getMetric(MetricType.USERS_ACTIVITY_LIST);

        ListValueData valueData = (ListValueData)metric.getValue(context);

        List<ValueData> events = valueData.getAll();

        if (events.size() == 0) {
            return 0;
        }

        Map<String, ValueData> event = ((MapValueData)events.get(0)).getAll();

        long date = ((LongValueData)event.get(DATE)).getAsLong();

        return date;
    }

    private void addArtificialActions(Map<String, String> clauses,
                                      SessionData sessionData,
                                      List<ValueData> list2Return) throws IOException {
        if (isFirstPage(clauses)) {
            list2Return.add(0, getIdeOpenedEvent(sessionData));
        }

        if (isLastPage(clauses)) {
            list2Return.add(getIdeClosedEvent(sessionData));
        }

        if (sessionData.logoutInterval != 0 && isLastPage(clauses)) {
            list2Return.add(getUserLogoutEvent(sessionData));
        }
    }

    private ValueData getUserLogoutEvent(SessionData sessionData) {
        Map<String, ValueData> items = new HashMap<>();

        items.put(DATE, LongValueData.valueOf(sessionData.toDate));
        items.put(EVENT, StringValueData.valueOf(EventsHolder.USER_SSO_LOGOUT_EVENT));
        items.put(TIME, LongValueData.valueOf(0));
        items.put(CUMULATIVE_TIME, LongValueData.valueOf(sessionData.time));

        return new MapValueData(items);
    }

    private ValueData getIdeClosedEvent(SessionData sessionData) {
        Map<String, ValueData> items = new HashMap<>();

        items.put(DATE, LongValueData.valueOf(sessionData.toDate));
        items.put(EVENT, StringValueData.valueOf(EventsHolder.IDE_CLOSED));
        items.put(TIME, LongValueData.valueOf(0));
        items.put(CUMULATIVE_TIME, LongValueData.valueOf(sessionData.time));

        return new MapValueData(items);
    }

    private ValueData getIdeOpenedEvent(SessionData sessionData) {
        Map<String, ValueData> items = new HashMap<>();

        items.put(DATE, LongValueData.valueOf(sessionData.fromDate));
        items.put(EVENT, StringValueData.valueOf(EventsHolder.IDE_OPENED));
        items.put(TIME, LongValueData.valueOf(0));
        items.put(CUMULATIVE_TIME, LongValueData.valueOf(0));

        return new MapValueData(items);
    }

    private boolean isFirstPage(Map<String, String> clauses) {
        return !Parameters.PAGE.exists(clauses) || Parameters.PAGE.get(clauses).equals("1");
    }

    private boolean isLastPage(Map<String, String> clauses) throws IOException {
        if (!Parameters.PAGE.exists(clauses)) {
            return true;
        }

        long maxActionNumber = Parameters.PAGE.getAsLong(clauses) * Parameters.PER_PAGE.getAsLong(clauses);
        return maxActionNumber >= getTotalActionsNumber(clauses);
    }

    private long getTotalActionsNumber(Map<String, String> clauses) throws IOException {
        // remove misleading parameter PAGE from clauses
        Map<String, String> clausesWithoutPageParameter = Utils.clone(clauses);
        Parameters.PAGE.remove(clausesWithoutPageParameter);

        Metric metric = MetricFactory.getMetric(MetricType.USERS_ACTIVITY);
        return ((LongValueData)metric.getValue(clausesWithoutPageParameter)).getAsLong();
    }

    private void overrideSortOrder(Map<String, String> clauses) {
        Parameters.SORT.put(clauses, ASC_SORT_SIGN + DATE);
    }

    private void excludeStartAndStopFactorySessionsEvents(Map<String, String> clauses) {
        String eventFilter = MetricFilter.EVENT.get(clauses);

        if (eventFilter != null) {
            MetricFilter.EVENT.put(clauses, eventFilter + "," + EventsHolder.NOT_FACTORY_SESSIONS);
        } else {
            MetricFilter.EVENT.put(clauses, EventsHolder.NOT_FACTORY_SESSIONS);
        }
    }

    private void setUserWsAndDateFilters(Map<String, String> clauses) throws IOException {
        SessionData sessionData = SessionData.init(clauses);

        if (sessionData != null) {
            MetricFilter.USER.putIfAbsent(clauses, sessionData.user);
            MetricFilter.WS.put(clauses, sessionData.ws);
            Parameters.FROM_DATE.put(clauses, sessionData.fromDate);
            Parameters.TO_DATE.put(clauses, sessionData.toDate);
        } else {
            Parameters.FROM_DATE.put(clauses, 0);
            Parameters.TO_DATE.put(clauses, 0);
        }
    }

    /**
     * The data of the sessions that contains all given user's actions.
     */
    private static class SessionData {
        private long   fromDate;
        private long   toDate;
        private long   time;
        private long   logoutInterval;

        private String ws;
        private String user;

        private SessionData() {
        }

        private static SessionData init(Map<String, String> clauses) throws IOException {
            String sessionId = MetricFilter.SESSION_ID.get(clauses);
            if (sessionId == null) {
                return null;
            }

            MapValueData valueData = getValueData(sessionId);
            if (valueData == null) {
                return null;
            }

            SessionData sessionData = new SessionData();
            sessionData.fromDate = getFromDate(valueData);
            sessionData.toDate = getToDate(valueData);
            sessionData.time = getTime(valueData);
            sessionData.logoutInterval = getLogoutInterval(valueData);
            sessionData.user = getUser(valueData);
            sessionData.ws = getWorkspace(valueData);

            return sessionData;
        }

        private static MapValueData getValueData(String sessionId) throws IOException {
            Map<String, String> context = Utils.newContext();
            MetricFilter.SESSION_ID.put(context, sessionId);

            Metric metric = MetricFactory.getMetric(MetricType.PRODUCT_USAGE_SESSIONS_LIST);
            ListValueData valueData = (ListValueData)metric.getValue(context);

            return valueData.size() == 0 ? null : (MapValueData)valueData.getAll().get(0);
        }

        private static long getToDate(MapValueData valueData) {
            long fromDate = getFromDate(valueData);
            long time = getTime(valueData);

            return fromDate + time;
        }

        private static String getWorkspace(MapValueData valueData) {
            return getStringParameter(valueData, WS);
        }

        private static String getUser(MapValueData valueData) {
            return getStringParameter(valueData, USER);
        }

        private static long getFromDate(MapValueData valueData) {
            return getLongParameter(valueData, DATE);
        }

        private static long getTime(MapValueData valueData) {
            return getLongParameter(valueData, TIME);
        }

        private static long getLogoutInterval(MapValueData valueData) {
            return getLongParameter(valueData, LOGOUT_INTERVAL);
        }

        private static String getStringParameter(MapValueData valueData, String param) {
            return valueData.getAll().get(param).getAsString();
        }

        private static long getLongParameter(MapValueData valueData, String param) {
            return ((LongValueData)valueData.getAll().get(param)).getAsLong();
        }
    }
}
