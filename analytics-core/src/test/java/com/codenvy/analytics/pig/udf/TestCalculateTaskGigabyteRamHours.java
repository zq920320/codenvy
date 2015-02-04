/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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
package com.codenvy.analytics.pig.udf;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.apache.pig.data.Tuple;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.codenvy.analytics.pig.scripts.util.Event.Builder.createFactoryUrlAcceptedEvent;
import static org.testng.Assert.assertEquals;

/** @author Dmytro Nochevnov */
public class TestCalculateTaskGigabyteRamHours extends BaseTest {
    @BeforeClass
    public void setUp() throws Exception {
        prepareData();
        doIntegrity("20131020");
    }

    private Tuple makeTuple(String factoryId) {
        Tuple tuple = tupleFactory.newTuple();
        tuple.append(factoryId);

        return tuple;
    }

    @Test(dataProvider = "builds-provider")
    public void testBuilds(String factoryId, Double result) throws Exception {
        CalculateBuildsGigabyteRamHours function = new CalculateBuildsGigabyteRamHours();

        Tuple tuple = makeTuple(factoryId);
        assertEquals(function.exec(tuple), result);
    }

    @DataProvider(name = "builds-provider")
    public Object[][] createBuildsData() {
        return new Object[][]{
            {null, null},
            {"", null},
            {"non-exists", null},
            {"factory1", 0.054},
        };
    }

    @Test(dataProvider = "runs-provider")
    public void testRuns(String factoryId, Double result) throws Exception {
        CalculateRunsGigabyteRamHours function = new CalculateRunsGigabyteRamHours();

        Tuple tuple = makeTuple(factoryId);
        assertEquals(function.exec(tuple), result);
    }

    @DataProvider(name = "runs-provider")
    public Object[][] createRunsData() {
        return new Object[][]{
            {null, null},
            {"", null},
            {"non-exists", null},
            {"factory1", 0.0083},
        };
    }

    @Test(dataProvider = "debugs-provider")
    public void testDebugs(String factoryId, Double result) throws Exception {
        CalculateDebugsGigabyteRamHours function = new CalculateDebugsGigabyteRamHours();

        Tuple tuple = makeTuple(factoryId);
        assertEquals(function.exec(tuple), result);
    }

    @DataProvider(name = "debugs-provider")
    public Object[][] createDebugsData() {
        return new Object[][]{
            {null, null},
            {"", null},
            {"non-exists", null},
            {"factory1", 0.0062},
        };
    }

    @Test(dataProvider = "edits-provider")
    public void testEdits(String factoryId, Double result) throws Exception {
        CalculateEditsGigabyteRamHours function = new CalculateEditsGigabyteRamHours();

        Tuple tuple = makeTuple(factoryId);
        assertEquals(function.exec(tuple), result);
    }

    @DataProvider(name = "edits-provider")
    public Object[][] createEditsData() {
        return new Object[][]{
            {null, null},
            {"", null},
            {"non-exists", null},
            {"factory1", 0.0012},
        };
    }

