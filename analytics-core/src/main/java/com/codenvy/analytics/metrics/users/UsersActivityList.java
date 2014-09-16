/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
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

import com.codenvy.analytics.Injector;
import com.codenvy.analytics.datamodel.*;
import com.codenvy.analytics.metrics.*;
import com.codenvy.analytics.persistent.MongoDataLoader;
import com.codenvy.analytics.pig.scripts.EventsHolder;

import javax.annotation.security.RolesAllowed;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
@RolesAllowed({"system/admin", "system/manager"})
public class UsersActivityList extends AbstractListValueResulted {

    private       Metric       totalActionsNumberMetric;
    private final EventsHolder eventsHolder;

    public UsersActivityList() {
        this(null);
    }

    /** For testing purpose only. */
    public UsersActivityList(Metric totalActionsNumberMetric) {
        super(MetricType.USERS_ACTIVITY_LIST);
        this.totalActionsNumberMetric = totalActionsNumberMetric;
        this.eventsHolder = Injector.getInstance(EventsHolder.class);
    }

    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.USERS_ACTIVITY);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{DATE,
                            MESSAGE,
                            EVENT,
                            ACTION,
                            WS,
                            USER};
    }

    @Override
    public String getDescription() {
        return "Users' actions";
    }

    @Override
    public Context applySpecificFilter(Context context) throws IOException {
        Context.Builder builder = new Context.Builder(context);
        builder.put(Parameters.SORT, MongoDataLoader.ASC_SORT_SIGN + DATE);

        if (context.exists(MetricFilter.SESSION_ID)) {
            setUserWsAndDateFilters(builder);
            builder.remove(MetricFilter.SESSION_ID);
        }

        return builder.build();
    }

    /**
     * Adds {@link #CUMULATIVE_TIME} and {@link #TIME} fields to the result. Also adds
     * {@link com.codenvy.analytics.pig.scripts.EventsHolder#IDE_OPENED} and
     * {@link com.codenvy.analytics.pig.scripts.EventsHolder#IDE_CLOSED} events.
     */
    @Override
    public ValueData postComputation(ValueData valueData, Context clauses) throws IOException {
        SessionData sessionData = SessionData.init(clauses);

        long prevActionDate = sessionData != null ? sessionData.fromDate : -1;

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

            StringValueData event = (StringValueData)row.get(EVENT);
            StringValueData message = (StringValueData)row.get(MESSAGE);
            row2Return.put(STATE, getState(event.getAsString(), message.getAsString()));

            if (row.containsKey(WS) && row.get(WS).getAsString().equals("default")) {
                row2Return.put(WS, StringValueData.DEFAULT);
            }
            if (row.containsKey(USER) && row.get(USER).getAsString().equals("default")) {
                row2Return.put(USER, StringValueData.DEFAULT);
            }

            item2Return.add(new MapValueData(row2Return));
        }

        if (sessionData != null && !items.isEmpty()) {
            addArtificialActions(sessionData,
                                 items.size(),
                                 clauses,
                                 item2Return);
        }

        return new ListValueData(item2Return);
    }

    /**
     * Extracts all available params out of {@link #MESSAGE}.
     */
    private StringValueData getState(String event, String message) {
        Map<String, String> result = eventsHolder.getParametersValues(event, message);
        result.remove("ID");
        result.remove("USER-ID");
        result.remove("SESSION-ID");
        result.remove("WORKSPACE-ID");

        return StringValueData.valueOf(result.toString());
    }

    /** Calculate duration of action, in millisec. */

    private long getTime(long actionDate,
                         long prevActionDate,
                         int actionNumber,
                         Context clauses) throws IOException {
        if (actionNumber == 0) {
            if (isFirstPage(clauses)) {
                return actionDate - prevActionDate;
            } else {
                if (clauses.exists(Parameters.PER_PAGE) && clauses.getAsLong(Parameters.PER_PAGE) > 1) {
                    long prevGlobalActionNumber = (clauses.getAsLong(Parameters.PAGE) - 1) * clauses.getAsLong(Parameters.PER_PAGE);
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
    private long getDateOfAction(Context clauses, long globalActionNumber) throws IOException {
        Context.Builder builder = new Context.Builder(clauses);
        builder.put(Parameters.PAGE, globalActionNumber);
        builder.put(Parameters.PER_PAGE, 1);
        Context context = builder.build();

        ListValueData valueData = ValueDataUtil.getAsList(this, context);

        List<ValueData> items = valueData.getAll();
        if (items.size() == 0) {
            return 0;
        }

        Map<String, ValueData> row = ((MapValueData)items.get(0)).getAll();
        return ValueDataUtil.treatAsLong(row.get(DATE));
    }

    private void addArtificialActions(SessionData sessionData,
                                      int actionCount,
                                      Context clauses,
                                      List<ValueData> items2Return) throws IOException {
        if (isFirstPage(clauses)) {
            items2Return.add(0, getIdeOpenedEvent(sessionData));
        }

        if (isLastPage(clauses)) {
            items2Return.add(getIdeClosedEvent(sessionData, actionCount));

            if (sessionData.logoutInterval != 0) {
                items2Return.add(getUserLogoutEvent(sessionData));
            } else {
                items2Return.add(getUserIdleEvent(sessionData));
            }
        }
    }

    private ValueData getUserLogoutEvent(SessionData sessionData) {
        Map<String, ValueData> items = new HashMap<>();

        String action = eventsHolder.getDescription(EventsHolder.USER_SSO_LOGOUT_EVENT);
        items.put(ACTION, StringValueData.valueOf(action));
        items.put(DATE, LongValueData.valueOf(sessionData.toDate));
        items.put(EVENT, StringValueData.valueOf(EventsHolder.USER_SSO_LOGOUT_EVENT));
        items.put(TIME, LongValueData.valueOf(sessionData.logoutInterval));
        items.put(CUMULATIVE_TIME, LongValueData.valueOf(sessionData.time));

        return new MapValueData(items);
    }

    private ValueData getUserIdleEvent(SessionData sessionData) {
        Map<String, ValueData> items = new HashMap<>();

        String action = eventsHolder.getDescription(EventsHolder.USER_IDLE_EVENT);
        items.put(ACTION, StringValueData.valueOf(action));
        items.put(DATE, LongValueData.valueOf(sessionData.toDate));
        items.put(EVENT, StringValueData.valueOf(EventsHolder.USER_IDLE_EVENT));
        items.put(TIME, LongValueData.valueOf(0));
        items.put(CUMULATIVE_TIME, LongValueData.valueOf(sessionData.time));

        return new MapValueData(items);
    }

    private ValueData getIdeClosedEvent(SessionData sessionData, int actionCount) {
        Map<String, ValueData> items = new HashMap<>();

        String action = eventsHolder.getDescription(EventsHolder.IDE_CLOSED);
        items.put(ACTION, StringValueData.valueOf(action));
        items.put(DATE, LongValueData.valueOf(sessionData.toDate));
        items.put(EVENT, StringValueData.valueOf(EventsHolder.IDE_CLOSED));
        items.put(TIME, LongValueData.valueOf(actionCount == 0 ? sessionData.time : 0));
        items.put(CUMULATIVE_TIME, LongValueData.valueOf(sessionData.time));

        return new MapValueData(items);
    }

    private ValueData getIdeOpenedEvent(SessionData sessionData) {
        Map<String, ValueData> items = new HashMap<>();

        String action = eventsHolder.getDescription(EventsHolder.IDE_OPENED);
        items.put(ACTION, StringValueData.valueOf(action));
        items.put(DATE, LongValueData.valueOf(sessionData.fromDate));
        items.put(EVENT, StringValueData.valueOf(EventsHolder.IDE_OPENED));
        items.put(TIME, LongValueData.valueOf(0));
        items.put(CUMULATIVE_TIME, LongValueData.valueOf(0));

        return new MapValueData(items);
    }

    private boolean isFirstPage(Context clauses) {
        return !clauses.exists(Parameters.PAGE) || clauses.getAsLong(Parameters.PAGE) == 1;
    }

    private boolean isLastPage(Context clauses) throws IOException {
        if (!clauses.exists(Parameters.PAGE)) {
            return true;
        }

        long maxActionNumber = clauses.getAsLong(Parameters.PAGE) * clauses.getAsLong(Parameters.PER_PAGE);
        return maxActionNumber >= getTotalActionsNumber(clauses);
    }

    private long getTotalActionsNumber(Context basedClauses) throws IOException {
        Context.Builder builder = new Context.Builder(basedClauses);
        builder.remove(Parameters.PAGE);
        builder.remove(Parameters.PER_PAGE);
        Context context = builder.build();

        if (totalActionsNumberMetric == null) {
            totalActionsNumberMetric = MetricFactory.getMetric(MetricType.USERS_ACTIVITY);
        }
        return ValueDataUtil.getAsLong(totalActionsNumberMetric, context).getAsLong();
    }

    private void setUserWsAndDateFilters(Context.Builder builder) throws IOException {
        SessionData sessionData = SessionData.init(builder.build());

        if (sessionData != null) {
            builder.put(Parameters.USER, sessionData.user);
            builder.put(Parameters.WS, sessionData.ws);
            builder.put(Parameters.TO_DATE, sessionData.toDate);
            builder.put(Parameters.FROM_DATE, sessionData.fromDate);
        } else {
            builder.put(Parameters.TO_DATE, 0);
            builder.put(Parameters.FROM_DATE, 0);
        }
    }

    /** The data of the sessions that contains all given user's actions. */
    private static class SessionData {
        private long fromDate;
        private long toDate;
        private long time;
        private long logoutInterval;

        private String ws;
        private String user;

        private SessionData() {
        }

        private static SessionData init(Context clauses) throws IOException {
            String sessionId = clauses.getAsString(MetricFilter.SESSION_ID);
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
            Context.Builder builder = new Context.Builder();
            builder.put(MetricFilter.SESSION_ID, sessionId);
            Context context = builder.build();

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
            if (valueData.getAll().containsKey(param)) {
                return valueData.getAll().get(param).getAsString();
            } else {
                return StringValueData.DEFAULT.getAsString();
            }
        }

        private static long getLongParameter(MapValueData valueData, String param) {
            if (valueData.getAll().containsKey(param)) {
                return ValueDataUtil.treatAsLong(valueData.getAll().get(param));
            } else {
                return LongValueData.DEFAULT.getAsLong();
            }
        }
    }
}
