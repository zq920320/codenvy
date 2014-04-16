/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
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
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.projects.AbstractProjectPaas;
import com.codenvy.analytics.metrics.projects.ProjectPaases;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestNumberOfDeploymentsByTypes extends BaseTest {

    @BeforeClass
    public void init() throws Exception {
        List<Event> events = new ArrayList<>();

        // standalone project created event
        events.add(Event.Builder.createProjectCreatedEvent("user4@gmail.com", "ws4", "session", "project2", "type")
                                .withDate("2013-01-01")
                                .withTime("09:00:00")
                                .build());

        // won't be taken in account, there is no project-created event before
        events.add(Event.Builder.createApplicationCreatedEvent("user1@gmail.com", "ws1", "session", "project1", "type",
                                                               "paas1")
                                .withDate("2013-01-01")
                                .withTime("10:00:00")
                                .build());

        // the same project is deployed twice, we are interested in the first deployment event. paas3 is target PaaS
        events.add(Event.Builder.createProjectCreatedEvent("user1@gmail.com", "ws2", "session", "project2", "type")
                                .withDate("2013-01-01")
                                .withTime("11:00:00")
                                .build());
        events.add(Event.Builder.createApplicationCreatedEvent("user1@gmail.com", "ws2", "session", "project2", "type",
                                                               "paas3")
                                .withDate("2013-01-01")
                                .withTime("11:10:00")
                                .build());
        events.add(Event.Builder.createApplicationCreatedEvent("user2@gmail.com", "ws1", "session", "project2", "type",
                                                               "paas4")
                                .withDate("2013-01-01")
                                .withTime("11:20:00")
                                .build());

        // the project is deployed once, paas3 is target PaaS
        events.add(Event.Builder.createProjectCreatedEvent("user1@gmail.com", "ws3", "session", "project4", "type")
                                .withDate("2013-01-01")
                                .withTime("13:00:00")
                                .build());
        events.add(Event.Builder.createApplicationCreatedEvent("user3@gmail.com", "ws3", "session", "project4", "type",
                                                               "paas3")
                                .withDate("2013-01-01")
                                .withTime("13:10:00")
                                .build());

        // the project has been removed and created again, paas1 is target PaaS
        events.add(Event.Builder.createProjectCreatedEvent("user1@gmail.com", "ws3", "session", "project4", "type")
                                .withDate("2013-01-01")
                                .withTime("14:00:00")
                                .build());
        events.add(Event.Builder.createApplicationCreatedEvent("user3@gmail.com", "ws3", "session", "project4", "type",
                                                               "paas1")
                                .withDate("2013-01-01")
                                .withTime("14:10:00")
                                .build());


        // the project is deployed once, local is target PaaS
        events.add(Event.Builder.createProjectCreatedEvent("user2@gmail.com", "ws4", "session", "project4", "type")
                                .withDate("2013-01-01")
                                .withTime("15:00:00")
                                .build());
        events.add(Event.Builder.createProjectDeployedEvent("user3@mail.ru", "ws4", "session", "project4", "type",
                                                            "local")
                                .withDate("2013-01-01")
                                .withTime("15:10:00")
                                .build());

        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.USER, Parameters.USER_TYPES.REGISTERED.name());
        builder.put(Parameters.WS, Parameters.WS_TYPES.PERSISTENT.name());
        builder.put(Parameters.STORAGE_TABLE, "testnumberofdeploymentsbytypes");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        pigServer.execute(ScriptType.DEPLOYMENTS_BY_TYPES, builder.build());
    }

    @Test
    public void testSingleDateFilterSingleParam() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");

        Metric metric = new TestProjectPaases();
        Map<String, ValueData> values = ((MapValueData)metric.getValue(builder.build())).getAll();
        assertEquals(values.size(), 3);
        assertEquals(values.get("local"), new LongValueData(1));
        assertEquals(values.get("paas1"), new LongValueData(1));
        assertEquals(values.get("paas3"), new LongValueData(2));
    }

    @Test
    public void testSingleUserFilter() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");

        Metric metric = new TestProjectPaases();

        builder.put(MetricFilter.USER, "user1@gmail.com");
        Map<String, ValueData> values = ((MapValueData)metric.getValue(builder.build())).getAll();
        assertEquals(values.size(), 2);
        assertEquals(values.get("paas1"), new LongValueData(1));
        assertEquals(values.get("paas3"), new LongValueData(2));

        builder.put(MetricFilter.USER, "user2@gmail.com");
        values = ((MapValueData)metric.getValue(builder.build())).getAll();
        assertEquals(values.size(), 1);
        assertEquals(values.get("local"), new LongValueData(1));

        builder.put(MetricFilter.USER, "user1@gmail.com OR user2@gmail.com");
        values = ((MapValueData)metric.getValue(builder.build())).getAll();
        assertEquals(values.size(), 3);
        assertEquals(values.get("local"), new LongValueData(1));
        assertEquals(values.get("paas1"), new LongValueData(1));
        assertEquals(values.get("paas3"), new LongValueData(2));

        builder.put(MetricFilter.USER, "user4@gmail.com");
        values = ((MapValueData)metric.getValue(builder.build())).getAll();
        assertEquals(values.size(), 0);
    }

    @Test
    public void testComplexFilterSingleParam() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(MetricFilter.USER, "user1@gmail.com OR user2@gmail.com");
        builder.put(MetricFilter.WS, "ws3");

        Metric metric = new TestProjectPaases();
        Map<String, ValueData> values = ((MapValueData)metric.getValue(builder.build())).getAll();
        assertEquals(values.size(), 2);
        assertEquals(values.get("paas1"), new LongValueData(1));
        assertEquals(values.get("paas3"), new LongValueData(1));

        metric = new TestAbstractProjectPaas(new String[]{"paas1"});
        assertEquals(metric.getValue(builder.build()), new LongValueData(1));
    }

    private class TestProjectPaases extends ProjectPaases {

        @Override
        public String getStorageCollectionName() {
            return "testnumberofdeploymentsbytypes";
        }

        @Override
        public String[] getTrackedFields() {
            return new String[]{"paas1", "pass2", "paas3", "local"};
        }
    }

    private class TestAbstractProjectPaas extends AbstractProjectPaas {

        protected TestAbstractProjectPaas(String[] types) {
            super("testnumberofdeploymentsbytypes", types);
        }

        @Override
        public String getStorageCollectionName() {
            return "testnumberofdeploymentsbytypes";
        }

        @Override
        public String getDescription() {
            return null;
        }
    }
}
