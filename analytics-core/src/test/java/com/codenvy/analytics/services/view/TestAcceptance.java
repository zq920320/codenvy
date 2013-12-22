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
package com.codenvy.analytics.services.view;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.services.pig.PigRunner;
import com.google.common.io.ByteStreams;
import com.google.common.io.OutputSupplier;

import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestAcceptance extends BaseTest {

    private StringBuilder builder = new StringBuilder();

    @BeforeClass
    public void prepare() throws Exception {
        runScript();
    }

    private void runScript() throws Exception {
        PigRunner pigRunner = new PigRunner();
        Map<String, String> context = Utils.initializeContext(Parameters.TimeUnit.DAY);

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        Parameters.LOG.put(context, getResourceAsBytes("2013-11-24", df.format(calendar.getTime())).getAbsolutePath());
        pigRunner.forceExecute(context);
    }

    private File getResourceAsBytes(String originalDate, String newDate) throws Exception {
        String archive = getClass().getClassLoader().getResource("messages_" + originalDate).getFile();

        try (ZipInputStream in = new ZipInputStream(new BufferedInputStream(new FileInputStream(archive)))) {
            ZipEntry zipEntry = in.getNextEntry();

            try {
                String name = zipEntry.getName();
                File resource = new File(BASE_DIR, name);

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
        ViewBuilder viewBuilder = spy(new ViewBuilder());

        viewBuilder.forceExecute(Utils.newContext());

        ArgumentCaptor<String> viewId = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> viewData = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map> context = ArgumentCaptor.forClass(Map.class);

        verify(viewBuilder, atLeastOnce()).retainViewData(viewId.capture(), viewData.capture(), context.capture());

        for (Map<String, List<List<ValueData>>> actualData : viewData.getAllValues()) { System.out.println(viewData.getAllValues().toString());
            for (Map.Entry<String, List<List<ValueData>>> entry : actualData.entrySet()) {
                acceptResult(entry.getKey(), entry.getValue());
            }
        }

        assertEquals(builder.length(), 0, builder.toString());
    }

    private void acceptResult(String tableName, List<List<ValueData>> sectionData) {
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
                default:
                    fail("Unknown table name " + tableName);
            }
        }
    }

    private void assertFactoryProductUsageDay(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Product Usage Mins"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("1,457"), sectionData.get(1).get(1));
    }

    private void assertFactoryUsersSessionsDay(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Factory Sessions"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("234"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("< 10 Mins"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("203"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("> 10 Mins"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("31"), sectionData.get(3).get(1));
    }

    private void assertFactorySessionsIdeUsageEventsDay(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Factory Sessions"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("234"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("% Built"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("19%"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("% Run"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("22%"), sectionData.get(3).get(1));

        row = sectionData.get(4).get(0).getAsString();
        aggregateResult(row, new StringValueData("% Deployed"), sectionData.get(4).get(0));
        aggregateResult(row, new StringValueData("13%"), sectionData.get(4).get(1));
    }

    private void assertConvertedFactorySessionsDay(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Factory Sessions"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("234"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("Abandoned Sessions"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("220"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("Converted Sessions"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("14"), sectionData.get(3).get(1));
    }

    private void assertAuthenticatedFactorySessionsDay(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Factory Sessions"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("234"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("Anonymous Sessions"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("211"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("Authenticated Sessions"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("23"), sectionData.get(3).get(1));
    }

    private void assertFactoriesDay(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Factories Created"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("12"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("Temporary Workspaces Created"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("280"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("Accounts Created From Factories"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("15"), sectionData.get(3).get(1));
    }

    private void assertProjectsPaasDay(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("400"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("AWS"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("1"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("AppFog"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("16"), sectionData.get(3).get(1));

        row = sectionData.get(4).get(0).getAsString();
        aggregateResult(row, new StringValueData("CloudBees"), sectionData.get(4).get(0));
        aggregateResult(row, new StringValueData("1"), sectionData.get(4).get(1));

        row = sectionData.get(5).get(0).getAsString();
        aggregateResult(row, new StringValueData("CloudFoundry"), sectionData.get(5).get(0));
        aggregateResult(row, new StringValueData("1"), sectionData.get(5).get(1));

        row = sectionData.get(6).get(0).getAsString();
        aggregateResult(row, new StringValueData("GAE"), sectionData.get(6).get(0));
        aggregateResult(row, new StringValueData("7"), sectionData.get(6).get(1));

        row = sectionData.get(7).get(0).getAsString();
        aggregateResult(row, new StringValueData("Heroku"), sectionData.get(7).get(0));
        aggregateResult(row, new StringValueData("1"), sectionData.get(7).get(1));

        row = sectionData.get(8).get(0).getAsString();
        aggregateResult(row, new StringValueData("OpenShift"), sectionData.get(8).get(0));
        aggregateResult(row, new StringValueData("7"), sectionData.get(8).get(1));

        row = sectionData.get(9).get(0).getAsString();
        aggregateResult(row, new StringValueData("Tier3"), sectionData.get(9).get(0));
        aggregateResult(row, new StringValueData("1"), sectionData.get(9).get(1));

        row = sectionData.get(10).get(0).getAsString();
        aggregateResult(row, new StringValueData("Manymo"), sectionData.get(10).get(0));
        aggregateResult(row, new StringValueData("1"), sectionData.get(10).get(1));

        row = sectionData.get(11).get(0).getAsString();
        aggregateResult(row, new StringValueData("No PaaS Defined"), sectionData.get(11).get(0));
        aggregateResult(row, new StringValueData("364"), sectionData.get(11).get(1));
    }

    private void assertProjectsTypesDay(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("400"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("Java Jar"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("29"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("Java War"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("4"), sectionData.get(3).get(1));

        row = sectionData.get(4).get(0).getAsString();
        aggregateResult(row, new StringValueData("Java JSP"), sectionData.get(4).get(0));
        aggregateResult(row, new StringValueData("31"), sectionData.get(4).get(1));

        row = sectionData.get(5).get(0).getAsString();
        aggregateResult(row, new StringValueData("Java Spring"), sectionData.get(5).get(0));
        aggregateResult(row, new StringValueData("8"), sectionData.get(5).get(1));

        row = sectionData.get(6).get(0).getAsString();
        aggregateResult(row, new StringValueData("PHP"), sectionData.get(6).get(0));
        aggregateResult(row, new StringValueData("104"), sectionData.get(6).get(1));

        row = sectionData.get(7).get(0).getAsString();
        aggregateResult(row, new StringValueData("Python"), sectionData.get(7).get(0));
        aggregateResult(row, new StringValueData("47"), sectionData.get(7).get(1));

        row = sectionData.get(8).get(0).getAsString();
        aggregateResult(row, new StringValueData("JavaScript"), sectionData.get(8).get(0));
        aggregateResult(row, new StringValueData("64"), sectionData.get(8).get(1));

        row = sectionData.get(9).get(0).getAsString();
        aggregateResult(row, new StringValueData("Ruby"), sectionData.get(9).get(0));
        aggregateResult(row, new StringValueData("16"), sectionData.get(9).get(1));

        row = sectionData.get(10).get(0).getAsString();
        aggregateResult(row, new StringValueData("Maven Multi Project"), sectionData.get(10).get(0));
        aggregateResult(row, new StringValueData("1"), sectionData.get(10).get(1));

        row = sectionData.get(11).get(0).getAsString();
        aggregateResult(row, new StringValueData("Node.js"), sectionData.get(11).get(0));
        aggregateResult(row, new StringValueData("19"), sectionData.get(11).get(1));

        row = sectionData.get(12).get(0).getAsString();
        aggregateResult(row, new StringValueData("Android"), sectionData.get(12).get(0));
        aggregateResult(row, new StringValueData("75"), sectionData.get(12).get(1));

        row = sectionData.get(13).get(0).getAsString();
        aggregateResult(row, new StringValueData("Django"), sectionData.get(13).get(0));
        aggregateResult(row, new StringValueData("1"), sectionData.get(13).get(1));

        row = sectionData.get(14).get(0).getAsString();
        aggregateResult(row, new StringValueData("Others"), sectionData.get(14).get(0));
        aggregateResult(row, new StringValueData("1"), sectionData.get(14).get(1));
    }

    private void assertUsersEngagementDay(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("202"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("< 10 Min"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("112"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData(">= 10 And < 60 Mins"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("59"), sectionData.get(3).get(1));

        row = sectionData.get(4).get(0).getAsString();
        aggregateResult(row, new StringValueData(">= 60 And < 300 Mins"), sectionData.get(4).get(0));
        aggregateResult(row, new StringValueData("29"), sectionData.get(4).get(1));

        row = sectionData.get(5).get(0).getAsString();
        aggregateResult(row, new StringValueData("> 300 Mins"), sectionData.get(5).get(0));
        aggregateResult(row, new StringValueData("2"), sectionData.get(5).get(1));
    }

    private void assertAuthenticationsDay(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Google Auth"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("66%"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("Github Auth"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("10%"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("Form Auth"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("24%"), sectionData.get(3).get(1));
    }

    private void assertUserSessionsDay(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("702"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("<= 1 Min"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("218"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("> 1 And < 10 Mins"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("298"), sectionData.get(3).get(1));

        row = sectionData.get(4).get(0).getAsString();
        aggregateResult(row, new StringValueData(">= 10 And <= 60 Mins"), sectionData.get(4).get(0));
        aggregateResult(row, new StringValueData("154"), sectionData.get(4).get(1));

        row = sectionData.get(5).get(0).getAsString();
        aggregateResult(row, new StringValueData("> 60 Mins"), sectionData.get(5).get(0));
        aggregateResult(row, new StringValueData("32"), sectionData.get(5).get(1));
    }

    private void assertWorkspaceUsageDay(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("82"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("New Active Workspaces"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("73"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("Returning Active Workspaces"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("414"), sectionData.get(3).get(1));

        row = sectionData.get(4).get(0).getAsString();
        aggregateResult(row, new StringValueData("Non-Active Workspaces"), sectionData.get(4).get(0));
        aggregateResult(row, new StringValueData("-405"), sectionData.get(4).get(1));
    }

    private void assertUsageTimeDay(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("8,568"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("<= 1 Min"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("218"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("> 1 And < 10 Mins"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("1,436"), sectionData.get(3).get(1));

        row = sectionData.get(4).get(0).getAsString();
        aggregateResult(row, new StringValueData(">= 10 And <= 60 Mins"), sectionData.get(4).get(0));
        aggregateResult(row, new StringValueData("3,584"), sectionData.get(4).get(1));

        row = sectionData.get(5).get(0).getAsString();
        aggregateResult(row, new StringValueData("> 60 Mins"), sectionData.get(5).get(0));
        aggregateResult(row, new StringValueData("3,330"), sectionData.get(5).get(1));
    }

    private void assertIdeUsageDay(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("# Refactors"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("1"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("# Code Completions"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("289"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("# Builds"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("573"), sectionData.get(3).get(1));

        row = sectionData.get(4).get(0).getAsString();
        aggregateResult(row, new StringValueData("# Runs"), sectionData.get(4).get(0));
        aggregateResult(row, new StringValueData("314"), sectionData.get(4).get(1));

        row = sectionData.get(5).get(0).getAsString();
        aggregateResult(row, new StringValueData("# Debugs"), sectionData.get(5).get(0));
        aggregateResult(row, new StringValueData("2"), sectionData.get(5).get(1));
    }

    private void assertUsersDay(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total Created"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("76"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("Created From Factory"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("15"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("Created From Form / oAuth"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("61"), sectionData.get(3).get(1));

        row = sectionData.get(4).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total"), sectionData.get(4).get(0));
        aggregateResult(row, new StringValueData("96"), sectionData.get(4).get(1));
    }

    private void assertUsersUsageDay(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("96"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("New Active Users"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("76"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("Returning Active Users"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("126"), sectionData.get(3).get(1));

        row = sectionData.get(4).get(0).getAsString();
        aggregateResult(row, new StringValueData("Non-Active Users"), sectionData.get(4).get(0));
        aggregateResult(row, new StringValueData("-106"), sectionData.get(4).get(1));
    }

    private void assertProjectsDay(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Created"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("400"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("Destroyed"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("84"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("346"), sectionData.get(3).get(1));
    }

    private void assertWorkspacesDay(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Created"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("73"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("Destroyed"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("1"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("82"), sectionData.get(3).get(1));
    }

    private void assertTimeSpentDay(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Builds"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("60"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("Runs"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("415"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("Debugs"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("4"), sectionData.get(3).get(1));
    }

    private void assertInvitationsDay(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Sent"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("5"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("Accepted"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("100%"), sectionData.get(2).get(1));
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

}
