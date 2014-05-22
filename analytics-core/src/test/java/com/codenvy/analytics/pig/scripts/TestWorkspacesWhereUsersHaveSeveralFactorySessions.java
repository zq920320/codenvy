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
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.metrics.*;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

/** @author Alexander Reshetnyak */
public class TestWorkspacesWhereUsersHaveSeveralFactorySessions extends BaseTest {

    @BeforeClass
    public void init() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createSessionFactoryStartedEvent("id1", "tmp-1", "user1", "true", "brType")
                                .withDate("2013-02-10").withTime("10:00:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id1", "tmp-1", "user1")
                                .withDate("2013-02-10").withTime("10:05:00").build());

        events.add(Event.Builder.createSessionFactoryStartedEvent("id2", "tmp-2", "user1", "true", "brType")
                                .withDate("2013-02-10").withTime("10:20:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id2", "tmp-2", "user1")
                                .withDate("2013-02-10").withTime("10:30:00").build());

        events.add(Event.Builder.createSessionFactoryStartedEvent("id3", "tmp-3", "anonymoususer_1", "false", "brType")
                                .withDate("2013-02-10").withTime("11:00:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id3", "tmp-3", "anonymoususer_1")
                                .withDate("2013-02-10").withTime("11:15:00").build());

        events.add(Event.Builder.createSessionFactoryStartedEvent("id4", "tmp-4", "anonymoususer_1", "false", "brType")
                                .withDate("2013-02-10").withTime("11:20:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id4", "tmp-4", "anonymoususer_1")
                                .withDate("2013-02-10").withTime("11:30:00").build());

        events.add(Event.Builder.createSessionFactoryStartedEvent("id5", "tmp-2", "user1", "true", "brType")
                                .withDate("2013-02-10").withTime("11:20:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id5", "tmp-2", "user1")
                                .withDate("2013-02-10").withTime("11:30:00").build());

        events.add(Event.Builder.createSessionFactoryStartedEvent("id6", "tmp-3", "anonymoususer_1", "false", "brType")
                                .withDate("2013-02-10").withTime("12:00:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id7", "tmp-3", "anonymoususer_1")
                                .withDate("2013-02-10").withTime("12:15:00").build());

        events.add(Event.Builder.createSessionFactoryStartedEvent("id8", "tmp-2", "user1", "true", "brType")
                                .withDate("2013-02-10").withTime("12:20:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id8", "tmp-2", "user1")
                                .withDate("2013-02-10").withTime("12:30:00").build());

        events.add(Event.Builder.createSessionFactoryStartedEvent("id9", "tmp-3", "anonymoususer_1", "false", "brType")
                                .withDate("2013-02-10").withTime("13:00:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id9", "tmp-3", "anonymoususer_1")
                                .withDate("2013-02-10").withTime("13:15:00").build());

        events.add(Event.Builder.createSessionFactoryStartedEvent("id10", "tmp-2", "anonymoususer_1", "false", "brType")
                                .withDate("2013-02-10").withTime("14:20:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id10", "tmp-2", "anonymoususer_1")
                                .withDate("2013-02-10").withTime("14:30:00").build());

        events.add(Event.Builder.createSessionFactoryStartedEvent("id11", "tmp-2", "anonymoususer_1", "false", "brType")
                                .withDate("2013-02-10").withTime("15:00:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id11", "tmp-2", "anonymoususer_1")
                                .withDate("2013-02-10").withTime("15:15:00").build());


        events.add(Event.Builder.createFactoryProjectImportedEvent("tmp-1", "user1", "project", "type")
                                .withDate("2013-02-10").withTime("10:05:00").build());

        events.add(
                Event.Builder.createFactoryUrlAcceptedEvent("tmp-1", "factoryUrl0", "referrer1", "org1", "affiliate1")
                             .withDate("2013-02-10").withTime("11:00:00").build());
        events.add(
                Event.Builder.createFactoryUrlAcceptedEvent("tmp-2", "factoryUrl1", "referrer2", "org2", "affiliate1")
                             .withDate("2013-02-10").withTime("11:00:01").build());
        events.add(
                Event.Builder.createFactoryUrlAcceptedEvent("tmp-3", "factoryUrl1", "referrer2", "org3", "affiliate2")
                             .withDate("2013-02-10").withTime("11:00:02").build());
        events.add(
                Event.Builder.createFactoryUrlAcceptedEvent("tmp-4", "factoryUrl0", "referrer3", "org4", "affiliate2")
                             .withDate("2013-02-10").withTime("11:00:03").build());


        events.add(Event.Builder.createTenantCreatedEvent("tmp-1", "user1")
                                .withDate("2013-02-10").withTime("12:00:00").build());
        events.add(Event.Builder.createTenantCreatedEvent("tmp-2", "user1")
                                .withDate("2013-02-10").withTime("12:01:00").build());

        // run event for session #1
        events.add(Event.Builder.createRunStartedEvent("user1", "tmp-1", "project", "type", "id")
                                .withDate("2013-02-10").withTime("10:03:00").build());

        events.add(Event.Builder.createProjectDeployedEvent("user1", "tmp-1", "session", "project", "type",
                                                            "local")
                                .withDate("2013-02-10")
                                .withTime("10:04:00")
                                .build());

        events.add(Event.Builder.createProjectBuiltEvent("user1", "tmp-1", "session", "project", "type")
                                .withDate("2013-02-10")
                                .withTime("10:04:00")
                                .build());


        // create user
        events.add(Event.Builder.createUserAddedToWsEvent("", "", "", "tmp-3", "anonymoususer_1", "website")
                                .withDate("2013-02-10").build());

        events.add(Event.Builder.createUserChangedNameEvent("anonymoususer_1", "user4@gmail.com").withDate("2013-02-10")
                                .build());

        events.add(Event.Builder.createUserCreatedEvent("user-id2", "user4@gmail.com").withDate("2013-02-10").build());


        //___
        events.add(Event.Builder.createUserAddedToWsEvent("", "", "", "tmp-2", "anonymoususer_1", "website2")
                                .withDate("2013-02-10").build());

        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130210");
        builder.put(Parameters.TO_DATE, "20130210");
        builder.putAll(scriptsManager.getScript(ScriptType.ACCEPTED_FACTORIES, MetricType.FACTORIES_ACCEPTED_LIST).getParamsAsMap());
        builder.put(Parameters.LOG, log.getAbsolutePath());
        pigServer.execute(ScriptType.ACCEPTED_FACTORIES, builder.build());

        builder.putAll(
                scriptsManager.getScript(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, builder.build());
    }

    @Test
    public void testMetricWorkspacesWhereUsersHaveSeveralFactorySessions() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130210");
        builder.put(Parameters.TO_DATE, "20130210");

        Metric metric = MetricFactory.getMetric(MetricType.WORKSPACES_WHERE_USERS_HAVE_SEVERAL_FACTORY_SESSIONS);
        LongValueData lvd = (LongValueData)metric.getValue(builder.build());

        assertEquals(lvd.getAsLong(), 2);
    }
}