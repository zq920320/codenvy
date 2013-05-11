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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.value.DoubleValueData;
import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.metrics.value.filters.ProjectCreatedListFilter;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestProjectCreatedListFilter extends BaseTest {

    @Test
    public void testDoFilter() throws Exception {

        ListStringValueData item1 = new ListStringValueData(Arrays.asList("ws1", "user1", "project1", "type1"));
        ListStringValueData item2 = new ListStringValueData(Arrays.asList("ws2", "user1", "project1", "type1"));
        ListStringValueData item3 = new ListStringValueData(Arrays.asList("ws1", "user2", "project1", "type2"));
        ListStringValueData item4 = new ListStringValueData(Arrays.asList("ws3", "user3", "project1", "type2"));
        ListListStringValueData value = new ListListStringValueData(Arrays.asList(new ListStringValueData[]{item1, item2, item3, item4}));

        ProjectCreatedListFilter wrapper = new ProjectCreatedListFilter(value);
        List<ListStringValueData> all = wrapper.doFilter(Metric.USER_FILTER_PARAM, "user1").getAll();

        assertEquals(all.size(), 2);
        assertTrue(all.contains(item1));
        assertTrue(all.contains(item2));

        all = wrapper.doFilter(Metric.TYPE_FILTER_PARAM, "type2").getAll();
        assertEquals(all.size(), 2);
        assertTrue(all.contains(item3));
        assertTrue(all.contains(item4));

        all = wrapper.doFilter(Metric.WS_FILTER_PARAM, "ws1").getAll();
        assertEquals(all.size(), 2);
        assertTrue(all.contains(item1));
        assertTrue(all.contains(item3));

        all = wrapper.doFilter(Metric.USER_FILTER_PARAM, "user2").getAll();
        assertEquals(all.size(), 1);
        assertTrue(all.contains(item3));

        all = wrapper.doFilter(Metric.PROJECT_FILTER_PARAM, "project1").getAll();
        assertEquals(all.size(), 4);
        assertTrue(all.contains(item1));
        assertTrue(all.contains(item2));
        assertTrue(all.contains(item3));
        assertTrue(all.contains(item4));

        Map<String, Long> projectsNumber = wrapper.getProjectsNumberByTypes().getAll();
        assertEquals(projectsNumber.get("type1"), Long.valueOf(2));
        assertEquals(projectsNumber.get("type2"), Long.valueOf(2));

        assertEquals(wrapper.getProjectsNumberByType("type1"), new DoubleValueData(2));
        assertEquals(wrapper.getProjectsPercentByType("type2"), new DoubleValueData(50));

        Set<String> users = wrapper.getAllUsers().getAll();
        assertEquals(users.size(), 3);
        assertTrue(users.contains("user1"));
        assertTrue(users.contains("user2"));
        assertTrue(users.contains("user3"));

        projectsNumber = wrapper.getProjectsNumberByUsers().getAll();
        assertEquals(projectsNumber.get("user1"), Long.valueOf(2));
        assertEquals(projectsNumber.get("user2"), Long.valueOf(1));
        assertEquals(projectsNumber.get("user3"), Long.valueOf(1));
    }
}
