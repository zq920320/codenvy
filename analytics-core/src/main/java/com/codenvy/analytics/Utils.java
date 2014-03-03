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


package com.codenvy.analytics;

import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.Parameters;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class Utils {

    private Utils() {
    }

    /**
     * Parse date represented by give string using {@link com.codenvy.analytics.metrics.Parameters#PARAM_DATE_FORMAT}
     * format. Wraps {@link
     * ParseException} into {@link IOException}.
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
    public static Parameters.TimeUnit getTimeUnit(Map<String, String> context) {
        return Parameters.TimeUnit.valueOf(Parameters.TIME_UNIT.get(context).toUpperCase());
    }

    /** @return fromDate value */
    public static Calendar getFromDate(Map<String, String> context) throws ParseException {
        return parseDate(Parameters.FROM_DATE.get(context));
    }

    public static boolean isDateFormat(String date) {
        return date.length() == Parameters.PARAM_DATE_FORMAT.length();
    }

    /** @return toDate value */
    public static Calendar getToDate(Map<String, String> context) throws ParseException {
        return parseDate(Parameters.TO_DATE.get(context));
    }

    public static Calendar getPrevToDate(Map<String, String> context) throws ParseException {
        Calendar toDate = parseDate(Parameters.TO_DATE.get(context));
        toDate.add(Calendar.DAY_OF_MONTH, -1);

        return toDate;
    }

    public static Calendar getReportDate(Map<String, String> context) throws ParseException {
        return parseDate(Parameters.REPORT_DATE.get(context));
    }

    /** Puts {@link Parameters#FROM_DATE} parameter into context. */
    public static void putFromDate(Map<String, String> context, Calendar fromDate) {
        Parameters.FROM_DATE.put(context, formatDate(fromDate));
    }

    /** Puts {@link Parameters#TO_DATE} parameter into context. */
    public static void putToDate(Map<String, String> context, Calendar toDate) {
        Parameters.TO_DATE.put(context, formatDate(toDate));
    }

    /** Puts {@link Parameters#TIME_UNIT} parameter into context. */
    public static void putTimeUnit(Map<String, String> context, Parameters.TimeUnit timeUnit) {
        Parameters.TIME_UNIT.put(context, timeUnit.name());
    }

    /** Initialize date interval accordingly to passed {@link Parameters#TIME_UNIT} */
    public static void initDateInterval(Calendar date, Map<String, String> context) throws ParseException {
        Parameters.TimeUnit timeUnit = getTimeUnit(context);

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

    /**
     * Shift date interval forward depending on {@link com.codenvy.analytics.metrics.Parameters.TimeUnit}. Should also
     * be placed in context.
     */
    public static Map<String, String> nextDateInterval(Map<String, String> context) throws ParseException {
        Map<String, String> resultContext = new HashMap<>(context);

        Calendar fromDate = getFromDate(context);
        Calendar toDate = getToDate(context);
        Parameters.TimeUnit timeUnit = getTimeUnit(context);

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

    /**
     * Shift date interval backward depending on {@link com.codenvy.analytics.metrics.Parameters.TimeUnit}. Should also
     * be placed in context.
     */
    public static Map<String, String> prevDateInterval(Map<String, String> context) throws ParseException {
        Map<String, String> resultContext = new HashMap<>(context);

        Calendar fromDate = getFromDate(context);
        Calendar toDate = getToDate(context);
        Parameters.TimeUnit timeUnit = getTimeUnit(context);

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

    public static Map<String, String> initializeContext(Parameters.TimeUnit timeUnit) throws ParseException {
        Calendar cal = Calendar.getInstance();

        Map<String, String> context = newContext();

        Parameters.TIME_UNIT.put(context, timeUnit.name());
        initDateInterval(cal, context);

        return timeUnit == Parameters.TimeUnit.DAY ? Utils.prevDateInterval(context) : context;
    }

    public static Map<String, String> clone(Map<String, String> context) {
        Map<String, String> result = new HashMap<>(context.size());
        result.putAll(context);

        return result;
    }

    public static Map<String, String> newContext() {
        return new HashMap<>();
    }


    /** Returns all filters existed in context. */
    public static Set<MetricFilter> getFilters(Map<String, String> context) {
        Set<MetricFilter> result = new HashSet<>();

        for (MetricFilter filter : MetricFilter.values()) {
            if (filter.exists(context)) {
                result.add(filter);
            }
        }

        return result;
    }

    public static boolean isSimpleContext(Map<String, String> context) {
        return !Parameters.SORT.exists(context) && !Parameters.PAGE.exists(context) && getFilters(context).isEmpty();
    }
}
