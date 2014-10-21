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
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.metrics.sessions.factory.TotalFactories;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

/** @author <a href="mailto:dnochevnov@codenvy.com">Dmytro Nochevnov</a> */
public class TestFactoriesMetrics extends BaseTest {

    private static final long INITIAL_VALUE_OF_TOTAL_FACTORIES = 4000;

    @BeforeClass
    public void init() throws Exception {
        List<Event> events = new ArrayList<>();

        events.add(Event.Builder.createUserCreatedEvent("uid1", "user1@gmail.com", "user1@gmail.com")
                                .withDate("2013-02-10").withTime("13:00:00,000").build());
        events.add(Event.Builder.createWorkspaceCreatedEvent("wsid1", "ws1", "user1@gmail.com")
                                .withDate("2013-02-10").withTime("13:00:00").build());
        events.add(Event.Builder.createFactoryCreatedEvent("ws1", "user1@gmail.com", "project1", "type1", "repo1", "factory1", "", "")
                                .withDate("2013-02-10").withTime("13:00:00").build());
        events.add(Event.Builder.createFactoryCreatedEvent("ws1", "user1@gmail.com", "project2", "type1", "repo2", "factory2", "", "")
                                .withDate("2013-02-11").withTime("13:00:00").build());


        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130210");
        builder.put(Parameters.TO_DATE, "20130210");
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.USERS_PROFILES, MetricType.USERS_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.WORKSPACES_PROFILES, MetricType.WORKSPACES_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.WORKSPACES_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.CREATED_FACTORIES, MetricType.CREATED_FACTORIES_SET).getParamsAsMap());
        pigServer.execute(ScriptType.CREATED_FACTORIES, builder.build());

        builder.put(Parameters.FROM_DATE, "20130211");
        builder.put(Parameters.TO_DATE, "20130211");
        pigServer.execute(ScriptType.CREATED_FACTORIES, builder.build());
    }

    @Test
    public void testTotalFactories() throws Exception {
        Context.Builder builder = new Context.Builder();
        TotalFactories metric = (TotalFactories)MetricFactory.getMetric(MetricType.TOTAL_FACTORIES);

        // total factories for one day             
        builder.put(Parameters.TO_DATE, "20130210");
        LongValueData value = (LongValueData)metric.getValue(builder.build());
        assertEquals(value.getAsLong(), INITIAL_VALUE_OF_TOTAL_FACTORIES + 1);

        // total factories for two days
        builder.put(Parameters.TO_DATE, "20130211");
        value = (LongValueData)metric.getValue(builder.build());
        assertEquals(value.getAsLong(), INITIAL_VALUE_OF_TOTAL_FACTORIES + 2);

    }
}