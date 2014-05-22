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
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.Configurator;
import com.codenvy.analytics.Injector;
import com.codenvy.analytics.datamodel.*;
import com.codenvy.analytics.metrics.projects.*;
import com.codenvy.analytics.metrics.sessions.*;
import com.codenvy.analytics.metrics.sessions.factory.AbstractFactorySessions;
import com.codenvy.analytics.metrics.sessions.factory.FactorySessionsBelow10Min;
import com.codenvy.analytics.metrics.sessions.factory.FactorySessionsWithBuildPercent;
import com.codenvy.analytics.metrics.sessions.factory.ProductUsageFactorySessionsList;
import com.codenvy.analytics.metrics.users.*;
import com.codenvy.analytics.metrics.workspaces.ActiveWorkspaces;
import com.codenvy.analytics.persistent.JdbcDataPersisterFactory;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;
import com.codenvy.analytics.services.configuration.XmlConfigurationManager;
import com.codenvy.analytics.services.view.*;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsList;
import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/** @author <a href="mailto:dnochevnov@codenvy.com">Dmytro Nochevnov</a> */
public class TestExpandedMetric extends BaseTest {

    private static final String TEST_WS    = "ws1";
    private static final String TEST_USER  = "user1@gmail.com";
    private static final String SESSION_ID = "session_id";

    private ViewBuilder viewBuilder;

    private File log;

    private static final String ANALYSIS_VIEW_CONFIGURATION = BASE_DIR + "/classes/views/analysis.xml";

    @BeforeClass
    public void prepareDatabase() throws IOException, ParseException {
        List<Event> events = new ArrayList<>();

        // add user activity at previous day
        events.add(Event.Builder.createUserAddedToWsEvent("user5@gmail.com", TEST_WS, "", "", "", "website")
                                .withDate("2013-10-31").withTime("10:00:00").build());


        // create user from factory
        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-4", "factoryUrl1", "referrer1", "org1", "affiliate1")
                                .withDate("2013-11-01").withTime("09:01:00").build());
        events.add(Event.Builder.createUserAddedToWsEvent("", "", "", "tmp-4", "anonymoususer_4", "website")
                                .withDate("2013-11-01").withTime("09:02:00").build());
        events.add(Event.Builder.createUserChangedNameEvent("anonymoususer_4", "user4@gmail.com")
                                .withDate("2013-11-01").withTime("09:03:00").build());
        events.add(Event.Builder.createUserCreatedEvent("user-id4", "user4@gmail.com")
                                .withDate("2013-11-01").withTime("09:04:00").build());

        // create user
        events.add(Event.Builder.createUserCreatedEvent("user-id5", "user5")
                                .withDate("2013-11-01").withTime("09:05:00").build());

        // create factory session events
        events.add(Event.Builder.createSessionFactoryStartedEvent("factory-id1", "tmp-1", "user1", "true", "brType")
                                .withDate("2013-11-01").withTime("10:00:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("factory-id1", "tmp-1", "user1")
                                .withDate("2013-11-01").withTime("10:05:00").build());

        events.add(Event.Builder.createSessionFactoryStartedEvent("factory-id2", "tmp-2", "user1", "true", "brType")
                                .withDate("2013-11-01").withTime("10:20:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("factory-id2", "tmp-2", "user1")
                                .withDate("2013-11-01").withTime("10:30:00").build());

        events.add(Event.Builder.createSessionFactoryStartedEvent("factory-id3", "tmp-3", "anonymoususer_1", "false", "brType")
                                .withDate("2013-11-01").withTime("11:00:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("factory-id3", "tmp-3", "anonymoususer_1")
                                .withDate("2013-11-01").withTime("11:15:00").build());

        events.add(Event.Builder.createFactoryProjectImportedEvent("tmp-1", "user1", "project", "type")
                                .withDate("2013-11-01").withTime("10:05:00").build());

        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-1", "factoryUrl1", "http://referrer1", "org1", "affiliate1")
                                .withDate("2013-11-01").withTime("11:00:00").build());
        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-2", "factoryUrl1", "http://referrer2", "org2", "affiliate1")
                                .withDate("2013-11-01").withTime("11:00:01").build());
        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-3", "factoryUrl1", "http://referrer3", "org3", "affiliate2")
                                .withDate("2013-11-01").withTime("11:00:02").build());

        events.add(Event.Builder.createTenantCreatedEvent("tmp-1", "user1")
                                .withDate("2013-11-01").withTime("12:00:00").build());
        events.add(Event.Builder.createTenantCreatedEvent("tmp-2", "user1")
                                .withDate("2013-11-01").withTime("12:01:00").build());

        // build event for session #1
        events.add(Event.Builder.createBuildStartedEvent("user1", "tmp-1", "project", "type", "id1")
                                .withDate("2013-11-01").withTime("10:03:00").build());


