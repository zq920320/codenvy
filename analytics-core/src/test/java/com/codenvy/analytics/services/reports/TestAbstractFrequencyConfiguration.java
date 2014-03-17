/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2014] Codenvy, S.A.
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
package com.codenvy.analytics.services.reports;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Parameters;

import org.testng.annotations.Test;

import static com.mongodb.util.MyAsserts.assertFalse;
import static com.mongodb.util.MyAsserts.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

/** @author Anatoliy Bazko */
public class TestAbstractFrequencyConfiguration extends BaseTest {

    @Test
    public void testDailyFrequencyConfiguration() throws Exception {
        DailyFrequencyConfiguration conf = new DailyFrequencyConfiguration();

        assertEquals(conf.getTimeUnit(), Parameters.TimeUnit.DAY);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130930");
        builder.put(Parameters.TO_DATE, "20130930");

        assertTrue(conf.isAppropriateDateToSendReport(builder.build()));

        Context context = conf.initContext(builder.build());
        assertEquals(context.get(Parameters.FROM_DATE), "20130929");
        assertEquals(context.get(Parameters.TO_DATE), "20130929");
    }

    @Test
    public void testWeeklyFrequencyConfiguration() throws Exception {
        WeeklyFrequencyConfiguration conf = new WeeklyFrequencyConfiguration();

        assertEquals(conf.getTimeUnit(), Parameters.TimeUnit.WEEK);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20140111");
        builder.put(Parameters.TO_DATE, "20140111");

        assertFalse(conf.isAppropriateDateToSendReport(builder.build()));

        builder.put(Parameters.FROM_DATE, "20140112");
        builder.put(Parameters.TO_DATE, "20140112");

        assertTrue(conf.isAppropriateDateToSendReport(builder.build()));

        Context context = conf.initContext(builder.build());
        assertEquals(context.get(Parameters.FROM_DATE), "20140105");
        assertEquals(context.get(Parameters.TO_DATE), "20140111");
    }

    @Test
    public void testMonthlyFrequencyConfiguration() throws Exception {
        MonthlyFrequencyConfiguration conf = new MonthlyFrequencyConfiguration();

        assertEquals(conf.getTimeUnit(), Parameters.TimeUnit.MONTH);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20140102");
        builder.put(Parameters.TO_DATE, "20140102");

        assertFalse(conf.isAppropriateDateToSendReport(builder.build()));

        builder.put(Parameters.FROM_DATE, "20140101");
        builder.put(Parameters.TO_DATE, "20140101");

        assertTrue(conf.isAppropriateDateToSendReport(builder.build()));

        Context context = conf.initContext(builder.build());
        assertEquals(context.get(Parameters.FROM_DATE), "20131201");
        assertEquals(context.get(Parameters.TO_DATE), "20131231");
    }
}
