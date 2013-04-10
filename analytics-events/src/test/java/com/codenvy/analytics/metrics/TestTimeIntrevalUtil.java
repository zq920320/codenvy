/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptExecutor;
import com.codenvy.analytics.scripts.ScriptParameters;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestTimeIntrevalUtil {


    @Test
    public void testInitDateIntervalByDay() throws Exception {
        Map<String, String> context = new HashMap<String, String>();
        context.put(ScriptParameters.TIME_UNIT.getName(), TimeUnit.DAY.toString());

        Calendar date = Calendar.getInstance();
        date.setTime(ScriptExecutor.PARAM_DATE_FORMAT.parse("20130410"));

        TimeIntervalUtil.initDateInterval(date, context);

        Assert.assertEquals(context.get(ScriptParameters.FROM_DATE.getName()), "20130410");
        Assert.assertEquals(context.get(ScriptParameters.TO_DATE.getName()), "20130410");

    }

    @Test
    public void testInitDateIntervalByWeek() throws Exception {
        Map<String, String> context = new HashMap<String, String>();
        context.put(ScriptParameters.TIME_UNIT.getName(), TimeUnit.WEEK.toString());

        Calendar date = Calendar.getInstance();
        date.setTime(ScriptExecutor.PARAM_DATE_FORMAT.parse("20130410"));

        TimeIntervalUtil.initDateInterval(date, context);

        Assert.assertEquals(context.get(ScriptParameters.FROM_DATE.getName()), "20130407");
        Assert.assertEquals(context.get(ScriptParameters.TO_DATE.getName()), "20130413");

    }

    @Test
    public void testInitDateIntervalByMonth() throws Exception {
        Map<String, String> context = new HashMap<String, String>();
        context.put(ScriptParameters.TIME_UNIT.getName(), TimeUnit.MONTH.toString());

        Calendar date = Calendar.getInstance();
        date.setTime(ScriptExecutor.PARAM_DATE_FORMAT.parse("20130410"));

        TimeIntervalUtil.initDateInterval(date, context);

        Assert.assertEquals(context.get(ScriptParameters.FROM_DATE.getName()), "20130401");
        Assert.assertEquals(context.get(ScriptParameters.TO_DATE.getName()), "20130430");

    }

    @Test
    public void testNextDay() throws Exception {
        Map<String, String> context = new HashMap<String, String>();
        context.put(ScriptParameters.FROM_DATE.getName(), "20130331");
        context.put(ScriptParameters.TO_DATE.getName(), "20130331");
        context.put(ScriptParameters.TIME_UNIT.getName(), TimeUnit.DAY.toString());

        TimeIntervalUtil.nextDateInterval(context);

        Assert.assertEquals(context.get(ScriptParameters.FROM_DATE.getName()), "20130401");
        Assert.assertEquals(context.get(ScriptParameters.TO_DATE.getName()), "20130401");
    }

    @Test
    public void testNextWeek() throws Exception {
        Map<String, String> context = new HashMap<String, String>();
        context.put(ScriptParameters.FROM_DATE.getName(), "20130429");
        context.put(ScriptParameters.TO_DATE.getName(), "20130505");
        context.put(ScriptParameters.TIME_UNIT.getName(), TimeUnit.WEEK.toString());

        TimeIntervalUtil.nextDateInterval(context);

        Assert.assertEquals(context.get(ScriptParameters.FROM_DATE.getName()), "20130506");
        Assert.assertEquals(context.get(ScriptParameters.TO_DATE.getName()), "20130512");
    }

    @Test
    public void testNextMonth() throws Exception {
        Map<String, String> context = new HashMap<String, String>();
        context.put(ScriptParameters.FROM_DATE.getName(), "20130301");
        context.put(ScriptParameters.TO_DATE.getName(), "20130331");
        context.put(ScriptParameters.TIME_UNIT.getName(), TimeUnit.MONTH.toString());

        TimeIntervalUtil.nextDateInterval(context);

        Assert.assertEquals(context.get(ScriptParameters.FROM_DATE.getName()), "20130401");
        Assert.assertEquals(context.get(ScriptParameters.TO_DATE.getName()), "20130430");
    }

    @Test
    public void testPrevDay() throws Exception {
        Map<String, String> context = new HashMap<String, String>();
        context.put(ScriptParameters.FROM_DATE.getName(), "20130331");
        context.put(ScriptParameters.TO_DATE.getName(), "20130331");
        context.put(ScriptParameters.TIME_UNIT.getName(), TimeUnit.DAY.toString());

        TimeIntervalUtil.prevDateInterval(context);

        Assert.assertEquals(context.get(ScriptParameters.FROM_DATE.getName()), "20130330");
        Assert.assertEquals(context.get(ScriptParameters.TO_DATE.getName()), "20130330");
    }

    @Test
    public void testPrevWeek() throws Exception {
        Map<String, String> context = new HashMap<String, String>();
        context.put(ScriptParameters.FROM_DATE.getName(), "20130429");
        context.put(ScriptParameters.TO_DATE.getName(), "20130505");
        context.put(ScriptParameters.TIME_UNIT.getName(), TimeUnit.WEEK.toString());

        TimeIntervalUtil.prevDateInterval(context);

        Assert.assertEquals(context.get(ScriptParameters.FROM_DATE.getName()), "20130422");
        Assert.assertEquals(context.get(ScriptParameters.TO_DATE.getName()), "20130428");
    }

    @Test
    public void testPrevMonth() throws Exception {
        Map<String, String> context = new HashMap<String, String>();
        context.put(ScriptParameters.FROM_DATE.getName(), "20130301");
        context.put(ScriptParameters.TO_DATE.getName(), "20130331");
        context.put(ScriptParameters.TIME_UNIT.getName(), TimeUnit.MONTH.toString());

        TimeIntervalUtil.prevDateInterval(context);

        Assert.assertEquals(context.get(ScriptParameters.FROM_DATE.getName()), "20130201");
        Assert.assertEquals(context.get(ScriptParameters.TO_DATE.getName()), "20130228");
    }
}
