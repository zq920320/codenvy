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
package com.codenvy.api.account.billing;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 1 month billing period.
 *
 * @author Sergii Kabashniuk
 */
public class MonthlyBillingPeriod implements BillingPeriod {

    public final static SimpleDateFormat ID_FORMAT = new SimpleDateFormat("yyyy-MM", Locale.ENGLISH);

    @Override
    public Period getCurrent() {
        return get(new Date());
    }

    @Override
    public Period get(String id) {
        try {
            return get(ID_FORMAT.parse(id));
        } catch (ParseException e) {
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public Period get(final Date date) {
        return new Period() {
            @Override
            public Date getStartDate() {
                final Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(date.getTime());
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                return calendar.getTime();
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj) {
                    return true;
                }
                if (obj instanceof Period) {
                    return this.getId().equals(((Period)obj).getId());
                }
                return false;
            }

            @Override
            public Date getEndDate() {
                final Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(date.getTime());
                calendar.add(Calendar.MONTH, 1);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.add(Calendar.MILLISECOND, -1);
                return calendar.getTime();
            }

            @Override
            public Period getNextPeriod() {
                final Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(date.getTime());
                calendar.add(Calendar.MONTH, 1);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                return get(calendar.getTime());
            }

            @Override
            public Period getPreviousPeriod() {
                final Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(date.getTime());
                calendar.add(Calendar.MONTH, -1);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                return get(calendar.getTime());
            }


            @Override
            public String getId() {
                return ID_FORMAT.format(date.getTime());
            }
        };
    }


}
