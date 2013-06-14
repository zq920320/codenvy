/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestTimeIntrevalUtil {

    private final DateFormat df = new SimpleDateFormat(MetricParameter.PARAM_DATE_FORMAT);

    @Test
    public void testInitDateIntervalByDay() throws Exception {
        Map<String, String> context = new HashMap<String, String>();
        context.put(MetricParameter.TIME_UNIT.name(), TimeUnit.DAY.toString());

        Calendar date = Calendar.getInstance();
        date.setTime(df.parse("20130410"));

        Utils.initDateInterval(date, context);

        Assert.assertEquals(context.get(MetricParameter.FROM_DATE.name()), "20130410");
        Assert.assertEquals(context.get(MetricParameter.TO_DATE.name()), "20130410");

    }

    @Test
    public void testInitDateIntervalByWeek() throws Exception {
        Map<String, String> context = new HashMap<String, String>();
        context.put(MetricParameter.TIME_UNIT.name(), TimeUnit.WEEK.toString());

        Calendar date = Calendar.getInstance();
        date.setTime(df.parse("20130410"));

        Utils.initDateInterval(date, context);

        Assert.assertEquals(context.get(MetricParameter.FROM_DATE.name()), "20130407");
        Assert.assertEquals(context.get(MetricParameter.TO_DATE.name()), "20130413");

    }

    @Test
    public void testInitDateIntervalByMonth() throws Exception {
        Map<String, String> context = new HashMap<String, String>();
        context.put(MetricParameter.TIME_UNIT.name(), TimeUnit.MONTH.toString());

        Calendar date = Calendar.getInstance();
        date.setTime(df.parse("20130410"));

        Utils.initDateInterval(date, context);

        Assert.assertEquals(context.get(MetricParameter.FROM_DATE.name()), "20130401");
        Assert.assertEquals(context.get(MetricParameter.TO_DATE.name()), "20130430");

    }

    @Test
    public void testNextDay() throws Exception {
        Map<String, String> context = new HashMap<String, String>();
        context.put(MetricParameter.FROM_DATE.name(), "20130331");
        context.put(MetricParameter.TO_DATE.name(), "20130331");
        context.put(MetricParameter.TIME_UNIT.name(), TimeUnit.DAY.toString());

        context = Utils.nextDateInterval(context);

        Assert.assertEquals(context.get(MetricParameter.FROM_DATE.name()), "20130401");
        Assert.assertEquals(context.get(MetricParameter.TO_DATE.name()), "20130401");
    }

    @Test
    public void testNextWeek() throws Exception {
        Map<String, String> context = new HashMap<String, String>();
        context.put(MetricParameter.FROM_DATE.name(), "20130429");
        context.put(MetricParameter.TO_DATE.name(), "20130505");
        context.put(MetricParameter.TIME_UNIT.name(), TimeUnit.WEEK.toString());

        context = Utils.nextDateInterval(context);

        Assert.assertEquals(context.get(MetricParameter.FROM_DATE.name()), "20130506");
        Assert.assertEquals(context.get(MetricParameter.TO_DATE.name()), "20130512");
    }

    @Test
    public void testNextMonth() throws Exception {
        Map<String, String> context = new HashMap<String, String>();
        context.put(MetricParameter.FROM_DATE.name(), "20130301");
        context.put(MetricParameter.TO_DATE.name(), "20130331");
        context.put(MetricParameter.TIME_UNIT.name(), TimeUnit.MONTH.toString());

        context = Utils.nextDateInterval(context);

        Assert.assertEquals(context.get(MetricParameter.FROM_DATE.name()), "20130401");
        Assert.assertEquals(context.get(MetricParameter.TO_DATE.name()), "20130430");
    }

    @Test
    public void testPrevDay() throws Exception {
        Map<String, String> context = new HashMap<String, String>();
        context.put(MetricParameter.FROM_DATE.name(), "20130331");
        context.put(MetricParameter.TO_DATE.name(), "20130331");
        context.put(MetricParameter.TIME_UNIT.name(), TimeUnit.DAY.toString());

        context = Utils.prevDateInterval(context);

        Assert.assertEquals(context.get(MetricParameter.FROM_DATE.name()), "20130330");
        Assert.assertEquals(context.get(MetricParameter.TO_DATE.name()), "20130330");
    }

    @Test
    public void testPrevWeek() throws Exception {
        Map<String, String> context = new HashMap<String, String>();
        context.put(MetricParameter.FROM_DATE.name(), "20130429");
        context.put(MetricParameter.TO_DATE.name(), "20130505");
        context.put(MetricParameter.TIME_UNIT.name(), TimeUnit.WEEK.toString());

        context = Utils.prevDateInterval(context);

        Assert.assertEquals(context.get(MetricParameter.FROM_DATE.name()), "20130422");
        Assert.assertEquals(context.get(MetricParameter.TO_DATE.name()), "20130428");
    }

    @Test
    public void testPrevMonth() throws Exception {
        Map<String, String> context = new HashMap<String, String>();
        context.put(MetricParameter.FROM_DATE.name(), "20130301");
        context.put(MetricParameter.TO_DATE.name(), "20130331");
        context.put(MetricParameter.TIME_UNIT.name(), TimeUnit.MONTH.toString());

        context = Utils.prevDateInterval(context);

        Assert.assertEquals(context.get(MetricParameter.FROM_DATE.name()), "20130201");
        Assert.assertEquals(context.get(MetricParameter.TO_DATE.name()), "20130228");
    }
}
