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


package com.codenvy.analytics.server.vew.template;

import com.codenvy.analytics.metrics.TimeUnit;
import com.codenvy.analytics.metrics.Utils;

import org.w3c.dom.Element;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
class DateRow extends AbstractRow {

    private static final String ATTRIBUTE_SECTION         = "section";
    private static final String ATTRIBUTE_FORMAT_DAY      = "formatDay";
    private static final String ATTRIBUTE_FORMAT_WEEK     = "formatWeek";
    private static final String ATTRIBUTE_FORMAT_MONTH    = "formatMonth";
    private static final String ATTRIBUTE_FORMAT_LIFETIME = "formatLifetime";

    private final String                sectionName;
    private final Map<TimeUnit, String> dateFormat;

    /** {@link DateRow} constructor. */
    private DateRow(String sectionName,
                    Map<TimeUnit, String> dateFormat) {
        super();

        this.sectionName = sectionName;
        this.dateFormat = dateFormat;
    }

    /** {@inheritDoc} */
    protected String doRetrieve(Map<String, String> context, int columnNumber) throws IOException {
        switch (columnNumber) {
            case 0:
                return sectionName;
            default:
                TimeUnit timeUnit = Utils.getTimeUnit(context);
                Calendar calendar = Utils.getToDate(context);

                DateFormat df = new SimpleDateFormat(dateFormat.get(timeUnit));
                return df.format(calendar.getTime());
        }
    }

    /** Factory method */
    public static DateRow initialize(Element element) {
        String sectionName = element.getAttribute(ATTRIBUTE_SECTION);

        Map<TimeUnit, String> dateFormat = new HashMap<>(4);
        dateFormat.put(TimeUnit.DAY, element.getAttribute(ATTRIBUTE_FORMAT_DAY));
        dateFormat.put(TimeUnit.WEEK, element.getAttribute(ATTRIBUTE_FORMAT_WEEK));
        dateFormat.put(TimeUnit.MONTH, element.getAttribute(ATTRIBUTE_FORMAT_MONTH));
        dateFormat.put(TimeUnit.LIFETIME, element.getAttribute(ATTRIBUTE_FORMAT_LIFETIME));

        return new DateRow(sectionName, dateFormat);
    }
}