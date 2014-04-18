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


import static org.testng.Assert.assertEquals;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.testng.annotations.Test;

import com.codenvy.analytics.Utils;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestTimeIntervalUtil {

    private final DateFormat df = new SimpleDateFormat(Parameters.PARAM_DATE_FORMAT);

    @Test
    public void testInitDateIntervalByDay() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TIME_UNIT, Parameters.TimeUnit.DAY.toString());

        Calendar date = Calendar.getInstance();
        date.setTime(df.parse("20130410"));

        Context context = Utils.initDateInterval(date, builder.getTimeUnit(), builder);

        assertEquals(context.getAsString(Parameters.FROM_DATE), "20130410");
        assertEquals(context.getAsString(Parameters.TO_DATE), "20130410");
    }

    @Test
    public void testInitDateIntervalByWeek() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TIME_UNIT, Parameters.TimeUnit.WEEK.toString());

        Calendar date = Calendar.getInstance();
        date.setTime(df.parse("20130410"));

        Context context = Utils.initDateInterval(date, builder.getTimeUnit(), builder);

        assertEquals(context.getAsString(Parameters.FROM_DATE), "20130407");
        assertEquals(context.getAsString(Parameters.TO_DATE), "20130413");
    }

    @Test
    public void testInitDateIntervalByMonth() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TIME_UNIT, Parameters.TimeUnit.MONTH.toString());

        Calendar date = Calendar.getInstance();
        date.setTime(df.parse("20130410"));

        Context context = Utils.initDateInterval(date, builder.getTimeUnit(), builder);

        assertEquals(context.getAsString(Parameters.FROM_DATE), "20130401");
        assertEquals(context.getAsString(Parameters.TO_DATE), "20130430");
    }

    @Test
    public void testInitDateIntervalByLifeTime() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TIME_UNIT, Parameters.TimeUnit.LIFETIME.toString());

        Calendar date = Calendar.getInstance();
        date.setTime(df.parse("20130410"));
        
        Context context = Utils.initDateInterval(date, builder.getTimeUnit(), builder);

        // calculate default value of TO_DATE parameter
        // @see com.codenvy.analytics.metrics.Parameters.FROM_DATE.{...}.getDefaultValue()
        String fromDateDefaultValue = "20130101";
        
        assertEquals(context.getAsString(Parameters.FROM_DATE), fromDateDefaultValue);
        
        // calculate default value of TO_DATE parameter
        // @see com.codenvy.analytics.metrics.Parameters.TO_DATE.{...}.getDefaultValue()
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        String toDateDefaultValue = df.format(calendar.getTime());
        
        assertEquals(context.getAsString(Parameters.TO_DATE), toDateDefaultValue);
    }
    
    @Test
    public void testInitDateIntervalByDayShifted() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TIME_UNIT, Parameters.TimeUnit.DAY.toString());

        Calendar date = Calendar.getInstance();
        date.setTime(df.parse("20130410"));

        int timeInterval = 0;
        
        Context context = Utils.initDateInterval(date, builder.getTimeUnit(), timeInterval, builder);

        assertEquals(context.getAsString(Parameters.FROM_DATE), "20130410");
        assertEquals(context.getAsString(Parameters.TO_DATE), "20130410");
    }

    @Test
    public void testInitDateIntervalByWeekShifted() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TIME_UNIT, Parameters.TimeUnit.WEEK.toString());

        Calendar date = Calendar.getInstance();
        date.setTime(df.parse("20130410"));

        int timeInterval = -1;
        
        Context context = Utils.initDateInterval(date, builder.getTimeUnit(), timeInterval, builder);

        assertEquals(context.getAsString(Parameters.FROM_DATE), "20130331");
        assertEquals(context.getAsString(Parameters.TO_DATE), "20130406");
    }

    @Test
    public void testInitDateIntervalByMonthShifted() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TIME_UNIT, Parameters.TimeUnit.MONTH.toString());

        Calendar date = Calendar.getInstance();
        date.setTime(df.parse("20130410"));

        int timeInterval = -2;
        
        Context context = Utils.initDateInterval(date, builder.getTimeUnit(), timeInterval, builder);

        assertEquals(context.getAsString(Parameters.FROM_DATE), "20130201");
        assertEquals(context.getAsString(Parameters.TO_DATE), "20130228");
    }

    @Test
    public void testInitDateIntervalByLifeTimeShifted() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TIME_UNIT, Parameters.TimeUnit.LIFETIME.toString());

        Calendar date = Calendar.getInstance();
        date.setTime(df.parse("20130410"));
        
        int timeInterval = -3;  // timeInterval should don't matter in case of LIFETIME time_unit 
        
        Context context = Utils.initDateInterval(date, builder.getTimeUnit(), timeInterval, builder);

        // calculate default value of TO_DATE parameter
        // @see com.codenvy.analytics.metrics.Parameters.FROM_DATE.{...}.getDefaultValue()
        String fromDateDefaultValue = "20130101";
        
        assertEquals(context.getAsString(Parameters.FROM_DATE), fromDateDefaultValue);
        
        // calculate default value of TO_DATE parameter
        // @see com.codenvy.analytics.metrics.Parameters.TO_DATE.{...}.getDefaultValue()
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        String toDateDefaultValue = df.format(calendar.getTime());
        
        assertEquals(context.getAsString(Parameters.TO_DATE), toDateDefaultValue);
    }
    
    @Test
    public void testNextDay() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TIME_UNIT, Parameters.TimeUnit.DAY.toString());
        builder.put(Parameters.FROM_DATE, "20130331");
        builder.put(Parameters.TO_DATE, "20130331");

        Context context = Utils.nextDateInterval(builder);

        assertEquals(context.getAsString(Parameters.FROM_DATE), "20130401");
        assertEquals(context.getAsString(Parameters.TO_DATE), "20130401");
    }

    @Test
    public void testNextWeek() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TIME_UNIT, Parameters.TimeUnit.WEEK.toString());
        builder.put(Parameters.FROM_DATE, "20130429");
        builder.put(Parameters.TO_DATE, "20130505");

        Context context = Utils.nextDateInterval(builder);

        assertEquals(context.getAsString(Parameters.FROM_DATE), "20130512");   // starting week on Sunday
        assertEquals(context.getAsString(Parameters.TO_DATE), "20130518");     // ending week on Saturday
    }

    @Test
    public void testNextMonth() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TIME_UNIT, Parameters.TimeUnit.MONTH.toString());
        builder.put(Parameters.FROM_DATE, "20130301");
        builder.put(Parameters.TO_DATE, "20130331");

        Context context = Utils.nextDateInterval(builder);

        assertEquals(context.getAsString(Parameters.FROM_DATE), "20130401");
        assertEquals(context.getAsString(Parameters.TO_DATE), "20130430");
    }

    @Test
    public void testPrevDay() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TIME_UNIT, Parameters.TimeUnit.DAY.toString());
        builder.put(Parameters.FROM_DATE, "20130331");
        builder.put(Parameters.TO_DATE, "20130331");

        Context context = Utils.prevDateInterval(builder);

        assertEquals(context.getAsString(Parameters.FROM_DATE), "20130330");
        assertEquals(context.getAsString(Parameters.TO_DATE), "20130330");
    }

    @Test
    public void testPrevWeek() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TIME_UNIT, Parameters.TimeUnit.WEEK.toString());
        builder.put(Parameters.FROM_DATE, "20130429");
        builder.put(Parameters.TO_DATE, "20130505");

        Context context = Utils.prevDateInterval(builder);

        assertEquals(context.getAsString(Parameters.FROM_DATE), "20130428");  // starting week on Sunday
        assertEquals(context.getAsString(Parameters.TO_DATE), "20130504");    // ending week on Saturday
    }

    @Test
    public void testPrevMonth() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TIME_UNIT, Parameters.TimeUnit.MONTH.toString());
        builder.put(Parameters.FROM_DATE, "20130301");
        builder.put(Parameters.TO_DATE, "20130331");

        Context context = Utils.prevDateInterval(builder);

        assertEquals(context.getAsString(Parameters.FROM_DATE), "20130201");
        assertEquals(context.getAsString(Parameters.TO_DATE), "20130228");
    }
}