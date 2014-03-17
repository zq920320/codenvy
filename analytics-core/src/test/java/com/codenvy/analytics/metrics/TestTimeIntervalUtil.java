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

import org.testng.annotations.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static org.testng.Assert.assertEquals;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestTimeIntervalUtil {

    private final DateFormat df = new SimpleDateFormat(Parameters.PARAM_DATE_FORMAT);

    @Test
    public void testInitDateIntervalByDay() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TIME_UNIT, Parameters.TimeUnit.DAY.toString());

        Calendar date = Calendar.getInstance();
        date.setTime(df.parse("20130410"));

        Context context = Utils.initDateInterval(date, builder);

        assertEquals(context.get(Parameters.FROM_DATE), "20130410");
        assertEquals(context.get(Parameters.TO_DATE), "20130410");

    }

    @Test
    public void testInitDateIntervalByWeek() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TIME_UNIT, Parameters.TimeUnit.WEEK.toString());

        Calendar date = Calendar.getInstance();
        date.setTime(df.parse("20130410"));

        Context context = Utils.initDateInterval(date, builder);

        assertEquals(context.get(Parameters.FROM_DATE), "20130407");
        assertEquals(context.get(Parameters.TO_DATE), "20130413");

    }

    @Test
    public void testInitDateIntervalByMonth() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TIME_UNIT, Parameters.TimeUnit.MONTH.toString());

        Calendar date = Calendar.getInstance();
        date.setTime(df.parse("20130410"));

        Context context = Utils.initDateInterval(date, builder);

        assertEquals(context.get(Parameters.FROM_DATE), "20130401");
        assertEquals(context.get(Parameters.TO_DATE), "20130430");

    }

    @Test
    public void testNextDay() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TIME_UNIT, Parameters.TimeUnit.DAY.toString());
        builder.put(Parameters.FROM_DATE, "20130331");
        builder.put(Parameters.TO_DATE, "20130331");

        Context context = Utils.nextDateInterval(builder);

        assertEquals(context.get(Parameters.FROM_DATE), "20130401");
        assertEquals(context.get(Parameters.TO_DATE), "20130401");
    }

    @Test
    public void testNextWeek() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TIME_UNIT, Parameters.TimeUnit.WEEK.toString());
        builder.put(Parameters.FROM_DATE, "20130429");
        builder.put(Parameters.TO_DATE, "20130505");

        Context context = Utils.nextDateInterval(builder);

        assertEquals(context.get(Parameters.FROM_DATE), "20130506");
        assertEquals(context.get(Parameters.TO_DATE), "20130512");
    }

    @Test
    public void testNextMonth() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TIME_UNIT, Parameters.TimeUnit.MONTH.toString());
        builder.put(Parameters.FROM_DATE, "20130301");
        builder.put(Parameters.TO_DATE, "20130331");

        Context context = Utils.nextDateInterval(builder);

        assertEquals(context.get(Parameters.FROM_DATE), "20130401");
        assertEquals(context.get(Parameters.TO_DATE), "20130430");
    }

    @Test
    public void testPrevDay() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TIME_UNIT, Parameters.TimeUnit.DAY.toString());
        builder.put(Parameters.FROM_DATE, "20130331");
        builder.put(Parameters.TO_DATE, "20130331");

        Context context = Utils.prevDateInterval(builder);

        assertEquals(context.get(Parameters.FROM_DATE), "20130330");
        assertEquals(context.get(Parameters.TO_DATE), "20130330");
    }

    @Test
    public void testPrevWeek() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TIME_UNIT, Parameters.TimeUnit.WEEK.toString());
        builder.put(Parameters.FROM_DATE, "20130429");
        builder.put(Parameters.TO_DATE, "20130505");

        Context context = Utils.prevDateInterval(builder);

        assertEquals(context.get(Parameters.FROM_DATE), "20130422");
        assertEquals(context.get(Parameters.TO_DATE), "20130428");
    }

    @Test
    public void testPrevMonth() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TIME_UNIT, Parameters.TimeUnit.MONTH.toString());
        builder.put(Parameters.FROM_DATE, "20130301");
        builder.put(Parameters.TO_DATE, "20130331");

        Context context = Utils.prevDateInterval(builder);

        assertEquals(context.get(Parameters.FROM_DATE), "20130201");
        assertEquals(context.get(Parameters.TO_DATE), "20130228");
    }
}
