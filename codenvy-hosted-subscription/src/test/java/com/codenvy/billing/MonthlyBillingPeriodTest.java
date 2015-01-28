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
package com.codenvy.billing;

import static org.testng.Assert.assertEquals;

import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Calendar;

@Listeners(MockitoTestNGListener.class)
public class MonthlyBillingPeriodTest {
    

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
    public void testGetPreviousPeriodStartDate() throws Exception {
        assertEquals(monthlyBillingPeriod.getPreviousPeriodStartDate(),
                     MonthlyBillingPeriod.ID_FORMAT.parse("1982-05"));

    }

    @Test
    public void testGetPreviousPeriodEndDate() throws Exception {
        assertEquals(monthlyBillingPeriod.getPreviousPeriodEndDate(),
                     MonthlyBillingPeriod.ID_FORMAT.parse("1982-06"));
    }

    @Test
    public void testGetCurrentPeriodStartDate() throws Exception {
        assertEquals(monthlyBillingPeriod.getPreviousPeriodEndDate(),
                     MonthlyBillingPeriod.ID_FORMAT.parse("1982-06"));
    }

    @Test
    public void testGetCurrentPeriodEndDate() throws Exception {
        assertEquals(monthlyBillingPeriod.getCurrentPeriodEndDate(),
                     MonthlyBillingPeriod.ID_FORMAT.parse("1982-07"));
    }

    @Test
    public void testGetCurrentPeriodId() throws Exception {
        assertEquals(monthlyBillingPeriod.getCurrentPeriodId(), "1982-06");
    }
}