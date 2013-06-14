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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.MetricParameter;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestProjectsDeployedLocal extends BaseTest {

    @Test
    public void testExecute() throws Exception {
        List<Event> events = new ArrayList<Event>();
        events.add(Event.Builder.createApplicationCreatedEvent("user1", "ws1", "session", "project1", "type1", "paas1")
                                .withDate("2010-10-01").build());
        events.add(Event.Builder.createApplicationCreatedEvent("user1", "ws2", "session", "project2", "type1", "paas3")
                                .withDate("2010-10-01").build());
        events.add(Event.Builder.createApplicationCreatedEvent("user2", "ws3", "session", "project3", "type2", "paas3")
                                .withDate("2010-10-01").build());
        events.add(Event.Builder.createApplicationCreatedEvent("user3", "ws3", "session", "project4", "type2", "paas3")
                                .withDate("2010-10-01").build());
        events.add(Event.Builder.createProjectDeployedEvent("user3", "ws4", "session", "project4", "type2", "local")
                                .withDate("2010-10-01").build());

        File log = LogGenerator.generateLog(events);

        Map<String, String> context = Utils.newContext();
        context.put(MetricParameter.FROM_DATE.name(), "20101001");
        context.put(MetricParameter.TO_DATE.name(), "20101001");

        ListListStringValueData value = (ListListStringValueData)executeAndReturnResult(ScriptType.PROJECTS_DEPLOYED_LOCAL, log, context);

        List<ListStringValueData> all = value.getAll();
        ListStringValueData item = new ListStringValueData(Arrays.asList("ws4", "user3", "project4", "type2", "local"));

        assertEquals(all.size(), 1);
        assertTrue(all.contains(item));
    }
}
