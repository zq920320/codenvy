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
package com.codenvy.analytics.metrics;


import static org.testng.Assert.assertEquals;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.scripts.executor.pig.PigScriptExecutor;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestActiveProjectsNumberMetric extends BaseTest {

    @Test
    public void testEventFound() throws Exception {
        List<Event> events = new ArrayList<Event>();

        events.add(Event.Builder.createProjectCreatedEvent("user2", "ws2", "session1", "project1", "type1").withDate("2010-10-02").build());
        events.add(Event.Builder.createProjectCreatedEvent("user2", "ws2", "session2", "project1", "type1").withDate("2010-10-02").build());
        events.add(Event.Builder.createProjectCreatedEvent("user2", "ws3", "session3", "project1", "type1").withDate("2010-10-02").build());
        events.add(Event.Builder.createProjectCreatedEvent("user3", "ws2", "session4", "project1", "type1").withDate("2010-10-02").build());
        events.add(Event.Builder.createProjectDestroyedEvent("user2", "ws2", "session5", "project1", "type1").withDate("2010-10-02")
                                .build());

        File log = LogGenerator.generateLog(events);

        Map<String, String> params = new HashMap<String, String>();
        params.put(MetricParameter.FROM_DATE.name(), "20101002");
        params.put(MetricParameter.TO_DATE.name(), "20101002");
        params.put(PigScriptExecutor.LOG, log.getParent());

        Metric metric = MetricFactory.createMetric(MetricType.ACTIVE_PROJECTS_NUMBER);
        assertEquals(metric.getValue(params), new LongValueData(2));
    }
}
