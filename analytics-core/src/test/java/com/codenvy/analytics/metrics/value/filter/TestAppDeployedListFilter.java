/*
 *    Copyright (C) 2013 eXo Platform SAS.
 *
 *    This is free software; you can redistribute it and/or modify it
 *    under the terms of the GNU Lesser General Public License as
 *    published by the Free Software Foundation; either version 2.1 of
 *    the License, or (at your option) any later version.
 *
 *    This software is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this software; if not, write to the Free
 *    Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *    02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.codenvy.analytics.metrics.value.filter;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.codenvy.analytics.metrics.value.filters.AppDeployedListFilter;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.metrics.value.StringValueData;

import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestAppDeployedListFilter extends BaseTest {

    @Test
    public void testDoFilter() throws Exception {

        ListStringValueData item1 =
                                    new ListStringValueData(Arrays.asList(new StringValueData("ws1"), new StringValueData("user1"),
                                                                          new StringValueData("project1"), new StringValueData("type1"),
                                                                          new StringValueData("paas1")));
        ListStringValueData item2 =
                                    new ListStringValueData(Arrays.asList(new StringValueData("ws2"), new StringValueData("user1"),
                                                                          new StringValueData("project2"), new StringValueData("type1"),
                                                                          new StringValueData("paas3")));
        ListStringValueData item3 =
                                    new ListStringValueData(Arrays.asList(new StringValueData("ws3"), new StringValueData("user2"),
                                                                          new StringValueData("project3"), new StringValueData("type2"),
                                                                          new StringValueData("paas3")));
        ListStringValueData item4 =
                                    new ListStringValueData(Arrays.asList(new StringValueData("ws3"), new StringValueData("user3"),
                                                                          new StringValueData("project4"), new StringValueData("type2"),
                                                                          new StringValueData("paas3")));

        ListStringValueData item5 =
                                    new ListStringValueData(Arrays.asList(new StringValueData("ws4"), new StringValueData("user3"),
                                                                          new StringValueData("project4"), new StringValueData("type2"),
                                                                          new StringValueData("local")));
        
        ListListStringValueData value =
                                        new ListListStringValueData(Arrays.asList(new ListStringValueData[]{item1, item2, item3, item4,
                                                item5}));

        AppDeployedListFilter wrapper = new AppDeployedListFilter(value);

        List<ListStringValueData> all = wrapper.doFilter(Metric.WS_FILTER_PARAM, "ws1").getAll();
        assertEquals(all.size(), 1);
        assertTrue(all.contains(item1));

        all = wrapper.doFilter(Metric.USER_FILTER_PARAM, "user1").getAll();
        assertEquals(all.size(), 2);
        assertTrue(all.contains(item1));
        assertTrue(all.contains(item2));

        all = wrapper.doFilter(Metric.PROJECT_FILTER_PARAM, "project1").getAll();
        assertEquals(all.size(), 1);
        assertTrue(all.contains(item1));

        all = wrapper.doFilter(Metric.TYPE_FILTER_PARAM, "type2").getAll();
        assertEquals(all.size(), 3);
        assertTrue(all.contains(item3));
        assertTrue(all.contains(item4));
        assertTrue(all.contains(item5));

        all = wrapper.doFilter(Metric.PAAS_FILTER_PARAM, "local").getAll();
        assertEquals(all.size(), 1);
        assertTrue(all.contains(item5));
    }
}
