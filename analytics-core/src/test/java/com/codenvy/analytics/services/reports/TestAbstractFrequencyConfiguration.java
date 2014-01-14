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
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.metrics.Parameters;

import org.testng.annotations.Test;

import java.util.Map;

import static com.mongodb.util.MyAsserts.assertFalse;
import static com.mongodb.util.MyAsserts.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

/** @author Anatoliy Bazko */
public class TestAbstractFrequencyConfiguration extends BaseTest {

    @Test
    public void testDailyFrequencyConfiguration() throws Exception {
        DailyFrequencyConfiguration conf = new DailyFrequencyConfiguration();

        assertEquals(conf.getTimeUnit(), Parameters.TimeUnit.DAY);

        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130930");
        Parameters.TO_DATE.put(context, "20130930");

        assertTrue(conf.isAppropriateDateToSendReport(context));

        context = conf.initContext(context);
        assertEquals(Parameters.FROM_DATE.get(context), "20130929");
        assertEquals(Parameters.TO_DATE.get(context), "20130929");
    }

    @Test
    public void testWeeklyFrequencyConfiguration() throws Exception {
        WeeklyFrequencyConfiguration conf = new WeeklyFrequencyConfiguration();

        assertEquals(conf.getTimeUnit(), Parameters.TimeUnit.WEEK);

        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20140111");
        Parameters.TO_DATE.put(context, "20140111");

        assertFalse(conf.isAppropriateDateToSendReport(context));

        Parameters.FROM_DATE.put(context, "20140112");
        Parameters.TO_DATE.put(context, "20140112");

        assertTrue(conf.isAppropriateDateToSendReport(context));

        context = conf.initContext(context);
        assertEquals(Parameters.FROM_DATE.get(context), "20140105");
        assertEquals(Parameters.TO_DATE.get(context), "20140111");
    }

    @Test
    public void testMonthlyFrequencyConfiguration() throws Exception {
        MonthlyFrequencyConfiguration conf = new MonthlyFrequencyConfiguration();

        assertEquals(conf.getTimeUnit(), Parameters.TimeUnit.MONTH);

        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20140102");
        Parameters.TO_DATE.put(context, "20140102");

        assertFalse(conf.isAppropriateDateToSendReport(context));

        Parameters.FROM_DATE.put(context, "20140101");
        Parameters.TO_DATE.put(context, "20140101");

        assertTrue(conf.isAppropriateDateToSendReport(context));

        context = conf.initContext(context);
        assertEquals(Parameters.FROM_DATE.get(context), "20131201");
        assertEquals(Parameters.TO_DATE.get(context), "20131231");
    }
}
