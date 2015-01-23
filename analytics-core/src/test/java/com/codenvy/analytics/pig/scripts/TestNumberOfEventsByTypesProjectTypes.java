/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
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
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

/** @author Anatoliy Bazko */
public class TestNumberOfEventsByTypesProjectTypes extends BaseTest {

    @BeforeClass
    public void init() throws Exception {
        addPersistentWs(WID1, "ws1");
        addPersistentWs(WID2, "ws2");

        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createProjectCreatedEvent("user1@gmail.com", "ws1", "project1", "jar")
                                .withDate("2013-01-01", "10:00:00").build());
        events.add(Event.Builder.createProjectCreatedEvent("user2@gmail.com", "ws2", "project2", "war")
                                .withDate("2013-01-01", "10:00:01").build());

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.LOG, LogGenerator.generateLog(events).getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.PROJECTS).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());
    }

    @Test
    public void testSingleDateFilterSingleParam() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.PROJECT_TYPE_JAR);
        assertEquals(metric.getValue(Context.EMPTY), new LongValueData(1L));
    }

    @Test
    public void testSingleDateFilterDoubleParam() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.PROJECT_TYPE_WAR);
        assertEquals(metric.getValue(Context.EMPTY), new LongValueData(1L));
    }
}
