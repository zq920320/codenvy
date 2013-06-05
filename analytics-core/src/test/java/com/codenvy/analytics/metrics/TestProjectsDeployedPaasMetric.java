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
import com.codenvy.analytics.metrics.value.DoubleValueData;
import com.codenvy.analytics.metrics.value.MapStringLongValueData;
import com.codenvy.analytics.scripts.executor.pig.PigScriptExecutor;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestProjectsDeployedPaasMetric extends BaseTest {

    @Test
    public void testScriptDetailsProjectCreatedTypes() throws Exception {
        List<Event> events = new ArrayList<Event>();
        events.add(Event.Builder.createApplicationCreatedEvent("user1", "ws1", "session", "project1", "type1", "paas1")
                                .withDate("2010-10-01").build());
        events.add(Event.Builder.createApplicationCreatedEvent("user1", "ws2", "session", "project2", "type1", "paas3")
                                .withDate("2010-10-01").build());
        events.add(Event.Builder.createApplicationCreatedEvent("user2", "ws3", "session", "project3", "type2", "paas3")
                                .withDate("2010-10-01").build());
        events.add(Event.Builder.createApplicationCreatedEvent("user3", "ws3", "session", "project4", "type2", "paas3")
                                .withDate("2010-10-01").build());
        events.add(Event.Builder.createProjectDeployedEvent("user3", "ws4", "session", "project4", "type2", "LOCAL")
                                .withDate("2010-10-01").build());

        File log = LogGenerator.generateLog(events);

        Map<String, String> context = Utils.newContext();
        context.put(MetricParameter.FROM_DATE.getName(), "20101001");
        context.put(MetricParameter.TO_DATE.getName(), "20101001");
        context.put(PigScriptExecutor.LOG, log.getParent());

        Metric metric = MetricFactory.createMetric(MetricType.PAAS_DEPLOYMENT_TYPES);
        
        MapStringLongValueData valueData = (MapStringLongValueData)metric.getValue(context);
        Map<String, Long> all = valueData.getAll();
        
        assertEquals(all.get("paas1"), Long.valueOf(1));
        assertEquals(all.get("paas3"), Long.valueOf(3));
        assertEquals(all.get("LOCAL"), Long.valueOf(1));

        metric = MetricFactory.createMetric(MetricType.PAAS_DEPLOYMENT_TYPE_LOCAL_NUMBER);
        assertEquals(metric.getValue(context), new DoubleValueData(1));
    }
}
