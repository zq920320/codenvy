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
package com.codenvy.analytics.metrics.value.wrapper;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.value.DoubleValueData;
import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.StringValueData;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestDetailsProjectCreatedWrapper extends BaseTest {

    @Test
    public void testScriptDetailsProjectCreatedTypes() throws Exception {

        ListStringValueData item1 =
                                    new ListStringValueData(Arrays.asList(new StringValueData("ws1"), new StringValueData("user1"),
                                                                          new StringValueData("project1"), new StringValueData("type1")));
        ListStringValueData item2 =
                                    new ListStringValueData(Arrays.asList(new StringValueData("ws2"), new StringValueData("user1"),
                                                                          new StringValueData("project1"), new StringValueData("type1")));
        ListStringValueData item3 =
                                    new ListStringValueData(Arrays.asList(new StringValueData("ws1"), new StringValueData("user2"),
                                                                          new StringValueData("project1"), new StringValueData("type2")));
        ListStringValueData item4 =
                                    new ListStringValueData(Arrays.asList(new StringValueData("ws3"), new StringValueData("user3"),
                                                                          new StringValueData("project1"), new StringValueData("type2")));

        ListListStringValueData value = new ListListStringValueData(Arrays.asList(new ListStringValueData[]{item1, item2, item3, item4}));

        DetailsProjectCreatedWrapper wrapper = new DetailsProjectCreatedWrapper(value);
        List<ListStringValueData> all = wrapper.getProjectsByUser("user1").getAll();

        assertEquals(all.size(), 2);
        assertTrue(all.contains(item1));
        assertTrue(all.contains(item2));

        all = wrapper.getProjectsByType("type2").getAll();
        assertEquals(all.size(), 2);
        assertTrue(all.contains(item3));
        assertTrue(all.contains(item4));

        Map<StringValueData, LongValueData> projectsNumberByTypes = wrapper.getProjectsNumberByTypes().getAll();
        assertEquals(projectsNumberByTypes.get(new StringValueData("type1")), new LongValueData(2L));
        assertEquals(projectsNumberByTypes.get(new StringValueData("type2")), new LongValueData(2L));

        assertEquals(wrapper.getProjectsNumberByType("type1"), new DoubleValueData(2));
        assertEquals(wrapper.getProjectsPercentByType("type2"), new DoubleValueData(50));

        Set<StringValueData> users = wrapper.getAllUsers().getAll();
        assertEquals(users.size(), 3);
        assertTrue(users.contains(new StringValueData("user1")));
        assertTrue(users.contains(new StringValueData("user2")));
        assertTrue(users.contains(new StringValueData("user3")));
    }
}
