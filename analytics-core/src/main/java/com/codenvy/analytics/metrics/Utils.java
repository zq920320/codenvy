/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.MetricParameter.ENTITY_TYPE;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class Utils {
    
    private Utils() {
    }

    /**
     * Parse date represented by give string using {@link MetricParameter#PARAM_DATE_FORMAT} format. Wraps {@link ParseException} into
     * {@link IOException}.
     * 
     * @throws IOException if exception is occurred
     */
    public static Calendar parseDate(String date) throws IOException {
        try {
            DateFormat df = new SimpleDateFormat(MetricParameter.PARAM_DATE_FORMAT);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(df.parse(date));

            return calendar;
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

    /**
     * Formats date.
     */
    public static String formatDate(Calendar calendar, String format) {
        DateFormat df = new SimpleDateFormat(format);
        return df.format(calendar.getTime());
    }

    /**
     * Formats date using {@link MetricParameter#PARAM_DATE_FORMAT}.
     */
    public static String formatDate(Calendar calendar) {
        return formatDate(calendar.getTime());
    }

    /**
     * Formats date using {@link MetricParameter#PARAM_DATE_FORMAT}.
     */
    public static String formatDate(Date date) {
        DateFormat df = new SimpleDateFormat(MetricParameter.PARAM_DATE_FORMAT);
        return df.format(date);
    }

    /**
     * Extracts {@link MetricParameter#TIME_UNIT} parameter value from context.
     */
    public static TimeUnit getTimeUnit(Map<String, String> context) {
        return TimeUnit.valueOf(context.get(MetricParameter.TIME_UNIT.getName()));
    }

    public static boolean isTimeUnitDay(Map<String, String> context) {
        return getTimeUnitParam(context) != null && getTimeUnit(context) == TimeUnit.DAY;
    }

    /**
     * Extracts {@link MetricParameter#TIME_UNIT} parameter value from context.
     */
    public static String getTimeUnitParam(Map<String, String> context) {
        return context.get(MetricParameter.TIME_UNIT.getName());
    }

    /**
     * Extracts {@link MetricParameter#FROM_DATE} parameter value from context.
     */
    public static String getFromDateParam(Map<String, String> context) {
        return context.get(MetricParameter.FROM_DATE.getName());
    }

    /**
     * Extracts {@link MetricParameter#TO_DATE} parameter value from context.
     */
    public static String getToDateParam(Map<String, String> context) {
        return context.get(MetricParameter.TO_DATE.getName());
    }

    /**
     * @return fromDate value
     */
    public static Calendar getFromDate(Map<String, String> context) throws IOException {
        return parseDate(context.get(MetricParameter.FROM_DATE.getName()));
    }

    /**
     * @return toDate value
     */
    public static Calendar getToDate(Map<String, String> context) throws IOException {
        return parseDate(context.get(MetricParameter.TO_DATE.getName()));
    }

    /**
     * Puts {@link MetricParameter#FROM_DATE} parameter into context.
     */
    public static void putFromDate(Map<String, String> context, Calendar fromDate) {
        context.put(MetricParameter.FROM_DATE.getName(), formatDate(fromDate));
    }

    /**
     * Puts {@link MetricParameter#TO_DATE} parameter into context.
     */
    public static void putToDate(Map<String, String> context, Calendar toDate) {
        context.put(MetricParameter.TO_DATE.getName(), formatDate(toDate));
    }

    /**
     * Puts {@link MetricParameter#FROM_DATE} parameter into context.
     */
    public static void putFromDate(Map<String, String> context, Date fromDate) {
        context.put(MetricParameter.FROM_DATE.getName(), formatDate(fromDate));
    }

    /**
     * Puts {@link MetricParameter#TO_DATE} parameter into context.
     */
    public static void putToDate(Map<String, String> context, Date toDate) {
        context.put(MetricParameter.TO_DATE.getName(), formatDate(toDate));
    }

    /**
     * Puts {@link MetricParameter#TO_DATE} parameter into context.
     */
    public static void putTimeUnit(Map<String, String> context, TimeUnit timeUnit) {
        context.put(MetricParameter.TIME_UNIT.getName(), timeUnit.toString());
    }

    /** @param true if entry's key is {@link MetricParameter#TO_DATE} */
    public static boolean isToDateParam(Entry<String, String> entry) {
        return entry.getKey().equals(MetricParameter.TO_DATE.getName());
    }

    /** @param true if entry's key is {@link MetricParameter#ALIAS} */
    public static boolean isAlias(Entry<String, String> entry) {
        return entry.getKey().equals(MetricParameter.ALIAS.getName());
    }

    /** @param true if entry's key is {@link MetricParameter#FROM_DATE} */
    public static boolean isFromDateParam(Entry<String, String> entry) {
        return entry.getKey().equals(MetricParameter.FROM_DATE.getName());
    }

    /** @param true if context contains {@link MetricParameter#TO_DATE} */
    public static boolean containsToDateParam(Map<String, String> context) {
        return getToDateParam(context) != null;
    }

    /** @param true if context contains {@link MetricParameter#FROM_DATE} */
    public static boolean containsFromDateParam(Map<String, String> context) {
        return getFromDateParam(context) != null;
    }

    /**
     * Initialize date interval accordingly to passed {@link MetricParameter#TIME_UNIT}
     * 
     * @throws if any exception is occurred
     */
    public static void initDateInterval(Date date, Map<String, String> context) throws IOException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        initDateInterval(calendar, context);
    }

    /**
     * Initialize date interval accordingly to passed {@link MetricParameter#TIME_UNIT}
     * 
     * @throws if any exception is occurred
     */
    public static void initDateInterval(Calendar date, Map<String, String> context) throws IOException {
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
        }
    }

    private static void initByWeek(Calendar date, Map<String, String> context) {
        Calendar fromDate = (Calendar)date.clone();
        fromDate.add(Calendar.DAY_OF_MONTH, fromDate.getActualMinimum(Calendar.DAY_OF_WEEK) - fromDate.get(Calendar.DAY_OF_WEEK));

        Calendar toDate = (Calendar)date.clone();
        toDate.add(Calendar.DAY_OF_MONTH, toDate.getActualMaximum(Calendar.DAY_OF_WEEK) - toDate.get(Calendar.DAY_OF_WEEK));

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
     * Shift date interval forward depending on {@link TimeUnit}. Should also be placed in context.
     */
    public static Map<String, String> nextDateInterval(Map<String, String> context) throws IOException {
        Map<String, String> resultContext = new HashMap<String, String>(context);

        Calendar fromDate = getFromDate(context);
        Calendar toDate = getToDate(context);
        TimeUnit timeUnit = getTimeUnit(context);

        switch (timeUnit) {
            case DAY:
                nextDay(fromDate, toDate, resultContext);
                break;
            case WEEK:
                nextWeek(fromDate, toDate, resultContext);
                break;
            case MONTH:
                nextMonth(fromDate, toDate, resultContext);
                break;
        }

        return resultContext;
    }

    /**
     * Shift date interval backward depending on {@link TimeUnit}. Should also be placed in context.
     */
    public static Map<String, String> prevDateInterval(Map<String, String> context) throws IOException {
        Map<String, String> resultContext = new HashMap<String, String>(context);

        Calendar fromDate = getFromDate(context);
        Calendar toDate = getToDate(context);
        TimeUnit timeUnit = getTimeUnit(context);

        switch (timeUnit) {
            case DAY:
                prevDay(fromDate, toDate, resultContext);
                break;
            case WEEK:
                prevWeek(fromDate, toDate, resultContext);
                break;
            case MONTH:
                prevMonth(fromDate, toDate, resultContext);
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

    private static void prevMonth(Calendar fromDate, Calendar toDate, Map<String, String> context) {
        toDate = (Calendar)fromDate.clone();
        toDate.add(Calendar.DAY_OF_MONTH, -1);

        fromDate = (Calendar)toDate.clone();
        fromDate.set(Calendar.DAY_OF_MONTH, 1);

        putFromDate(context, fromDate);
        putToDate(context, toDate);
    }

    private static void prevDay(Calendar fromDate, Calendar toDate, Map<String, String> context) {
        toDate.add(Calendar.DAY_OF_MONTH, -1);

        putFromDate(context, toDate);
        putToDate(context, toDate);
    }

    private static void nextMonth(Calendar fromDate, Calendar toDate, Map<String, String> context) {
        fromDate = (Calendar)toDate.clone();
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

    private static void nextDay(Calendar fromDate, Calendar toDate, Map<String, String> context) throws IOException {
        toDate.add(Calendar.DAY_OF_MONTH, 1);

        putFromDate(context, toDate);
        putToDate(context, toDate);
    }

    public static Map<String, String> initializeContext(TimeUnit timeUnit, Date date) throws IOException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        Map<String, String> context = newContext();

        putTimeUnit(context, timeUnit);
        initDateInterval(cal, context);

        return timeUnit == TimeUnit.DAY ? context = Utils.prevDateInterval(context) : context;
    }

    public static Map<String, String> initializeContext(int interval) throws IOException {
        Map<String, String> context = newContext();
        context.put(MetricParameter.TO_DATE.getName(), MetricParameter.TO_DATE.getDefaultValue());

        Calendar calendar = getToDate(context);
        calendar.add(Calendar.DAY_OF_MONTH, -(interval - 1));

        putFromDate(context, calendar);
        return context;
    }

    public static Map<String, String> clone(Map<String, String> context) {
        Map<String, String> result = new HashMap<String, String>(context.size());
        result.putAll(context);

        return result;
    }

    public static Map<String, String> newContext() {
        return new HashMap<String, String>();
    }

    /**
     * Validates context. Throws {@link IllegalArgumentException} if something wrong.
     */
    public static void validate(Map<String, String> context, Metric metric) throws IllegalArgumentException {
        final String df = "yyyy MMM dd";

        for (MetricParameter parameter : metric.getParams()) {
            String name = parameter.getName();
            
            if (!context.containsKey(name)) {
                throw new IllegalArgumentException("Parameter " + name + " was not set.");
            }
            
            String value = context.get(name);

            switch (parameter) {
                case RESULT_DIR:
                    break;

                case ENTITY:
                    for (ENTITY_TYPE eType : ENTITY_TYPE.values()) {
                        if (eType.name().equals(value)) {
                            continue;
                        }
                    }

                    throw new IllegalArgumentException("The illegal entity parameter value " + value);

                case INTERVAL:
                    break;

                case TIME_UNIT:
                    TimeUnit.valueOf(value.toUpperCase());
                    break;

                case FROM_DATE:
                    try {
                        Calendar fromDate = Utils.parseDate(value);
                        Calendar minDate = Utils.parseDate(parameter.getDefaultValue());

                        if (fromDate.before(minDate)) {
                            throw new IllegalArgumentException("The illegal fromDate parameter value '"
                                                               + Utils.formatDate(fromDate, df)
                                                               + "' The lowest allowed date is '"
                                                               + Utils.formatDate(minDate, df)
                                                               + "'");
                        }
                    } catch (IOException e) {
                        throw new IllegalArgumentException("fromDate parameter has illegal format '" + value
                                                           + "' The only supported format is 'yyyyMMdd'");
                    }
                    break;

                case TO_DATE:
                    try {
                        Calendar toDate = Utils.parseDate(value);
                        Calendar maxDate = Utils.parseDate(parameter.getDefaultValue());

                        if (toDate.after(maxDate)) {
                            throw new IllegalArgumentException("The illegal toDate parameter value: '"
                                                               + Utils.formatDate(toDate, df)
                                                               + "' The higest allowed date is '"
                                                               + Utils.formatDate(maxDate, df)
                                                               + "'");

                        }

                        if (containsFromDateParam(context)) {
                            Calendar fromDate = getFromDate(context);
                            if (fromDate.after(toDate)) {
                                throw new IllegalArgumentException("The illegal toDate parameter value: '"
                                                                   + Utils.formatDate(toDate, df)
                                                                   + "'. Should be higher than fromDate parameter value: '"
                                                                   + Utils.formatDate(fromDate, df)
                                                                   + "'");
                            }
                        }

                    } catch (IOException e) {
                        throw new IllegalArgumentException("toDate parameter has illegal format '" + value
                                                           + "'. The only supported format is 'yyyyMMdd'");
                    }
                    break;

                default:
                    throw new IllegalArgumentException("Unknown parameter " + name);
            }
        }
    }
}
