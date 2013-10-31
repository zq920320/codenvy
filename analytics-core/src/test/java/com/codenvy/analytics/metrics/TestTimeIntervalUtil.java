/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
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


package com.codenvy.analytics.metrics;


import com.codenvy.analytics.Utils;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestTimeIntervalUtil {

    private final DateFormat df = new SimpleDateFormat(Parameters.PARAM_DATE_FORMAT);

    @Test
    public void testInitDateIntervalByDay() throws Exception {
        Map<String, String> context = new HashMap<String, String>();
        context.put(Parameters.TIME_UNIT.name(), Parameters.TimeUnit.DAY.toString());

        Calendar date = Calendar.getInstance();
        date.setTime(df.parse("20130410"));

        Utils.initDateInterval(date, context);

        Assert.assertEquals(context.get(Parameters.FROM_DATE.name()), "20130410");
        Assert.assertEquals(context.get(Parameters.TO_DATE.name()), "20130410");

    }

    @Test
    public void testInitDateIntervalByWeek() throws Exception {
        Map<String, String> context = new HashMap<String, String>();
        context.put(Parameters.TIME_UNIT.name(), Parameters.TimeUnit.WEEK.toString());

        Calendar date = Calendar.getInstance();
        date.setTime(df.parse("20130410"));

        Utils.initDateInterval(date, context);

        Assert.assertEquals(context.get(Parameters.FROM_DATE.name()), "20130407");
        Assert.assertEquals(context.get(Parameters.TO_DATE.name()), "20130413");

    }

    @Test
    public void testInitDateIntervalByMonth() throws Exception {
        Map<String, String> context = new HashMap<String, String>();
        context.put(Parameters.TIME_UNIT.name(), Parameters.TimeUnit.MONTH.toString());

        Calendar date = Calendar.getInstance();
        date.setTime(df.parse("20130410"));

        Utils.initDateInterval(date, context);

        Assert.assertEquals(context.get(Parameters.FROM_DATE.name()), "20130401");
        Assert.assertEquals(context.get(Parameters.TO_DATE.name()), "20130430");

    }

    @Test
    public void testNextDay() throws Exception {
        Map<String, String> context = new HashMap<String, String>();
        context.put(Parameters.FROM_DATE.name(), "20130331");
        context.put(Parameters.TO_DATE.name(), "20130331");
        context.put(Parameters.TIME_UNIT.name(), Parameters.TimeUnit.DAY.toString());

        context = Utils.nextDateInterval(context);

        Assert.assertEquals(context.get(Parameters.FROM_DATE.name()), "20130401");
        Assert.assertEquals(context.get(Parameters.TO_DATE.name()), "20130401");
    }

    @Test
    public void testNextWeek() throws Exception {
        Map<String, String> context = new HashMap<String, String>();
        context.put(Parameters.FROM_DATE.name(), "20130429");
        context.put(Parameters.TO_DATE.name(), "20130505");
        context.put(Parameters.TIME_UNIT.name(), Parameters.TimeUnit.WEEK.toString());

        context = Utils.nextDateInterval(context);

        Assert.assertEquals(context.get(Parameters.FROM_DATE.name()), "20130506");
        Assert.assertEquals(context.get(Parameters.TO_DATE.name()), "20130512");
    }

    @Test
    public void testNextMonth() throws Exception {
        Map<String, String> context = new HashMap<String, String>();
        context.put(Parameters.FROM_DATE.name(), "20130301");
        context.put(Parameters.TO_DATE.name(), "20130331");
        context.put(Parameters.TIME_UNIT.name(), Parameters.TimeUnit.MONTH.toString());

        context = Utils.nextDateInterval(context);

        Assert.assertEquals(context.get(Parameters.FROM_DATE.name()), "20130401");
        Assert.assertEquals(context.get(Parameters.TO_DATE.name()), "20130430");
    }

    @Test
    public void testPrevDay() throws Exception {
        Map<String, String> context = new HashMap<String, String>();
        context.put(Parameters.FROM_DATE.name(), "20130331");
        context.put(Parameters.TO_DATE.name(), "20130331");
        context.put(Parameters.TIME_UNIT.name(), Parameters.TimeUnit.DAY.toString());

        context = Utils.prevDateInterval(context);

        Assert.assertEquals(context.get(Parameters.FROM_DATE.name()), "20130330");
        Assert.assertEquals(context.get(Parameters.TO_DATE.name()), "20130330");
    }

    @Test
    public void testPrevWeek() throws Exception {
        Map<String, String> context = new HashMap<String, String>();
        context.put(Parameters.FROM_DATE.name(), "20130429");
        context.put(Parameters.TO_DATE.name(), "20130505");
        context.put(Parameters.TIME_UNIT.name(), Parameters.TimeUnit.WEEK.toString());

        context = Utils.prevDateInterval(context);

        Assert.assertEquals(context.get(Parameters.FROM_DATE.name()), "20130422");
        Assert.assertEquals(context.get(Parameters.TO_DATE.name()), "20130428");
    }

    @Test
    public void testPrevMonth() throws Exception {
        Map<String, String> context = new HashMap<String, String>();
        context.put(Parameters.FROM_DATE.name(), "20130301");
        context.put(Parameters.TO_DATE.name(), "20130331");
        context.put(Parameters.TIME_UNIT.name(), Parameters.TimeUnit.MONTH.toString());

        context = Utils.prevDateInterval(context);

        Assert.assertEquals(context.get(Parameters.FROM_DATE.name()), "20130201");
        Assert.assertEquals(context.get(Parameters.TO_DATE.name()), "20130228");
    }
}
