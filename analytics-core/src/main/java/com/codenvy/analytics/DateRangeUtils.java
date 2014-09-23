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

import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Parameters;

import org.joda.time.LocalDate;

import java.util.Calendar;

import static org.joda.time.Days.daysBetween;
import static org.joda.time.Months.monthsBetween;
import static org.joda.time.Weeks.weeksBetween;

/** @author Dmytro Nochevnov */
public final class DateRangeUtils {
    private DateRangeUtils() {
    }

    /** @return date of first day of time unit containing date. */
    public static Calendar getFirstDayOfPeriod(Parameters.TimeUnit timeUnit, Calendar date) {
        date = (Calendar)date.clone();

        switch (timeUnit) {
            case DAY:
                return date;

            case WEEK:
                date.set(Calendar.DAY_OF_WEEK, date.getFirstDayOfWeek());
                return date;

            case MONTH:
                date.set(Calendar.DAY_OF_MONTH, 1);
                return date;

            default:
                throw new IllegalArgumentException("Illegal TimeUnit: " + timeUnit);
        }
    }

    /**
     * Returns number of units between the dates, taking into account incomplete weeks and months.
     */
    public static int getNumberOfUnitsBetweenDates(Parameters.TimeUnit timeUnit, Calendar fromDate, Calendar toDate) {
        toDate = (Calendar)toDate.clone();
        fromDate = (Calendar)fromDate.clone();

        switch (timeUnit) {
            case DAY:
                return daysBetween(new LocalDate(fromDate), new LocalDate(toDate)).getDays() + 1;  // add one for last day

            case WEEK:
                fromDate.set(Calendar.DAY_OF_WEEK, 1);
                toDate.set(Calendar.DAY_OF_WEEK, toDate.getActualMaximum(Calendar.DAY_OF_WEEK));

                return weeksBetween(new LocalDate(fromDate), new LocalDate(toDate)).getWeeks() + 1;

            case MONTH:
                fromDate.set(Calendar.DAY_OF_MONTH, 1);
                toDate.set(Calendar.DAY_OF_MONTH, toDate.getActualMaximum(Calendar.DAY_OF_MONTH));

                return monthsBetween(new LocalDate(fromDate), new LocalDate(toDate)).getMonths() + 1;

            case LIFETIME:
                return 1;

            default:
                throw new IllegalArgumentException("Illegal TimeUnit: " + timeUnit);
        }
    }

    public static boolean isCustomDateRange(Context context) {
        return context.exists(Parameters.IS_CUSTOM_DATE_RANGE)
               && context.exists(Parameters.TIME_UNIT)
               && context.getTimeUnit() != Parameters.TimeUnit.LIFETIME;
    }
}
