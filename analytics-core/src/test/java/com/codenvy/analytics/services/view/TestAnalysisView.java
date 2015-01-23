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
package com.codenvy.analytics.services.view;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.Configurator;
import com.codenvy.analytics.Injector;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.persistent.JdbcDataPersisterFactory;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;
import com.codenvy.analytics.services.configuration.XmlConfigurationManager;

import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.mongodb.util.MyAsserts.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/** @author Dmytro Nochevnov */
public class TestAnalysisView extends BaseTest {

    private static final String ANALYSIS_VIEW_CONFIGURATION = BASE_DIR + "/classes/views/analysis.xml";

    private ViewBuilder viewBuilder;

    private static final String DATE1 = "2013-11-01";
    private static final String DATE2 = "2013-12-02";

    private static final List<String> monthlyReportColumnLabels = Arrays.asList("", "Jan 2014", "Dec 2013",
                                                                                "Nov 2013", "Oct 2013", "Sep 2013",
                                                                                "Aug 2013", "Jul 2013", "Jun 2013",
                                                                                "May 2013", "Apr 2013",
                                                                                "Mar 2013", "Feb 2013", "Jan 2013",
                                                                                "Dec 2012");

    private static final List<String> weeklyReportColumnLabels = Arrays.asList("", "04 Jan", "28 Dec", "21 Dec",
                                                                               "14 Dec", "07 Dec", "30 Nov",
                                                                               "23 Nov", "16 Nov", "09 Nov",
                                                                               "02 Nov", "26 Oct", "19 Oct",
                                                                               "12 Oct", "05 Oct");

    private static final List<String> dailyReportColumnLabels = Arrays.asList("", "01 Jan", "31 Dec", "30 Dec",
                                                                              "29 Dec", "28 Dec", "27 Dec",
                                                                              "26 Dec", "25 Dec", "24 Dec",
                                                                              "23 Dec", "22 Dec", "21 Dec",
                                                                              "20 Dec", "19 Dec");

    private static final List<RowLabel> rowsLabels = Arrays.asList(RowLabel.EMPTY,
                                                                   RowLabel.TOTAL_USERS,
                                                                   RowLabel.TOTAL_NUMBER_OF_USERS_WE_TRACK,
                                                                   RowLabel.CREATED_PROJECTS,
                                                                   RowLabel.AND_BUILT,
                                                                   RowLabel.AND_RUN,
                                                                   RowLabel.AND_DEPLOYED_TO_PAAS,
                                                                   RowLabel.SENT_INVITES);

    private enum RowLabel {
        TOTAL_USERS("Total Users"),
        TOTAL_NUMBER_OF_USERS_WE_TRACK("Total Number of Users We Track"),
        CREATED_PROJECTS("Created Projects"),
        AND_BUILT("& Built"),
        AND_RUN("& Run"),
        AND_DEPLOYED_TO_PAAS("& Deployed to PAAS"),
        SENT_INVITES("Sent Invites"),
        EMPTY(""),
        UNKNOWN;

        private String label;

        RowLabel(String label) {
            this.label = label;
        }

        RowLabel() {
        }

        String getLabel() {
            return this.label;
        }
    }

