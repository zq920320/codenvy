/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.vew.layout;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.codenvy.analytics.metrics.TimeUnit;
import com.codenvy.analytics.metrics.Utils;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
class DateRowLayoutImpl implements RowLayout {

    private final String                sectionName;
    private final Map<TimeUnit, String> format;

    DateRowLayoutImpl(String sectionName, String formatDay, String formatWeek, String formatMonth) {
        this.sectionName = sectionName;
        this.format = new HashMap<TimeUnit, String>(3);
        this.format.put(TimeUnit.DAY, formatDay);
        this.format.put(TimeUnit.WEEK, formatWeek);
        this.format.put(TimeUnit.MONTH, formatMonth);
    }

    /**
     * Fills row by the next rule:<br>
     * <li>First column is {@link #sectionName}</li><br>
     * <li>Others columns are filed by date represented in string by providing format depending on {@link TimeUnit}</li><br>
     * {@inheritedDoc}
     */
    public List<String> fill(Map<String, String> context, int length) throws Exception {
        TimeUnit timeUnit = Utils.getTimeUnit(context);
        DateFormat df = new SimpleDateFormat(format.get(timeUnit));

        List<String> row = new ArrayList<String>(length);
        row.add(sectionName);

        for (int i = 1; i < length; i++) {
            Calendar toDate = Utils.getToDate(context);
            row.add(df.format(toDate.getTime()));

            context = Utils.prevDateInterval(context);
        }

        return row;
    }
}