        // same user invites twice
        events.add(Event.Builder.createUserInviteEvent(TEST_USER, TEST_WS, TEST_USER + "_invite")
                                .withDate("2013-11-01").withTime("15:00:00,155").build());
        events.add(Event.Builder.createUserInviteEvent(TEST_USER, TEST_WS, TEST_USER + "_invite")
                                .withDate("2013-11-01").withTime("16:00:00,155").build());
        // add user to workspace by accepting invite
        events.add(Event.Builder.createUserAddedToWsEvent(TEST_USER + "_invite", TEST_WS, "", "", "", "invite")
                                .withDate("2013-11-01").withTime("16:01:03").build());


        // login users
        events.add(Event.Builder.createUserSSOLoggedInEvent(TEST_USER, "jaas")
                                .withDate("2013-11-01").withTime("18:55:00,155").build());
        events.add(Event.Builder.createUserSSOLoggedInEvent("user2@gmail.com", "google")
                                .withDate("2013-11-01").withTime("19:55:00,155").build());

        // start main session
        events.add(Event.Builder.createSessionStartedEvent(TEST_USER, TEST_WS, "ide", SESSION_ID)
                                .withDate("2013-11-01").withTime("19:00:00,155").build());

        // create test projects and deploy they into PaaS
        events.add(Event.Builder.createProjectCreatedEvent(TEST_USER, TEST_WS, "id1", "project1", "python")
                                .withDate("2013-11-01").withTime("18:08:00,600").build());
        events.add(Event.Builder.createApplicationCreatedEvent(TEST_USER, TEST_WS, "id1", "project1", "python", "gae")
                                .withDate("2013-11-01").withTime("18:08:10").build());

        events.add(Event.Builder.createProjectCreatedEvent(TEST_USER, "ws2", "id2", "project2", "war")
                                .withDate("2013-11-01").withTime("18:12:00").build());
        events.add(Event.Builder.createApplicationCreatedEvent(TEST_USER, "ws2", "id2", "project2", "war", "gae")
                                .withDate("2013-11-01").withTime("18:12:30").build());

        events.add(Event.Builder.createProjectCreatedEvent("user2@gmail.com", "ws3", "id3", "project2", "java")
                                .withDate("2013-11-01").withTime("18:20:10").build());
        events.add(Event.Builder.createProjectDeployedEvent("user2@gmail.com", "ws3", "id3", "project2", "java", "local")
                                .withDate("2013-11-01").withTime("18:21:30").build());

        // event of target user in the target workspace and in time of first session
        events.add(Event.Builder.createRunStartedEvent(TEST_USER, TEST_WS, "project1", "Python", "id1")
                                .withDate("2013-11-01").withTime("19:08:00,600").build());
        events.add(Event.Builder.createRunFinishedEvent(TEST_USER, TEST_WS, "project1", "Python", "id1")
                                .withDate("2013-11-01").withTime("19:10:00,900").build());

        // event of target user in another workspace and in time of main session
        events.add(Event.Builder.createBuildStartedEvent(TEST_USER, "ws2", "project2", "war", "id2")
                                .withDate("2013-11-01").withTime("19:12:00").build());
        events.add(Event.Builder.createBuildFinishedEvent(TEST_USER, "ws2", "project2", "war", "id2")
                                .withDate("2013-11-01").withTime("19:14:00").build());

        // event of another user in the another workspace and in time of main session
        events.add(Event.Builder.createRunStartedEvent("user2@gmail.com", "ws3", "project2", "java", "id3")
                                .withDate("2013-11-01").withTime("19:08:00").build());
        events.add(Event.Builder.createRunFinishedEvent("user2@gmail.com", "ws3", "project2", "java", "id3")
                                .withDate("2013-11-01").withTime("19:10:00").build());

        // finish main session
        events.add(Event.Builder.createSessionFinishedEvent(TEST_USER, TEST_WS, "ide", SESSION_ID)
                                .withDate("2013-11-01").withTime("19:55:00,555").build());

        // make micro-session with duration < than 1 min
        events.add(Event.Builder.createSessionStartedEvent("user4@gmail.com", TEST_WS, "ide", SESSION_ID + "_micro")
                                .withDate("2013-11-01").withTime("23:00:00,155").build());
        // finish main session
        events.add(Event.Builder.createSessionFinishedEvent("user4@gmail.com", TEST_WS, "ide", SESSION_ID + "_micro")
                                .withDate("2013-11-01").withTime("23:00:30,555").build());

        // add user6@gmail.com activity (6 sessions && (120min < time < 300min)) for test
        // testAbstractTimelineProductUsageConditionMetric
        events.add(Event.Builder.createSessionStartedEvent("user6@gmail.com", TEST_WS, "ide", "user6@gmail.com1")
                                .withDate("2013-11-20").withTime("01:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user6@gmail.com", TEST_WS, "ide", "user6@gmail.com1")
                                .withDate("2013-11-20").withTime("03:01:00").build());

