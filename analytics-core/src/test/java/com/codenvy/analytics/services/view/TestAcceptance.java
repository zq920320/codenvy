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
package com.codenvy.analytics.services.view;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.Configurator;
import com.codenvy.analytics.Injector;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.CollectionValueData;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.*;
import com.codenvy.analytics.persistent.JdbcDataPersisterFactory;
import com.codenvy.analytics.services.configuration.XmlConfigurationManager;
import com.codenvy.analytics.services.logchecker.LogChecker;
import com.codenvy.analytics.services.pig.PigRunner;
import com.google.common.io.ByteStreams;
import com.google.common.io.OutputSupplier;

import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestAcceptance extends BaseTest {

    private StringBuilder builder = new StringBuilder();
    private ViewBuilder viewBuilder;
    private PigRunner   pigRunner;

    private static final String BASE_TEST_RESOURCE_DIR = BASE_DIR + "/test-classes/" + TestAcceptance.class.getSimpleName();

    private static final String TEST_VIEW_CONFIGURATION_FILE     = BASE_TEST_RESOURCE_DIR + "/view.xml";
    private static final String TEST_EXPECTED_LOG_CHECKER_REPORT = BASE_TEST_RESOURCE_DIR + "/report.txt";
    private static final String TEST_STATISTICS_ARCHIVE          = TestAcceptance.class.getSimpleName() + "/messages_2014-04-23";

    @BeforeClass
    public void init() throws Exception {
        viewBuilder = getViewBuilder(TEST_VIEW_CONFIGURATION_FILE);
        pigRunner = Injector.getInstance(PigRunner.class);
        runScript();
    }

    private void runScript() throws Exception {
        Context context = Utils.initializeContext(Parameters.TimeUnit.DAY);

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        context = context.cloneAndPut(Parameters.LOG, getResourceAsBytes("2014-04-23", df.format(calendar.getTime())).getAbsolutePath());
        pigRunner.forceExecute(context);
    }

    private File getResourceAsBytes(String originalDate, String newDate) throws Exception {
        String archive = getClass().getClassLoader().getResource(TEST_STATISTICS_ARCHIVE).getFile();

        try (ZipInputStream in = new ZipInputStream(new BufferedInputStream(new FileInputStream(archive)))) {
            ZipEntry zipEntry = in.getNextEntry();

            try {
                String name = zipEntry.getName();
                File resource = new File(BASE_TEST_RESOURCE_DIR, name);

                try (OutputStream out = new BufferedOutputStream(new FileOutputStream(resource))) {
                    String resourceAsString = new String(ByteStreams.toByteArray(in), "UTF-8");
                    resourceAsString = resourceAsString.replace(originalDate, newDate);

                    ByteStreams.write(resourceAsString.getBytes("UTF-8"), new OutputSupplier<OutputStream>() {
                        @Override
                        public OutputStream getOutput() throws IOException {
                            return out;
                        }
                    });

                    return resource;
                }
            } finally {
                in.closeEntry();
            }
        }
    }

    @Test
    public void test() throws Exception {
        viewBuilder.forceExecute(Utils.initializeContext(Parameters.TimeUnit.DAY));

        ArgumentCaptor<String> viewId = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ViewData> viewData = ArgumentCaptor.forClass(ViewData.class);
        ArgumentCaptor<Context> context = ArgumentCaptor.forClass(Context.class);

        verify(viewBuilder, atLeastOnce()).retainViewData(viewId.capture(), viewData.capture(), context.capture());

        for (ViewData actualData : viewData.getAllValues()) {
            for (Map.Entry<String, SectionData> entry : actualData.entrySet()) {
                acceptResult(entry.getKey(), entry.getValue());
            }
        }

        assertEquals(builder.length(), 0, builder.toString());
    }

    @Test
    public void testNumberOfItems() throws Exception {
        assertNumberOfItems(MetricType.USAGE_TIME_BY_WORKSPACES_LIST, MetricType.USAGE_TIME_BY_WORKSPACES);
        assertNumberOfItems(MetricType.USERS_ACTIVITY_LIST, MetricType.USERS_ACTIVITY);
        assertNumberOfItems(MetricType.USAGE_TIME_BY_USERS_LIST, MetricType.USAGE_TIME_BY_USERS);
        assertNumberOfItems(MetricType.WORKSPACES_STATISTICS_LIST, MetricType.WORKSPACES_STATISTICS);
        assertNumberOfItems(MetricType.USERS_STATISTICS_LIST, MetricType.USERS_STATISTICS);
        assertNumberOfItems(MetricType.PRODUCT_USAGE_SESSIONS_LIST, MetricType.PRODUCT_USAGE_SESSIONS);
        assertNumberOfItems(MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST, MetricType.PRODUCT_USAGE_FACTORY_SESSIONS);
        assertNumberOfItems(MetricType.CREATED_FACTORIES_SET, MetricType.CREATED_UNIQUE_FACTORIES);
        assertNumberOfItems(MetricType.CREATED_FACTORIES_LIST, MetricType.CREATED_FACTORIES);
        assertNumberOfItems(MetricType.FACTORY_USERS_LIST, MetricType.FACTORY_USERS);
        assertNumberOfItems(MetricType.FACTORY_STATISTICS_LIST, MetricType.FACTORY_STATISTICS);
    }

    @Test
    public void testLogChecker() throws Exception {
        File actualReport = new File(BASE_DIR, "report.txt");
        File expectedReport = new File(TEST_EXPECTED_LOG_CHECKER_REPORT);

        Context context = Utils.initializeContext(Parameters.TimeUnit.DAY);
        LogChecker logChecker = Injector.getInstance(LogChecker.class);

        try (BufferedWriter out = new BufferedWriter(new FileWriter(actualReport))) {
            logChecker.doEventChecker(context, out);
        }

        try (BufferedReader in1 = new BufferedReader(new FileReader(actualReport));
             BufferedReader in2 = new BufferedReader(new FileReader(expectedReport))) {

            String strLine1, strLine2;
            while ((strLine1 = in1.readLine()) != null && (strLine2 = in2.readLine()) != null) {
                assertNotNull(strLine1);
                assertNotNull(strLine2);
                assertTrue(strLine1.equals(strLine2));
            }
        }
    }

    private void assertNumberOfItems(MetricType listMetricType, MetricType countMetricType) throws IOException {
        Context context = new Context.Builder().build();

        Metric listMetric = MetricFactory.getMetric(listMetricType);
        Metric countMetric = MetricFactory.getMetric(countMetricType);

        CollectionValueData listValueData = (CollectionValueData)listMetric.getValue(context);
        LongValueData longValueData = (LongValueData)countMetric.getValue(context);

        assertEquals(listValueData.size(), longValueData.getAsLong());
    }

    private void acceptResult(String tableName, SectionData sectionData) {
        if (tableName.endsWith("day")) {
            switch (tableName) {
                case "invitations_day":
                    assertInvitationsDay(sectionData);
                    break;
                case "time_spent_day":
                    assertTimeSpentDay(sectionData);
                    break;
                case "workspaces_day":
                    assertWorkspacesDay(sectionData);
                    break;
                case "projects_day":
                    assertProjectsDay(sectionData);
                    break;
                case "users_day":
                    assertUsersDay(sectionData);
                    break;
                case "ide_usage_day":
                    assertIdeUsageDay(sectionData);
                    break;
                case "usage_time_day":
                    assertUsageTimeDay(sectionData);
                    break;
                case "workspaces_usage_day":
                    assertWorkspaceUsageDay(sectionData);
                    break;
                case "user_sessions_day":
                    assertUserSessionsDay(sectionData);
                    break;
                case "users_usage_day":
                    assertUsersUsageDay(sectionData);
                    break;
                case "active_users_usage_day":
                    assertActiveUsersUsageDay(sectionData);
                    break;
                case "authentications_day":
                    assertAuthenticationsDay(sectionData);
                    break;
                case "users_engagement_day":
                    assertUsersEngagementDay(sectionData);
                    break;
                case "projects_types_day":
                    assertProjectsTypesDay(sectionData);
                    break;
                case "projects_paas_day":
                    assertProjectsPaasDay(sectionData);
                    break;
                case "factories_day":
                    assertFactoriesDay(sectionData);
                    break;
                case "authenticated_factory_sessions_day":
                    assertAuthenticatedFactorySessionsDay(sectionData);
                    break;
                case "converted_factory_sessions_day":
                    assertConvertedFactorySessionsDay(sectionData);
                    break;
                case "factory_sessions_ide_usage_events_day":
                    assertFactorySessionsIdeUsageEventsDay(sectionData);
                    break;
                case "factory_users_sessions_day":
                    assertFactoryUsersSessionsDay(sectionData);
                    break;
                case "factory_product_usage_day":
                    assertFactoryProductUsageDay(sectionData);
                    break;
                case "analysis_day":
                    assertAnalysis(sectionData);
                    break;
                default:
                    break;
            }
        } else if (tableName.endsWith("lifetime")) {
            switch (tableName) {
                case "user_profile_lifetime":
                    assertUsersProfiles(sectionData);
                    break;
            }
        }
    }

    private void assertAnalysis(SectionData sectionData) {
        // Total users
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("2,031"), sectionData.get(1).get(1));

        // The total number of users we track
        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("96"), sectionData.get(2).get(1));

        // The number of users who created projects
        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("47"), sectionData.get(3).get(1));

        // & Built
        row = sectionData.get(4).get(0).getAsString();
        aggregateResult(row, new StringValueData("18"), sectionData.get(4).get(1));

        // & Run
        row = sectionData.get(5).get(0).getAsString();
        aggregateResult(row, new StringValueData("29"), sectionData.get(5).get(1));

        // & Deployed to PAAS
        row = sectionData.get(6).get(0).getAsString();
        aggregateResult(row, new StringValueData("14"), sectionData.get(6).get(1));

        // sent invitations
        row = sectionData.get(7).get(0).getAsString();
        aggregateResult(row, new StringValueData("1"), sectionData.get(7).get(1));

        // launched shell
        row = sectionData.get(8).get(0).getAsString();
        aggregateResult(row, new StringValueData("8"), sectionData.get(8).get(1));
    }

    private void assertUsersProfiles(SectionData sectionData) {
        aggregateResult("User's profiles", new StringValueData("Email"), sectionData.get(0).get(0));
        aggregateResult("User's profiles", new StringValueData("First Name"), sectionData.get(0).get(1));
        aggregateResult("User's profiles", new StringValueData("Last Name"), sectionData.get(0).get(2));
        aggregateResult("User's profiles", new StringValueData("Company"), sectionData.get(0).get(3));
        aggregateResult("User's profiles", new StringValueData("Job"), sectionData.get(0).get(4));

        aggregateResult("User's profiles", LongValueData.valueOf(47), LongValueData.valueOf(sectionData.size()));
    }

    private void assertFactoryProductUsageDay(SectionData sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Product Usage Mins"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("13:16:33"), sectionData.get(1).get(1));
    }

    private void assertFactoryUsersSessionsDay(SectionData sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Factory Sessions"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("130"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("< 10 Mins"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("117"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("> 10 Mins"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("13"), sectionData.get(3).get(1));
    }

    private void assertFactorySessionsIdeUsageEventsDay(SectionData sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Factory Sessions"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("130"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("% Built"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("2%"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("% Run"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("13%"), sectionData.get(3).get(1));

        row = sectionData.get(4).get(0).getAsString();
        aggregateResult(row, new StringValueData("% Deployed"), sectionData.get(4).get(0));
        aggregateResult(row, new StringValueData("6%"), sectionData.get(4).get(1));
    }

    private void assertConvertedFactorySessionsDay(SectionData sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Factory Sessions"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("130"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("Abandoned Sessions"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("124"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("Converted Sessions"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("6"), sectionData.get(3).get(1));
    }

    private void assertAuthenticatedFactorySessionsDay(SectionData sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Factory Sessions"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("130"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("Anonymous Sessions"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("123"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("Authenticated Sessions"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("7"), sectionData.get(3).get(1));
    }

    private void assertFactoriesDay(SectionData sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Factories Registered"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("7"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("Accounts Created from Factory Sessions"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("5"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("Temporary Workspaces Created"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("110"), sectionData.get(3).get(1));

        row = sectionData.get(4).get(0).getAsString();
        aggregateResult(row, new StringValueData("# with more than one session"), sectionData.get(4).get(0));
        aggregateResult(row, new StringValueData("8"), sectionData.get(4).get(1));

        row = sectionData.get(5).get(0).getAsString();
        aggregateResult(row, new StringValueData("# with empty sessions"), sectionData.get(5).get(0));
        aggregateResult(row, new StringValueData("41"), sectionData.get(5).get(1));
    }

    private void assertProjectsPaasDay(SectionData sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("176"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("AWS"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("1"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("AppFog"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("7"), sectionData.get(3).get(1));

        row = sectionData.get(4).get(0).getAsString();
        aggregateResult(row, new StringValueData("CloudBees"), sectionData.get(4).get(0));
        aggregateResult(row, new StringValueData("1"), sectionData.get(4).get(1));

        row = sectionData.get(5).get(0).getAsString();
        aggregateResult(row, new StringValueData("CloudFoundry"), sectionData.get(5).get(0));
        aggregateResult(row, new StringValueData("1"), sectionData.get(5).get(1));

        row = sectionData.get(6).get(0).getAsString();
        aggregateResult(row, new StringValueData("GAE"), sectionData.get(6).get(0));
        aggregateResult(row, new StringValueData("2"), sectionData.get(6).get(1));

        row = sectionData.get(7).get(0).getAsString();
        aggregateResult(row, new StringValueData("Heroku"), sectionData.get(7).get(0));
        aggregateResult(row, new StringValueData("1"), sectionData.get(7).get(1));

        row = sectionData.get(8).get(0).getAsString();
        aggregateResult(row, new StringValueData("OpenShift"), sectionData.get(8).get(0));
        aggregateResult(row, new StringValueData("5"), sectionData.get(8).get(1));

        row = sectionData.get(9).get(0).getAsString();
        aggregateResult(row, new StringValueData("Tier3"), sectionData.get(9).get(0));
        aggregateResult(row, new StringValueData("1"), sectionData.get(9).get(1));

        row = sectionData.get(10).get(0).getAsString();
        aggregateResult(row, new StringValueData("Manymo"), sectionData.get(10).get(0));
        aggregateResult(row, new StringValueData("1"), sectionData.get(10).get(1));

        row = sectionData.get(11).get(0).getAsString();
        aggregateResult(row, new StringValueData("No PaaS Defined"), sectionData.get(11).get(0));
        aggregateResult(row, new StringValueData("156"), sectionData.get(11).get(1));
    }

    private void assertProjectsTypesDay(SectionData sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("176"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("Java Jar"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("14"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("Java War"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("2"), sectionData.get(3).get(1));

        row = sectionData.get(4).get(0).getAsString();
        aggregateResult(row, new StringValueData("Java JSP"), sectionData.get(4).get(0));
        aggregateResult(row, new StringValueData("21"), sectionData.get(4).get(1));

        row = sectionData.get(5).get(0).getAsString();
        aggregateResult(row, new StringValueData("Java Spring"), sectionData.get(5).get(0));
        aggregateResult(row, new StringValueData("2"), sectionData.get(5).get(1));

        row = sectionData.get(6).get(0).getAsString();
        aggregateResult(row, new StringValueData("PHP"), sectionData.get(6).get(0));
        aggregateResult(row, new StringValueData("51"), sectionData.get(6).get(1));

        row = sectionData.get(7).get(0).getAsString();
        aggregateResult(row, new StringValueData("Python"), sectionData.get(7).get(0));
        aggregateResult(row, new StringValueData("13"), sectionData.get(7).get(1));

        row = sectionData.get(8).get(0).getAsString();
        aggregateResult(row, new StringValueData("JavaScript"), sectionData.get(8).get(0));
        aggregateResult(row, new StringValueData("23"), sectionData.get(8).get(1));

        row = sectionData.get(9).get(0).getAsString();
        aggregateResult(row, new StringValueData("Ruby"), sectionData.get(9).get(0));
        aggregateResult(row, new StringValueData("6"), sectionData.get(9).get(1));

        row = sectionData.get(10).get(0).getAsString();
        aggregateResult(row, new StringValueData("Maven Multi Project"), sectionData.get(10).get(0));
        aggregateResult(row, new StringValueData("1"), sectionData.get(10).get(1));

        row = sectionData.get(11).get(0).getAsString();
        aggregateResult(row, new StringValueData("Node.js"), sectionData.get(11).get(0));
        aggregateResult(row, new StringValueData("7"), sectionData.get(11).get(1));

        row = sectionData.get(12).get(0).getAsString();
        aggregateResult(row, new StringValueData("Android"), sectionData.get(12).get(0));
        aggregateResult(row, new StringValueData("34"), sectionData.get(12).get(1));

        row = sectionData.get(13).get(0).getAsString();
        aggregateResult(row, new StringValueData("Django"), sectionData.get(13).get(0));
        aggregateResult(row, new StringValueData("1"), sectionData.get(13).get(1));

        row = sectionData.get(14).get(0).getAsString();
        aggregateResult(row, new StringValueData("Others"), sectionData.get(14).get(0));
        aggregateResult(row, new StringValueData("1"), sectionData.get(14).get(1));
    }

    private void assertUsersEngagementDay(SectionData sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("96"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("< 10 Min"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("51"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData(">= 10 And < 60 Mins"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("32"), sectionData.get(3).get(1));

        row = sectionData.get(4).get(0).getAsString();
        aggregateResult(row, new StringValueData(">= 60 And < 300 Mins"), sectionData.get(4).get(0));
        aggregateResult(row, new StringValueData("12"), sectionData.get(4).get(1));

        row = sectionData.get(5).get(0).getAsString();
        aggregateResult(row, new StringValueData("> 300 Mins"), sectionData.get(5).get(0));
        aggregateResult(row, new StringValueData("1"), sectionData.get(5).get(1));
    }

    private void assertAuthenticationsDay(SectionData sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Google Auth"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("69%"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("Github Auth"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("11%"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("Form Auth"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("20%"), sectionData.get(3).get(1));
    }

    private void assertUserSessionsDay(SectionData sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("344"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("<= 1 Min"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("172"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("> 1 And < 10 Mins"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("101"), sectionData.get(3).get(1));

        row = sectionData.get(4).get(0).getAsString();
        aggregateResult(row, new StringValueData(">= 10 And <= 60 Mins"), sectionData.get(4).get(0));
        aggregateResult(row, new StringValueData("57"), sectionData.get(4).get(1));

        row = sectionData.get(5).get(0).getAsString();
        aggregateResult(row, new StringValueData("> 60 Mins"), sectionData.get(5).get(0));
        aggregateResult(row, new StringValueData("14"), sectionData.get(5).get(1));
    }

    private void assertWorkspaceUsageDay(SectionData sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("1,030"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("New Active Workspaces"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("31"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("Returning Active Workspaces"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("195"), sectionData.get(3).get(1));

        row = sectionData.get(4).get(0).getAsString();
        aggregateResult(row, new StringValueData("Non-Active Workspaces"), sectionData.get(4).get(0));
        aggregateResult(row, new StringValueData("804"), sectionData.get(4).get(1));
    }

    private void assertUsageTimeDay(SectionData sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("56:39:39"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("<= 1 Min"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("02:52:00"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("> 1 And < 10 Mins"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("06:50:02"), sectionData.get(3).get(1));

        row = sectionData.get(4).get(0).getAsString();
        aggregateResult(row, new StringValueData(">= 10 And <= 60 Mins"), sectionData.get(4).get(0));
        aggregateResult(row, new StringValueData("21:48:39"), sectionData.get(4).get(1));

        row = sectionData.get(5).get(0).getAsString();
        aggregateResult(row, new StringValueData("> 60 Mins"), sectionData.get(5).get(0));
        aggregateResult(row, new StringValueData("25:08:56"), sectionData.get(5).get(1));
    }

    private void assertIdeUsageDay(SectionData sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("# Refactors"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("2"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("# Code Completions"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("75"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("# Builds"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("66"), sectionData.get(3).get(1));

        row = sectionData.get(4).get(0).getAsString();
        aggregateResult(row, new StringValueData("# Runs"), sectionData.get(4).get(0));
        aggregateResult(row, new StringValueData("209"), sectionData.get(4).get(1));

        row = sectionData.get(5).get(0).getAsString();
        aggregateResult(row, new StringValueData("# Debugs"), sectionData.get(5).get(0));
        aggregateResult(row, new StringValueData("1"), sectionData.get(5).get(1));
    }

    private void assertUsersDay(SectionData sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total Created"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("31"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("Created From Factory"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("5"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("Created From Form / oAuth"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("26"), sectionData.get(3).get(1));

        row = sectionData.get(4).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total"), sectionData.get(4).get(0));
        aggregateResult(row, new StringValueData("2,031"), sectionData.get(4).get(1));
    }

    private void assertUsersUsageDay(SectionData sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("2,031"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("Active Users"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("96"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("Non-Active Users"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("1,935"), sectionData.get(3).get(1));
    }

    private void assertActiveUsersUsageDay(SectionData sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total Active Users"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("96"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("New Active Users"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("31"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("Returning Active Users"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("65"), sectionData.get(3).get(1));
    }

    private void assertProjectsDay(SectionData sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Created"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("176"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("Destroyed"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("47"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("3,129"), sectionData.get(3).get(1));
    }

    private void assertWorkspacesDay(SectionData sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Created"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("31"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("Destroyed"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("1"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("1,030"), sectionData.get(3).get(1));
    }

    private void assertTimeSpentDay(SectionData sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Builds"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("00:16:01"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("Runs"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("05:51:14"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("Debugs"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("00:02:56"), sectionData.get(3).get(1));
    }

    private void assertInvitationsDay(SectionData sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Sent"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("2"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("Accepted"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("50%"), sectionData.get(2).get(1));
    }

    private void aggregateResult(String row, ValueData expected, ValueData actual) {
        if (!expected.equals(actual)) {
            builder.append('[');
            builder.append(row);
            builder.append(']');
            builder.append(" expected: ");
            builder.append(expected.getAsString());
            builder.append(" actual: ");
            builder.append(actual.getAsString());
            builder.append('\n');
        }
    }

    /** Creates view builder with test configuration */
    private ViewBuilder getViewBuilder(final String viewConfigurationPath) throws IOException {
        XmlConfigurationManager viewConfigurationManager = mock(XmlConfigurationManager.class);

        when(viewConfigurationManager.loadConfiguration(any(Class.class), anyString())).thenAnswer(
                new Answer<Object>() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        XmlConfigurationManager manager = new XmlConfigurationManager();
                        return manager.loadConfiguration(DisplayConfiguration.class, viewConfigurationPath);
                    }
                });

        Configurator viewConfigurator = spy(Injector.getInstance(Configurator.class));
        doReturn(new String[]{viewConfigurationPath}).when(viewConfigurator).getArray(anyString());

        return spy(new ViewBuilder(Injector.getInstance(JdbcDataPersisterFactory.class),
                                   Injector.getInstance(CSVReportPersister.class),
                                   viewConfigurationManager,
                                   viewConfigurator));
    }
}
