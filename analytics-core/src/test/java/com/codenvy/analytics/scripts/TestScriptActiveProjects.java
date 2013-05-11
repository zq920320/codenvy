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
package com.codenvy.analytics.scripts;


import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.testng.annotations.Test;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.MetricParameter;
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.metrics.value.SetListStringValueData;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestScriptActiveProjects extends BaseTest {

    @Test
    public void testEventFound() throws Exception {
        List<Event> events = new ArrayList<Event>();

        // 3 active projects
        events.add(Event.Builder.createProjectCreatedEvent("user2", "ws2", "session", "project1").withDate("2010-10-02")
                        .build());
        events.add(Event.Builder.createProjectCreatedEvent("user2", "ws2", "session", "project2").withDate("2010-10-02")
                        .build());
        events.add(Event.Builder.createProjectCreatedEvent("user2", "ws3", "session", "project2").withDate("2010-10-02")
                                .build());

        // project already mentioned
        events.add(Event.Builder.createProjectCreatedEvent("user3", "ws2", "session", "project1").withDate("2010-10-02")
                        .build());

        // events should be ignored
        events.add(Event.Builder.createProjectDestroyedEvent("user2", "ws2", "session", "project3")
                                .withDate("2010-10-02").build());

        File log = LogGenerator.generateLog(events);

        Map<String, String> params = new HashMap<String, String>();
        params.put(MetricParameter.FROM_DATE.getName(), "20101002");
        params.put(MetricParameter.TO_DATE.getName(), "20101002");

        SetListStringValueData valueData = (SetListStringValueData)executeAndReturnResult(ScriptType.ACTIVE_PROJECTS, log, params);
        Set<ListStringValueData> all = valueData.getAll();

        Assert.assertEquals(all.size(), 3);

        ListStringValueData value = new ListStringValueData(Arrays.asList(new String[]{"ws2", "project1"}));
        assertTrue(all.contains(value));

        value = new ListStringValueData(Arrays.asList(new String[]{"ws2", "project2"}));
        assertTrue(all.contains(value));

        value = new ListStringValueData(Arrays.asList(new String[]{"ws3", "project2"}));
        assertTrue(all.contains(value));
    }
}
