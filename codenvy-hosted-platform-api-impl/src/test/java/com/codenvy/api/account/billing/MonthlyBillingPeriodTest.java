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

import static org.testng.Assert.assertEquals;

import com.codenvy.api.account.billing.MonthlyBillingPeriod;

import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@Listeners(MockitoTestNGListener.class)
public class MonthlyBillingPeriodTest {

    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
    MonthlyBillingPeriod monthlyBillingPeriod;

    @BeforeMethod
    public void setUp() {
        monthlyBillingPeriod = Mockito.spy(new MonthlyBillingPeriod());

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 1982);
        calendar.set(Calendar.MONTH, 5);
        calendar.set(Calendar.DAY_OF_MONTH, 6);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 13);
        calendar.set(Calendar.SECOND, 18);
        calendar.set(Calendar.MILLISECOND, 564);
        Mockito.when(monthlyBillingPeriod.now()).thenReturn(calendar);
    }

    @Test
    public void shouldReturnPreviousPeriodStartDate() throws Exception {
        assertEquals(monthlyBillingPeriod.getPreviousPeriodStartDate(),
                     df.parse("1982-05-01 00:00:00:000"));

    }

    @Test
    public void shouldReturnPreviousPeriodEndDate() throws Exception {
        assertEquals(monthlyBillingPeriod.getPreviousPeriodEndDate(),
                     df.parse("1982-05-31 23:59:59:999"));
    }

    @Test
    public void shouldReturnCurrentPeriodStartDate() throws Exception {
        assertEquals(monthlyBillingPeriod.getCurrentPeriodStartDate(),
                     df.parse("1982-06-01 00:00:00:000"));
    }

    @Test
    public void shouldReturnCurrentPeriodEndDate() throws Exception {
        assertEquals(monthlyBillingPeriod.getCurrentPeriodEndDate(),
                     df.parse("1982-06-30 23:59:59:999"));
    }

    @Test
    public void shouldReturnCurrentPeriodId() throws Exception {
        assertEquals(monthlyBillingPeriod.getCurrentPeriodId(), "1982-06");
    }

    @Test
    public void shouldReturnPreviousPeriodId() throws Exception {
        assertEquals(monthlyBillingPeriod.getPreviousPeriodId(), "1982-05");
    }
}