        events.add(Event.Builder.createSessionStartedEvent("user6@gmail.com", TEST_WS, "ide", "user6@gmail.com2")
                                .withDate("2013-11-20").withTime("04:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user6@gmail.com", TEST_WS, "ide", "user6@gmail.com2")
                                .withDate("2013-11-20").withTime("04:01:00").build());

        events.add(Event.Builder.createSessionStartedEvent("user6@gmail.com", TEST_WS, "ide", "user6@gmail.com3")
                                .withDate("2013-11-20").withTime("05:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user6@gmail.com", TEST_WS, "ide", "user6@gmail.com3")
                                .withDate("2013-11-20").withTime("05:01:00").build());

        events.add(Event.Builder.createSessionStartedEvent("user6@gmail.com", TEST_WS, "ide", "user6@gmail.com4")
                                .withDate("2013-11-20").withTime("06:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user6@gmail.com", TEST_WS, "ide", "user6@gmail.com4")
                                .withDate("2013-11-20").withTime("06:01:00").build());

        events.add(Event.Builder.createSessionStartedEvent("user6@gmail.com", TEST_WS, "ide", "user6@gmail.com5")
                                .withDate("2013-11-20").withTime("07:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user6@gmail.com", TEST_WS, "ide", "user6@gmail.com5")
                                .withDate("2013-11-20").withTime("07:01:00").build());

        events.add(Event.Builder.createSessionStartedEvent("user6@gmail.com", TEST_WS, "ide", "user6@gmail.com6")
                                .withDate("2013-11-20").withTime("08:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user6@gmail.com", TEST_WS, "ide", "user6@gmail.com6")
                                .withDate("2013-11-20").withTime("08:01:00").build());

        // add user7@gmail.com activity (6 sessions, time > 300 min) for test
        // testAbstractTimelineProductUsageConditionMetric
        events.add(Event.Builder.createSessionStartedEvent("user7@gmail.com", TEST_WS, "ide", "user7@gmail.com1")
                                .withDate("2013-12-20").withTime("01:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user7@gmail.com", TEST_WS, "ide", "user7@gmail.com1")
                                .withDate("2013-12-20").withTime("03:15:00").build());

        events.add(Event.Builder.createSessionStartedEvent("user7@gmail.com", TEST_WS, "ide", "user7@gmail.com2")
                                .withDate("2013-12-20").withTime("04:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user7@gmail.com", TEST_WS, "ide", "user7@gmail.com2")
                                .withDate("2013-12-20").withTime("06:15:00").build());

        events.add(Event.Builder.createSessionStartedEvent("user7@gmail.com", TEST_WS, "ide", "user7@gmail.com3")
                                .withDate("2013-12-20").withTime("07:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user7@gmail.com", TEST_WS, "ide", "user7@gmail.com3")
                                .withDate("2013-12-20").withTime("09:15:00").build());

        events.add(Event.Builder.createSessionStartedEvent("user7@gmail.com", TEST_WS, "ide", "user7@gmail.com4")
                                .withDate("2013-12-20").withTime("10:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user7@gmail.com", TEST_WS, "ide", "user7@gmail.com4")
                                .withDate("2013-12-20").withTime("13:15:00").build());

        events.add(Event.Builder.createSessionStartedEvent("user7@gmail.com", TEST_WS, "ide", "user7@gmail.com5")
                                .withDate("2013-12-20").withTime("14:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user7@gmail.com", TEST_WS, "ide", "user7@gmail.com5")
                                .withDate("2013-12-20").withTime("16:15:00").build());

        events.add(Event.Builder.createSessionStartedEvent("user7@gmail.com", TEST_WS, "ide", "user7@gmail.com6")
                                .withDate("2013-12-20").withTime("17:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user7@gmail.com", TEST_WS, "ide", "user7@gmail.com6")
                                .withDate("2013-12-20").withTime("19:15:00").build());

