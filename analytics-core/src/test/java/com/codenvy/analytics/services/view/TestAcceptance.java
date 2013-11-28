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
import static org.testng.Assert.fail;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestAcceptance extends BaseTest {

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

        Parameters.LOG.put(context, getResourceAsBytes("2013-11-23", df.format(calendar.getTime())).getAbsolutePath());
        pigRunner.forceExecute(context);

        calendar.add(Calendar.DAY_OF_MONTH, -1);
        context = Utils.prevDateInterval(context);

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

        ArgumentCaptor<String> tblName = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<List> data = ArgumentCaptor.forClass(List.class);

        verify(viewBuilder, atLeastOnce()).retainData(tblName.capture(), data.capture());

        for (int i = 0; i < tblName.getAllValues().size(); i++) {
            acceptResult(tblName.getAllValues().get(i), data.getAllValues().get(i));
        }
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
                case "factory_day":
                    assertFactoryDay(sectionData);
                    break;
                default:
                    fail("Unknown table name " + tableName);
            }
        } else if (tableName.endsWith("lifetime")) {
            switch (tableName) {
                case "invitations_lifetime":
                    break;
                case "time_spent_lifetime":
                    break;
                case "workspaces_lifetime":
                    break;
                case "projects_lifetime":
                    break;
                case "users_lifetime":
                    break;
                case "ide_usage_lifetime":
                    break;
                case "usage_time_lifetime":
                    break;
                case "workspaces_usage_lifetime":
                    break;
                case "user_sessions_lifetime":
                    break;
                case "users_usage_lifetime":
                    break;
                case "authentications_lifetime":
                    break;
                case "users_engagement_lifetime":
                    break;
                case "projects_types_lifetime":
                    break;
                case "projects_paas_lifetime":
                    break;
                case "factory_lifetime":
                    assertFactoryLifeTime(sectionData);
                    break;
                default:
                    fail("Unknown table name " + tableName);
            }
        }
    }

    private void assertFactoryDay(List<List<ValueData>> sectionData) {
        assertEquals(new StringValueData("Factories Created"), sectionData.get(1).get(0));
        assertEquals(new StringValueData("5"), sectionData.get(1).get(1));
        assertEquals(new StringValueData("12"), sectionData.get(1).get(2));

        assertEquals(new StringValueData("Temporary Workspaces Created"), sectionData.get(2).get(0));
        assertEquals(new StringValueData("348"), sectionData.get(2).get(1));
        assertEquals(new StringValueData("280"), sectionData.get(2).get(2));

        assertEquals(new StringValueData("Accounts Created From Factories"), sectionData.get(3).get(0));
        assertEquals(new StringValueData("15"), sectionData.get(3).get(1));
        assertEquals(new StringValueData("15"), sectionData.get(3).get(2));

        assertEquals(StringValueData.DEFAULT, sectionData.get(4).get(0));
        assertEquals(StringValueData.DEFAULT, sectionData.get(4).get(1));
        assertEquals(StringValueData.DEFAULT, sectionData.get(4).get(2));

        assertEquals(new StringValueData("Factory Sessions"), sectionData.get(5).get(0));
        assertEquals(new StringValueData("293"), sectionData.get(5).get(1));
        assertEquals(new StringValueData("233"), sectionData.get(5).get(2));

        assertEquals(new StringValueData("Anonymous Sessions"), sectionData.get(6).get(0));
        assertEquals(new StringValueData("275"), sectionData.get(6).get(1));
        assertEquals(new StringValueData("210"), sectionData.get(6).get(2));

        assertEquals(new StringValueData("Authenticated Sessions"), sectionData.get(7).get(0));
        assertEquals(new StringValueData("18"), sectionData.get(7).get(1));
        assertEquals(new StringValueData("23"), sectionData.get(7).get(2));

        assertEquals(StringValueData.DEFAULT, sectionData.get(8).get(0));
        assertEquals(StringValueData.DEFAULT, sectionData.get(8).get(1));
        assertEquals(StringValueData.DEFAULT, sectionData.get(8).get(2));

        assertEquals(new StringValueData("Factory Sessions"), sectionData.get(9).get(0));
        assertEquals(new StringValueData("293"), sectionData.get(9).get(1));
        assertEquals(new StringValueData("233"), sectionData.get(9).get(2));

        assertEquals(new StringValueData("Abandoned Sessions"), sectionData.get(10).get(0));
        assertEquals(new StringValueData("282"), sectionData.get(10).get(1));
        assertEquals(new StringValueData("223"), sectionData.get(10).get(2));

        assertEquals(new StringValueData("Converted Sessions"), sectionData.get(11).get(0));
        assertEquals(new StringValueData("11"), sectionData.get(11).get(1));
        assertEquals(new StringValueData("10"), sectionData.get(11).get(2));

        assertEquals(StringValueData.DEFAULT, sectionData.get(12).get(0));
        assertEquals(StringValueData.DEFAULT, sectionData.get(12).get(1));
        assertEquals(StringValueData.DEFAULT, sectionData.get(12).get(2));

        assertEquals(new StringValueData("Factory Sessions"), sectionData.get(13).get(0));
        assertEquals(new StringValueData("293"), sectionData.get(13).get(1));
        assertEquals(new StringValueData("233"), sectionData.get(13).get(2));

        assertEquals(new StringValueData("% Built"), sectionData.get(14).get(0));
        assertEquals(new StringValueData("19%"), sectionData.get(14).get(1));
        assertEquals(new StringValueData("19%"), sectionData.get(14).get(2));

        assertEquals(new StringValueData("% Run"), sectionData.get(15).get(0));
        assertEquals(new StringValueData("17%"), sectionData.get(15).get(1));
        assertEquals(new StringValueData("23%"), sectionData.get(15).get(2));

        assertEquals(new StringValueData("% Deployed"), sectionData.get(16).get(0));
        assertEquals(new StringValueData("10%"), sectionData.get(16).get(1));
        assertEquals(new StringValueData("14%"), sectionData.get(16).get(2));

        assertEquals(StringValueData.DEFAULT, sectionData.get(17).get(0));
        assertEquals(StringValueData.DEFAULT, sectionData.get(17).get(1));
        assertEquals(StringValueData.DEFAULT, sectionData.get(17).get(2));

        assertEquals(new StringValueData("Factory Sessions"), sectionData.get(18).get(0));
        assertEquals(new StringValueData("293"), sectionData.get(18).get(1));
        assertEquals(new StringValueData("233"), sectionData.get(18).get(2));

        assertEquals(new StringValueData("< 10 Mins"), sectionData.get(19).get(0));
        assertEquals(new StringValueData("261"), sectionData.get(19).get(1));
        assertEquals(new StringValueData("202"), sectionData.get(19).get(2));

        assertEquals(new StringValueData("> 10 Mins"), sectionData.get(20).get(0));
        assertEquals(new StringValueData("32"), sectionData.get(20).get(1));
        assertEquals(new StringValueData("31"), sectionData.get(20).get(2));

        assertEquals(StringValueData.DEFAULT, sectionData.get(21).get(0));
        assertEquals(StringValueData.DEFAULT, sectionData.get(21).get(1));
        assertEquals(StringValueData.DEFAULT, sectionData.get(21).get(2));

        assertEquals(new StringValueData("Product Usage Mins"), sectionData.get(22).get(0));
        assertEquals(new StringValueData("1,445"), sectionData.get(22).get(1));
        assertEquals(new StringValueData("1,456"), sectionData.get(22).get(2));
    }

    private void assertFactoryLifeTime(List<List<ValueData>> sectionData) {
        assertEquals(new StringValueData("Factories Created"), sectionData.get(1).get(0));
        assertEquals(new StringValueData("17"), sectionData.get(1).get(1));

        assertEquals(new StringValueData("Temporary Workspaces Created"), sectionData.get(2).get(0));
        assertEquals(new StringValueData("628"), sectionData.get(2).get(1));

        assertEquals(new StringValueData("Accounts Created From Factories"), sectionData.get(3).get(0));
        assertEquals(new StringValueData("30"), sectionData.get(3).get(1));

        assertEquals(StringValueData.DEFAULT, sectionData.get(4).get(0));
        assertEquals(StringValueData.DEFAULT, sectionData.get(4).get(1));

        assertEquals(new StringValueData("Factory Sessions"), sectionData.get(5).get(0));
        assertEquals(new StringValueData("526"), sectionData.get(5).get(1));

        assertEquals(new StringValueData("Anonymous Sessions"), sectionData.get(6).get(0));
        assertEquals(new StringValueData("485"), sectionData.get(6).get(1));

        assertEquals(new StringValueData("Authenticated Sessions"), sectionData.get(7).get(0));
        assertEquals(new StringValueData("41"), sectionData.get(7).get(1));

        assertEquals(StringValueData.DEFAULT, sectionData.get(8).get(0));
        assertEquals(StringValueData.DEFAULT, sectionData.get(8).get(1));

        assertEquals(new StringValueData("Factory Sessions"), sectionData.get(9).get(0));
        assertEquals(new StringValueData("526"), sectionData.get(9).get(1));

        assertEquals(new StringValueData("Abandoned Sessions"), sectionData.get(10).get(0));
        assertEquals(new StringValueData("505"), sectionData.get(10).get(1));

        assertEquals(new StringValueData("Converted Sessions"), sectionData.get(11).get(0));
        assertEquals(new StringValueData("21"), sectionData.get(11).get(1));

        assertEquals(StringValueData.DEFAULT, sectionData.get(12).get(0));
        assertEquals(StringValueData.DEFAULT, sectionData.get(12).get(1));

        assertEquals(new StringValueData("Factory Sessions"), sectionData.get(13).get(0));
        assertEquals(new StringValueData("526"), sectionData.get(13).get(1));

        assertEquals(new StringValueData("% Built"), sectionData.get(14).get(0));
        assertEquals(new StringValueData("19%"), sectionData.get(14).get(1));

        assertEquals(new StringValueData("% Run"), sectionData.get(15).get(0));
        assertEquals(new StringValueData("20%"), sectionData.get(15).get(1));

        assertEquals(new StringValueData("% Deployed"), sectionData.get(16).get(0));
        assertEquals(new StringValueData("12%"), sectionData.get(16).get(1));

        assertEquals(StringValueData.DEFAULT, sectionData.get(17).get(0));
        assertEquals(StringValueData.DEFAULT, sectionData.get(17).get(1));

        assertEquals(new StringValueData("Factory Sessions"), sectionData.get(18).get(0));
        assertEquals(new StringValueData("526"), sectionData.get(18).get(1));

        assertEquals(new StringValueData("< 10 Mins"), sectionData.get(19).get(0));
        assertEquals(new StringValueData("463"), sectionData.get(19).get(1));

        assertEquals(new StringValueData("> 10 Mins"), sectionData.get(20).get(0));
        assertEquals(new StringValueData("63"), sectionData.get(20).get(1));

        assertEquals(StringValueData.DEFAULT, sectionData.get(21).get(0));
        assertEquals(StringValueData.DEFAULT, sectionData.get(21).get(1));

        assertEquals(new StringValueData("Product Usage Mins"), sectionData.get(22).get(0));
        assertEquals(new StringValueData("2,902"), sectionData.get(22).get(1));
    }

    private void assertProjectsPaasDay(List<List<ValueData>> sectionData) {
        assertEquals(new StringValueData("Total"), sectionData.get(1).get(0));
        assertEquals(new StringValueData("433"), sectionData.get(1).get(1));

        assertEquals(new StringValueData("AWS"), sectionData.get(2).get(0));
        assertEquals(new StringValueData("1"), sectionData.get(2).get(1));

        assertEquals(new StringValueData("AppFog"), sectionData.get(3).get(0));
        assertEquals(new StringValueData("7"), sectionData.get(3).get(1));

        assertEquals(new StringValueData("CloudBees"), sectionData.get(4).get(0));
        assertEquals(new StringValueData("1"), sectionData.get(4).get(1));

        assertEquals(new StringValueData("CloudFoundry"), sectionData.get(5).get(0));
        assertEquals(new StringValueData("1"), sectionData.get(5).get(1));

        assertEquals(new StringValueData("GAE"), sectionData.get(6).get(0));
        assertEquals(new StringValueData("21"), sectionData.get(6).get(1));

        assertEquals(new StringValueData("Heroku"), sectionData.get(7).get(0));
        assertEquals(new StringValueData("1"), sectionData.get(7).get(1));

        assertEquals(new StringValueData("OpenShift"), sectionData.get(8).get(0));
        assertEquals(new StringValueData("6"), sectionData.get(8).get(1));

        assertEquals(new StringValueData("Tier3"), sectionData.get(9).get(0));
        assertEquals(new StringValueData("1"), sectionData.get(9).get(1));

        assertEquals(new StringValueData("Manyamo"), sectionData.get(10).get(0));
        assertEquals(new StringValueData("1"), sectionData.get(10).get(1));

        assertEquals(new StringValueData("No PaaS Defined"), sectionData.get(11).get(0));
        assertEquals(new StringValueData("394"), sectionData.get(11).get(1));
    }

    private void assertProjectsTypesDay(List<List<ValueData>> sectionData) {
        assertEquals(new StringValueData("Total"), sectionData.get(1).get(0));
        assertEquals(new StringValueData("433"), sectionData.get(1).get(1));

        assertEquals(new StringValueData("Java Jar"), sectionData.get(2).get(0));
        assertEquals(new StringValueData("31"), sectionData.get(2).get(1));

        assertEquals(new StringValueData("Java War"), sectionData.get(3).get(0));
        assertEquals(new StringValueData("4"), sectionData.get(3).get(1));

        assertEquals(new StringValueData("Java JSP"), sectionData.get(4).get(0));
        assertEquals(new StringValueData("57"), sectionData.get(4).get(1));

        assertEquals(new StringValueData("Java Spring"), sectionData.get(5).get(0));
        assertEquals(new StringValueData("15"), sectionData.get(5).get(1));

        assertEquals(new StringValueData("PHP"), sectionData.get(6).get(0));
        assertEquals(new StringValueData("90"), sectionData.get(6).get(1));

        assertEquals(new StringValueData("Python"), sectionData.get(7).get(0));
        assertEquals(new StringValueData("43"), sectionData.get(7).get(1));

        assertEquals(new StringValueData("JavaScript"), sectionData.get(8).get(0));
        assertEquals(new StringValueData("72"), sectionData.get(8).get(1));

        assertEquals(new StringValueData("Ruby"), sectionData.get(9).get(0));
        assertEquals(new StringValueData("19"), sectionData.get(9).get(1));

        assertEquals(new StringValueData("Maven Multi Project"), sectionData.get(10).get(0));
        assertEquals(new StringValueData("8"), sectionData.get(10).get(1));

        assertEquals(new StringValueData("Node.js"), sectionData.get(11).get(0));
        assertEquals(new StringValueData("24"), sectionData.get(11).get(1));

        assertEquals(new StringValueData("Android"), sectionData.get(12).get(0));
        assertEquals(new StringValueData("68"), sectionData.get(12).get(1));

        assertEquals(new StringValueData("Django"), sectionData.get(13).get(0));
        assertEquals(new StringValueData("1"), sectionData.get(13).get(1));

        assertEquals(new StringValueData("Others"), sectionData.get(14).get(0));
        assertEquals(new StringValueData("1"), sectionData.get(14).get(1));
    }

    private void assertUsersEngagementDay(List<List<ValueData>> sectionData) {
        assertEquals(new StringValueData("Total"), sectionData.get(1).get(0));
        assertEquals(new StringValueData("203"), sectionData.get(1).get(1));

        assertEquals(new StringValueData("< 10 Min"), sectionData.get(2).get(0));
        assertEquals(new StringValueData("85"), sectionData.get(2).get(1));

        assertEquals(new StringValueData(">= 10 And < 60 Mins"), sectionData.get(3).get(0));
        assertEquals(new StringValueData("81"), sectionData.get(3).get(1));

        assertEquals(new StringValueData(">= 60 And < 300 Mins"), sectionData.get(4).get(0));
        assertEquals(new StringValueData("33"), sectionData.get(4).get(1));

        assertEquals(new StringValueData("> 300 Mins"), sectionData.get(5).get(0));
        assertEquals(new StringValueData("4"), sectionData.get(5).get(1));
    }

    private void assertEquals(StringValueData stringValueData, ValueData valueData) {
        if (!stringValueData.equals(valueData)) {
            System.out.println("================> " + stringValueData.getAsString() + " != " + valueData.getAsString());
        }
    }

    private void assertAuthenticationsDay(List<List<ValueData>> sectionData) {
        assertEquals(new StringValueData("Google Auth"), sectionData.get(1).get(0));
        assertEquals(new StringValueData("56"), sectionData.get(1).get(1));

        assertEquals(new StringValueData("Github Auth"), sectionData.get(2).get(0));
        assertEquals(new StringValueData("11"), sectionData.get(2).get(1));

        assertEquals(new StringValueData("Form Auth"), sectionData.get(3).get(0));
        assertEquals(new StringValueData("33"), sectionData.get(3).get(1));
    }

    private void assertUserSessionsDay(List<List<ValueData>> sectionData) {
        assertEquals(new StringValueData("Total"), sectionData.get(1).get(0));
        assertEquals(new StringValueData("744"), sectionData.get(1).get(1));

        assertEquals(new StringValueData("<= 1 Min"), sectionData.get(2).get(0));
        assertEquals(new StringValueData("227"), sectionData.get(2).get(1));

        assertEquals(new StringValueData("> 1 And < 10 Mins"), sectionData.get(3).get(0));
        assertEquals(new StringValueData("297"), sectionData.get(3).get(1));

        assertEquals(new StringValueData(">= 10 And <= 60 Mins"), sectionData.get(4).get(0));
        assertEquals(new StringValueData("190"), sectionData.get(4).get(1));

        assertEquals(new StringValueData("> 60 Mins"), sectionData.get(5).get(0));
        assertEquals(new StringValueData("30"), sectionData.get(5).get(1));
    }

    private void assertWorkspaceUsageDay(List<List<ValueData>> sectionData) {
        assertEquals(new StringValueData("Total"), sectionData.get(1).get(0));
        assertEquals(new StringValueData("63"), sectionData.get(1).get(1));

        assertEquals(new StringValueData("New Active Workspaces"), sectionData.get(2).get(0));
        assertEquals(new StringValueData("54"), sectionData.get(2).get(1));

        assertEquals(new StringValueData("Returning Active Workspaces"), sectionData.get(3).get(0));
        assertEquals(new StringValueData("449"), sectionData.get(3).get(1));

        assertEquals(new StringValueData("Non-Active Workspaces"), sectionData.get(4).get(0));
        assertEquals(new StringValueData("-439"), sectionData.get(4).get(1));
    }

    private void assertUsageTimeDay(List<List<ValueData>> sectionData) {
        assertEquals(new StringValueData("Total"), sectionData.get(1).get(0));
        assertEquals(new StringValueData("8992"), sectionData.get(1).get(1));

        assertEquals(new StringValueData("<= 1 Min"), sectionData.get(2).get(0));
        assertEquals(new StringValueData("227"), sectionData.get(2).get(1));

        assertEquals(new StringValueData("> 1 And < 10 Mins"), sectionData.get(3).get(0));
        assertEquals(new StringValueData("1204"), sectionData.get(3).get(1));

        assertEquals(new StringValueData(">= 10 And <= 60 Mins"), sectionData.get(4).get(0));
        assertEquals(new StringValueData("4794"), sectionData.get(4).get(1));

        assertEquals(new StringValueData("> 60 Mins"), sectionData.get(5).get(0));
        assertEquals(new StringValueData("2767"), sectionData.get(5).get(1));
    }

    private void assertIdeUsageDay(List<List<ValueData>> sectionData) {
        assertEquals(new StringValueData("# Refactors"), sectionData.get(1).get(0));
        assertEquals(new StringValueData("1"), sectionData.get(1).get(1));

        assertEquals(new StringValueData("# Code Completions"), sectionData.get(2).get(0));
        assertEquals(new StringValueData("147"), sectionData.get(2).get(1));

        assertEquals(new StringValueData("# Builds"), sectionData.get(3).get(0));
        assertEquals(new StringValueData("614"), sectionData.get(3).get(1));

        assertEquals(new StringValueData("# Runs"), sectionData.get(4).get(0));
        assertEquals(new StringValueData("329"), sectionData.get(4).get(1));

        assertEquals(new StringValueData("# Debugs"), sectionData.get(5).get(0));
        assertEquals(new StringValueData("2"), sectionData.get(5).get(1));
    }

    private void assertUsersDay(List<List<ValueData>> sectionData) {
        assertEquals(new StringValueData("Total Created"), sectionData.get(1).get(0));
        assertEquals(new StringValueData("59"), sectionData.get(1).get(1));

        assertEquals(new StringValueData("Created From Factory"), sectionData.get(2).get(0));
        assertEquals(new StringValueData("15"), sectionData.get(2).get(1));

        assertEquals(new StringValueData("Created From Form / oAuth"), sectionData.get(3).get(0));
        assertEquals(new StringValueData("44"), sectionData.get(3).get(1));

        assertEquals(new StringValueData("Total"), sectionData.get(4).get(0));
        assertEquals(new StringValueData("69"), sectionData.get(4).get(1));
    }

    private void assertUsersUsageDay(List<List<ValueData>> sectionData) {
        assertEquals(new StringValueData("Total"), sectionData.get(1).get(0));
        assertEquals(new StringValueData("79"), sectionData.get(1).get(1));

        assertEquals(new StringValueData("New Active Users"), sectionData.get(2).get(0));
        assertEquals(new StringValueData("59"), sectionData.get(2).get(1));

        assertEquals(new StringValueData("Returning Active Users"), sectionData.get(3).get(0));
        assertEquals(new StringValueData("144"), sectionData.get(3).get(1));

        assertEquals(new StringValueData("Non-Active Users"), sectionData.get(4).get(0));
        assertEquals(new StringValueData("-124"), sectionData.get(4).get(1));
    }

    private void assertProjectsDay(List<List<ValueData>> sectionData) {
        assertEquals(new StringValueData("Created"), sectionData.get(1).get(0));
        assertEquals(new StringValueData("433"), sectionData.get(1).get(1));

        assertEquals(new StringValueData("Destroyed"), sectionData.get(2).get(0));
        assertEquals(new StringValueData("94"), sectionData.get(2).get(1));

        assertEquals(new StringValueData("Total"), sectionData.get(3).get(0));
        assertEquals(new StringValueData("369"), sectionData.get(3).get(1));
    }

    private void assertWorkspacesDay(List<List<ValueData>> sectionData) {
        assertEquals(new StringValueData("Created"), sectionData.get(1).get(0));
        assertEquals(new StringValueData("54"), sectionData.get(1).get(1));

        assertEquals(new StringValueData("Destroyed"), sectionData.get(2).get(0));
        assertEquals(new StringValueData("1"), sectionData.get(2).get(1));

        assertEquals(new StringValueData("Total"), sectionData.get(3).get(0));
        assertEquals(new StringValueData("63"), sectionData.get(3).get(1));
    }

    private void assertTimeSpentDay(List<List<ValueData>> sectionData) {
        assertEquals(new StringValueData("Builds"), sectionData.get(1).get(0));
        assertEquals(new StringValueData("40"), sectionData.get(1).get(1));

        assertEquals(new StringValueData("Runs"), sectionData.get(2).get(0));
        assertEquals(new StringValueData("424"), sectionData.get(2).get(1));

        assertEquals(new StringValueData("Debugs"), sectionData.get(3).get(0));
        assertEquals(new StringValueData("2"), sectionData.get(3).get(1));
    }

    private void assertInvitationsDay(List<List<ValueData>> sectionData) {
        assertEquals(new StringValueData("Sent"), sectionData.get(1).get(0));
        assertEquals(new StringValueData("16"), sectionData.get(1).get(1));

        assertEquals(new StringValueData("Accepted"), sectionData.get(2).get(0));
        assertEquals(new StringValueData("31"), sectionData.get(2).get(1));
    }
}
