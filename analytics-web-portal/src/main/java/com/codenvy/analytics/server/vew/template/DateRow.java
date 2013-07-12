/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.vew.template;

import com.codenvy.analytics.metrics.TimeUnit;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.shared.RowData;
import org.w3c.dom.Element;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
class DateRow implements Row {

    private static final String ATTRIBUTE_SECTION = "section";
    private static final String ATTRIBUTE_FORMAT_DAY = "formatDay";
    private static final String ATTRIBUTE_FORMAT_WEEK = "formatWeek";
    private static final String ATTRIBUTE_FORMAT_MONTH = "formatMonth";

    private final String sectionName;
    private final Map<TimeUnit, String> dateFormat;

    /**
     * {@link DateRow} constructor.
     */
    private DateRow(String sectionName, Map<TimeUnit, String> dateFormat) {
        this.sectionName = sectionName;
        this.dateFormat = dateFormat;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RowData> fill(Map<String, String> context, int length) throws Exception {
        TimeUnit timeUnit = Utils.getTimeUnit(context);

        RowData row = new RowData();
        row.add(sectionName);

        DateFormat df = new SimpleDateFormat(dateFormat.get(timeUnit));
        for (int i = 1; i < length; i++) {
            Calendar calendar = Utils.getToDate(context);
            String date = df.format(calendar.getTime());

            row.add(date);

            context = Utils.prevDateInterval(context);
        }

        ArrayList<RowData> result = new ArrayList<>();
        result.add(row);

        return result;
    }

    /**
     * Factory method
     */
    public static DateRow initialize(Element element) {
        String sectionName = element.getAttribute(ATTRIBUTE_SECTION);

        Map<TimeUnit, String> dateFormat = new HashMap<TimeUnit, String>(3);
        dateFormat.put(TimeUnit.DAY, element.getAttribute(ATTRIBUTE_FORMAT_DAY));
        dateFormat.put(TimeUnit.LIFETIME, element.getAttribute(ATTRIBUTE_FORMAT_DAY));
        dateFormat.put(TimeUnit.WEEK, element.getAttribute(ATTRIBUTE_FORMAT_WEEK));
        dateFormat.put(TimeUnit.MONTH, element.getAttribute(ATTRIBUTE_FORMAT_MONTH));

        return new DateRow(sectionName, dateFormat);
    }
}