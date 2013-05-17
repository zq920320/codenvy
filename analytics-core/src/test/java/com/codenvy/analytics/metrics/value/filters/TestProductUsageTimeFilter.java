/*
 *    Copyright (C) 2013 Codenvy.
 *
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
}
