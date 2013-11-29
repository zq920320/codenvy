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
                case "factory_day":
                    assertFactoryDay(sectionData);
                    break;
                default:
                    fail("Unknown table name " + tableName);
            }
        } else if (tableName.endsWith("lifetime")) {
            switch (tableName) {
                case "invitations_lifetime":
                    assertInvitationsLifeTime(sectionData);
                    break;
                case "time_spent_lifetime":
                    assertTimeSpentLifeTime(sectionData);
                    break;
                case "workspaces_lifetime":
                    assertWorkspacesLifeTime(sectionData);
                    break;
                case "projects_lifetime":
                    assertProjectsLifeTime(sectionData);
                    break;
                case "users_lifetime":
                    assertUsersLifeTime(sectionData);
                    break;
                case "ide_usage_lifetime":
                    assertIdeUsageLifeTime(sectionData);
                    break;
                case "usage_time_lifetime":
                    assertUsageTimeLifeTime(sectionData);
                    break;
                case "workspaces_usage_lifetime":
                    assertWorkspaceUsageLifeTime(sectionData);
                    break;
                case "user_sessions_lifetime":
                    assertUserSessionsLifeTime(sectionData);
                    break;
                case "users_usage_lifetime":
                    assertUsersUsageLifeTime(sectionData);
                    break;
                case "authentications_lifetime":
                    assertAuthenticationsLifeTime(sectionData);
                    break;
                case "users_engagement_lifetime":
                    assertUsersEngagementLifeTime(sectionData);
                    break;
                case "projects_types_lifetime":
                    assertProjectsTypesLifeTime(sectionData);
                    break;
                case "projects_paas_lifetime":
                    assertProjectsPaasLifeTime(sectionData);
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
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Factories Created"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("5"), sectionData.get(1).get(1));
        aggregateResult(row, new StringValueData("12"), sectionData.get(1).get(2));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("Temporary Workspaces Created"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("348"), sectionData.get(2).get(1));
        aggregateResult(row, new StringValueData("280"), sectionData.get(2).get(2));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("Accounts Created From Factories"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("15"), sectionData.get(3).get(1));
        aggregateResult(row, new StringValueData("15"), sectionData.get(3).get(2));

        row = sectionData.get(4).get(0).getAsString();
        aggregateResult(row, StringValueData.DEFAULT, sectionData.get(4).get(0));
        aggregateResult(row, StringValueData.DEFAULT, sectionData.get(4).get(1));
        aggregateResult(row, StringValueData.DEFAULT, sectionData.get(4).get(2));

        row = sectionData.get(5).get(0).getAsString();
        aggregateResult(row, new StringValueData("Factory Sessions"), sectionData.get(5).get(0));
        aggregateResult(row, new StringValueData("293"), sectionData.get(5).get(1));
        aggregateResult(row, new StringValueData("233"), sectionData.get(5).get(2));

        row = sectionData.get(6).get(0).getAsString();
        aggregateResult(row, new StringValueData("Anonymous Sessions"), sectionData.get(6).get(0));
        aggregateResult(row, new StringValueData("275"), sectionData.get(6).get(1));
        aggregateResult(row, new StringValueData("210"), sectionData.get(6).get(2));

        row = sectionData.get(7).get(0).getAsString();
        aggregateResult(row, new StringValueData("Authenticated Sessions"), sectionData.get(7).get(0));
        aggregateResult(row, new StringValueData("18"), sectionData.get(7).get(1));
        aggregateResult(row, new StringValueData("23"), sectionData.get(7).get(2));

        row = sectionData.get(8).get(0).getAsString();
        aggregateResult(row, StringValueData.DEFAULT, sectionData.get(8).get(0));
        aggregateResult(row, StringValueData.DEFAULT, sectionData.get(8).get(1));
        aggregateResult(row, StringValueData.DEFAULT, sectionData.get(8).get(2));

        row = sectionData.get(9).get(0).getAsString();
        aggregateResult(row, new StringValueData("Factory Sessions"), sectionData.get(9).get(0));
        aggregateResult(row, new StringValueData("293"), sectionData.get(9).get(1));
        aggregateResult(row, new StringValueData("233"), sectionData.get(9).get(2));

        row = sectionData.get(10).get(0).getAsString();
        aggregateResult(row, new StringValueData("Abandoned Sessions"), sectionData.get(10).get(0));
        aggregateResult(row, new StringValueData("282"), sectionData.get(10).get(1));
        aggregateResult(row, new StringValueData("223"), sectionData.get(10).get(2));

        row = sectionData.get(11).get(0).getAsString();
        aggregateResult(row, new StringValueData("Converted Sessions"), sectionData.get(11).get(0));
        aggregateResult(row, new StringValueData("11"), sectionData.get(11).get(1));
        aggregateResult(row, new StringValueData("10"), sectionData.get(11).get(2));

        row = sectionData.get(12).get(0).getAsString();
        aggregateResult(row, StringValueData.DEFAULT, sectionData.get(12).get(0));
        aggregateResult(row, StringValueData.DEFAULT, sectionData.get(12).get(1));
        aggregateResult(row, StringValueData.DEFAULT, sectionData.get(12).get(2));

        row = sectionData.get(13).get(0).getAsString();
        aggregateResult(row, new StringValueData("Factory Sessions"), sectionData.get(13).get(0));
        aggregateResult(row, new StringValueData("293"), sectionData.get(13).get(1));
        aggregateResult(row, new StringValueData("233"), sectionData.get(13).get(2));

        row = sectionData.get(14).get(0).getAsString();
        aggregateResult(row, new StringValueData("% Built"), sectionData.get(14).get(0));
        aggregateResult(row, new StringValueData("19%"), sectionData.get(14).get(1));
        aggregateResult(row, new StringValueData("19%"), sectionData.get(14).get(2));

        row = sectionData.get(15).get(0).getAsString();
        aggregateResult(row, new StringValueData("% Run"), sectionData.get(15).get(0));
        aggregateResult(row, new StringValueData("17%"), sectionData.get(15).get(1));
        aggregateResult(row, new StringValueData("23%"), sectionData.get(15).get(2));

        row = sectionData.get(16).get(0).getAsString();
        aggregateResult(row, new StringValueData("% Deployed"), sectionData.get(16).get(0));
        aggregateResult(row, new StringValueData("10%"), sectionData.get(16).get(1));
        aggregateResult(row, new StringValueData("14%"), sectionData.get(16).get(2));

        row = sectionData.get(17).get(0).getAsString();
        aggregateResult(row, StringValueData.DEFAULT, sectionData.get(17).get(0));
        aggregateResult(row, StringValueData.DEFAULT, sectionData.get(17).get(1));
        aggregateResult(row, StringValueData.DEFAULT, sectionData.get(17).get(2));

        row = sectionData.get(18).get(0).getAsString();
        aggregateResult(row, new StringValueData("Factory Sessions"), sectionData.get(18).get(0));
        aggregateResult(row, new StringValueData("293"), sectionData.get(18).get(1));
        aggregateResult(row, new StringValueData("233"), sectionData.get(18).get(2));

        row = sectionData.get(19).get(0).getAsString();
        aggregateResult(row, new StringValueData("< 10 Mins"), sectionData.get(19).get(0));
        aggregateResult(row, new StringValueData("261"), sectionData.get(19).get(1));
        aggregateResult(row, new StringValueData("202"), sectionData.get(19).get(2));

        row = sectionData.get(20).get(0).getAsString();
        aggregateResult(row, new StringValueData("> 10 Mins"), sectionData.get(20).get(0));
        aggregateResult(row, new StringValueData("32"), sectionData.get(20).get(1));
        aggregateResult(row, new StringValueData("31"), sectionData.get(20).get(2));

        row = sectionData.get(21).get(0).getAsString();
        aggregateResult(row, StringValueData.DEFAULT, sectionData.get(21).get(0));
        aggregateResult(row, StringValueData.DEFAULT, sectionData.get(21).get(1));
        aggregateResult(row, StringValueData.DEFAULT, sectionData.get(21).get(2));

        row = sectionData.get(22).get(0).getAsString();
        aggregateResult(row, new StringValueData("Product Usage Mins"), sectionData.get(22).get(0));
        aggregateResult(row, new StringValueData("1,445"), sectionData.get(22).get(1));
        aggregateResult(row, new StringValueData("1,456"), sectionData.get(22).get(2));
    }

    private void assertFactoryLifeTime(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Factories Created"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("17"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("Temporary Workspaces Created"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("628"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("Accounts Created From Factories"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("30"), sectionData.get(3).get(1));

        row = sectionData.get(4).get(0).getAsString();
        aggregateResult(row, StringValueData.DEFAULT, sectionData.get(4).get(0));
        aggregateResult(row, StringValueData.DEFAULT, sectionData.get(4).get(1));

        row = sectionData.get(5).get(0).getAsString();
        aggregateResult(row, new StringValueData("Factory Sessions"), sectionData.get(5).get(0));
        aggregateResult(row, new StringValueData("526"), sectionData.get(5).get(1));

        row = sectionData.get(6).get(0).getAsString();
        aggregateResult(row, new StringValueData("Anonymous Sessions"), sectionData.get(6).get(0));
        aggregateResult(row, new StringValueData("485"), sectionData.get(6).get(1));

        row = sectionData.get(7).get(0).getAsString();
        aggregateResult(row, new StringValueData("Authenticated Sessions"), sectionData.get(7).get(0));
        aggregateResult(row, new StringValueData("41"), sectionData.get(7).get(1));

        row = sectionData.get(8).get(0).getAsString();
        aggregateResult(row, StringValueData.DEFAULT, sectionData.get(8).get(0));
        aggregateResult(row, StringValueData.DEFAULT, sectionData.get(8).get(1));

        row = sectionData.get(9).get(0).getAsString();
        aggregateResult(row, new StringValueData("Factory Sessions"), sectionData.get(9).get(0));
        aggregateResult(row, new StringValueData("526"), sectionData.get(9).get(1));

        row = sectionData.get(10).get(0).getAsString();
        aggregateResult(row, new StringValueData("Abandoned Sessions"), sectionData.get(10).get(0));
        aggregateResult(row, new StringValueData("505"), sectionData.get(10).get(1));

        row = sectionData.get(11).get(0).getAsString();
        aggregateResult(row, new StringValueData("Converted Sessions"), sectionData.get(11).get(0));
        aggregateResult(row, new StringValueData("21"), sectionData.get(11).get(1));

        row = sectionData.get(12).get(0).getAsString();
        aggregateResult(row, StringValueData.DEFAULT, sectionData.get(12).get(0));
        aggregateResult(row, StringValueData.DEFAULT, sectionData.get(12).get(1));

        row = sectionData.get(13).get(0).getAsString();
        aggregateResult(row, new StringValueData("Factory Sessions"), sectionData.get(13).get(0));
        aggregateResult(row, new StringValueData("526"), sectionData.get(13).get(1));

        row = sectionData.get(14).get(0).getAsString();
        aggregateResult(row, new StringValueData("% Built"), sectionData.get(14).get(0));
        aggregateResult(row, new StringValueData("19%"), sectionData.get(14).get(1));

        row = sectionData.get(15).get(0).getAsString();
        aggregateResult(row, new StringValueData("% Run"), sectionData.get(15).get(0));
        aggregateResult(row, new StringValueData("20%"), sectionData.get(15).get(1));

        row = sectionData.get(16).get(0).getAsString();
        aggregateResult(row, new StringValueData("% Deployed"), sectionData.get(16).get(0));
        aggregateResult(row, new StringValueData("12%"), sectionData.get(16).get(1));

        row = sectionData.get(17).get(0).getAsString();
        aggregateResult(row, StringValueData.DEFAULT, sectionData.get(17).get(0));
        aggregateResult(row, StringValueData.DEFAULT, sectionData.get(17).get(1));

        row = sectionData.get(18).get(0).getAsString();
        aggregateResult(row, new StringValueData("Factory Sessions"), sectionData.get(18).get(0));
        aggregateResult(row, new StringValueData("526"), sectionData.get(18).get(1));

        row = sectionData.get(19).get(0).getAsString();
        aggregateResult(row, new StringValueData("< 10 Mins"), sectionData.get(19).get(0));
        aggregateResult(row, new StringValueData("463"), sectionData.get(19).get(1));

        row = sectionData.get(20).get(0).getAsString();
        aggregateResult(row, new StringValueData("> 10 Mins"), sectionData.get(20).get(0));
        aggregateResult(row, new StringValueData("63"), sectionData.get(20).get(1));

        row = sectionData.get(21).get(0).getAsString();
        aggregateResult(row, StringValueData.DEFAULT, sectionData.get(21).get(0));
        aggregateResult(row, StringValueData.DEFAULT, sectionData.get(21).get(1));

        row = sectionData.get(22).get(0).getAsString();
        aggregateResult(row, new StringValueData("Product Usage Mins"), sectionData.get(22).get(0));
        aggregateResult(row, new StringValueData("2,902"), sectionData.get(22).get(1));
    }

    private void assertProjectsPaasDay(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("437"), sectionData.get(1).get(1));
        aggregateResult(row, new StringValueData("390"), sectionData.get(1).get(2));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("AWS"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("3"), sectionData.get(2).get(1));
        aggregateResult(row, new StringValueData(""), sectionData.get(2).get(2));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("AppFog"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("8"), sectionData.get(3).get(1));
        aggregateResult(row, new StringValueData("16"), sectionData.get(3).get(2));

        row = sectionData.get(4).get(0).getAsString();
        aggregateResult(row, new StringValueData("CloudBees"), sectionData.get(4).get(0));
        aggregateResult(row, new StringValueData("1"), sectionData.get(4).get(1));
        aggregateResult(row, new StringValueData(""), sectionData.get(4).get(2));

        row = sectionData.get(5).get(0).getAsString();
        aggregateResult(row, new StringValueData("CloudFoundry"), sectionData.get(5).get(0));
        aggregateResult(row, new StringValueData("1"), sectionData.get(5).get(1));
        aggregateResult(row, new StringValueData(""), sectionData.get(5).get(2));

        row = sectionData.get(6).get(0).getAsString();
        aggregateResult(row, new StringValueData("GAE"), sectionData.get(6).get(0));
        aggregateResult(row, new StringValueData("16"), sectionData.get(6).get(1));
        aggregateResult(row, new StringValueData("7"), sectionData.get(6).get(2));

        row = sectionData.get(7).get(0).getAsString();
        aggregateResult(row, new StringValueData("Heroku"), sectionData.get(7).get(0));
        aggregateResult(row, new StringValueData("1"), sectionData.get(7).get(1));
        aggregateResult(row, new StringValueData(""), sectionData.get(7).get(2));

        row = sectionData.get(8).get(0).getAsString();
        aggregateResult(row, new StringValueData("OpenShift"), sectionData.get(8).get(0));
        aggregateResult(row, new StringValueData("7"), sectionData.get(8).get(1));
        aggregateResult(row, new StringValueData("7"), sectionData.get(8).get(2));

        row = sectionData.get(9).get(0).getAsString();
        aggregateResult(row, new StringValueData("Tier3"), sectionData.get(9).get(0));
        aggregateResult(row, new StringValueData("1"), sectionData.get(9).get(1));
        aggregateResult(row, new StringValueData(""), sectionData.get(9).get(2));

        row = sectionData.get(10).get(0).getAsString();
        aggregateResult(row, new StringValueData("Manyamo"), sectionData.get(10).get(0));
        aggregateResult(row, new StringValueData("1"), sectionData.get(10).get(1));
        aggregateResult(row, new StringValueData(""), sectionData.get(10).get(2));

        row = sectionData.get(11).get(0).getAsString();
        aggregateResult(row, new StringValueData("No PaaS Defined"), sectionData.get(11).get(0));
        aggregateResult(row, new StringValueData("398"), sectionData.get(11).get(1));
        aggregateResult(row, new StringValueData("360"), sectionData.get(11).get(2));
    }
    
    private void assertProjectsPaasLifeTime(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("827"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("AWS"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("3"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("AppFog"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("24"), sectionData.get(3).get(1));

        row = sectionData.get(4).get(0).getAsString();
        aggregateResult(row, new StringValueData("CloudBees"), sectionData.get(4).get(0));
        aggregateResult(row, new StringValueData("1"), sectionData.get(4).get(1));

        row = sectionData.get(5).get(0).getAsString();
        aggregateResult(row, new StringValueData("CloudFoundry"), sectionData.get(5).get(0));
        aggregateResult(row, new StringValueData("1"), sectionData.get(5).get(1));

        row = sectionData.get(6).get(0).getAsString();
        aggregateResult(row, new StringValueData("GAE"), sectionData.get(6).get(0));
        aggregateResult(row, new StringValueData("23"), sectionData.get(6).get(1));

        row = sectionData.get(7).get(0).getAsString();
        aggregateResult(row, new StringValueData("Heroku"), sectionData.get(7).get(0));
        aggregateResult(row, new StringValueData("1"), sectionData.get(7).get(1));

        row = sectionData.get(8).get(0).getAsString();
        aggregateResult(row, new StringValueData("OpenShift"), sectionData.get(8).get(0));
        aggregateResult(row, new StringValueData("14"), sectionData.get(8).get(1));

        row = sectionData.get(9).get(0).getAsString();
        aggregateResult(row, new StringValueData("Tier3"), sectionData.get(9).get(0));
        aggregateResult(row, new StringValueData("1"), sectionData.get(9).get(1));

        row = sectionData.get(10).get(0).getAsString();
        aggregateResult(row, new StringValueData("Manyamo"), sectionData.get(10).get(0));
        aggregateResult(row, new StringValueData("1"), sectionData.get(10).get(1));

        row = sectionData.get(11).get(0).getAsString();
        aggregateResult(row, new StringValueData("No PaaS Defined"), sectionData.get(11).get(0));
        aggregateResult(row, new StringValueData("758"), sectionData.get(11).get(1));
    }

    private void assertProjectsTypesDay(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("437"), sectionData.get(1).get(1));
        aggregateResult(row, new StringValueData("390"), sectionData.get(1).get(2));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("Java Jar"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("31"), sectionData.get(2).get(1));
        aggregateResult(row, new StringValueData("29"), sectionData.get(2).get(2));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("Java War"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("4"), sectionData.get(3).get(1));
        aggregateResult(row, new StringValueData("4"), sectionData.get(3).get(2));

        row = sectionData.get(4).get(0).getAsString();
        aggregateResult(row, new StringValueData("Java JSP"), sectionData.get(4).get(0));
        aggregateResult(row, new StringValueData("57"), sectionData.get(4).get(1));
        aggregateResult(row, new StringValueData("31"), sectionData.get(4).get(2));

        row = sectionData.get(5).get(0).getAsString();
        aggregateResult(row, new StringValueData("Java Spring"), sectionData.get(5).get(0));
        aggregateResult(row, new StringValueData("15"), sectionData.get(5).get(1));
        aggregateResult(row, new StringValueData("8"), sectionData.get(5).get(2));

        row = sectionData.get(6).get(0).getAsString();
        aggregateResult(row, new StringValueData("PHP"), sectionData.get(6).get(0));
        aggregateResult(row, new StringValueData("94"), sectionData.get(6).get(1));
        aggregateResult(row, new StringValueData("96"), sectionData.get(6).get(2));

        row = sectionData.get(7).get(0).getAsString();
        aggregateResult(row, new StringValueData("Python"), sectionData.get(7).get(0));
        aggregateResult(row, new StringValueData("43"), sectionData.get(7).get(1));
        aggregateResult(row, new StringValueData("47"), sectionData.get(7).get(2));

        row = sectionData.get(8).get(0).getAsString();
        aggregateResult(row, new StringValueData("JavaScript"), sectionData.get(8).get(0));
        aggregateResult(row, new StringValueData("72"), sectionData.get(8).get(1));
        aggregateResult(row, new StringValueData("64"), sectionData.get(8).get(2));

        row = sectionData.get(9).get(0).getAsString();
        aggregateResult(row, new StringValueData("Ruby"), sectionData.get(9).get(0));
        aggregateResult(row, new StringValueData("19"), sectionData.get(9).get(1));
        aggregateResult(row, new StringValueData("16"), sectionData.get(9).get(2));

        row = sectionData.get(10).get(0).getAsString();
        aggregateResult(row, new StringValueData("Maven Multi Project"), sectionData.get(10).get(0));
        aggregateResult(row, new StringValueData("8"), sectionData.get(10).get(1));
        aggregateResult(row, new StringValueData("1"), sectionData.get(10).get(2));

        row = sectionData.get(11).get(0).getAsString();
        aggregateResult(row, new StringValueData("Node.js"), sectionData.get(11).get(0));
        aggregateResult(row, new StringValueData("24"), sectionData.get(11).get(1));
        aggregateResult(row, new StringValueData("19"), sectionData.get(11).get(2));

        row = sectionData.get(12).get(0).getAsString();
        aggregateResult(row, new StringValueData("Android"), sectionData.get(12).get(0));
        aggregateResult(row, new StringValueData("68"), sectionData.get(12).get(1));
        aggregateResult(row, new StringValueData("75"), sectionData.get(12).get(2));

        row = sectionData.get(13).get(0).getAsString();
        aggregateResult(row, new StringValueData("Django"), sectionData.get(13).get(0));
        aggregateResult(row, new StringValueData("1"), sectionData.get(13).get(1));
        aggregateResult(row, new StringValueData(""), sectionData.get(13).get(2));

        row = sectionData.get(14).get(0).getAsString();
        aggregateResult(row, new StringValueData("Others"), sectionData.get(14).get(0));
        aggregateResult(row, new StringValueData("1"), sectionData.get(14).get(1));
        aggregateResult(row, new StringValueData(""), sectionData.get(14).get(2));
    }
    
    private void assertProjectsTypesLifeTime(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("827"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("Java Jar"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("60"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("Java War"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("8"), sectionData.get(3).get(1));

        row = sectionData.get(4).get(0).getAsString();
        aggregateResult(row, new StringValueData("Java JSP"), sectionData.get(4).get(0));
        aggregateResult(row, new StringValueData("88"), sectionData.get(4).get(1));

        row = sectionData.get(5).get(0).getAsString();
        aggregateResult(row, new StringValueData("Java Spring"), sectionData.get(5).get(0));
        aggregateResult(row, new StringValueData("23"), sectionData.get(5).get(1));

        row = sectionData.get(6).get(0).getAsString();
        aggregateResult(row, new StringValueData("PHP"), sectionData.get(6).get(0));
        aggregateResult(row, new StringValueData("190"), sectionData.get(6).get(1));

        row = sectionData.get(7).get(0).getAsString();
        aggregateResult(row, new StringValueData("Python"), sectionData.get(7).get(0));
        aggregateResult(row, new StringValueData("90"), sectionData.get(7).get(1));

        row = sectionData.get(8).get(0).getAsString();
        aggregateResult(row, new StringValueData("JavaScript"), sectionData.get(8).get(0));
        aggregateResult(row, new StringValueData("136"), sectionData.get(8).get(1));

        row = sectionData.get(9).get(0).getAsString();
        aggregateResult(row, new StringValueData("Ruby"), sectionData.get(9).get(0));
        aggregateResult(row, new StringValueData("35"), sectionData.get(9).get(1));

        row = sectionData.get(10).get(0).getAsString();
        aggregateResult(row, new StringValueData("Maven Multi Project"), sectionData.get(10).get(0));
        aggregateResult(row, new StringValueData("9"), sectionData.get(10).get(1));

        row = sectionData.get(11).get(0).getAsString();
        aggregateResult(row, new StringValueData("Node.js"), sectionData.get(11).get(0));
        aggregateResult(row, new StringValueData("43"), sectionData.get(11).get(1));

        row = sectionData.get(12).get(0).getAsString();
        aggregateResult(row, new StringValueData("Android"), sectionData.get(12).get(0));
        aggregateResult(row, new StringValueData("143"), sectionData.get(12).get(1));

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
        aggregateResult(row, new StringValueData("203"), sectionData.get(1).get(1));
        aggregateResult(row, new StringValueData("203"), sectionData.get(1).get(2));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("< 10 Min"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("85"), sectionData.get(2).get(1));
        aggregateResult(row, new StringValueData("85"), sectionData.get(2).get(2));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData(">= 10 And < 60 Mins"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("81"), sectionData.get(3).get(1));
        aggregateResult(row, new StringValueData("81"), sectionData.get(3).get(2));

        row = sectionData.get(4).get(0).getAsString();
        aggregateResult(row, new StringValueData(">= 60 And < 300 Mins"), sectionData.get(4).get(0));
        aggregateResult(row, new StringValueData("33"), sectionData.get(4).get(1));
        aggregateResult(row, new StringValueData("33"), sectionData.get(4).get(2));

        row = sectionData.get(5).get(0).getAsString();
        aggregateResult(row, new StringValueData("> 300 Mins"), sectionData.get(5).get(0));
        aggregateResult(row, new StringValueData("4"), sectionData.get(5).get(1));
        aggregateResult(row, new StringValueData("4"), sectionData.get(5).get(2));
    }
    
    private void assertUsersEngagementLifeTime(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("406"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("< 10 Min"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("170"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData(">= 10 And < 60 Mins"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("162"), sectionData.get(3).get(1));

        row = sectionData.get(4).get(0).getAsString();
        aggregateResult(row, new StringValueData(">= 60 And < 300 Mins"), sectionData.get(4).get(0));
        aggregateResult(row, new StringValueData("66"), sectionData.get(4).get(1));

        row = sectionData.get(5).get(0).getAsString();
        aggregateResult(row, new StringValueData("> 300 Mins"), sectionData.get(5).get(0));
        aggregateResult(row, new StringValueData("8"), sectionData.get(5).get(1));
    }

    private void assertAuthenticationsDay(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Google Auth"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("56"), sectionData.get(1).get(1));
        aggregateResult(row, new StringValueData("66"), sectionData.get(1).get(2));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("Github Auth"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("11"), sectionData.get(2).get(1));
        aggregateResult(row, new StringValueData("10"), sectionData.get(2).get(2));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("Form Auth"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("33"), sectionData.get(3).get(1));
        aggregateResult(row, new StringValueData("24"), sectionData.get(3).get(2));
    }
    
    private void assertAuthenticationsLifeTime(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Google Auth"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("122"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("Github Auth"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("21"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("Form Auth"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("57"), sectionData.get(3).get(1));
    }

    private void assertUserSessionsDay(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("744"), sectionData.get(1).get(1));
        aggregateResult(row, new StringValueData("744"), sectionData.get(1).get(2));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("<= 1 Min"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("227"), sectionData.get(2).get(1));
        aggregateResult(row, new StringValueData("227"), sectionData.get(2).get(2));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("> 1 And < 10 Mins"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("297"), sectionData.get(3).get(1));
        aggregateResult(row, new StringValueData("227"), sectionData.get(3).get(2));

        row = sectionData.get(4).get(0).getAsString();
        aggregateResult(row, new StringValueData(">= 10 And <= 60 Mins"), sectionData.get(4).get(0));
        aggregateResult(row, new StringValueData("190"), sectionData.get(4).get(1));
        aggregateResult(row, new StringValueData("227"), sectionData.get(4).get(2));

        row = sectionData.get(5).get(0).getAsString();
        aggregateResult(row, new StringValueData("> 60 Mins"), sectionData.get(5).get(0));
        aggregateResult(row, new StringValueData("30"), sectionData.get(5).get(1));
        aggregateResult(row, new StringValueData("227"), sectionData.get(5).get(2));
    }
    
    private void assertUserSessionsLifeTime(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("1488"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("<= 1 Min"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("454"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("> 1 And < 10 Mins"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("524"), sectionData.get(3).get(1));

        row = sectionData.get(4).get(0).getAsString();
        aggregateResult(row, new StringValueData(">= 10 And <= 60 Mins"), sectionData.get(4).get(0));
        aggregateResult(row, new StringValueData("417"), sectionData.get(4).get(1));

        row = sectionData.get(5).get(0).getAsString();
        aggregateResult(row, new StringValueData("> 60 Mins"), sectionData.get(5).get(0));
        aggregateResult(row, new StringValueData("257"), sectionData.get(5).get(1));
    }

    private void assertWorkspaceUsageDay(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("136"), sectionData.get(1).get(1));
        aggregateResult(row, new StringValueData("83"), sectionData.get(1).get(2));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("New Active Workspaces"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("54"), sectionData.get(2).get(1));
        aggregateResult(row, new StringValueData("73"), sectionData.get(2).get(2));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("Returning Active Workspaces"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("453"), sectionData.get(3).get(1));
        aggregateResult(row, new StringValueData("408"), sectionData.get(3).get(2));

        row = sectionData.get(4).get(0).getAsString();
        aggregateResult(row, new StringValueData("Non-Active Workspaces"), sectionData.get(4).get(0));
        aggregateResult(row, new StringValueData("-371"), sectionData.get(4).get(1));
        aggregateResult(row, new StringValueData("-398"), sectionData.get(4).get(2));
    }
    
    private void assertWorkspaceUsageLifeTime(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("219"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("New Active Workspaces"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("127"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("Returning Active Workspaces"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("861"), sectionData.get(3).get(1));

        row = sectionData.get(4).get(0).getAsString();
        aggregateResult(row, new StringValueData("Non-Active Workspaces"), sectionData.get(4).get(0));
        aggregateResult(row, new StringValueData("-769"), sectionData.get(4).get(1));
    }

    private void assertUsageTimeDay(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("8,992"), sectionData.get(1).get(1));
        aggregateResult(row, new StringValueData("8,338"), sectionData.get(1).get(2));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("<= 1 Min"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("227"), sectionData.get(2).get(1));
        aggregateResult(row, new StringValueData("218"), sectionData.get(2).get(2));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("> 1 And < 10 Mins"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("1,204"), sectionData.get(3).get(1));
        aggregateResult(row, new StringValueData("1,206"), sectionData.get(3).get(2));

        row = sectionData.get(4).get(0).getAsString();
        aggregateResult(row, new StringValueData(">= 10 And <= 60 Mins"), sectionData.get(4).get(0));
        aggregateResult(row, new StringValueData("4,794"), sectionData.get(4).get(1));
        aggregateResult(row, new StringValueData("3,584"), sectionData.get(4).get(2));

        row = sectionData.get(5).get(0).getAsString();
        aggregateResult(row, new StringValueData("> 60 Mins"), sectionData.get(5).get(0));
        aggregateResult(row, new StringValueData("2,767"), sectionData.get(5).get(1));
        aggregateResult(row, new StringValueData("3,330"), sectionData.get(5).get(2));
    }
    
    private void assertUsageTimeLifeTime(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("17,330"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("<= 1 Min"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("445"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("> 1 And < 10 Mins"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("2,410"), sectionData.get(3).get(1));

        row = sectionData.get(4).get(0).getAsString();
        aggregateResult(row, new StringValueData(">= 10 And <= 60 Mins"), sectionData.get(4).get(0));
        aggregateResult(row, new StringValueData("8,378"), sectionData.get(4).get(1));

        row = sectionData.get(5).get(0).getAsString();
        aggregateResult(row, new StringValueData("> 60 Mins"), sectionData.get(5).get(0));
        aggregateResult(row, new StringValueData("6,097"), sectionData.get(5).get(1));
    }

    private void assertIdeUsageDay(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("# Refactors"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("1"), sectionData.get(1).get(1));
        aggregateResult(row, new StringValueData(""), sectionData.get(1).get(2));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("# Code Completions"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("147"), sectionData.get(2).get(1));
        aggregateResult(row, new StringValueData("289"), sectionData.get(2).get(2));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("# Builds"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("614"), sectionData.get(3).get(1));
        aggregateResult(row, new StringValueData("567"), sectionData.get(3).get(2));

        row = sectionData.get(4).get(0).getAsString();
        aggregateResult(row, new StringValueData("# Runs"), sectionData.get(4).get(0));
        aggregateResult(row, new StringValueData("329"), sectionData.get(4).get(1));
        aggregateResult(row, new StringValueData("314"), sectionData.get(4).get(2));

        row = sectionData.get(5).get(0).getAsString();
        aggregateResult(row, new StringValueData("# Debugs"), sectionData.get(5).get(0));
        aggregateResult(row, new StringValueData("2"), sectionData.get(5).get(1));
        aggregateResult(row, new StringValueData("2"), sectionData.get(5).get(2));
    }
    
    private void assertIdeUsageLifeTime(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("# Refactors"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("1"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("# Code Completions"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("436"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("# Builds"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("1,181"), sectionData.get(3).get(1));

        row = sectionData.get(4).get(0).getAsString();
        aggregateResult(row, new StringValueData("# Runs"), sectionData.get(4).get(0));
        aggregateResult(row, new StringValueData("643"), sectionData.get(4).get(1));

        row = sectionData.get(5).get(0).getAsString();
        aggregateResult(row, new StringValueData("# Debugs"), sectionData.get(5).get(0));
        aggregateResult(row, new StringValueData("4"), sectionData.get(5).get(1));
    }

    private void assertUsersDay(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total Created"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("59"), sectionData.get(1).get(1));
        aggregateResult(row, new StringValueData("76"), sectionData.get(1).get(2));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("Created From Factory"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("15"), sectionData.get(2).get(1));
        aggregateResult(row, new StringValueData("15"), sectionData.get(2).get(2));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("Created From Form / oAuth"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("44"), sectionData.get(3).get(1));
        aggregateResult(row, new StringValueData("61"), sectionData.get(3).get(2));

        row = sectionData.get(4).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total"), sectionData.get(4).get(0));
        aggregateResult(row, new StringValueData("155"), sectionData.get(4).get(1));
        aggregateResult(row, new StringValueData("96"), sectionData.get(4).get(2));
    }
    
    private void assertUsersLifeTime(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total Created"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("135"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("Created From Factory"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("30"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("Created From Form / oAuth"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("105"), sectionData.get(3).get(1));

        row = sectionData.get(4).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total"), sectionData.get(4).get(0));
        aggregateResult(row, new StringValueData("251"), sectionData.get(4).get(1));
    }

    private void assertUsersUsageDay(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("155"), sectionData.get(1).get(1));
        aggregateResult(row, new StringValueData("96"), sectionData.get(1).get(2));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("New Active Users"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("59"), sectionData.get(2).get(1));
        aggregateResult(row, new StringValueData("76"), sectionData.get(2).get(2));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("Returning Active Users"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("144"), sectionData.get(3).get(1));
        aggregateResult(row, new StringValueData("126"), sectionData.get(3).get(2));

        row = sectionData.get(4).get(0).getAsString();
        aggregateResult(row, new StringValueData("Non-Active Users"), sectionData.get(4).get(0));
        aggregateResult(row, new StringValueData("-48"), sectionData.get(4).get(1));
        aggregateResult(row, new StringValueData("-106"), sectionData.get(4).get(2));
    }
    
    private void assertUsersUsageLifeTime(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("251"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("New Active Users"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("135"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("Returning Active Users"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("270"), sectionData.get(3).get(1));

        row = sectionData.get(4).get(0).getAsString();
        aggregateResult(row, new StringValueData("Non-Active Users"), sectionData.get(4).get(0));
        aggregateResult(row, new StringValueData("-154"), sectionData.get(4).get(1));
    }

    private void assertProjectsDay(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Created"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("437"), sectionData.get(1).get(1));
        aggregateResult(row, new StringValueData("390"), sectionData.get(1).get(2));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("Destroyed"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("94"), sectionData.get(2).get(1));
        aggregateResult(row, new StringValueData("84"), sectionData.get(2).get(2));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("679"), sectionData.get(3).get(1));
        aggregateResult(row, new StringValueData("336"), sectionData.get(3).get(2));
    }
    
    private void assertProjectsLifeTime(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Created"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("827"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("Destroyed"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("178"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("1,015"), sectionData.get(3).get(1));
    }

    private void assertWorkspacesDay(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Created"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("54"), sectionData.get(1).get(1));
        aggregateResult(row, new StringValueData("73"), sectionData.get(1).get(2));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("Destroyed"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("1"), sectionData.get(2).get(1));
        aggregateResult(row, new StringValueData(""), sectionData.get(2).get(2));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("136"), sectionData.get(3).get(1));
        aggregateResult(row, new StringValueData("83"), sectionData.get(2).get(2));
    }
    
    private void assertWorkspacesLifeTime(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Created"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("127"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("Destroyed"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("1"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("Total"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("219"), sectionData.get(3).get(1));
    }

    private void assertTimeSpentDay(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Builds"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("40"), sectionData.get(1).get(1));
        aggregateResult(row, new StringValueData("60"), sectionData.get(1).get(2));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("Runs"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("424"), sectionData.get(2).get(1));
        aggregateResult(row, new StringValueData("415"), sectionData.get(2).get(2));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("Debugs"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("2"), sectionData.get(3).get(1));
        aggregateResult(row, new StringValueData("4"), sectionData.get(3).get(2));
    }
    
    private void assertTimeSpentLifeTime(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Builds"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("100"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("Runs"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("839"), sectionData.get(2).get(1));

        row = sectionData.get(3).get(0).getAsString();
        aggregateResult(row, new StringValueData("Debugs"), sectionData.get(3).get(0));
        aggregateResult(row, new StringValueData("6"), sectionData.get(3).get(1));
    }

    private void assertInvitationsDay(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Sent"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("16"), sectionData.get(1).get(1));
        aggregateResult(row, new StringValueData("5"), sectionData.get(1).get(2));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("Accepted"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("31"), sectionData.get(2).get(1));
        aggregateResult(row, new StringValueData("100"), sectionData.get(2).get(2));
    }
    
    private void assertInvitationsLifeTime(List<List<ValueData>> sectionData) {
        String row = sectionData.get(1).get(0).getAsString();
        aggregateResult(row, new StringValueData("Sent"), sectionData.get(1).get(0));
        aggregateResult(row, new StringValueData("21"), sectionData.get(1).get(1));

        row = sectionData.get(2).get(0).getAsString();
        aggregateResult(row, new StringValueData("Accepted"), sectionData.get(2).get(0));
        aggregateResult(row, new StringValueData("131"), sectionData.get(2).get(1));
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
