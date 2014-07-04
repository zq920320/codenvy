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

import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsList;
import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.Configurator;
import com.codenvy.analytics.Injector;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.projects.AbstractProjectPaas;
import com.codenvy.analytics.metrics.projects.AbstractProjectType;
import com.codenvy.analytics.metrics.projects.CreatedProjects;
import com.codenvy.analytics.metrics.projects.ProjectPaasGae;
import com.codenvy.analytics.metrics.projects.ProjectTypeWar;
import com.codenvy.analytics.metrics.projects.ProjectsList;
import com.codenvy.analytics.metrics.sessions.AbstractTimelineProductUsageCondition;
import com.codenvy.analytics.metrics.sessions.ProductUsageTimeBelow1Min;
import com.codenvy.analytics.metrics.sessions.ProductUsageTimeTotal;
import com.codenvy.analytics.metrics.sessions.ProductUsageUsersBelow10Min;
import com.codenvy.analytics.metrics.sessions.TimelineProductUsageConditionAbove300Min;
import com.codenvy.analytics.metrics.sessions.TimelineProductUsageConditionBelow120Min;
import com.codenvy.analytics.metrics.sessions.TimelineProductUsageConditionBetween120And300Min;
import com.codenvy.analytics.metrics.sessions.factory.AbstractFactorySessions;
import com.codenvy.analytics.metrics.sessions.factory.FactorySessionsBelow10Min;
import com.codenvy.analytics.metrics.sessions.factory.FactorySessionsWithBuildPercent;
import com.codenvy.analytics.metrics.sessions.factory.ProductUsageFactorySessionsList;
import com.codenvy.analytics.metrics.users.AbstractLoggedInType;
import com.codenvy.analytics.metrics.users.CreatedUsers;
import com.codenvy.analytics.metrics.users.CreatedUsersFromAuth;
import com.codenvy.analytics.metrics.users.NonActiveUsers;
import com.codenvy.analytics.metrics.users.UserInvite;
import com.codenvy.analytics.metrics.users.UsersAcceptedInvitesPercent;
import com.codenvy.analytics.metrics.users.UsersLoggedInWithForm;
import com.codenvy.analytics.metrics.users.UsersStatisticsList;
import com.codenvy.analytics.metrics.workspaces.ActiveWorkspaces;
import com.codenvy.analytics.persistent.JdbcDataPersisterFactory;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;
import com.codenvy.analytics.services.configuration.XmlConfigurationManager;
import com.codenvy.analytics.services.view.CSVReportPersister;
import com.codenvy.analytics.services.view.DisplayConfiguration;
import com.codenvy.analytics.services.view.SectionData;
import com.codenvy.analytics.services.view.ViewBuilder;
import com.codenvy.analytics.services.view.ViewData;

/** @author <a href="mailto:dnochevnov@codenvy.com">Dmytro Nochevnov</a> */
public class TestExpandedMetric extends BaseTest {

    private static final String TEST_WS    = "ws1";
    private static final String TEST_USER  = "user1@gmail.com";
    private static final String SESSION_ID = "session_id";
    private static final String TEST_COMPANY = "comp";    

    private ViewBuilder viewBuilder;

    private File log;

    private static final String ANALYSIS_VIEW_CONFIGURATION = BASE_DIR + "/classes/views/analysis.xml";

