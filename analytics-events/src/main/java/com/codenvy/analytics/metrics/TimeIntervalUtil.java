/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptExecutor;
import com.codenvy.analytics.scripts.ScriptParameters;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Map;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TimeIntervalUtil {

    /**
     * 
     */
    public static void initDateInterval(Calendar date, Map<String, String> context) throws IOException,
                                                                                       ParseException,
                                                                                       IllegalArgumentException {

        TimeUnit timeUnit = TimeUnit.valueOf(context.get(ScriptParameters.TIME_UNIT.getName()));

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
            default:
                throw new IllegalArgumentException("Illegal time unit " + timeUnit);
        }
    }

    private static void initByWeek(Calendar date, Map<String, String> context) {
        Calendar fromDate = (Calendar)date.clone();
        fromDate.add(Calendar.DAY_OF_MONTH, fromDate.getActualMinimum(Calendar.DAY_OF_WEEK) - fromDate.get(Calendar.DAY_OF_WEEK));

        Calendar toDate = (Calendar)date.clone();
        toDate.add(Calendar.DAY_OF_MONTH, toDate.getActualMaximum(Calendar.DAY_OF_WEEK) - toDate.get(Calendar.DAY_OF_WEEK));

        putInContext(fromDate, toDate, context);
    }

    private static void initByMonth(Calendar date, Map<String, String> context) {
        Calendar fromDate = (Calendar)date.clone();
        fromDate.set(Calendar.DAY_OF_MONTH, 1);

        Calendar toDate = (Calendar)date.clone();
        toDate.set(Calendar.DAY_OF_MONTH, toDate.getActualMaximum(Calendar.DAY_OF_MONTH));

        putInContext(fromDate, toDate, context);
    }

    private static void initByDay(Calendar date, Map<String, String> context) {
        putInContext(date, date, context);
    }

    /**
     * Shift date interval forward depending on {@link TimeUnit}. Should also be placed in context.
     */
    public static void nextDateInterval(Map<String, String> context) throws IOException, ParseException, IllegalArgumentException {
        String fromDateParam = context.get(ScriptParameters.FROM_DATE.getName());
        String toDateParam = context.get(ScriptParameters.TO_DATE.getName());

        if (fromDateParam == null || toDateParam == null) {
            throw new IOException("Parameters " + ScriptParameters.FROM_DATE + " or " + ScriptParameters.TO_DATE
                                  + " were not found in context");
        }

        Calendar toDate = Calendar.getInstance();
        Calendar fromDate = Calendar.getInstance();

        toDate.setTime(ScriptExecutor.PARAM_DATE_FORMAT.parse(toDateParam));
        fromDate.setTime(ScriptExecutor.PARAM_DATE_FORMAT.parse(fromDateParam));

        TimeUnit timeUnit = TimeUnit.valueOf(context.get(ScriptParameters.TIME_UNIT.getName()));

        switch (timeUnit) {
            case DAY:
                nextDay(fromDate, toDate, context);
                break;
            case WEEK:
                nextWeek(fromDate, toDate, context);
                break;
            case MONTH:
                nextMonth(fromDate, toDate, context);
                break;
            default:
                throw new IllegalArgumentException("Illegal time unit " + timeUnit);
        }
    }

    /**
     * Shift date interval backward depending on {@link TimeUnit}. Should also be placed in context.
     */
    public static void prevDateInterval(Map<String, String> context) throws IOException, ParseException, IllegalArgumentException {
        String fromDateParam = context.get(ScriptParameters.FROM_DATE.getName());
        String toDateParam = context.get(ScriptParameters.TO_DATE.getName());

        if (fromDateParam == null || toDateParam == null) {
            throw new IOException("Parameters " + ScriptParameters.FROM_DATE + " or " + ScriptParameters.TO_DATE
                                  + " were not found in context");
        }

        Calendar toDate = Calendar.getInstance();
        Calendar fromDate = Calendar.getInstance();

        toDate.setTime(ScriptExecutor.PARAM_DATE_FORMAT.parse(toDateParam));
        fromDate.setTime(ScriptExecutor.PARAM_DATE_FORMAT.parse(fromDateParam));

        TimeUnit timeUnit = TimeUnit.valueOf(context.get(ScriptParameters.TIME_UNIT.getName()));

        switch (timeUnit) {
            case DAY:
                prevDay(fromDate, toDate, context);
                break;
            case WEEK:
                prevWeek(fromDate, toDate, context);
                break;
            case MONTH:
                prevMonth(fromDate, toDate, context);
                break;
            default:
                throw new IllegalArgumentException("Illegal time unit " + timeUnit);
        }
    }

    private static void prevWeek(Calendar fromDate, Calendar toDate, Map<String, String> context) {
        fromDate.add(Calendar.DAY_OF_MONTH, -7);
        toDate.add(Calendar.DAY_OF_MONTH, -7);
        putInContext(fromDate, toDate, context);
    }

    private static void prevMonth(Calendar fromDate, Calendar toDate, Map<String, String> context) {
        toDate = (Calendar)fromDate.clone();
        toDate.add(Calendar.DAY_OF_MONTH, -1);

        fromDate = (Calendar)toDate.clone();
        fromDate.set(Calendar.DAY_OF_MONTH, 1);

        putInContext(fromDate, toDate, context);
    }

    private static void prevDay(Calendar fromDate, Calendar toDate, Map<String, String> context) {
        toDate.add(Calendar.DAY_OF_MONTH, -1);
        putInContext(toDate, toDate, context);
    }

    private static void nextMonth(Calendar fromDate, Calendar toDate, Map<String, String> context) {
        fromDate = (Calendar)toDate.clone();
        fromDate.add(Calendar.DAY_OF_MONTH, 1);

        toDate.add(Calendar.MONTH, 1);

        putInContext(fromDate, toDate, context);
    }

    private static void nextWeek(Calendar fromDate, Calendar toDate, Map<String, String> context) {
        fromDate.add(Calendar.DAY_OF_MONTH, 7);
        toDate.add(Calendar.DAY_OF_MONTH, 7);
        putInContext(fromDate, toDate, context);
    }

    private static void nextDay(Calendar fromDate, Calendar toDate, Map<String, String> context) throws IOException {
        toDate.add(Calendar.DAY_OF_MONTH, 1);
        putInContext(toDate, toDate, context);
    }

    private static void putInContext(Calendar fromDate, Calendar toDate, Map<String, String> context) {
        context.put(ScriptParameters.FROM_DATE.getName(), ScriptExecutor.PARAM_DATE_FORMAT.format(fromDate.getTime()));
        context.put(ScriptParameters.TO_DATE.getName(), ScriptExecutor.PARAM_DATE_FORMAT.format(toDate.getTime()));
    }
}
