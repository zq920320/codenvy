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


package com.codenvy.analytics.old_metrics;

import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.old_metrics.value.FSValueDataManager;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class Utils {

    private Utils() {
    }

    /**
     * Parse date represented by give string using {@link com.codenvy.analytics.metrics.Parameters#PARAM_DATE_FORMAT} format. Wraps {@link
     * ParseException} into {@link IOException}.
     *
     * @throws IOException
     *         if exception is occurred
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

    /** Extracts {@link Parameters#TIME_UNIT} parameter value from context. */
    public static TimeUnit getTimeUnit(Map<String, String> context) {
        return TimeUnit.valueOf(Parameters.TIME_UNIT.get(context));
    }

    /** @return fromDate value */
    public static Calendar getFromDate(Map<String, String> context) throws ParseException {
        return parseDate(Parameters.FROM_DATE.get(context));
    }

    /** @return toDate value */
    public static Calendar getToDate(Map<String, String> context) throws ParseException {
        return parseDate(Parameters.TO_DATE.get(context));
    }

    /** Puts {@link Parameters#FROM_DATE} parameter into context. */
    public static void putFromDate(Map<String, String> context, Calendar fromDate) {
        Parameters.FROM_DATE.put(context, formatDate(fromDate));
    }

    /** Puts {@link Parameters#TO_DATE} parameter into context. */
    public static void putToDate(Map<String, String> context, Calendar toDate) {
        Parameters.TO_DATE.put(context, formatDate(toDate));
    }

    public static String getLoadDirFor(MetricType metricType) {
        return FSValueDataManager.SCRIPT_LOAD_DIRECTORY + File.separator + metricType.name();
    }

    public static String getStoreDirFor(MetricType metricType) {
        return FSValueDataManager.SCRIPT_STORE_DIRECTORY + File.separator + metricType.name();
    }

    /** Prepares load and store directories for Pig script execution */
    public static void initLoadStoreDirectories(Map<String, String> context) throws IOException {
        if (Parameters.LOAD_DIR.exists(context) && Parameters.STORE_DIR.exists(context)) {

            File loadDir = new File(Parameters.LOAD_DIR.get(context));
            File storeDir = new File(Parameters.STORE_DIR.get(context));

            if (storeDir.exists()) {
                if (loadDir.exists()) {
                    FileUtils.deleteDirectory(loadDir);
                }
                FileUtils.moveDirectory(storeDir, loadDir);
            } else {
                if (!loadDir.exists()) {
                    if (!loadDir.mkdirs()) {
                        throw new IOException("Can't create directory tree " + loadDir.getAbsolutePath());
                    }

                    File.createTempFile("tmp", "data", loadDir);
                }
            }
        }
    }

    /** Initialize date interval accordingly to passed {@link Parameters#TIME_UNIT} */
    public static void initDateInterval(Calendar date, Map<String, String> context) throws ParseException {
        TimeUnit timeUnit = getTimeUnit(context);

        switch (timeUnit) {
            case DAY:
                initByDay(date, context);
                break;
            case WEEK:
                initByWeek(date, context);
                break;
            case MONTH:
                initByMonth(date, context);
                break;
            case LIFETIME:
                initByLifeTime(context);
                break;
        }
    }

    private static void initByLifeTime(Map<String, String> context) {
        Parameters.FROM_DATE.putDefaultValue(context);
        Parameters.TO_DATE.putDefaultValue(context);
    }

    private static void initByWeek(Calendar date, Map<String, String> context) {
        Calendar fromDate = (Calendar)date.clone();
        fromDate.add(Calendar.DAY_OF_MONTH,
                     fromDate.getActualMinimum(Calendar.DAY_OF_WEEK) - fromDate.get(Calendar.DAY_OF_WEEK));

        Calendar toDate = (Calendar)date.clone();
        toDate.add(Calendar.DAY_OF_MONTH,
                   toDate.getActualMaximum(Calendar.DAY_OF_WEEK) - toDate.get(Calendar.DAY_OF_WEEK));

        putFromDate(context, fromDate);
        putToDate(context, toDate);
    }

    private static void initByMonth(Calendar date, Map<String, String> context) {
        Calendar fromDate = (Calendar)date.clone();
        fromDate.set(Calendar.DAY_OF_MONTH, 1);

        Calendar toDate = (Calendar)date.clone();
        toDate.set(Calendar.DAY_OF_MONTH, toDate.getActualMaximum(Calendar.DAY_OF_MONTH));

        putFromDate(context, fromDate);
        putToDate(context, toDate);
    }

    private static void initByDay(Calendar date, Map<String, String> context) {
        putFromDate(context, date);
        putToDate(context, date);
    }

    /** Shift date interval forward depending on {@link TimeUnit}. Should also be placed in context. */
    public static Map<String, String> nextDateInterval(Map<String, String> context) throws ParseException {
        Map<String, String> resultContext = new HashMap<>(context);

        Calendar fromDate = getFromDate(context);
        Calendar toDate = getToDate(context);
        TimeUnit timeUnit = getTimeUnit(context);

        switch (timeUnit) {
            case DAY:
                nextDay(toDate, resultContext);
                break;
            case WEEK:
                nextWeek(fromDate, toDate, resultContext);
                break;
            case MONTH:
                nextMonth(toDate, resultContext);
                break;
            case LIFETIME:
                break;
        }

        return resultContext;
    }

    /** Shift date interval backward depending on {@link TimeUnit}. Should also be placed in context. */
    public static Map<String, String> prevDateInterval(Map<String, String> context) throws ParseException {
        Map<String, String> resultContext = new HashMap<>(context);

        Calendar fromDate = getFromDate(context);
        Calendar toDate = getToDate(context);
        TimeUnit timeUnit = getTimeUnit(context);

        switch (timeUnit) {
            case DAY:
                prevDay(toDate, resultContext);
                break;
            case WEEK:
                prevWeek(fromDate, toDate, resultContext);
                break;
            case MONTH:
                prevMonth(fromDate, resultContext);
                break;
            case LIFETIME:
                break;
        }

        return resultContext;
    }

    private static void prevWeek(Calendar fromDate, Calendar toDate, Map<String, String> context) {
        fromDate.add(Calendar.DAY_OF_MONTH, -7);
        toDate.add(Calendar.DAY_OF_MONTH, -7);

        putFromDate(context, fromDate);
        putToDate(context, toDate);
    }

    private static void prevMonth(Calendar fromDate, Map<String, String> context) {
        Calendar toDate = (Calendar)fromDate.clone();
        toDate.add(Calendar.DAY_OF_MONTH, -1);

        fromDate = (Calendar)toDate.clone();
        fromDate.set(Calendar.DAY_OF_MONTH, 1);

        putFromDate(context, fromDate);
        putToDate(context, toDate);
    }

    private static void prevDay(Calendar toDate, Map<String, String> context) {
        toDate.add(Calendar.DAY_OF_MONTH, -1);

        putFromDate(context, toDate);
        putToDate(context, toDate);
    }

    private static void nextMonth(Calendar toDate, Map<String, String> context) {
        Calendar fromDate = (Calendar)toDate.clone();
        fromDate.add(Calendar.DAY_OF_MONTH, 1);

        toDate.add(Calendar.MONTH, 1);

        putFromDate(context, fromDate);
        putToDate(context, toDate);
    }

    private static void nextWeek(Calendar fromDate, Calendar toDate, Map<String, String> context) {
        fromDate.add(Calendar.DAY_OF_MONTH, 7);
        toDate.add(Calendar.DAY_OF_MONTH, 7);

        putFromDate(context, fromDate);
        putToDate(context, toDate);
    }

    private static void nextDay(Calendar toDate, Map<String, String> context) throws ParseException {
        toDate.add(Calendar.DAY_OF_MONTH, 1);

        putFromDate(context, toDate);
        putToDate(context, toDate);
    }

    public static Map<String, String> initializeContext(TimeUnit timeUnit) throws ParseException {
        Calendar cal = Calendar.getInstance();

        Map<String, String> context = newContext();

        Parameters.TIME_UNIT.put(context, timeUnit.name());
        initDateInterval(cal, context);

        return timeUnit == TimeUnit.DAY ? Utils.prevDateInterval(context) : context;
    }

    public static Map<String, String> clone(Map<String, String> context) {
        Map<String, String> result = new HashMap<>(context.size());
        result.putAll(context);

        return result;
    }

    public static Map<String, String> newContext() {
        return new HashMap<>();
    }

    public static Properties readProperties(String resource) throws IOException {
        Properties properties = new Properties();

        try (InputStream in = new BufferedInputStream(new FileInputStream(new File(resource)))) {
            properties.load(in);
        }

        return properties;
    }

    public static String removeBracket(String value) {
        int beginIndex = value.startsWith("[") ? 1 : 0;
        int endIndex = value.endsWith("]") ? value.length() - 1 : value.length();

        return value.substring(beginIndex, endIndex);
    }

    /** @return all available filters from context */
    public static Set<MetricFilter> getAvailableFilters(Map<String, String> dayContext) {
        Set<MetricFilter> filters = new HashSet<>(3);

        for (MetricFilter filterKey : MetricFilter.values()) {
            if (dayContext.containsKey(filterKey.name())) {
                filters.add(filterKey);
            }
        }

        return filters;
    }

    public static Map<String, String> cloneAndClearFilters(Map<String, String> context) {
        context = Utils.clone(context);
        for (MetricFilter filter : MetricFilter.values()) {
            filter.remove(context);
        }

        return context;
    }

    /** @return true if user's name represent registered user */
    public static boolean isRegisteredUser(String user) {
        return !user.toUpperCase().startsWith("ANONYMOUSUSER_");
    }
}