    @BeforeMethod
    public void prepare() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.USER, Parameters.USER_TYPES.REGISTERED.toString());
        builder.put(Parameters.LOG, prepareLog().getAbsolutePath());
        builder.put(Parameters.FROM_DATE, DATE1.replace("-", ""));
        builder.put(Parameters.TO_DATE, DATE1.replace("-", ""));

        extractDataFromLog(builder);

        builder.put(Parameters.FROM_DATE, DATE2.replace("-", ""));
        builder.put(Parameters.TO_DATE, DATE2.replace("-", ""));
        extractDataFromLog(builder);

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
    public void testAnalysisReport() throws Exception {
        ArgumentCaptor<String> viewId = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ViewData> viewData = ArgumentCaptor.forClass(ViewData.class);
        ArgumentCaptor<Context> context = ArgumentCaptor.forClass(Context.class);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20140101");
        builder.put(Parameters.TIME_UNIT, Parameters.TimeUnit.MONTH.toString().toLowerCase());

        viewBuilder.computeDisplayData(builder.build());
        verify(viewBuilder, atLeastOnce()).retainViewData(viewId.capture(), viewData.capture(), context.capture());

        List<ViewData> allReports = viewData.getAllValues();

        assertEquals(allReports.size(), 4);

        for (ViewData data : allReports) {
            if (data.containsKey("analysis_lifetime")) {
                testLifetimeReport(data.get("analysis_lifetime"));
            } else if (data.containsKey("analysis_month")) {
                testMonthlyReport(data.get("analysis_month"));
            } else if (data.containsKey("analysis_week")) {
                testWeeklyReport(data.get("analysis_week"));
            } else if (data.containsKey("analysis_day")) {
                testDailyReport(data.get("analysis_day"));
            } else {
                fail("Unknown analysis report: '" + data.keySet().toString() + "'");
            }
        }
    }

    private void testLifetimeReport(SectionData lifetimeAnalysisReport) {
        // test row labels
        assertEquals(lifetimeAnalysisReport.size(), 8);
        for (int i = 0; i < lifetimeAnalysisReport.size(); i++) {
            String actualRowLabel = lifetimeAnalysisReport.get(i).get(0).getAsString();
            String expectedRowLabel = rowsLabels.get(i).getLabel();
            assertEquals(actualRowLabel, expectedRowLabel);
        }

        // test column labels
        List<ValueData> actualColumnLabels = lifetimeAnalysisReport.get(0);
        assertEquals(actualColumnLabels.size(), 2);

        // test data of column
        Map<RowLabel, String> columnData = getColumn(1, lifetimeAnalysisReport, rowsLabels, true);
        assertEquals(columnData.get(RowLabel.TOTAL_USERS), "2,004");
        assertEquals(columnData.get(RowLabel.TOTAL_NUMBER_OF_USERS_WE_TRACK), "3");
        assertEquals(columnData.get(RowLabel.CREATED_PROJECTS), "3");
        assertEquals(columnData.get(RowLabel.AND_BUILT), "1");
        assertEquals(columnData.get(RowLabel.AND_RUN), "2");
        assertEquals(columnData.get(RowLabel.AND_DEPLOYED_TO_PAAS), "3");
        assertEquals(columnData.get(RowLabel.SENT_INVITES), "2");
    }

    private void testMonthlyReport(SectionData monthlyAnalysisReport) {
        // test row labels
        assertEquals(monthlyAnalysisReport.size(), 8);
        for (int i = 0; i < monthlyAnalysisReport.size(); i++) {
            String actualRowLabel = monthlyAnalysisReport.get(i).get(0).getAsString();
            String expectedRowLabel = rowsLabels.get(i).getLabel();
            assertEquals(actualRowLabel, expectedRowLabel);
        }

        // test column labels
        List<ValueData> actualColumnLabels = monthlyAnalysisReport.get(0);
        assertEquals(actualColumnLabels.size(), 15);
        for (int i = 0; i < monthlyReportColumnLabels.size(); i++) {
            String actualColumnLabel = actualColumnLabels.get(i).getAsString();
            String expectedColumnLabel = monthlyReportColumnLabels.get(i);
            assertEquals(actualColumnLabel, expectedColumnLabel);
        }

        // test data of column 4 with label "Oct 2013"
        Map<RowLabel, String> column4Data = getColumn(4, monthlyAnalysisReport, rowsLabels, true);
        assertEquals(column4Data.get(RowLabel.TOTAL_USERS), "2,000");
        assertEquals(column4Data.get(RowLabel.TOTAL_NUMBER_OF_USERS_WE_TRACK), "0");
        assertEquals(column4Data.get(RowLabel.CREATED_PROJECTS), "0");
        assertEquals(column4Data.get(RowLabel.AND_BUILT), "0");
        assertEquals(column4Data.get(RowLabel.AND_RUN), "0");
        assertEquals(column4Data.get(RowLabel.AND_DEPLOYED_TO_PAAS), "0");
        assertEquals(column4Data.get(RowLabel.SENT_INVITES), "0");

        // test data of column 3 with label "Nov 2013"
        Map<RowLabel, String> column3Data = getColumn(3, monthlyAnalysisReport, rowsLabels, true);
        assertEquals(column3Data.get(RowLabel.TOTAL_USERS), "2,003");
        assertEquals(column3Data.get(RowLabel.TOTAL_NUMBER_OF_USERS_WE_TRACK), "3");
        assertEquals(column3Data.get(RowLabel.CREATED_PROJECTS), "2");
        assertEquals(column3Data.get(RowLabel.AND_BUILT), "1");
        assertEquals(column3Data.get(RowLabel.AND_RUN), "1");
        assertEquals(column3Data.get(RowLabel.AND_DEPLOYED_TO_PAAS), "1");
        assertEquals(column3Data.get(RowLabel.SENT_INVITES), "1");

        // test data of column 2 with label "Dec 2013"
        Map<RowLabel, String> column2Data = getColumn(2, monthlyAnalysisReport, rowsLabels, true);
        assertEquals(column2Data.get(RowLabel.TOTAL_USERS), "2,004");
        assertEquals(column2Data.get(RowLabel.TOTAL_NUMBER_OF_USERS_WE_TRACK), "3");
        assertEquals(column2Data.get(RowLabel.CREATED_PROJECTS), "3");
        assertEquals(column2Data.get(RowLabel.AND_BUILT), "1");
        assertEquals(column2Data.get(RowLabel.AND_RUN), "2");
        assertEquals(column2Data.get(RowLabel.AND_DEPLOYED_TO_PAAS), "3");
        assertEquals(column2Data.get(RowLabel.SENT_INVITES), "2");

        // test data of column 1 with label "Jan 2014"
        Map<RowLabel, String> column1Data = getColumn(1, monthlyAnalysisReport, rowsLabels, true);
        assertEquals(column1Data.get(RowLabel.TOTAL_USERS), "2,004");
        assertEquals(column1Data.get(RowLabel.TOTAL_NUMBER_OF_USERS_WE_TRACK), "3");
        assertEquals(column1Data.get(RowLabel.CREATED_PROJECTS), "3");
        assertEquals(column1Data.get(RowLabel.AND_BUILT), "1");
        assertEquals(column1Data.get(RowLabel.AND_RUN), "2");
        assertEquals(column1Data.get(RowLabel.AND_DEPLOYED_TO_PAAS), "3");
        assertEquals(column1Data.get(RowLabel.SENT_INVITES), "2");
    }

    private void testWeeklyReport(SectionData weeklyAnalysisReport) {
        // test row labels
        assertEquals(weeklyAnalysisReport.size(), 8);
        for (int i = 0; i < weeklyAnalysisReport.size(); i++) {
            String actualRowLabel = weeklyAnalysisReport.get(i).get(0).getAsString();
            String expectedRowLabel = rowsLabels.get(i).getLabel();
            assertEquals(actualRowLabel, expectedRowLabel);
        }

        // test column labels
        List<ValueData> actualColumnLabels = weeklyAnalysisReport.get(0);
        assertEquals(actualColumnLabels.size(), 15);
        for (int i = 0; i < weeklyReportColumnLabels.size(); i++) {
            String actualColumnLabel = actualColumnLabels.get(i).getAsString();
            String expectedColumnLabel = weeklyReportColumnLabels.get(i);
            assertEquals(actualColumnLabel, expectedColumnLabel);
        }

        // test data of column 1 with label "04 Jan"
        {
            Map<RowLabel, String> columnData = getColumn(1, weeklyAnalysisReport, rowsLabels, true);
            assertEquals(columnData.get(RowLabel.TOTAL_USERS), "2,004");
            assertEquals(columnData.get(RowLabel.TOTAL_NUMBER_OF_USERS_WE_TRACK), "3");
            assertEquals(columnData.get(RowLabel.CREATED_PROJECTS), "3");
            assertEquals(columnData.get(RowLabel.AND_BUILT), "1");
            assertEquals(columnData.get(RowLabel.AND_RUN), "2");
            assertEquals(columnData.get(RowLabel.AND_DEPLOYED_TO_PAAS), "3");
            assertEquals(columnData.get(RowLabel.SENT_INVITES), "2");
        }

        // test data of column 6 with label "30 Nov"
        {
            Map<RowLabel, String> columnData = getColumn(6, weeklyAnalysisReport, rowsLabels, true);
            assertEquals(columnData.get(RowLabel.TOTAL_USERS), "2,003");
            assertEquals(columnData.get(RowLabel.TOTAL_NUMBER_OF_USERS_WE_TRACK), "3");
            assertEquals(columnData.get(RowLabel.CREATED_PROJECTS), "2");
            assertEquals(columnData.get(RowLabel.AND_BUILT), "1");
            assertEquals(columnData.get(RowLabel.AND_RUN), "1");
            assertEquals(columnData.get(RowLabel.AND_DEPLOYED_TO_PAAS), "1");
            assertEquals(columnData.get(RowLabel.SENT_INVITES), "1");
        }

        // test data of column 11 with label "26 Oct"
        {
            Map<RowLabel, String> columnData = getColumn(11, weeklyAnalysisReport, rowsLabels, true);
            assertEquals(columnData.get(RowLabel.TOTAL_USERS), "2,000");
            assertEquals(columnData.get(RowLabel.TOTAL_NUMBER_OF_USERS_WE_TRACK), "0");
            assertEquals(columnData.get(RowLabel.CREATED_PROJECTS), "0");
            assertEquals(columnData.get(RowLabel.AND_BUILT), "0");
            assertEquals(columnData.get(RowLabel.AND_RUN), "0");
            assertEquals(columnData.get(RowLabel.AND_DEPLOYED_TO_PAAS), "0");
            assertEquals(columnData.get(RowLabel.SENT_INVITES), "0");
        }
    }

    private void testDailyReport(SectionData weeklyAnalysisReport) {
        // test row labels
        assertEquals(weeklyAnalysisReport.size(), 8);
        for (int i = 0; i < weeklyAnalysisReport.size(); i++) {
            String actualRowLabel = weeklyAnalysisReport.get(i).get(0).getAsString();
            String expectedRowLabel = rowsLabels.get(i).getLabel();
            assertEquals(actualRowLabel, expectedRowLabel);
        }

        // test column labels
        List<ValueData> actualColumnLabels = weeklyAnalysisReport.get(0);
        assertEquals(actualColumnLabels.size(), 15);
        for (int i = 0; i < dailyReportColumnLabels.size(); i++) {
            String actualColumnLabel = actualColumnLabels.get(i).getAsString();
            String expectedColumnLabel = dailyReportColumnLabels.get(i);
            assertEquals(actualColumnLabel, expectedColumnLabel);
        }

        // test data of first column 1 with label "01 Jan"
        {
            Map<RowLabel, String> columnData = getColumn(1, weeklyAnalysisReport, rowsLabels, true);
            assertEquals(columnData.get(RowLabel.TOTAL_USERS), "2,004");
            assertEquals(columnData.get(RowLabel.TOTAL_NUMBER_OF_USERS_WE_TRACK), "3");
            assertEquals(columnData.get(RowLabel.CREATED_PROJECTS), "3");
            assertEquals(columnData.get(RowLabel.AND_BUILT), "1");
            assertEquals(columnData.get(RowLabel.AND_RUN), "2");
            assertEquals(columnData.get(RowLabel.AND_DEPLOYED_TO_PAAS), "3");
            assertEquals(columnData.get(RowLabel.SENT_INVITES), "2");
        }

        // test data of last column 14 with label "19 Dec"
        {
            Map<RowLabel, String> columnData = getColumn(14, weeklyAnalysisReport, rowsLabels, true);
            assertEquals(columnData.get(RowLabel.TOTAL_USERS), "2,004");
            assertEquals(columnData.get(RowLabel.TOTAL_NUMBER_OF_USERS_WE_TRACK), "3");
            assertEquals(columnData.get(RowLabel.CREATED_PROJECTS), "3");
            assertEquals(columnData.get(RowLabel.AND_BUILT), "1");
            assertEquals(columnData.get(RowLabel.AND_RUN), "2");
            assertEquals(columnData.get(RowLabel.AND_DEPLOYED_TO_PAAS), "3");
            assertEquals(columnData.get(RowLabel.SENT_INVITES), "2");
        }
    }

    /**
     * Returns column from section table of view
     */
    private Map<RowLabel, String> getColumn(int columnIndex, SectionData report, List<RowLabel> rowLabels,
                                            boolean passFirstRow) {
        LinkedHashMap<RowLabel, String> columnData = new LinkedHashMap<>();
        assertEquals(report.size(), rowLabels.size());

        int i = (passFirstRow) ? 1 : 0;   // don't taking into account first row which consists of column labels
        for (; i < rowLabels.size(); i++) {
            columnData.put(rowLabels.get(i), report.get(i).get(columnIndex).getAsString());
        }

        return columnData;
    }

    private File prepareLog() throws IOException {
        List<Event> events = new ArrayList<>();

        /** events at the DATE1 */
        // create users
        events.add(Event.Builder.createUserCreatedEvent("user1-id", "user1@gmail.com", "user1@gmail.com")
                                .withDate(DATE1).build());
        events.add(Event.Builder.createUserCreatedEvent("user2-id", "user2@gmail.com", "user2@gmail.com")
                                .withDate(DATE1).build());
        events.add(Event.Builder.createUserCreatedEvent("user3-id", "user3@gmail.com", "user3@gmail.com")
                                .withDate(DATE1).build());

        events.add(Event.Builder.createWorkspaceCreatedEvent("wsid1", "ws1", "user1@gmail.com")
                                .withDate(DATE1).build());
        events.add(Event.Builder.createWorkspaceCreatedEvent("wsid2", "ws2", "user2@gmail.com")
                                .withDate(DATE1).build());
        events.add(Event.Builder.createWorkspaceCreatedEvent("wsid3", "ws3", "user3@gmail.com")
                                .withDate(DATE1).build());

        // update users' profiles
        events.add(Event.Builder.createUserUpdateProfile("user1-id", "user1@gmail.com", "user1@gmail.com", "f1", "l1", "company1", "phone1", "jobtitle1")
                                .withDate(DATE1).build());
        events.add(Event.Builder.createUserUpdateProfile("user2-id", "user2@gmail.com", "user2@gmail.com", "", "", "", "", "")
                                .withDate(DATE1).build());

        // active users [user1, user2]
        events.add(Event.Builder.createWorkspaceCreatedEvent(WID1, "ws1", "user1@gmail.com").withTime("09:00:00").withDate(DATE1)
                                .build());
        events.add(Event.Builder.createWorkspaceCreatedEvent(WID2, "ws2", "user2@gmail.com").withTime("09:00:00").withDate(DATE1)
                                .build());

        // projects created
        events.add(
                Event.Builder.createProjectCreatedEvent("user1@gmail.com", "ws1", "project1", "type1").withDate(DATE1)
                             .withTime("10:00:00").build());
        events.add(
                Event.Builder.createProjectCreatedEvent("user1@gmail.com", "ws1", "project2", "type1").withDate(DATE1)
                             .withTime("10:05:00").build());
        events.add(
                Event.Builder.createProjectCreatedEvent("user2@gmail.com", "ws2", "project1", "type1").withDate(DATE1)
                             .withTime("10:03:00").build());

        // projects deployed to PaaS
        events.add(Event.Builder.createApplicationCreatedEvent("user1@gmail.com", "ws1", "project1", "type1", "paas1")
                                .withTime("10:10:00,000")
                                .withDate(DATE1).build());

        // projects built
        events.add(Event.Builder.createProjectBuiltEvent("user2@gmail.com", "ws1", "project1", "type1").withTime("10:06:00")
                                .withDate(DATE1).build());


        events.add(Event.Builder.createFactoryCreatedEvent("user1@gmail.com", "ws1", "", "", "", "", "", "")
                                .withDate(DATE1)
                                .withTime("20:03:00").build());

        events.add(Event.Builder.createDebugStartedEvent("user2@gmail.com", "ws1", "", "", "id1")
                                .withDate(DATE1)
                                .withTime("20:06:00").build());

        // invite users
        events.add(Event.Builder.createUserInviteEvent("user1@gmail.com", "ws1", "email1")
                                .withDate(DATE1).build());

        events.add(Event.Builder.createRunStartedEvent("user2@gmail.com", "ws2", "project", "type", "id1")
                                .withDate(DATE1)
                                .withTime("20:59:00").build());
        events.add(Event.Builder.createRunFinishedEvent("user2@gmail.com", "ws2", "project", "type", "id1", 0, 1)
                                .withDate(DATE1)
                                .withTime("21:01:00").build());

        events.add(Event.Builder.createBuildStartedEvent("user1@gmail.com", "ws1", "project", "type", "id2")
                                .withDate(DATE1)
                                .withTime("21:12:00").build());
        events.add(Event.Builder.createBuildFinishedEvent("user1@gmail.com", "ws1", "project", "type", "id2", 0)
                                .withDate(DATE1)
                                .withTime("21:14:00").build());


        /** events at the DATE2 */
        // create user
        events.add(Event.Builder.createUserCreatedEvent("user3-id", "user3@gmail.com", "user3@gmail.com")
                                .withDate(DATE2).build());

        events.add(Event.Builder.createWorkspaceCreatedEvent(WID3, "ws3", "user3@gmail.com").withTime("09:00:00").withDate(DATE2)
                                .build());

        // update user's profile
        events.add(Event.Builder.createUserUpdateProfile("user3-id", "user3@gmail.com", "user3@gmail.com", "f3", "l3", "company3", "phone3", "jobtitle3")
                                .withDate(DATE2).build());

        // projects created
        events.add(
                Event.Builder.createProjectCreatedEvent("user2@gmail.com", "ws2", "project22", "type1").withDate(DATE2)
                             .withTime("10:03:00").build());
        events.add(
                Event.Builder.createProjectCreatedEvent("user3@gmail.com", "ws3", "project33", "type1").withDate(DATE2)
                             .withTime("10:03:00").build());

        // projects run
        events.add(Event.Builder.createRunStartedEvent("user2@gmail.com", "ws2", "project1", "type1", "1")
                                .withTime("10:10:00,000").withDate(DATE2).build());
        events.add(Event.Builder.createRunStartedEvent("user3@gmail.com", "ws3", "project1", "type1", "1")
                                .withTime("10:10:00,000").withDate(DATE2).build());


        // projects deployed to PaaS
        events.add(Event.Builder.createApplicationCreatedEvent("user2@gmail.com", "ws2", "project1", "type1", "paas1")
                                .withTime("10:10:00,000")
                                .withDate(DATE2).build());

        events.add(Event.Builder.createApplicationCreatedEvent("user3@gmail.com", "ws2", "project1", "type1", "paas2")
                                .withTime("10:00:00")
                                .withDate(DATE2).build());

        events.add(Event.Builder.createApplicationCreatedEvent("user2@gmail.com", "ws2", "project2", "type1", "paas1")
                                .withTime("10:11:00,100")
                                .withDate(DATE2).build());


        // invite users
        events.add(Event.Builder.createUserInviteEvent("user1@gmail.com", "ws1", "email2")
                                .withDate(DATE2).build());
        events.add(Event.Builder.createUserInviteEvent("user3@gmail.com", "ws3", "email3")
                                .withDate(DATE2).build());

        return LogGenerator.generateLog(events);
    }

    private void extractDataFromLog(Context.Builder builder) throws IOException, ParseException {
        builder.put(Parameters.WS, Parameters.WS_TYPES.ANY.toString());
        builder.putAll(scriptsManager.getScript(ScriptType.USERS_PROFILES, MetricType.USERS_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.WORKSPACES_PROFILES, MetricType.WORKSPACES_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.WORKSPACES_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.REMOVED_USERS).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());

        /** create collections "active_users_set" to calculate "active_users" metric */
        builder.putAll(scriptsManager.getScript(ScriptType.USERS_ACTIVITY, MetricType.USERS_ACTIVITY_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_ACTIVITY, builder.build());

        /** create collections "created_project" to calculate "users_who_created_project" metric */
        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.PROJECTS).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());

        /** create collections "tasks" to calculate "users_who_built", "users_who_deploy" metrics */
        builder.putAll(scriptsManager.getScript(ScriptType.TASKS, MetricType.TASKS).getParamsAsMap());
        pigServer.execute(ScriptType.TASKS, builder.build());

        /** create collections "deploys_to_paas" to calculate "users_who_deployed_to_paas" metric */
        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.DEPLOYS_TO_PAAS).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());

        /** create collections "user_invite" to calculate "users_who_invited" metric */
        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.USER_INVITE).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.CREATED_USERS).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.ACTIVE_ENTITIES, MetricType.ACTIVE_USERS_SET).getParamsAsMap());
        pigServer.execute(ScriptType.ACTIVE_ENTITIES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.ACTIVE_ENTITIES, MetricType.ACTIVE_WORKSPACES_SET).getParamsAsMap());
        pigServer.execute(ScriptType.ACTIVE_ENTITIES, builder.build());
    }
}
