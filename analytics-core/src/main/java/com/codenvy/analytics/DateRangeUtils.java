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
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.Weeks;

import javax.annotation.Nullable;
import java.util.Calendar;

/** @author Dmytro Nochevnov */
public final class DateRangeUtils {
    private DateRangeUtils() {

    }

    /**
     * @return date of first day of time unit containing toDate.
     */
    @Nullable
    public static Calendar getFirstDayDate(Parameters.TimeUnit timeUnit, Calendar toDate) {
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

    /**
     * Returns number of units between the dates, taking into account incomplete weeks and months.
     */
    public static int getUnitsAboveDates(Parameters.TimeUnit timeUnit, Calendar fromDate, Calendar toDate) {
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

    public static boolean isCustomDateRange(Context context) {
        return context.exists(Parameters.IS_CUSTOM_DATE_RANGE)
               && context.exists(Parameters.TIME_UNIT)
               && context.getTimeUnit() != Parameters.TimeUnit.LIFETIME;
    }
}
