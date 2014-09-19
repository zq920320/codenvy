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

package com.codenvy.analytics;

import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.*;
import com.codenvy.analytics.metrics.Context.Builder;
import com.codenvy.analytics.metrics.Parameters.PassedDaysCount;
import com.codenvy.analytics.metrics.Parameters.TimeUnit;
import com.codenvy.analytics.persistent.MongoDataLoader;
import com.codenvy.analytics.services.view.ViewBuilder;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import org.joda.time.*;

import javax.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsList;
import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsMap;
import static java.net.URLDecoder.decode;


/**
 * @author Anatoliy Bazko
 */
public class Utils {

    private static Pattern VALID_EMAIL_ADDRESS_RFC822 =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    private Utils() {
    }

    /**
     * Parse date represented by give string using {@link com.codenvy.analytics.metrics.Parameters#PARAM_DATE_FORMAT}
     * format. Wraps {@link
     * ParseException} into {@link java.io.IOException}.
     */
    public static Calendar parseDate(String date) throws ParseException {
        DateFormat df = new SimpleDateFormat(Parameters.PARAM_DATE_FORMAT);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(df.parse(date));

        return calendar;
    }

    /** Formats date using {@link Parameters#PARAM_DATE_FORMAT}. */
    public static String formatDate(Calendar date) {
        DateFormat df = new SimpleDateFormat(Parameters.PARAM_DATE_FORMAT);
        return df.format(date.getTime());
    }

    public static boolean isDateFormat(String date) {
        return date.length() == Parameters.PARAM_DATE_FORMAT.length();
    }

    /** Initialize date interval accordingly to passed {@link Parameters#TIME_UNIT} */
    public static Context initDateInterval(Calendar toDate, Parameters.TimeUnit timeUnit, Context.Builder builder)
            throws ParseException {
        return initDateInterval(toDate, timeUnit, 0, builder);
    }

    private static void initByLifeTime(Context.Builder builder) {
        builder.putDefaultValue(Parameters.FROM_DATE);
        builder.putDefaultValue(Parameters.TO_DATE);
    }

    private static void initByWeek(Calendar toDate, int shift, Context.Builder builder) {
        toDate.add(Calendar.WEEK_OF_MONTH, shift);

        Calendar fromDate = (Calendar)toDate.clone();
        fromDate.add(Calendar.DAY_OF_MONTH,
                     fromDate.getActualMinimum(Calendar.DAY_OF_WEEK) - fromDate.get(Calendar.DAY_OF_WEEK));

        toDate.add(Calendar.DAY_OF_MONTH,
                   toDate.getActualMaximum(Calendar.DAY_OF_WEEK) - toDate.get(Calendar.DAY_OF_WEEK));

        builder.put(Parameters.FROM_DATE, fromDate);
        builder.put(Parameters.TO_DATE, toDate);
    }

    private static void initByMonth(Calendar toDate, int shift, Context.Builder builder) {
        toDate.add(Calendar.MONTH, shift);

        Calendar fromDate = (Calendar)toDate.clone();
        fromDate.set(Calendar.DAY_OF_MONTH, 1);

        toDate.set(Calendar.DAY_OF_MONTH, toDate.getActualMaximum(Calendar.DAY_OF_MONTH));

        builder.put(Parameters.FROM_DATE, fromDate);
        builder.put(Parameters.TO_DATE, toDate);
    }

    private static void initByDay(Calendar toDate, int shift, Context.Builder builder) {
        toDate.add(Calendar.DATE, shift);
        Calendar fromDate = (Calendar)toDate.clone();
        builder.put(Parameters.FROM_DATE, fromDate);
        builder.put(Parameters.TO_DATE, toDate);
    }

    /**
     * Shift date interval forward depending on {@link com.codenvy.analytics.metrics.Parameters.TimeUnit}. Should also
     * be placed in context.
     */
    public static Context nextDateInterval(Context.Builder builder) throws ParseException {
        Calendar toDate = builder.getAsDate(Parameters.TO_DATE);
        Parameters.TimeUnit timeUnit = builder.getTimeUnit();

        return initDateInterval(toDate, timeUnit, 1, builder);
    }

