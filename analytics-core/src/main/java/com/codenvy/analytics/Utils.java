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

import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Parameters;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class Utils {

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
    public static Context initDateInterval(Calendar date, Context.Builder builder) throws ParseException {
        Parameters.TimeUnit timeUnit = builder.getTimeUnit();

        switch (timeUnit) {
            case DAY:
                initByDay(date, builder);
                break;
            case WEEK:
                initByWeek(date, builder);
                break;
            case MONTH:
                initByMonth(date, builder);
                break;
            case LIFETIME:
                initByLifeTime(builder);
                break;
        }

        return builder.build();
    }

    private static void initByLifeTime(Context.Builder builder) {
        builder.putDefaultValue(Parameters.FROM_DATE);
        builder.putDefaultValue(Parameters.TO_DATE);
    }

    private static void initByWeek(Calendar date, Context.Builder builder) {
        Calendar fromDate = (Calendar)date.clone();
        fromDate.add(Calendar.DAY_OF_MONTH,
                     fromDate.getActualMinimum(Calendar.DAY_OF_WEEK) - fromDate.get(Calendar.DAY_OF_WEEK));

        Calendar toDate = (Calendar)date.clone();
        toDate.add(Calendar.DAY_OF_MONTH,
                   toDate.getActualMaximum(Calendar.DAY_OF_WEEK) - toDate.get(Calendar.DAY_OF_WEEK));

        builder.put(Parameters.FROM_DATE, fromDate);
        builder.put(Parameters.TO_DATE, toDate);
    }

    private static void initByMonth(Calendar date, Context.Builder builder) {
        Calendar fromDate = (Calendar)date.clone();
        fromDate.set(Calendar.DAY_OF_MONTH, 1);

        Calendar toDate = (Calendar)date.clone();
        toDate.set(Calendar.DAY_OF_MONTH, toDate.getActualMaximum(Calendar.DAY_OF_MONTH));

        builder.put(Parameters.FROM_DATE, fromDate);
        builder.put(Parameters.TO_DATE, toDate);
    }

    private static void initByDay(Calendar date, Context.Builder builder) {
        builder.put(Parameters.FROM_DATE, date);
        builder.put(Parameters.TO_DATE, date);
    }

    /**
     * Shift date interval forward depending on {@link com.codenvy.analytics.metrics.Parameters.TimeUnit}. Should also
     * be placed in context.
     */
    public static Context nextDateInterval(Context.Builder builder) throws ParseException {
        Calendar fromDate = builder.getAsDate(Parameters.FROM_DATE);
        Calendar toDate = builder.getAsDate(Parameters.TO_DATE);
        Parameters.TimeUnit timeUnit = builder.getTimeUnit();

        switch (timeUnit) {
            case DAY:
                nextDay(toDate, builder);
                break;
            case WEEK:
                nextWeek(fromDate, toDate, builder);
                break;
            case MONTH:
                nextMonth(toDate, builder);
                break;
            case LIFETIME:
                break;
        }

        return builder.build();
    }

    /**
     * Shift date interval backward depending on {@link com.codenvy.analytics.metrics.Parameters.TimeUnit}. Should also
     * be placed in context.
     */
    public static Context prevDateInterval(Context.Builder builder) throws ParseException {
        Calendar fromDate = builder.getAsDate(Parameters.FROM_DATE);
        Calendar toDate = builder.getAsDate(Parameters.TO_DATE);
        Parameters.TimeUnit timeUnit = builder.getTimeUnit();

        switch (timeUnit) {
            case DAY:
                prevDay(toDate, builder);
                break;
            case WEEK:
                prevWeek(fromDate, toDate, builder);
                break;
            case MONTH:
                prevMonth(fromDate, builder);
                break;
            case LIFETIME:
                break;
        }

        return builder.build();
    }

    private static void prevWeek(Calendar fromDate, Calendar toDate, Context.Builder builder) {
        fromDate.add(Calendar.DAY_OF_MONTH, -7);
        toDate.add(Calendar.DAY_OF_MONTH, -7);

        builder.put(Parameters.FROM_DATE, fromDate);
        builder.put(Parameters.TO_DATE, toDate);
    }

    private static void prevMonth(Calendar fromDate, Context.Builder builder) {
        Calendar toDate = (Calendar)fromDate.clone();
        toDate.add(Calendar.DAY_OF_MONTH, -1);

        fromDate = (Calendar)toDate.clone();
        fromDate.set(Calendar.DAY_OF_MONTH, 1);

        builder.put(Parameters.FROM_DATE, fromDate);
        builder.put(Parameters.TO_DATE, toDate);
    }

    private static void prevDay(Calendar toDate, Context.Builder builder) {
        toDate.add(Calendar.DAY_OF_MONTH, -1);

        builder.put(Parameters.FROM_DATE, toDate);
        builder.put(Parameters.TO_DATE, toDate);
    }

    private static void nextMonth(Calendar toDate, Context.Builder builder) {
        Calendar fromDate = (Calendar)toDate.clone();
        fromDate.add(Calendar.DAY_OF_MONTH, 1);

        toDate.add(Calendar.MONTH, 1);

        builder.put(Parameters.FROM_DATE, fromDate);
        builder.put(Parameters.TO_DATE, toDate);
    }

    private static void nextWeek(Calendar fromDate, Calendar toDate, Context.Builder builder) {
        fromDate.add(Calendar.DAY_OF_MONTH, 7);
        toDate.add(Calendar.DAY_OF_MONTH, 7);

        builder.put(Parameters.FROM_DATE, fromDate);
        builder.put(Parameters.TO_DATE, toDate);
    }

    private static void nextDay(Calendar toDate, Context.Builder builder) throws ParseException {
        toDate.add(Calendar.DAY_OF_MONTH, 1);

        builder.put(Parameters.FROM_DATE, toDate);
        builder.put(Parameters.TO_DATE, toDate);
    }

    public static Context initializeContext(Parameters.TimeUnit timeUnit) throws ParseException {
        Calendar cal = Calendar.getInstance();

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TIME_UNIT, timeUnit.name());

        initDateInterval(cal, builder);

        return timeUnit == Parameters.TimeUnit.DAY ? Utils.prevDateInterval(builder) : builder.build();
    }
}
