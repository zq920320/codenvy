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


package com.codenvy.analytics.metrics.value.filters;

import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;

/**
 * <li>0 - the workspace name</li><br>
 * <li>1 - the user name</li><br>
 * <li>2 - the session start time</li><br>
 * <li>3 - the session duration</li><br>
 * 
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ProductUsageTimeFilter extends AbstractFilter {

    private final static int SESSION_DURATION = 3;
    
    public ProductUsageTimeFilter(ListListStringValueData valueData) {
        super(valueData);
    }

    /**
     * @return total time for all sessions
     */
    public long getTotalUsageTime() {
        long total = 0;

        for (ListStringValueData item : valueData.getAll()) {
            total += Long.valueOf(item.getAll().get(SESSION_DURATION));
        }

        return total / 60;
    }

    /**
     * Returns number of user sessions.
     * 
     * @param min the time in seconds
     * @param max the time in seconds
     */
    public long getNumberOfSessions(long min, boolean inclusiveMin, long max, boolean inclusiveMax) {
        long result = 0;

        for (ListStringValueData item : valueData.getAll()) {
            long duration = Long.valueOf(item.getAll().get(SESSION_DURATION));

            if (acceptInterval(min, inclusiveMin, max, inclusiveMax, duration)) {
                ++result;
            }
        }

        return result;
    }

    /**
     * Returns usage time
     * 
     * @param min the time in seconds
     * @param max the time in seconds
     */
    public long getUsageTime(long min, boolean inclusiveMin, long max, boolean inclusiveMax) {
        long result = 0;

        for (ListStringValueData item : valueData.getAll()) {
            long duration = Long.valueOf(item.getAll().get(SESSION_DURATION));

            if (acceptInterval(min, inclusiveMin, max, inclusiveMax, duration)) {
                result += duration;
            }
        }

        return result / 60;
    }

    private boolean acceptInterval(long min, boolean inclusiveMin, long max, boolean inclusiveMax, long duration) {
        return (min < duration || (min <= duration && inclusiveMin)) && (duration < max || (duration <= max && inclusiveMax));
    }

    protected int getIndex(MetricFilter key) throws IllegalArgumentException {
        switch (key) {
            case FILTER_WS:
            case FILTER_USER:
                return key.ordinal();
            default:
                throw new IllegalArgumentException();
        }
    }
}
