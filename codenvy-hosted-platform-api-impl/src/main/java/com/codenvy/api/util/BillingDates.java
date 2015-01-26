/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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
package com.codenvy.api.util;

import java.util.Calendar;
import java.util.Date;

/**
 * Helper to get different billing dates.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com) on 1/19/15.
 * @author Sergii Leschenko
 */
public class BillingDates {

    public static Date getPreviousPeriodStartDate() {
        final Calendar calendar = getCurrentMonthStart();
        calendar.add(Calendar.MONTH, -1);
        return calendar.getTime();
    }

    public static Date getPreviousPeriodEndDate() {
        return getCurrentMonthStart().getTime();
    }

    public static Date getCurrentPeriodStartDate() {
        return getCurrentMonthStart().getTime();
    }

    public static Date getCurrentPeriodEndDate() {
        final Calendar calendar = getCurrentMonthStart();
        calendar.add(Calendar.MONTH, 1);
        return calendar.getTime();
    }

    private static Calendar getCurrentMonthStart() {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }
}