        log = LogGenerator.generateLog(events);
    }

    @BeforeClass
    public void prepareViewBuilder() throws IOException {
        XmlConfigurationManager configurationManager = mock(XmlConfigurationManager.class);

        when(configurationManager.loadConfiguration(any(Class.class), anyString())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                XmlConfigurationManager manager = new XmlConfigurationManager();
                return manager.loadConfiguration(DisplayConfiguration.class, ANALYSIS_VIEW_CONFIGURATION);
            }
        });

        Configurator configurator = spy(Injector.getInstance(Configurator.class));
        doReturn(new String[]{ANALYSIS_VIEW_CONFIGURATION}).when(configurator).getArray(anyString());

        viewBuilder = spy(new ViewBuilder(Injector.getInstance(JdbcDataPersisterFactory.class),
                                          Injector.getInstance(CSVReportPersister.class),
                                          configurationManager,
                                          configurator));
    }

    @Test
    public void testAbstractTimelineProductUsageConditionMetric() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_SESSIONS, MetricType.PRODUCT_USAGE_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, builder.build());

        builder.put(Parameters.FROM_DATE, "20131120");
        builder.put(Parameters.TO_DATE, "20131120");
        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_SESSIONS, MetricType.PRODUCT_USAGE_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, builder.build());

        builder.put(Parameters.FROM_DATE, "20131220");
        builder.put(Parameters.TO_DATE, "20131220");
        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_SESSIONS, MetricType.PRODUCT_USAGE_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, builder.build());

        /** test TimelineProductUsageConditionBelow120Min */
        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131220");
        builder.put(Parameters.TIME_INTERVAL, "0");
        AbstractTimelineProductUsageCondition metric = new TimelineProductUsageConditionBelow120Min();
        Context context = metric.initContextBasedOnTimeInterval(builder.build());
        ValueData expandedValue = metric.getExpandedValue(context);
        List<ValueData> all = treatAsList(expandedValue);
        assertEquals(all.size(), 3);

        Map<String, ValueData> record = ((MapValueData)all.get(0)).getAll();
        assertEquals(record.size(), 1);
        assertEquals(record.get("user").toString(), "user4@gmail.com");
        record = ((MapValueData)all.get(1)).getAll();
        assertEquals(record.size(), 1);
        assertEquals(record.get("user").toString(), "user1@gmail.com");
        record = ((MapValueData)all.get(2)).getAll();
        assertEquals(record.size(), 1);
        assertEquals(record.get("user").toString(), "user1");

        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131220");
        builder.put(Parameters.TIME_INTERVAL, "1");
        context = metric.initContextBasedOnTimeInterval(builder.build());
        expandedValue = metric.getExpandedValue(context);
        all = treatAsList(expandedValue);
        assertEquals(all.size(), 3);

        record = ((MapValueData)all.get(0)).getAll();
        assertEquals(record.size(), 1);
        assertEquals(record.get("user").toString(), "user4@gmail.com");
        record = ((MapValueData)all.get(1)).getAll();
        assertEquals(record.size(), 1);
        assertEquals(record.get("user").toString(), "user1@gmail.com");
        record = ((MapValueData)all.get(2)).getAll();
        assertEquals(record.size(), 1);
        assertEquals(record.get("user").toString(), "user1");

        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131220");
        builder.put(Parameters.TIME_INTERVAL, "2");
        context = metric.initContextBasedOnTimeInterval(builder.build());
        expandedValue = metric.getExpandedValue(context);
        all = treatAsList(expandedValue);
        assertEquals(all.size(), 3);

        record = ((MapValueData)all.get(0)).getAll();
        assertEquals(record.size(), 1);
        assertEquals(record.get("user").toString(), "user4@gmail.com");
        record = ((MapValueData)all.get(1)).getAll();
        assertEquals(record.size(), 1);
        assertEquals(record.get("user").toString(), "user1@gmail.com");
        record = ((MapValueData)all.get(2)).getAll();
        assertEquals(record.size(), 1);
        assertEquals(record.get("user").toString(), "user1");

        /** test TimelineProductUsageConditionBetween120And300Min */
        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131220");
        builder.put(Parameters.TIME_INTERVAL, "0");
        metric = new TimelineProductUsageConditionBetween120And300Min();
        context = metric.initContextBasedOnTimeInterval(builder.build());
        expandedValue = metric.getExpandedValue(context);
        all = treatAsList(expandedValue);
        assertEquals(all.size(), 1);

        record = ((MapValueData)all.get(0)).getAll();
        assertEquals(record.size(), 1);
        assertEquals(record.get("user").toString(), "user6@gmail.com");

        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131220");
        builder.put(Parameters.TIME_INTERVAL, "1");
        context = metric.initContextBasedOnTimeInterval(builder.build());
        expandedValue = metric.getExpandedValue(context);
        all = treatAsList(expandedValue);
        assertEquals(all.size(), 1);

        record = ((MapValueData)all.get(0)).getAll();
        assertEquals(record.size(), 1);
        assertEquals(record.get("user").toString(), "user6@gmail.com");

        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131220");
        builder.put(Parameters.TIME_INTERVAL, "2");
        context = metric.initContextBasedOnTimeInterval(builder.build());
        expandedValue = metric.getExpandedValue(context);
        all = treatAsList(expandedValue);
        assertEquals(all.size(), 1);

        record = ((MapValueData)all.get(0)).getAll();
        assertEquals(record.size(), 1);
        assertEquals(record.get("user").toString(), "user6@gmail.com");


        /** test TimelineProductUsageConditionAbove300Min */
        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131220");
        builder.put(Parameters.TIME_INTERVAL, "0");
        metric = new TimelineProductUsageConditionAbove300Min();
        context = metric.initContextBasedOnTimeInterval(builder.build());
        expandedValue = metric.getExpandedValue(context);
        all = treatAsList(expandedValue);
        assertEquals(all.size(), 1);

        record = ((MapValueData)all.get(0)).getAll();
        assertEquals(record.size(), 1);
        assertEquals(record.get("user").toString(), "user7@gmail.com");

        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131220");
        builder.put(Parameters.TIME_INTERVAL, "1");
        context = metric.initContextBasedOnTimeInterval(builder.build());
        expandedValue = metric.getExpandedValue(context);
        all = treatAsList(expandedValue);
        assertEquals(all.size(), 0);

        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131220");
        builder.put(Parameters.TIME_INTERVAL, "2");
        context = metric.initContextBasedOnTimeInterval(builder.build());
        expandedValue = metric.getExpandedValue(context);
        all = treatAsList(expandedValue);
        assertEquals(all.size(), 0);
    }

    @Test
    public void testNonActiveUsersMetric() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.put(Parameters.FROM_DATE, "20131031");
        builder.put(Parameters.TO_DATE, "20131031");
        builder.putAll(scriptsManager.getScript(ScriptType.ACTIVE_ENTITIES, MetricType.ACTIVE_USERS_SET).getParamsAsMap());
        pigServer.execute(ScriptType.ACTIVE_ENTITIES, builder.build());

        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.putAll(scriptsManager.getScript(ScriptType.ACTIVE_ENTITIES, MetricType.ACTIVE_USERS_SET).getParamsAsMap());
        pigServer.execute(ScriptType.ACTIVE_ENTITIES, builder.build());

        builder.put(Parameters.FROM_DATE, "20131031");
        builder.put(Parameters.TO_DATE, "20131031");
        builder.putAll(scriptsManager.getScript(ScriptType.USERS_STATISTICS, MetricType.USERS_STATISTICS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_STATISTICS, builder.build());

        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.putAll(scriptsManager.getScript(ScriptType.USERS_STATISTICS, MetricType.USERS_STATISTICS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_STATISTICS, builder.build());

        builder.put(Parameters.FROM_DATE, "20131031");
        builder.put(Parameters.TO_DATE, "20131031");
        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.CREATED_USERS).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());

        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.CREATED_USERS).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());

        // test expanded metric value
        builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");

        Expandable metric = new NonActiveUsers();
        ValueData expandedValue = metric.getExpandedValue(builder.build());
        List<ValueData> all = treatAsList(expandedValue);
        assertEquals(all.size(), 0);

        metric = new CreatedUsers();
        expandedValue = metric.getExpandedValue(builder.build());
        all = treatAsList(expandedValue);
        assertEquals(all.size(), 2);
        assertTrue(all.contains(MapValueData.valueOf("user=user4@gmail.com")));
        assertTrue(all.contains(MapValueData.valueOf("user=user5")));

        // test filtering user list by "non_active_users" metric
        builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");

        UsersStatisticsList usersStatisticsListMetric = new UsersStatisticsList();
        ListValueData value = (ListValueData)usersStatisticsListMetric.getValue(builder.build());
        all = value.getAll();
        assertEquals(all.size(), 3);

        // calculate non-active user list
        builder.put(Parameters.EXPANDED_METRIC_NAME, "non_active_users");

        ListValueData filteredValue = (ListValueData)usersStatisticsListMetric.getValue(builder.build());
        all = filteredValue.getAll();
        assertEquals(all.size(), 0);
    }

    @Test
    public void testUsersAcceptedInvitesPercentMetric() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS_BY_TYPE, MetricType.USERS_ADDED_TO_WORKSPACES).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS_BY_TYPE, builder.build());

        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131101");

        UsersAcceptedInvitesPercent metric = new UsersAcceptedInvitesPercent();

        // test expanded metric value
        ValueData expandedValue = metric.getExpandedValue(builder.build());
        List<ValueData> all = treatAsList(expandedValue);
        assertEquals(all.size(), 1);

        Map<String, ValueData> record = ((MapValueData)all.get(0)).getAll();
        assertEquals(record.size(), 1);
        assertEquals(record.get("user").toString(), TEST_USER + "_invite");
    }

    @Test
    public void testAbstractFactorySessionsMetrics() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(
                scriptsManager.getScript(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, builder.build());

        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131101");

        AbstractFactorySessions metric = new FactorySessionsBelow10Min();

        // test expanded metric value
        ValueData expandedValue = metric.getExpandedValue(builder.build());
        List<ValueData> all = treatAsList(expandedValue);
        assertEquals(all.size(), 2);

        Map<String, ValueData> record = ((MapValueData)all.get(1)).getAll();
        assertEquals(record.size(), 1);
        assertEquals(record.get("session_id").toString(), "factory-id1");
    }

    @Test
    public void testProductUsageUsersBelow10MinMetric() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_SESSIONS, MetricType.PRODUCT_USAGE_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, builder.build());

        builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.putAll(scriptsManager.getScript(ScriptType.ACTIVE_ENTITIES, MetricType.ACTIVE_USERS_SET).getParamsAsMap());
        builder.put(Parameters.LOG, log.getAbsolutePath());
        pigServer.execute(ScriptType.ACTIVE_ENTITIES, builder.build());

        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131101");

        ProductUsageUsersBelow10Min metric = new ProductUsageUsersBelow10Min();

        // test expanded metric value
        ValueData expandedValue = metric.getExpandedValue(builder.build());
        List<ValueData> all = treatAsList(expandedValue);
        assertEquals(all.size(), 5);

        assertTrue(all.contains(MapValueData.valueOf("user=user2@gmail.com")));
        assertTrue(all.contains(MapValueData.valueOf("user=user1")));
        assertTrue(all.contains(MapValueData.valueOf("user=user5")));
        assertTrue(all.contains(MapValueData.valueOf("user=user5")));
        assertTrue(all.contains(MapValueData.valueOf("user=" + TEST_USER + "_invite")));
    }

    @Test
    public void testProductUsageTimeBelow1MinMetric() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_SESSIONS, MetricType.PRODUCT_USAGE_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, builder.build());

        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131101");

        ProductUsageTimeBelow1Min metric = new ProductUsageTimeBelow1Min();

        // test expanded metric value
        ValueData expandedValue = metric.getExpandedValue(builder.build());
        List<ValueData> all = treatAsList(expandedValue);
        assertEquals(all.size(), 5);
        assertTrue(all.contains(MapValueData.valueOf("session_id=" + SESSION_ID + "_micro")));
    }

    @Test
    public void testProductUsageTimeTotalMetric() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_SESSIONS, MetricType.PRODUCT_USAGE_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, builder.build());

        builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");

        ProductUsageTimeTotal metric = new ProductUsageTimeTotal();

        // test expanded metric value
        ValueData expandedValue = metric.getExpandedValue(builder.build());
        List<ValueData> all = treatAsList(expandedValue);
        assertEquals(all.size(), 6);

        assertTrue(all.contains(MapValueData.valueOf("session_id=" + SESSION_ID)));
        assertTrue(all.contains(MapValueData.valueOf("session_id=" + SESSION_ID + "_micro")));
    }

    @Test
    public void testAbstractLoggedInTypeMetrics() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS_BY_TYPE, MetricType.USERS_LOGGED_IN_TYPES).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS_BY_TYPE, builder.build());

        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131101");

        AbstractLoggedInType metric = new UsersLoggedInWithForm();

        // test expanded metric value
        ValueData expandedValue = metric.getExpandedValue(builder.build());
        List<ValueData> all = treatAsList(expandedValue);
        assertEquals(all.size(), 1);

        Map<String, ValueData> record = ((MapValueData)all.get(0)).getAll();
        assertEquals(record.size(), 1);
        assertEquals(record.get(metric.getExpandedField()).toString(), TEST_USER);
    }

    @Test
    public void testCalculatedSubtractionMetrics() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.CREATED_USERS).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());

        builder.put(Parameters.WS, Parameters.WS_TYPES.ANY.name());
        builder.putAll(scriptsManager.getScript(ScriptType.CREATED_USERS_FROM_FACTORY, MetricType.CREATED_USERS_FROM_FACTORY).getParamsAsMap());
        pigServer.execute(ScriptType.CREATED_USERS_FROM_FACTORY, builder.build());

        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131101");

        AbstractLongValueResulted createdUsersMetric = new CreatedUsers();

        // test expanded metric value
        ValueData expandedValue = createdUsersMetric.getExpandedValue(builder.build());
        List<ValueData> all = treatAsList(expandedValue);
        assertEquals(all.size(), 2);

        CalculatedMetric createdUsersFromAuthMetric = new CreatedUsersFromAuth();

        // test expanded metric value
        expandedValue = ((Expandable)createdUsersFromAuthMetric).getExpandedValue(builder.build());
        all = treatAsList(expandedValue);
        assertEquals(all.size(), 1);

        Map<String, ValueData> record = ((MapValueData)all.get(0)).getAll();
        assertEquals(record.size(), 1);
        assertEquals(record.get(createdUsersMetric.getExpandedField()).toString(), "user5");
    }

    @Test
    public void testCalculatedPercentMetric() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(
                scriptsManager.getScript(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, builder.build());

        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131101");

        Expandable metric = new FactorySessionsWithBuildPercent();

        // test expanded metric value
        ValueData expandedValue = metric.getExpandedValue(builder.build());
        List<ValueData> all = treatAsList(expandedValue);
        assertEquals(all.size(), 1);

        Map<String, ValueData> workspace1 = ((MapValueData)all.get(0)).getAll();
        assertEquals(workspace1.size(), 1);
        assertEquals(workspace1.get("session_id").toString(), "factory-id1");
    }

    @Test
    public void testSessionsListFilteredByCalculatedMetric() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(
                scriptsManager.getScript(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, builder.build());

        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131101");

        Expandable metric = new FactorySessionsWithBuildPercent();

        // test expanded metric value
        ValueData expandedValue = metric.getExpandedValue(builder.build());
        List<ValueData> all = treatAsList(expandedValue);
        assertEquals(all.size(), 1);

        Map<String, ValueData> workspace1 = ((MapValueData)all.get(0)).getAll();
        assertEquals(workspace1.size(), 1);
        assertEquals(workspace1.get("session_id").toString(), "factory-id1");

        // filter factory sessions by "factory_sessions_with_build_percent" metric
        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131102");

        ProductUsageFactorySessionsList sessionsListMetric = new ProductUsageFactorySessionsList();

        ListValueData value = (ListValueData)sessionsListMetric.getValue(builder.build());
        all = value.getAll();
        assertEquals(all.size(), 4);

        // calculate build projects list
        builder.put(Parameters.EXPANDED_METRIC_NAME, "factory_sessions_with_build_percent");

        ListValueData filteredValue = (ListValueData)sessionsListMetric.getValue(builder.build());
        all = filteredValue.getAll();
        assertEquals(all.size(), 1);

        Map<String, ValueData> record = ((MapValueData)all.get(0)).getAll();
        assertEquals(record.get(ProductUsageFactorySessionsList.SESSION_ID).toString(), "factory-id1");
    }

    @Test
    public void testExpandedAbstractActiveEntitiesMetrics() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(scriptsManager.getScript(ScriptType.ACTIVE_ENTITIES, MetricType.ACTIVE_WORKSPACES_SET).getParamsAsMap());
        pigServer.execute(ScriptType.ACTIVE_ENTITIES, builder.build());

        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.USER, TEST_USER);

        AbstractActiveEntities metric = new ActiveWorkspaces();

        LongValueData value = (LongValueData)metric.getValue(builder.build());
        assertEquals(value.getAsLong(), 3);

        // test expanded metric value
        ValueData expandedValue = metric.getExpandedValue(builder.build());
        List<ValueData> all = treatAsList(expandedValue);
        assertEquals(all.size(), 3);
        assertTrue(all.contains(MapValueData.valueOf("ws=ws1")));
        assertTrue(all.contains(MapValueData.valueOf("ws=ws2")));
        assertTrue(all.contains(MapValueData.valueOf("ws=ws3")));

        // test expanded metric value pagination
        builder.put(Parameters.PAGE, 2);
        builder.put(Parameters.PER_PAGE, 1);
        builder.put(Parameters.SORT, "+ws");

        expandedValue = metric.getExpandedValue(builder.build());

        all = treatAsList(expandedValue);
        assertEquals(all.size(), 1);
        assertTrue(all.contains(MapValueData.valueOf("ws=ws2")));
    }

    @Test
    public void testExpandedAbstractLongValueResultedMetrics() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.USER_INVITE).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());

        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131101");

        AbstractLongValueResulted metric = new UserInvite();

        LongValueData value = (LongValueData)metric.getValue(builder.build());
        assertEquals(value.getAsLong(), 2);

        // test expanded metric value
        ValueData expandedValue = metric.getExpandedValue(builder.build());
        List<ValueData> all = treatAsList(expandedValue);
        assertEquals(all.size(), 1);

        Map<String, ValueData> workspace1 = ((MapValueData)all.get(0)).getAll();
        assertEquals(workspace1.size(), 1);
        assertEquals(workspace1.get(metric.getExpandedField()).toString(), TEST_USER);
    }

    @Test
    public void testExpandedAbstractCountMetrics() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.LOG, log.getAbsolutePath());

        // calculate projects list
        builder.putAll(scriptsManager.getScript(ScriptType.PROJECTS, MetricType.PROJECTS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PROJECTS, builder.build());

        // test expanded metric value
        AbstractCount metric = new CreatedProjects();
        ValueData expandedValue = metric.getExpandedValue(builder.build());
        List<ValueData> all = treatAsList(expandedValue);
        assertEquals(all.size(), 3);

        Map<String, ValueData> record1 = ((MapValueData)all.get(0)).getAll();
        assertEquals(record1.get(metric.getExpandedField()).toString(), "user2@gmail.com/ws3/project2");

        Map<String, ValueData> record2 = ((MapValueData)all.get(1)).getAll();
        assertEquals(record2.get(metric.getExpandedField()).toString(), TEST_USER + "/ws2/project2");

        Map<String, ValueData> record3 = ((MapValueData)all.get(2)).getAll();
        assertEquals(record3.get(metric.getExpandedField()).toString(), TEST_USER + "/" + TEST_WS + "/project1");
    }

    @Test
    public void testExpandedAbstractProjectTypeMetrics() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");

        // calculate projects list
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(scriptsManager.getScript(ScriptType.PROJECTS, MetricType.PROJECTS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PROJECTS, builder.build());

        // test expanded metric value
        AbstractProjectType metric = new ProjectTypeWar();
        ValueData expandedValue = metric.getExpandedValue(builder.build());
        List<ValueData> all = treatAsList(expandedValue);
        assertEquals(all.size(), 2);

        Map<String, ValueData> record1 = ((MapValueData)all.get(0)).getAll();
        assertEquals(record1.get(metric.getExpandedField()).toString(), "user2@gmail.com/ws3/project2");

        Map<String, ValueData> record2 = ((MapValueData)all.get(1)).getAll();
        assertEquals(record2.get(metric.getExpandedField()).toString(), TEST_USER + "/ws2/project2");
    }

    @Test
    public void testExpandedAbstractProjectPaasMetrics() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");

        // calculate projects list
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(scriptsManager.getScript(ScriptType.DEPLOYMENTS_BY_TYPES, MetricType.PROJECT_PAASES).getParamsAsMap());
        pigServer.execute(ScriptType.DEPLOYMENTS_BY_TYPES, builder.build());

        // test expanded metric value
        AbstractProjectPaas metric = new ProjectPaasGae();
        ValueData expandedValue = metric.getExpandedValue(builder.build());
        List<ValueData> all = treatAsList(expandedValue);
        assertEquals(all.size(), 2);

        Map<String, ValueData> record = ((MapValueData)all.get(0)).getAll();
        assertEquals(record.get(metric.getExpandedField()).toString(), TEST_USER + "/ws2/project2");

        record = ((MapValueData)all.get(1)).getAll();
        assertEquals(record.get(metric.getExpandedField()).toString(), TEST_USER + "/" + TEST_WS + "/project1");
    }

    @Test
    public void testProjectListFilteredByReadBasedMetric() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");

        // calculate number of runs
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.RUNS).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());

        // calculate projects list
        builder.put(Parameters.STORAGE_TABLE, MetricType.PROJECTS_LIST.toString().toLowerCase());
        builder.putAll(scriptsManager.getScript(ScriptType.PROJECTS, MetricType.PROJECTS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PROJECTS, builder.build());

        // calculate all projects list
        builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");

        ProjectsList projectsListMetric = new ProjectsList();

        ListValueData value = (ListValueData)projectsListMetric.getValue(builder.build());
        assertEquals(value.getAll().size(), 3);

        // calculate run projects list
        builder.put(Parameters.EXPANDED_METRIC_NAME, "runs");

        ListValueData filteredValue = (ListValueData)projectsListMetric.getValue(builder.build());
        List<ValueData> all = filteredValue.getAll();
        assertEquals(all.size(), 2);

        Map<String, ValueData> project1 = ((MapValueData)all.get(0)).getAll();
        assertEquals(project1.get(ProjectsList.PROJECT).toString(), "project1");
        assertEquals(project1.get(ProjectsList.WS).toString(), TEST_WS);

        Map<String, ValueData> project2 = ((MapValueData)all.get(1)).getAll();
        assertEquals(project2.get(ProjectsList.PROJECT).toString(), "project2");
        assertEquals(project2.get(ProjectsList.WS).toString(), "ws3");
    }

    @Test
    public void testConversionExpandedValueDataIntoViewData() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(scriptsManager.getScript(ScriptType.ACTIVE_ENTITIES, MetricType.ACTIVE_WORKSPACES_SET).getParamsAsMap());
        pigServer.execute(ScriptType.ACTIVE_ENTITIES, builder.build());

        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.USER, TEST_USER);

        AbstractActiveEntities metric = new ActiveWorkspaces();

        ValueData expandedValue = metric.getExpandedValue(builder.build());

        // test view data builded on expanded metric value data
        ViewData viewData = viewBuilder.getViewData(expandedValue);
        assertEquals(viewData.size(), 1);

        SectionData sectionData = viewData.get("section_expended");
        assertEquals(sectionData.size(), 4);

        assertTrue(sectionData.contains(asList(StringValueData.valueOf("ws"))));
        assertTrue(sectionData.contains(asList(StringValueData.valueOf("ws1"))));
        assertTrue(sectionData.contains(asList(StringValueData.valueOf("ws2"))));
        assertTrue(sectionData.contains(asList(StringValueData.valueOf("ws3"))));
    }

    @BeforeMethod
    public void clearDatabase() {
        super.clearDatabase();
    }
}