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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DailyBillingPeriodTest {

    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
    DailyBillingPeriod dailyBillingPeriod;

    Date testDate;

    @BeforeMethod
    public void setUp() throws ParseException {
        testDate = df.parse("1982-06-06 23:13:18:564");
        dailyBillingPeriod = new DailyBillingPeriod();

    }

    @Test
    public void shouldReturnPreviousPeriodStartDate() throws Exception {
        assertEquals(dailyBillingPeriod.get(testDate).getPreviousPeriod().getStartDate(),
                     df.parse("1982-06-05 00:00:00:000"));

    }

    @Test
    public void shouldReturnPreviousPeriodEndDate() throws Exception {
        assertEquals(dailyBillingPeriod.get(testDate).getPreviousPeriod().getEndDate(),
                     df.parse("1982-06-05 23:59:59:999"));
    }

    @Test
    public void shouldReturnCurrentPeriodStartDate() throws Exception {
        assertEquals(dailyBillingPeriod.get(testDate).getStartDate(),
                     df.parse("1982-06-06 00:00:00:000"));
    }

    @Test
    public void shouldReturnCurrentPeriodEndDate() throws Exception {
        assertEquals(dailyBillingPeriod.get(testDate).getEndDate(),
                     df.parse("1982-06-06 23:59:59:999"));
    }

    @Test
    public void shouldReturnCurrentPeriodId() throws Exception {
        assertEquals(dailyBillingPeriod.get(testDate).getId(), "1982-06-06");
    }

    @Test
    public void shouldReturnPreviousPeriodId() throws Exception {
        assertEquals(dailyBillingPeriod.get(testDate).getPreviousPeriod().getId(), "1982-06-05");
    }

    @Test
    public void shouldReturnNextPeriodId() throws Exception {
        assertEquals(dailyBillingPeriod.get(testDate).getNextPeriod().getId(), "1982-06-07");
    }

    @Test
    public void shouldReturnPeriodById() throws Exception {
        //given
        Period expected = dailyBillingPeriod.get(testDate);
        //when
        Period actual = dailyBillingPeriod.get("1982-06-06");
        //then
        assertEquals(actual, expected);
        assertEquals(actual.getId(), expected.getId());
        assertEquals(actual.getStartDate(), expected.getStartDate());
        assertEquals(actual.getEndDate(), expected.getEndDate());
        assertEquals(actual.getNextPeriod(), expected.getNextPeriod());
        assertEquals(actual.getPreviousPeriod(), expected.getPreviousPeriod());

    }
}