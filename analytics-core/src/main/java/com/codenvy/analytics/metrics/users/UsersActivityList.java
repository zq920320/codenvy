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

    private Metric totalActionsNumberMetric;

    public UsersActivityList() {
        super(MetricType.USERS_ACTIVITY_LIST);
    }

    /**
     * For testing purpose only.
     */
    public UsersActivityList(Metric totalActionsNumberMetric) {
        super(MetricType.USERS_ACTIVITY_LIST);
        this.totalActionsNumberMetric = totalActionsNumberMetric;
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
        SessionData sessionData = SessionData.init(clauses);

        long prevActionDate = -1;

        List<ValueData> items = ((ListValueData)valueData).getAll();
        List<ValueData> item2Return = new ArrayList<>(items.size() + 3);

        for (int i = 0; i < items.size(); i++) {
            Map<String, ValueData> row = ((MapValueData)items.get(i)).getAll();
            Map<String, ValueData> row2Return = new HashMap<>(row);

            long actionDate = ((LongValueData)row.get(DATE)).getAsLong();

            if (sessionData != null) {
                long actionTime = getTime(actionDate, prevActionDate, i, clauses);
                long actionCumulativeTime = actionDate - sessionData.fromDate;

                prevActionDate = actionDate;

                row2Return.put(TIME, LongValueData.valueOf(actionTime));
                row2Return.put(CUMULATIVE_TIME, LongValueData.valueOf(actionCumulativeTime));
            } else {
                row2Return.put(TIME, LongValueData.DEFAULT);
                row2Return.put(CUMULATIVE_TIME, LongValueData.DEFAULT);
            }

            item2Return.add(new MapValueData(row2Return));
        }

        if (sessionData != null) {
            addArtificialActions(sessionData, clauses, item2Return);
        }

        return new ListValueData(item2Return);
    }

    /**
     * Calculate duration of action, in millisec.
     */
    private long getTime(long actionDate,
                         long prevActionDate,
                         int actionNumber,
                         Map<String, String> clauses) throws IOException {
        if (actionNumber == 0) {
            if (isFirstPage(clauses)) {
                return 0;
            } else {
                if (Parameters.PER_PAGE.exists(clauses) && Parameters.PER_PAGE.getAsLong(clauses) > 1) {
                    long prevGlobalActionNumber =
                            (Parameters.PAGE.getAsLong(clauses) - 1) * Parameters.PER_PAGE.getAsLong(clauses);

                    return actionDate - getDateOfAction(clauses, prevGlobalActionNumber);
                } else {
                    return 0;
                }
            }
        } else {
            return actionDate - prevActionDate;
        }
    }

    /**
     * Returns date of event from USERS_ACTIVITY_LIST
     *
     * @param globalActionNumber
     *         starting from 0
     */
    private long getDateOfAction(Map<String, String> clauses, long globalActionNumber) throws IOException {
        Map<String, String> context = Utils.clone(clauses);
        Parameters.PAGE.put(context, globalActionNumber);
        Parameters.PER_PAGE.put(context, 1);

        ListValueData valueData = (ListValueData)getValue(context);

        List<ValueData> items = valueData.getAll();
        if (items.size() == 0) {
            return 0;
        }

        Map<String, ValueData> row = ((MapValueData)items.get(0)).getAll();
        return ((LongValueData)row.get(DATE)).getAsLong();
    }

    private void addArtificialActions(SessionData sessionData,
                                      Map<String, String> clauses,
                                      List<ValueData> items2Return) throws IOException {
        if (isFirstPage(clauses)) {
            items2Return.add(0, getIdeOpenedEvent(sessionData));
        }

        if (isLastPage(clauses)) {
            items2Return.add(getIdeClosedEvent(sessionData));
            if (sessionData.logoutInterval != 0) {
                items2Return.add(getUserLogoutEvent(sessionData));
            } else {
                items2Return.add(getUserIdleEvent(sessionData));
            }
        }
    }

    private ValueData getUserLogoutEvent(SessionData sessionData) {
        Map<String, ValueData> items = new HashMap<>();

        items.put(DATE, LongValueData.valueOf(sessionData.toDate));
        items.put(EVENT, StringValueData.valueOf(EventsHolder.USER_SSO_LOGOUT_EVENT));
        items.put(TIME, LongValueData.valueOf(sessionData.logoutInterval));
        items.put(CUMULATIVE_TIME, LongValueData.valueOf(sessionData.time));

        return new MapValueData(items);
    }

    private ValueData getUserIdleEvent(SessionData sessionData) {
        Map<String, ValueData> items = new HashMap<>();

        items.put(DATE, LongValueData.valueOf(sessionData.toDate));
        items.put(EVENT, StringValueData.valueOf(EventsHolder.USER_IDLE_EVENT));
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
        clauses = Utils.clone(clauses);
        Parameters.PAGE.remove(clauses);
        Parameters.PER_PAGE.remove(clauses);

        if (totalActionsNumberMetric == null) {
            totalActionsNumberMetric = MetricFactory.getMetric(MetricType.USERS_ACTIVITY);
        }
        return ValueDataUtil.getAsLong(totalActionsNumberMetric.getValue(clauses));
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
        private long fromDate;
        private long toDate;
        private long time;
        private long logoutInterval;

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
            return ValueDataUtil.getAsLong(valueData.getAll().get(param));
        }
    }
}