    /**
     * Shift date interval backward depending on {@link com.codenvy.analytics.metrics.Parameters.TimeUnit}. Should also
     * be placed in context.
     */
    public static Context prevDateInterval(Context.Builder builder) throws ParseException {
        Calendar toDate = builder.getAsDate(Parameters.TO_DATE);
        Parameters.TimeUnit timeUnit = builder.getTimeUnit();

        return initDateInterval(toDate, timeUnit, -1, builder);
    }

    public static Context initializeContext(Parameters.TimeUnit timeUnit) throws ParseException {
        Calendar cal = Calendar.getInstance();

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TIME_UNIT, timeUnit.toString());

        initDateInterval(cal, builder.getTimeUnit(), builder);

        return timeUnit == Parameters.TimeUnit.DAY ? Utils.prevDateInterval(builder) : builder.build();
    }

    /**
     * Calculate FROM_DATE and TO_DATE parameters of context:
     * FROM_DATE = (current_{date/week/month}_at_from_date + timeUnit * (timeShift - 1))
     * TO_FATE =  (current_{date/week/month}_at_from_date + timeUnit * timeShift)
     *
     * @param timeShift
     *         = starting from 0 to represent current time period.
     */
    public static Context initDateInterval(Calendar toDate, TimeUnit timeUnit, int timeShift, Builder builder) {
        switch (timeUnit) {
            case DAY:
                initByDay(toDate, timeShift, builder);
                break;
            case WEEK:
                initByWeek(toDate, timeShift, builder);
                break;
            case MONTH:
                initByMonth(toDate, timeShift, builder);
                break;
            case LIFETIME:
                initByLifeTime(builder);
                break;
        }

        return builder.build();
    }

    /**
     * Calculate FROM_DATE and TO_DATE parameters of context:
     * FROM_DATE will be = @param toDate - @param passedDaysCount)
     * TO_DATE will be = @param toDate
     */
    public static Context initDateInterval(Calendar toDate, PassedDaysCount passedDaysCount, Builder builder) {
        switch (passedDaysCount) {
            case BY_LIFETIME:
                initByLifeTime(builder);
                break;

            default:
                initByPassedDays(toDate, builder, passedDaysCount.getDayCount());
                break;
        }

        return builder.build();
    }

    private static void initByPassedDays(Calendar toDate, Builder builder, int passedDaysCount) {
        Calendar fromDate = (Calendar)toDate.clone();
        fromDate.add(Calendar.DAY_OF_MONTH, 1 - passedDaysCount);
        builder.put(Parameters.FROM_DATE, fromDate);
        builder.put(Parameters.TO_DATE, toDate);
    }

    public static Map<String, String> fetchEncodedPairs(String data, boolean keyToLowerCase) throws UnsupportedEncodingException {
        if (data.isEmpty()) {
            return Collections.emptyMap();
        }

        String[] splitted = data.split(",");
        Map<String, String> result = new HashMap<>(splitted.length);

        for (String entry : splitted) {
            String[] pair = entry.split("=");

            String key = decode(pair[0], "UTF-8");
            if (keyToLowerCase) {
                key = key.toLowerCase();
            }

            if (pair.length == 2) {
                result.put(key, decode(pair[1], "UTF-8"));
            } else if (pair.length == 1) {
                result.put(key, "");
            }
        }

        return result;
    }

    public static DBObject setDateFilter(Context context) throws ParseException {
        DBObject dateFilter = new BasicDBObject();
        dateFilter.put("$gte", context.getAsDate(Parameters.FROM_DATE).getTimeInMillis());
        dateFilter.put("$lt",
                       context.getAsDate(Parameters.TO_DATE).getTimeInMillis() + MongoDataLoader.DAY_IN_MILLISECONDS);
        return new BasicDBObject(ReadBasedMetric.DATE, dateFilter);
    }

    /**
     * Returns number of units between the dates, taking into account incomplete weeks and months.
     */
    public static int getUnitsAboveDates(TimeUnit timeUnit, Calendar fromDate, Calendar toDate) {
        switch (timeUnit) {
            case DAY:
                return Days.daysBetween(new LocalDate(fromDate), new LocalDate(toDate)).getDays() + 1;  // add one for last day

            case WEEK:
                fromDate.set(Calendar.DAY_OF_WEEK, fromDate.getFirstDayOfWeek());
                toDate.set(Calendar.DAY_OF_WEEK, toDate.getFirstDayOfWeek());
                if (fromDate.getTime() != toDate.getTime()) {
                    toDate.add(Calendar.WEEK_OF_MONTH, 1);
                }

                return Weeks.weeksBetween(new LocalDate(fromDate), new LocalDate(toDate)).getWeeks();

            case MONTH:
                fromDate.set(Calendar.DAY_OF_MONTH, 1);
                toDate.set(Calendar.DAY_OF_MONTH, 1);
                if (fromDate.getTime() != toDate.getTime()) {
                    toDate.add(Calendar.MONTH, 1);
                }

                return Months.monthsBetween(new LocalDate(fromDate), new LocalDate(toDate)).getMonths();

            case LIFETIME:
                return 1;
        }
        
        return 0;
    }

    public static String getFilterAsString(Set<String> values) {
        StringBuilder result = new StringBuilder();
        for (String value : values) {
            if (result.length() > 0) {
                result.append(MongoDataLoader.SEPARATOR);
            }

            result.append(value);
        }

        return result.toString();
    }

    public static Set<String> getFilterAsSet(String value) {
        if (value.trim().isEmpty()) {
            return Collections.emptySet();
        }

        return new HashSet<>(Arrays.asList(value.split(MongoDataLoader.SEPARATOR)));
    }


    public static boolean isRegisteredUserName(Object userName) {
        return VALID_EMAIL_ADDRESS_RFC822.matcher(userName.toString()).matches();
    }

    public static boolean isAnonymousUserName(Object userName) {
        String s = String.valueOf(userName).toUpperCase();
        return s.startsWith("ANONYMOUSUSER_") || s.equals("DEFAULT");
    }

    public static boolean isDefaultUserName(Object userName) {
        return String.valueOf(userName).equalsIgnoreCase("DEFAULT");
    }

    public static boolean isAnonymousUser(Object user) {
        try {
            if (isAnonymousUserName(user)) {
                return true;
            } else if (isRegisteredUserName(user)) {
                return false;
            }

            Context.Builder builder = new Context.Builder(MetricFilter.USER, user.toString().toLowerCase());

            Metric metric = MetricFactory.getMetric(MetricType.USERS_PROFILES_LIST);
            ListValueData value = getAsList(metric, builder.build());

            if (value.size() == 0) {
                return true;
            }

            Map<String, ValueData> m = treatAsMap(value.getAll().get(0));
            return m.get(ReadBasedMetric.REGISTERED_USER).equals(LongValueData.DEFAULT);
        } catch (Exception e) {
            throw new IllegalStateException("Can not check user " + user, e);
        }
    }

    public static boolean isTemporaryWorkspaceName(Object name) {
        String s = String.valueOf(name).toUpperCase();
        return s.startsWith("TMP-") || s.equals("DEFAULT");
    }

    public static boolean isDefaultWorkspaceName(Object name) {
        return String.valueOf(name).equalsIgnoreCase("DEFAULT");
    }

    public static boolean isTemporaryWorkspace(Object workspace) {
        try {
            if (isTemporaryWorkspaceName(workspace)) {
                return true;
            }

            Context.Builder builder = new Context.Builder(MetricFilter.WS, workspace.toString().toLowerCase());

            Metric metric = MetricFactory.getMetric(MetricType.WORKSPACES_PROFILES_LIST);
            ListValueData value = getAsList(metric, builder.build());

            if (value.size() == 0) {
                return true;
            }

            Map<String, ValueData> m = treatAsMap(value.getAll().get(0));
            return m.get(ReadBasedMetric.PERSISTENT_WS).equals(LongValueData.DEFAULT);

        } catch (Exception e) {
            throw new IllegalStateException("Can not check workspace " + workspace, e);
        }
    }

    public static boolean isTemporaryExist(Set<String> workspaces) {
        for (String workspace : workspaces) {
            if (isTemporaryWorkspace(workspace)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isAnonymousUserExist(Set<String> users) {
        for (Object user : users) {
            if (isAnonymousUser(user)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isAllowedEntities(Object testedEntitiesAsString, Object allowedEntities) {
        if (allowedEntities == null || testedEntitiesAsString == null) {
            return true;
        } else {
            Set<String> testedEntities;
            if (testedEntitiesAsString.getClass().isArray()) {
                testedEntities = new HashSet<>(Arrays.asList((String[])testedEntitiesAsString));
            } else {
                testedEntities = getFilterAsSet((String)testedEntitiesAsString);
            }

            if (allowedEntities instanceof String) {
                return getFilterAsSet((String)allowedEntities).containsAll(testedEntities);

            } else if (allowedEntities instanceof String[]) {
                return new HashSet<>(Arrays.asList(allowedEntities)).containsAll(testedEntities);

            } else if (allowedEntities instanceof Pattern) {
                Pattern pattern = (Pattern)allowedEntities;
                for (String entity : testedEntities) {
                    if (!pattern.matcher(entity).find()) {
                        return false;
                    }
                }
                return true;

            } else if (allowedEntities instanceof Pattern[]) {
                Pattern[] patterns = (Pattern[])allowedEntities;
                for (String entity : testedEntities) {

                    boolean matched = false;
                    for (Pattern pattern : patterns) {
                        if (pattern.matcher(entity).find()) {
                            matched = true;
                            break;
                        }
                    }

                    if (!matched) {
                        return false;
                    }
                }
                return true;

            } else {
                throw new IllegalArgumentException("Unsupported type " + allowedEntities.getClass());
            }
        }
    }

    /** @return mongodb operation (arg1 - arg2Field) */

    public static BasicDBObject getSubtractOperation(long arg1, String arg2Field) {
        BasicDBList subtractArgs = new BasicDBList();
        subtractArgs.add(arg1);
        subtractArgs.add(arg2Field);

        return new BasicDBObject("$subtract", subtractArgs);
    }

    public static BasicDBObject getAndOperation(BasicDBObject... predicates) {
        BasicDBList andArgs = new BasicDBList();
        Collections.addAll(andArgs, predicates);
        return new BasicDBObject("$and", andArgs);
    }

    public static BasicDBObject getOrOperation(BasicDBObject... predicates) {
        BasicDBList orArgs = new BasicDBList();
        Collections.addAll(orArgs, predicates);
        return new BasicDBObject("$or", predicates);
    }

    public static String[] toArray(String value) {
        if (value == null) {
            return new String[0];
        }

        int startIndex = value.startsWith("[") ? 1 : 0;
        int endIndex = value.endsWith("]") ? value.length() - 1 : value.length();

        String bareValue = value.substring(startIndex, endIndex);
        if (bareValue.trim().isEmpty()) {
            return new String[0];
        } else {
            return bareValue.split(",");
        }
    }

    public static String[] toArray(ValueData value) {
        return toArray(value == null ? null : value.getAsString());
    }

    /**
     * @return c = a INTERSECT b
     */
    public static String[] arrayIntersect(String[] a, String[] b) {
        List<String> a_list = Arrays.asList(a);
        List<String> b_list = Arrays.asList(b);

        int intersectListSize = a_list.size() > b_list.size() ? a_list.size() : b_list.size();
        List<String> c_list = new ArrayList<>(intersectListSize);

        c_list.addAll(a_list);

        c_list.retainAll(b_list);

        return c_list.toArray(new String[0]);
    }

    /**
     * @return date of first day of time unit containing toDate.
     */
    @Nullable
    public static Calendar getFirstDayDate(TimeUnit timeUnit, Calendar toDate) {
        switch (timeUnit) {
            case DAY:
                return toDate;

            case WEEK:
                toDate.set(Calendar.DAY_OF_WEEK, toDate.getFirstDayOfWeek());
                return toDate;

            case MONTH:
                toDate.set(Calendar.DAY_OF_MONTH, 1);
                return toDate;
        }

        return null;
    }
}

