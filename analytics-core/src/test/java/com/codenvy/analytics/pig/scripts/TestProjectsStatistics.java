/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2014] Codenvy, S.A.
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
package com.codenvy.analytics.pig.scripts;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.projects.ProjectsStatisticsList;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/**
 * @author Alexander Reshetnyak
 */
public class TestProjectsStatistics extends BaseTest {

    @BeforeClass
    public void init() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createProjectCreatedEvent("user1@gmail.com", "ws1", "sid1", "project1", "jar")
                                .withDate("2013-01-01")
                                .withTime("10:00:00")
                                .build());
        events.add(Event.Builder.createProjectDestroyedEvent("user1@gmail.com", "ws1", "sid2", "project1", "jar")
                                .withDate("2013-01-01")
                                .withTime("11:00:00")
                                .build());

        events.add(Event.Builder.createBuildStartedEvent("user1@gmail.com", "ws1", "project1", "jar", "id1")
                                .withDate("2013-01-01")
                                .withTime("10:01:00")
                                .build());
        events.add(Event.Builder.createBuildFinishedEvent("user1@gmail.com", "ws1", "project1", "jar", "id1")
                                .withDate("2013-01-01")
                                .withTime("10:02:00")
                                .build());

        events.add(Event.Builder.createDebugStartedEvent("user1@gmail.com", "ws1", "project1", "jar", "id1")
                                .withDate("2013-01-01")
                                .withTime("10:07:00")
                                .build());
        events.add(Event.Builder.createDebugFinishedEvent("user1@gmail.com", "ws1", "project1", "jar", "id1")
                                .withDate("2013-01-01")
                                .withTime("10:08:00")
                                .build());

        events.add(Event.Builder.createRunStartedEvent("user1@gmail.com", "ws1", "project1", "jar", "id1")
                                .withDate("2013-01-01")
                                .withTime("10:09:00")
                                .build());
        events.add(Event.Builder.createRunFinishedEvent("user1@gmail.com", "ws1", "project1", "jar", "id1")
                                .withDate("2013-01-01")
                                .withTime("10:10:00")
                                .build());

        events.add(Event.Builder.createApplicationCreatedEvent("user1@gmail.com", "ws1", "sid1", "project1", "jar", "paas")
                                .withDate("2013-01-01")
                                .withTime("10:11:00")
                                .build());

        events.add(Event.Builder.createProjectDeployedEvent("user1@gmail.com", "ws1", "sid1", "project1", "jar", "paas")
                                .withDate("2013-01-01")
                                .withTime("10:12:00")
                                .build());

        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.USER, Parameters.USER_TYPES.REGISTERED.name());
        builder.put(Parameters.WS, Parameters.WS_TYPES.PERSISTENT.name());
        builder.put(Parameters.STORAGE_TABLE, "testprojectsevents");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        pigServer.execute(ScriptType.PROJECTS_STATISTICS, builder.build());
    }


    @Test
    public void testTestUsersProjectsListMetricFilterByUser() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(MetricFilter.PROJECT, "project1");

        Metric metric = new TestProjectsEventsMetric();
        List<ValueData> items = ((ListValueData)metric.getValue(builder.build())).getAll();

        assertEquals(1, items.size());

        Map<String, ValueData> m = ((MapValueData)items.get(0)).getAll();
        assertEquals(m.get("project").getAsString(), "project1");
        assertEquals(m.get("ws").getAsString(), "ws1");
        assertEquals(m.get("code_refactories").getAsString(),"0");
        assertEquals(m.get("code_completes").getAsString(),"0");
        assertEquals(m.get("builds").getAsString(),"2");
        assertEquals(m.get("runs").getAsString(),"1");
        assertEquals(m.get("debugs").getAsString(),"1");
        assertEquals(m.get("deploys").getAsString(),"2");
        assertEquals(m.get("build_interrupts").getAsString(),"0");
        assertEquals(m.get("artifact_deploys").getAsString(),"0");
        assertEquals(m.get("project_creates").getAsString(),"1");
        assertEquals(m.get("project_destroys").getAsString(),"1");
        assertEquals(m.get("paas_deploys").getAsString(),"1");
        assertEquals(m.get("run_time").getAsString(),"60000");
        assertEquals(m.get("build_time").getAsString(),"60000");
        assertEquals(m.get("debug_time").getAsString(),"60000");
    }

    private class TestProjectsEventsMetric extends ProjectsStatisticsList {

        @Override
        public String getStorageCollectionName() {
            return "testprojectsevents";
        }
    }
}
