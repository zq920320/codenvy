/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value.filters;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.metrics.value.MapStringLongValueData;
import com.codenvy.analytics.metrics.value.SetStringValueData;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestAbstractFilter {

    private AbstractFilter      filter;
    private ListStringValueData item1;
    private ListStringValueData item2;
    private ListStringValueData item3;
    private ListStringValueData item4;

    @BeforeMethod
    public void setUp() {
        item1 = new ListStringValueData(Arrays.asList("ws1", "user1", "project1", "type1", "paas1"));
        item2 = new ListStringValueData(Arrays.asList("ws1", "user2", "project1", "type2", "paas2"));
        item3 = new ListStringValueData(Arrays.asList("ws2", "user2", "project2", "type3", "paas3"));
        item4 = new ListStringValueData(Arrays.asList("ws2", "user3", "project3", "type2", "paas4"));

        ListListStringValueData value = new ListListStringValueData(Arrays.asList(item1, item2, item3, item4));
        filter = new TestedFilter(value);
    }

    @Test
    public void testGetIndex() {
        assertEquals(filter.getIndex(MetricFilter.FILTER_WS), 0);
        assertEquals(filter.getIndex(MetricFilter.FILTER_USER), 1);
        assertEquals(filter.getIndex(MetricFilter.FILTER_PROJECT_NAME), 2);
        assertEquals(filter.getIndex(MetricFilter.FILTER_PROJECT_TYPE), 3);
        assertEquals(filter.getIndex(MetricFilter.FILTER_PROJECT_PAAS), 4);
    }

    @Test
    public void testApplyFilter() throws Exception {
        ListListStringValueData result = filter.apply(MetricFilter.FILTER_USER, "user1");
        List<ListStringValueData> all = result.getAll();

        assertEquals(all.size(), 1);
        assertTrue(all.contains(item1));
    }

    @Test
    public void testSize() throws Exception {
        assertEquals(filter.size(), 4);
    }

    @Test
    public void testSizeFilter() throws Exception {
        assertEquals(filter.size(MetricFilter.FILTER_WS, "ws1"), 2);
    }

    @Test
    public void testGetAvailable() throws Exception {
        SetStringValueData available = filter.getAvailable(MetricFilter.FILTER_PROJECT_NAME);
        Set<String> all = available.getAll();

        assertEquals(all.size(), 3);
        assertTrue(all.contains("project1"));
        assertTrue(all.contains("project2"));
        assertTrue(all.contains("project3"));
    }

    @Test
    public void testSizeGroup() throws Exception {
        MapStringLongValueData sizeOfGroups = filter.sizeOfGroups(MetricFilter.FILTER_PROJECT_TYPE);
        Map<String, Long> all = sizeOfGroups.getAll();

        assertEquals(all.size(), 3);
        assertEquals(all.get("type1"), Long.valueOf(1));
        assertEquals(all.get("type2"), Long.valueOf(2));
        assertEquals(all.get("type3"), Long.valueOf(1));
    }

    private class TestedFilter extends AbstractFilter {

        public TestedFilter(ListListStringValueData valueData) {
            super(valueData);
        }

        @Override
        protected int getIndex(MetricFilter key) throws IllegalArgumentException {
            return key.ordinal();
        }
    }
}