    @BeforeClass
    public void prepareDatabase() throws IOException, ParseException {
        List<Event> events = new ArrayList<>();

        // add user activity at previous day
        events.add(Event.Builder.createUserAddedToWsEvent("user5@gmail.com", TEST_WS, "", "", "", "website")
                                .withDate("2013-10-31").withTime("08:00:00").build());

        // set user company
        events.add(Event.Builder.createUserCreatedEvent(TEST_USER, TEST_USER, TEST_USER)
                                .withDate("2013-11-01").withTime("08:40:00").build());
        events.add(Event.Builder.createUserUpdateProfile(TEST_USER, 
                                                         TEST_USER,
                                                         TEST_USER,
                                                         "first name 1", 
                                                         "last name 1", 
                                                         TEST_COMPANY, 
                                                         "555-444-333", 
                                                         "adm")
                                .withDate("2013-11-01").withTime("08:50:00").build());
        
        events.add(Event.Builder.createUserCreatedEvent("user2@gmail.com", "user2@gmail.com", "user2@gmail.com")
                                .withDate("2013-11-01").withTime("08:51:00").build());
        
        events.add(Event.Builder.createUserUpdateProfile("user2@gmail.com", 
                                                         "user2@gmail.com",
                                                         "user2@gmail.com",
                                                         "first name 2", 
                                                         "last name 3", 
                                                         TEST_COMPANY, 
                                                         "555-444-333", 
                                                         "develop")
                                .withDate("2013-11-01").withTime("08:52:00").build());
        
        
        // create user from factory
        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-4", "factoryUrl1", "referrer1", "org1", "affiliate1")
                                .withDate("2013-11-01").withTime("09:01:00").build());
        events.add(Event.Builder.createTenantCreatedEvent("tmp-4", "anonymoususer_04")
                                .withDate("2013-11-01").withTime("09:01:30").build());
        events.add(Event.Builder.createUserAddedToWsEvent("", "", "", "tmp-4", "anonymoususer_4", "website")
                                .withDate("2013-11-01").withTime("09:02:00").build());
        events.add(Event.Builder.createUserChangedNameEvent("anonymoususer_4", "user4@gmail.com")
                                .withDate("2013-11-01").withTime("09:03:00").build());
        events.add(Event.Builder.createUserCreatedEvent("user-id4", "user4@gmail.com", "user4@gmail.com")
                                .withDate("2013-11-01").withTime("09:04:00").build());

        // create user
        events.add(Event.Builder.createUserCreatedEvent("user-id5", "user5", "user5")
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
        events.add(Event.Builder.createTenantCreatedEvent("tmp-3", "user1")
                                .withDate("2013-11-01").withTime("12:02:00").build());        

        // build event for session #1
        events.add(Event.Builder.createBuildStartedEvent("user1", "tmp-1", "project", "type", "id1")
                                .withDate("2013-11-01").withTime("10:03:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user1", "tmp-1", "", "project", "type")
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
        events.add(Event.Builder.createProjectBuiltEvent(TEST_USER, "ws2", "project2", "war", "id2")
                                .withDate("2013-11-01").withTime("19:13:00").build());
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

        // add event of accepting factory url for the testDrillDownTopFactoriesMetric test
        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-5", "factoryUrl1", "http://referrer3", "org3", "affiliate2")
                   .withDate("2013-12-20").withTime("11:00:02").build());
        events.add(Event.Builder.createTenantCreatedEvent("tmp-5", "factory_user5")
                   .withDate("2013-12-20").withTime("12:01:00").build());  
        
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
    public void testFilteringUsersStatisticsListByTotalUsersAndTimeUnit() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        
        builder.putAll(scriptsManager.getScript(ScriptType.USERS_STATISTICS, MetricType.USERS_STATISTICS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_STATISTICS, builder.build());
        
        builder.put(Parameters.FROM_DATE, "20131220");
        builder.put(Parameters.TO_DATE, "20131220");
        pigServer.execute(ScriptType.USERS_STATISTICS, builder.build());
        
        // test filtering user list by "total_users" metric
        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131120");
        builder.put(Parameters.TIME_UNIT, Parameters.TimeUnit.WEEK.toString());
        builder.put(Parameters.TIME_INTERVAL, 0);  // interval from 20131208 to 20131214  
        builder.put(Parameters.EXPANDED_METRIC_NAME, MetricType.TOTAL_USERS.toString());

        Metric usersStatisticsListMetric = MetricFactory.getMetric(MetricType.USERS_STATISTICS_LIST);
        
        Context context = builder.build();
        context = viewBuilder.initializeTimeInterval(context);
        
        ListValueData filteredValue = (ListValueData)usersStatisticsListMetric.getValue(context);
        List<ValueData> all = filteredValue.getAll();        
        assertEquals(all.size(), 5);
    }    
    
    @Test
    public void testFilteringOfDrillDownPage() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        
        builder.putAll(scriptsManager.getScript(ScriptType.USERS_PROFILES, MetricType.USERS_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_PROFILES, builder.build());
        
        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.BUILDS).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.USERS_STATISTICS, MetricType.USERS_STATISTICS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_STATISTICS, builder.build());
        
