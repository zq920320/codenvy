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
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.AbstractMetric;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsList;
import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsMap;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Anatoliy Bazko
 */
public class TestFiltersByWs extends BaseTest {

    @BeforeClass
    public void prepare() throws Exception {
        List<Event> events = new ArrayList<>();

        events.add(Event.Builder.createWorkspaceCreatedEvent(WID1, "ws1", "uid1").withDate("2013-01-01").withTime("10:00:00,000").build());
        events.add(Event.Builder.createWorkspaceCreatedEvent(WID2, "ws2", "uid2").withDate("2013-01-01").withTime("10:00:00,000").build());
        events.add(Event.Builder.createWorkspaceCreatedEvent(WID3, "tmp-3", "uid3").withDate("2013-01-01").withTime("10:00:00,000").build());
        events.add(Event.Builder.createWorkspaceCreatedEvent(WID4, "tmp-4", "uid4").withDate("2013-01-01").withTime("10:00:00,000").build());

        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.WORKSPACES_PROFILES, MetricType.WORKSPACES_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.WORKSPACES_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.USERS_ACTIVITY, MetricType.USERS_ACTIVITY).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_ACTIVITY, builder.build());
    }

    @Test(dataProvider = "metricsToTest")
    public void testFilterByID(Metric metric, String wsIdField) throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.WS_ID, WID1);

        ListValueData l = getAsList(metric, builder.build());

        assertResult(l, wsIdField, WID1);
    }

    @Test(dataProvider = "metricsToTest")
    public void testSeveralFilterByID(Metric metric, String wsIdField) throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.WS_ID, WID1 + " OR " + WID3);

        ListValueData l = getAsList(metric, builder.build());

        assertResult(l, wsIdField, WID1, WID3);
    }

    @Test(dataProvider = "metricsToTest")
    public void testNegotiatedFilterByID(Metric metric, String wsIdField) throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.WS_ID, "~ " + WID1);

        ListValueData l = getAsList(metric, builder.build());

        assertResult(l, wsIdField, WID2, WID3, WID4);
    }

    @Test(dataProvider = "metricsToTest")
    public void testFilterByWSUseCase1(Metric metric, String wsIdField) throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.WS, WID1);

        ListValueData l = getAsList(metric, builder.build());

        assertResult(l, wsIdField, WID1);
    }

    @Test(dataProvider = "metricsToTest")
    public void testFilterByWSUseCase2(Metric metric, String wsIdField) throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.WS, WID1 + " OR ws2");

        ListValueData l = getAsList(metric, builder.build());

        assertResult(l, wsIdField, WID1, WID2);
    }

    @Test(dataProvider = "metricsToTest")
    public void testFilterByWSUseCase3(Metric metric, String wsIdField) throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.WS, "~ " + WID1 + " OR ws2");

        ListValueData l = getAsList(metric, builder.build());

        assertResult(l, wsIdField, WID3, WID4);
    }

    @Test(dataProvider = "metricsToTest")
    public void testFilterByName(Metric metric, String wsIdField) throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.WS, "ws1");

        ListValueData l = getAsList(metric, builder.build());

        assertResult(l, wsIdField, WID1);
    }

    @Test(dataProvider = "metricsToTest")
    public void testSeveralFilterByName(Metric metric, String wsIdField) throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.WS, "ws1 OR tmp-3");

        ListValueData l = getAsList(metric, builder.build());

        assertResult(l, wsIdField, WID1, WID3);
    }

    @Test(dataProvider = "metricsToTest")
    public void testNegotiatedFilterByName(Metric metric, String wsIdField) throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.WS, "~ ws1");

        ListValueData l = getAsList(metric, builder.build());

        assertResult(l, wsIdField, WID2, WID3, WID4);
    }


    @Test(dataProvider = "metricsToTest")
    public void testMixFiltersUseCase1(Metric metric, String wsIdField) throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.WS, "ws1 OR ws2 OR tmp-4");
        builder.put(MetricFilter.WS_ID, WID1 + " OR " + WID2);

        ListValueData l = getAsList(metric, builder.build());

        assertResult(l, wsIdField, WID1, WID2);
    }

    @Test(dataProvider = "metricsToTest")
    public void testMixFiltersUseCase2(Metric metric, String wsIdField) throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.WS, "ws1 OR ws2 OR tmp-4");
        builder.put(MetricFilter.WS_ID, "~ " + WID1);

        ListValueData l = getAsList(metric, builder.build());

        assertResult(l, wsIdField, WID2, WID4);
    }

    @Test(dataProvider = "metricsToTest")
    public void testMixFiltersUseCase3(Metric metric, String wsIdField) throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.WS, "~ ws2");
        builder.put(MetricFilter.WS_ID, "~ " + WID1);

        ListValueData l = getAsList(metric, builder.build());

        assertResult(l, wsIdField, WID3, WID4);
    }

    @Test(dataProvider = "metricsToTest")
    public void testMixFiltersUseCase4(Metric metric, String wsIdField) throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.WS, Parameters.WS_TYPES.ANY.toString());

        ListValueData l = getAsList(metric, builder.build());

        assertResult(l, wsIdField, WID1, WID2, WID3, WID4);
    }

    @Test(dataProvider = "metricsToTest")
    public void testMixFiltersUseCase5(Metric metric, String wsIdField) throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.WS, Parameters.WS_TYPES.PERSISTENT.toString());

        ListValueData l = getAsList(metric, builder.build());

        assertResult(l, wsIdField, WID1, WID2);
    }

    @Test(dataProvider = "metricsToTest")
    public void testMixFiltersUseCase6(Metric metric, String wsIdField) throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.WS, Parameters.WS_TYPES.TEMPORARY.toString());

        ListValueData l = getAsList(metric, builder.build());

        assertResult(l, wsIdField, WID3, WID4);
    }

    @Test(dataProvider = "metricsToTest")
    public void testMixFiltersUseCase7(Metric metric, String wsIdField) throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.WS, "ws1");
        builder.put(MetricFilter.WS_ID, WID2);

        ListValueData l = getAsList(metric, builder.build());
        assertTrue(l.isEmpty());
    }

    @Test(dataProvider = "metricsToTest")
    public void testMixFiltersUseCase8(Metric metric, String wsIdField) throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.WS, "ws1 OR " + WID2);
        builder.put(MetricFilter.WS_ID, WID2);

        ListValueData l = getAsList(metric, builder.build());

        assertResult(l, wsIdField, WID2);
    }

    private void assertResult(ListValueData l, String wsIdField, String... ws) {
        assertEquals(l.size(), ws.length);

        Set<String> actual = new HashSet<>();
        for (ValueData valueData : l.getAll()) {
            ValueData wsId = treatAsMap(valueData).get(wsIdField);
            actual.add(wsId.getAsString());
        }

        Set<String> expected = new HashSet<>(Arrays.asList(ws));
        assertEquals(actual, expected);
    }

    @DataProvider(name = "metricsToTest")
    public static Object[][] metricsToTest() {
        return new Object[][]{{MetricFactory.getMetric(MetricType.USERS_ACTIVITY_LIST), AbstractMetric.WS},
                              {MetricFactory.getMetric(MetricType.WORKSPACES_PROFILES_LIST), AbstractMetric.ID}};
    }
}
