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
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.projects.AbstractProjectType;
import com.codenvy.analytics.metrics.projects.ProjectTypes;
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
public class TestNumberOfEventsByTypes_ProjectTypes extends BaseTest {

    private static final String PROJECTS_LIST_COLLECTION = "projects_list";
    
    private static final String PROJECT_TYPES_COLLECTION = "project_types";
    
    @BeforeClass
    public void init() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createProjectCreatedEvent("user1@gmail.com", "ws1", "", "", "jar")
                                .withDate("2013-01-01")
                                .withTime("10:00:00")
                                .build());
        events.add(Event.Builder.createProjectCreatedEvent("user1@yahoo.com", "ws2", "", "", "war")
                                .withDate("2013-01-01")
                                .withTime("10:00:01")
                                .build());
        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.USER, Parameters.USER_TYPES.REGISTERED.name());
        builder.put(Parameters.WS, Parameters.WS_TYPES.PERSISTENT.name());
        builder.put(Parameters.STORAGE_TABLE, PROJECTS_LIST_COLLECTION);
        builder.put(Parameters.LOG, log.getAbsolutePath());
        pigServer.execute(ScriptType.PROJECTS, builder.build());

        /* is needed for testAllTypes() test only */
        builder.put(Parameters.STORAGE_TABLE, PROJECT_TYPES_COLLECTION);
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.put(Parameters.EVENT, "project-created");
        builder.put(Parameters.PARAM, "TYPE");
        pigServer.execute(ScriptType.EVENTS_BY_TYPE, builder.build());

        
        events = new ArrayList<>();
        events.add(Event.Builder.createProjectCreatedEvent("user1@gmail.com", "ws1", "", "", "jar")
                                .withDate("2013-01-02")
                                .withTime("10:00:00")
                                .build());
        events.add(Event.Builder.createProjectCreatedEvent("user1@yahoo.com", "ws2", "", "", "war")
                                .withDate("2013-01-02")
                                .withTime("10:00:01")
                                .build());
        log = LogGenerator.generateLog(events);

        builder.put(Parameters.FROM_DATE, "20130102");
        builder.put(Parameters.TO_DATE, "20130102");
        builder.put(Parameters.STORAGE_TABLE, PROJECTS_LIST_COLLECTION);        
        builder.put(Parameters.LOG, log.getAbsolutePath());
        pigServer.execute(ScriptType.PROJECTS, builder.build());
        
        /* is needed for testAllTypes() test only */
        builder.put(Parameters.STORAGE_TABLE, PROJECT_TYPES_COLLECTION);
        pigServer.execute(ScriptType.EVENTS_BY_TYPE, builder.build());
    }

    @Test
    public void testSingleDateFilterSingleParam() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130102");
        builder.put(Parameters.TO_DATE, "20130102");

        Metric metric = new TestAbstractProjectType(new String[]{"jar"});
        assertEquals(metric.getValue(builder.build()), new LongValueData(1L));
    }

    @Test
    public void testSingleDateFilterDoubleParam() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130102");
        builder.put(Parameters.TO_DATE, "20130102");

        Metric metric = new TestAbstractProjectType(new String[]{"jar", "war"});
        assertEquals(metric.getValue(builder.build()), new LongValueData(2L));
    }


    @Test
    public void testDatePeriodFilter() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130102");

        Metric metric = new TestAbstractProjectType(new String[]{"jar"});
        assertEquals(metric.getValue(builder.build()), new LongValueData(2L));
    }

    @Test
    public void testSingleUserFilter() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130102");
        builder.put(Parameters.USER, "user1@gmail.com");

        Metric metric = new TestAbstractProjectType(new String[]{"jar"});
        assertEquals(metric.getValue(builder.build()), new LongValueData(2L));
    }

    @Test
    public void testSingleUserFilterShouldReturnZero() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130102");
        builder.put(Parameters.USER, "user2@gmail.com");

        Metric metric = new TestAbstractProjectType(new String[]{"jar"});
        assertEquals(metric.getValue(builder.build()), new LongValueData(0L));
    }

    @Test
    public void testDoubleUserFilter() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130102");
        builder.put(Parameters.USER, "user1@gmail.com,user1@yahoo.com");

        Metric metric = new TestAbstractProjectType(new String[]{"jar"});
        assertEquals(metric.getValue(builder.build()), new LongValueData(2L));
    }

    @Test
    public void testComplexFilterSingleParam() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130102");
        builder.put(Parameters.USER, "user1@gmail.com,user1@yahoo.com");
        builder.put(Parameters.WS, "ws1,ws2");

        Metric metric = new TestAbstractProjectType(new String[]{"jar"});
        assertEquals(metric.getValue(builder.build()), new LongValueData(2L));
    }

    @Test
    public void testComplexFilterDoubleParam() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130102");
        builder.put(Parameters.USER, "user1@gmail.com,user1@yahoo.com");
        builder.put(Parameters.WS, "ws1,ws2");

        Metric metric = new TestAbstractProjectType(new String[]{"jar", "war"});
        assertEquals(metric.getValue(builder.build()), new LongValueData(4L));
    }

    @Test
    public void testAllTypes() throws Exception {       
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130102");

        Metric metric = new TestProjectTypes();
        Map<String, ValueData> items = ((MapValueData)metric.getValue(builder.build())).getAll();

        assertEquals(items.size(), 2);
        assertEquals(items.get("jar"), new LongValueData(2));
        assertEquals(items.get("war"), new LongValueData(2));
    }

    private class TestProjectTypes extends ProjectTypes {
        @Override
        public String getStorageCollectionName() {
            return PROJECT_TYPES_COLLECTION;
        }
    }

    private class TestAbstractProjectType extends AbstractProjectType {

        private TestAbstractProjectType(String[] types) {
            super("testnumberofeventsbytypes_projecttypes", types);
        }

        @Override
        public String getDescription() {
            return null;
        }
    }
}