    void prepareData() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131020");
        builder.put(Parameters.TO_DATE, "20131020");
        builder.put(Parameters.LOG, initLogs().getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.ACCEPTED_FACTORIES, MetricType.FACTORIES_ACCEPTED_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.ACCEPTED_FACTORIES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.TASKS, MetricType.TASKS).getParamsAsMap());
        pigServer.execute(ScriptType.TASKS, builder.build());
    }

    private File initLogs() throws Exception {
        List<Event> events = new ArrayList<>();

        /** FACTORY ACCEPTED EVENTS */
        events.add(createFactoryUrlAcceptedEvent("temp-ws1", "http://1.com?id=factory1", "", "", "").withDate("2013-10-20").withTime("16:00:00").build());

        /** BUILD EVENTS */
        // #1 2min, stopped normally
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("10:00:00")
                                      .withParam("EVENT", "build-started")
                                      .withParam("WS", "temp-ws1")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project1")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id1_b")
                                      .withParam("TIMEOUT", "600")
                                      .build());
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("10:02:00")
                                      .withParam("EVENT", "build-finished")
                                      .withParam("WS", "temp-ws1")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project1")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id1_b")
                                      .withParam("TIMEOUT", "600")
                                      .build());

        // #2 1m, stopped normally
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("11:00:00")
                                      .withParam("EVENT", "build-started")
                                      .withParam("WS", "temp-ws1")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project2")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id2_b")
                                      .withParam("TIMEOUT", "-1")
                                      .build());
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("11:01:00")
                                      .withParam("EVENT", "build-finished")
                                      .withParam("WS", "temp-ws1")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project2")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id2_b")
                                      .withParam("MEMORY", "250")
                                      .withParam("TIMEOUT", "-1")
                                      .build());

        /** RUN EVENTS */
        // #1 2min, stopped by user
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("10:00:00")
                                      .withParam("EVENT", "run-started")
                                      .withParam("WS", "temp-ws1")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project1")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id1_r")
                                      .withParam("MEMORY", "128")
                                      .withParam("LIFETIME", "600")
                                      .build());
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("10:02:00")
                                      .withParam("EVENT", "run-finished")
                                      .withParam("WS", "temp-ws1")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project1")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id1_r")
                                      .withParam("MEMORY", "128")
                                      .withParam("LIFETIME", "600")
                                      .build());

        // #2 1m, stopped by user
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("11:00:00")
                                      .withParam("EVENT", "run-started")
                                      .withParam("WS", "temp-ws1")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project2")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id2_r")
                                      .withParam("MEMORY", "128")
                                      .withParam("LIFETIME", "-1")
                                      .build());
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("11:02:00")
                                      .withParam("EVENT", "run-finished")
                                      .withParam("WS", "temp-ws1")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project2")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id2_r")
                                      .withParam("MEMORY", "128")
                                      .withParam("LIFETIME", "-1")
                                      .build());

        /** DEBUGS EVENTS */
        // #1 2min, stopped by user
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("13:00:00")
                                      .withParam("EVENT", "debug-started")
                                      .withParam("WS", "temp-ws1")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id1_d")
                                      .withParam("MEMORY", "128")
                                      .withParam("LIFETIME", "600")
                                      .build());
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("13:02:00")
                                      .withParam("EVENT", "debug-finished")
                                      .withParam("WS", "temp-ws1")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id1_d")
                                      .withParam("MEMORY", "128")
                                      .withParam("LIFETIME", "600")
                                      .build());

        // #2 1m, stopped by user
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("14:00:00")
                                      .withParam("EVENT", "debug-started")
                                      .withParam("WS", "temp-ws1")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id2_d")
                                      .withParam("MEMORY", "128")
                                      .withParam("LIFETIME", "-1")
                                      .build());
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("14:01:00")
                                      .withParam("EVENT", "debug-finished")
                                      .withParam("WS", "temp-ws1")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id2_d")
                                      .withParam("MEMORY", "128")
                                      .withParam("LIFETIME", "-1")
                                      .build());

        /** EDIT EVENTS */
        events.add(Event.Builder.createSessionUsageEvent("anonymoususer_user11", "temp-ws1", "session1", true)
                                .withDate("2013-10-20")
                                .withTime("16:00:00")
                                .build());
        events.add(Event.Builder.createSessionUsageEvent("anonymoususer_user11", "temp-ws1", "session1", true)
                                .withDate("2013-10-20")
                                .withTime("16:03:00")
                                .build());

        events.add(Event.Builder.createSessionUsageEvent("user1@gmail.com", "ws1", "session2", false)
                                .withDate("2013-10-20")
                                .withTime("16:20:00")
                                .build());
        events.add(Event.Builder.createSessionUsageEvent("user1@gmail.com", "ws1", "session2", false)
                                .withDate("2013-10-20")
                                .withTime("16:23:00")
                                .build());

        return LogGenerator.generateLog(events);
    }

}
