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

import static org.testng.Assert.assertEquals;

import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;

import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestProductUsageTimeFilter {

    @Test
    public void testTotalTime() throws Exception {
        ListStringValueData item1 = new ListStringValueData(Arrays.asList("ws1", "user1", "2010-10-01T20:00:00.000Z", "420"));
        ListStringValueData item2 = new ListStringValueData(Arrays.asList("ws1", "user2", "2010-10-01T20:25:00.000Z", "240"));
        ListStringValueData item3 = new ListStringValueData(Arrays.asList("ws1", "user1", "2010-10-01T21:00:00.000Z", "420"));
        ListListStringValueData valueData = new ListListStringValueData(Arrays.asList(item1, item2, item3));

        ProductUsageTimeFilter filter = new ProductUsageTimeFilter(valueData);
        assertEquals(filter.getTotalUsageTime(), 18);
    }

    @Test
    public void testNumberOfSessions() throws Exception {
        ListStringValueData item1 = new ListStringValueData(Arrays.asList("ws1", "user1", "2010-10-01T20:00:00.000Z", "420"));
        ListStringValueData item2 = new ListStringValueData(Arrays.asList("ws1", "user2", "2010-10-01T20:25:00.000Z", "240"));
        ListStringValueData item3 = new ListStringValueData(Arrays.asList("ws1", "user1", "2010-10-01T21:00:00.000Z", "420"));
        ListListStringValueData valueData = new ListListStringValueData(Arrays.asList(item1, item2, item3));

        ProductUsageTimeFilter filter = new ProductUsageTimeFilter(valueData);

        assertEquals(filter.getNumberOfSessions(0, true, 240, true), 1);
        assertEquals(filter.getNumberOfSessions(0, true, 240, false), 0);

        assertEquals(filter.getNumberOfSessions(240, true, 420, true), 3);
        assertEquals(filter.getNumberOfSessions(240, false, 420, true), 2);
        assertEquals(filter.getNumberOfSessions(240, false, 420, false), 0);
        assertEquals(filter.getNumberOfSessions(240, true, 420, false), 1);

        assertEquals(filter.getNumberOfSessions(420, true, Integer.MAX_VALUE, true), 2);
        assertEquals(filter.getNumberOfSessions(420, false, Integer.MAX_VALUE, true), 0);
    }

    @Test
    public void testUsageTime() throws Exception {
        ListStringValueData item1 = new ListStringValueData(Arrays.asList("ws1", "user1", "2010-10-01T20:00:00.000Z", "420"));
        ListStringValueData item2 = new ListStringValueData(Arrays.asList("ws1", "user2", "2010-10-01T20:25:00.000Z", "240"));
        ListStringValueData item3 = new ListStringValueData(Arrays.asList("ws1", "user1", "2010-10-01T21:00:00.000Z", "420"));
        ListListStringValueData valueData = new ListListStringValueData(Arrays.asList(item1, item2, item3));

        ProductUsageTimeFilter filter = new ProductUsageTimeFilter(valueData);

        assertEquals(filter.getUsageTime(0, true, 240, true), 4);
        assertEquals(filter.getUsageTime(0, true, 240, false), 0);

        assertEquals(filter.getUsageTime(240, true, 420, true), 18);
        assertEquals(filter.getUsageTime(240, false, 420, true), 14);
        assertEquals(filter.getUsageTime(240, false, 420, false), 0);
        assertEquals(filter.getUsageTime(240, true, 420, false), 4);

        assertEquals(filter.getUsageTime(420, true, Integer.MAX_VALUE, true), 14);
        assertEquals(filter.getUsageTime(420, false, Integer.MAX_VALUE, true), 0);
    }
}