        // test expanded metric value
        builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");   
        
        // filter users who built: {user1@gmail.com, user1}
        builder.put(Parameters.EXPANDED_METRIC_NAME, MetricType.USERS_WHO_BUILT.toString());

        // filter users by USER_COMPANY=TEST_COMPANY : {user1@gmail.com, user2@gmail.com}
        builder.put(MetricFilter.USER_COMPANY, TEST_COMPANY);
        
        // result = {user1@gmail.com, user1} INTERSECT {user1@gmail.com, user2@gmail.com} = {user1@gmail.com}
        Metric metric = MetricFactory.getMetric(MetricType.USERS_STATISTICS_LIST);
        ListValueData filteredValue = (ListValueData)metric.getValue(builder.build());
        List<ValueData> all = filteredValue.getAll();
        
        assertEquals(all.size(), 1);
        
        Map<String, ValueData> record = ((MapValueData)all.get(0)).getAll();
        assertEquals(record.get("user").getAsString(), TEST_USER);
    }    
    
    /**
     * Testing metric: WorkspacesStatisticsList
     * Filtered by workspaces list from expanded_metric_name=temporary_workspaces_created
     * passed_days_count=by_7_days|by_60_days
     * to_date=20131101
     * factory=factoryUrl1
     */
    @Test
    public void testDrillDownTopFactoriesMetric() throws Exception {
        Context.Builder builder = new Context.Builder();

        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        
        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_SESSIONS, MetricType.PRODUCT_USAGE_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.CREATED_TEMPORARY_WORKSPACES, MetricType.TEMPORARY_WORKSPACES_CREATED).getParamsAsMap());
        pigServer.execute(ScriptType.CREATED_TEMPORARY_WORKSPACES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, builder.build());

        
        builder.put(Parameters.FROM_DATE, "20131220");
        builder.put(Parameters.TO_DATE, "20131220");
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_SESSIONS, MetricType.PRODUCT_USAGE_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, builder.build());
        
        builder.putAll(scriptsManager.getScript(ScriptType.CREATED_TEMPORARY_WORKSPACES, MetricType.TEMPORARY_WORKSPACES_CREATED).getParamsAsMap());
        pigServer.execute(ScriptType.CREATED_TEMPORARY_WORKSPACES, builder.build());
        
        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, builder.build());               
        
        builder = new Context.Builder();
        builder.put(Parameters.EXPANDED_METRIC_NAME, MetricType.TEMPORARY_WORKSPACES_CREATED.toString());
        builder.put(MetricFilter.FACTORY, "factoryUrl1");

        builder.put(Parameters.TO_DATE, "20131220");
        builder.put(Parameters.PASSED_DAYS_COUNT, Parameters.PassedDaysCount.BY_7_DAYS.toString());
        Context context = Utils.initDateInterval(builder.getAsDate(Parameters.TO_DATE), builder.getPassedDaysCount(), builder);        
        
        Metric metric = MetricFactory.getMetric(MetricType.WORKSPACES_STATISTICS_LIST);

        // test drill down page values
        ValueData value = metric.getValue(context);
        List<ValueData> all = treatAsList(value);
        assertEquals(all.size(), 1);

        Map<String, ValueData> record = ((MapValueData)all.get(0)).getAll();
        assertEquals(record.get("ws").toString(), "tmp-5");        
        
        builder.put(Parameters.PASSED_DAYS_COUNT, Parameters.PassedDaysCount.BY_60_DAYS.toString());
        context = Utils.initDateInterval(builder.getAsDate(Parameters.TO_DATE), builder.getPassedDaysCount(), builder);
        
        value = metric.getValue(context);
        all = treatAsList(value);

        assertEquals(all.size(), 3);
        
        record = ((MapValueData)all.get(1)).getAll();
        assertEquals(record.get("ws").toString(), "tmp-4");
    }

    /**
     * Testing metric: ProductUsageSessionsList
     * filtered by user list from expanded_metric_name=product_usage_sessions
     * passed_days_count=by_1_days
     * to_date=20131221|20131220
     * user=factory_user5
     * @throws Exception
     */
    @Test
    public void testDrillDownTopUsersMetric() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.put(Parameters.FROM_DATE, "20131220");
        builder.put(Parameters.TO_DATE, "20131220");
        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_SESSIONS, MetricType.PRODUCT_USAGE_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, builder.build());
        
        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, builder.build());
        
        
        builder = new Context.Builder();
        builder.put(Parameters.EXPANDED_METRIC_NAME, MetricType.PRODUCT_USAGE_SESSIONS.toString());
        builder.put(MetricFilter.USER, "factory_user5");

        builder.put(Parameters.TO_DATE, "20131221");
        builder.put(Parameters.PASSED_DAYS_COUNT, Parameters.PassedDaysCount.BY_1_DAY.toString());
        Context context = Utils.initDateInterval(builder.getAsDate(Parameters.TO_DATE), builder.getPassedDaysCount(), builder);
        
        Metric metric = MetricFactory.getMetric(MetricType.PRODUCT_USAGE_SESSIONS_LIST);

        // test drill down page values
        ValueData value = metric.getValue(context);
        List<ValueData> all = treatAsList(value);
        assertEquals(all.size(), 0);
        
        builder.put(Parameters.TO_DATE, "20131220");
        context = Utils.initDateInterval(builder.getAsDate(Parameters.TO_DATE), builder.getPassedDaysCount(), builder);
        
        value = metric.getValue(context);
        all = treatAsList(value);
        assertEquals(all.size(), 1);
        
        Map<String, ValueData> record = ((MapValueData)all.get(0)).getAll();
        assertEquals(record.get("user").toString(), "factory_user5");
    }
    
    @Test
    public void testAbstractTimelineProductUsageConditionMetric() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_SESSIONS, MetricType.PRODUCT_USAGE_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, builder.build());

        builder.put(Parameters.FROM_DATE, "20131120");
        builder.put(Parameters.TO_DATE, "20131120");
        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_SESSIONS, MetricType.PRODUCT_USAGE_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, builder.build());
        
        builder.put(Parameters.FROM_DATE, "20131220");
        builder.put(Parameters.TO_DATE, "20131220");
        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_SESSIONS, MetricType.PRODUCT_USAGE_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, builder.build());
        
        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, builder.build());


        /** test TimelineProductUsageConditionBelow120Min */
        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131220");
        builder.put(Parameters.TIME_INTERVAL, "0");
        AbstractTimelineProductUsageCondition metric = new TimelineProductUsageConditionBelow120Min();
        Context context = metric.initContextBasedOnTimeInterval(builder.build());
        ValueData expandedValue = metric.getExpandedValue(context);
        List<ValueData> all = treatAsList(expandedValue);
        assertEquals(all.size(), 4);

        Map<String, ValueData> record = ((MapValueData)all.get(0)).getAll();
        assertEquals(record.size(), 1);
        assertEquals(record.get("user").toString(), "factory_user5");

        record = ((MapValueData)all.get(1)).getAll();
        assertEquals(record.size(), 1);
        assertEquals(record.get("user").toString(), "user1");
        
        record = ((MapValueData)all.get(2)).getAll();
        assertEquals(record.size(), 1);
        assertEquals(record.get("user").toString(), "user4@gmail.com");
        
        record = ((MapValueData)all.get(3)).getAll();
        assertEquals(record.size(), 1);
        assertEquals(record.get("user").toString(), TEST_USER);


        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131220");
        builder.put(Parameters.TIME_INTERVAL, "2");
        context = metric.initContextBasedOnTimeInterval(builder.build());
        expandedValue = metric.getExpandedValue(context);
        all = treatAsList(expandedValue);
        assertEquals(all.size(), 3);

        record = ((MapValueData)all.get(0)).getAll();
        assertEquals(record.size(), 1);
        assertEquals(record.get("user").toString(), "user1");
        record = ((MapValueData)all.get(1)).getAll();
        assertEquals(record.size(), 1);
        assertEquals(record.get("user").toString(), "user4@gmail.com");
        record = ((MapValueData)all.get(2)).getAll();
        assertEquals(record.size(), 1);
        assertEquals(record.get("user").toString(), TEST_USER);

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
        pigServer.execute(ScriptType.ACTIVE_ENTITIES, builder.build());

        builder.put(Parameters.FROM_DATE, "20131031");
        builder.put(Parameters.TO_DATE, "20131031");
        builder.putAll(scriptsManager.getScript(ScriptType.USERS_STATISTICS, MetricType.USERS_STATISTICS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_STATISTICS, builder.build());

        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        pigServer.execute(ScriptType.USERS_STATISTICS, builder.build());

        builder.put(Parameters.FROM_DATE, "20131031");
        builder.put(Parameters.TO_DATE, "20131031");
        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.CREATED_USERS).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());

        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
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
        assertEquals(all.size(), 4);
        assertTrue(all.contains(MapValueData.valueOf("user=user4@gmail.com")));
        assertTrue(all.contains(MapValueData.valueOf("user=user5")));

        // test filtering user list by "non_active_users" metric
        builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");

        UsersStatisticsList usersStatisticsListMetric = new UsersStatisticsList();
        ListValueData value = (ListValueData)usersStatisticsListMetric.getValue(builder.build());
        all = value.getAll();
        assertEquals(all.size(), 5);

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
        assertEquals(all.size(), 4);

        Map<String, ValueData> record = ((MapValueData)all.get(3)).getAll();
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

        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, builder.build());
        
        builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20131101");

        ProductUsageTimeBelow1Min metric = new ProductUsageTimeBelow1Min();

        // test expanded metric value
        ValueData expandedValue = metric.getExpandedValue(builder.build());
        List<ValueData> all = treatAsList(expandedValue);
        assertEquals(all.size(), 4);
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

        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, builder.build());
        
        builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");

        ProductUsageTimeTotal metric = new ProductUsageTimeTotal();

        // test expanded metric value
        ValueData expandedValue = metric.getExpandedValue(builder.build());
        List<ValueData> all = treatAsList(expandedValue);
        assertEquals(all.size(), 5);

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

        Expandable createdUsersMetric = new CreatedUsers();

        // test expanded metric value
        ValueData expandedValue = createdUsersMetric.getExpandedValue(builder.build());
        List<ValueData> all = treatAsList(expandedValue);
        assertEquals(all.size(), 4);

        CalculatedMetric createdUsersFromAuthMetric = new CreatedUsersFromAuth();

        // test expanded metric value
        expandedValue = ((Expandable)createdUsersFromAuthMetric).getExpandedValue(builder.build());
        all = treatAsList(expandedValue);
        assertEquals(all.size(), 3);
        assertTrue(all.contains(MapValueData.valueOf("user=user5")));
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
        assertEquals(all.size(), 6);

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

        Expandable metric = (Expandable)MetricFactory.getMetric(MetricType.ACTIVE_WORKSPACES);

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

    @Test
    public void testTotalUsers() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131101");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.CREATED_USERS).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());

        Expandable metric = (Expandable)MetricFactory.getMetric(MetricType.CREATED_USERS);
        ValueData expandedValue = metric.getExpandedValue(Context.EMPTY);

        List<ValueData> list = treatAsList(expandedValue);
        assertEquals(list.size(), 4);
        assertTrue(list.contains(MapValueData.valueOf("user=user5")));
        assertTrue(list.contains(MapValueData.valueOf("user=user4@gmail.com")));
    }

    @BeforeMethod
    public void clearDatabase() {
        super.clearDatabase();
    }
}