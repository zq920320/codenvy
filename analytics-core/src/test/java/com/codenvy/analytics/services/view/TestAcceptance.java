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

//        calendar.add(Calendar.DAY_OF_MONTH, -1);
//        context = Utils.prevDateInterval(context);
//
//        Parameters.LOG.put(context, getResourceAsBytes("2013-11-20", df.format(calendar.getTime())).getAbsolutePath
// ());
//        pigRunner.forceExecute(context);
    }

    private File getResourceAsBytes(String originalDate, String newDate) throws Exception {
        String archive = getClass().getClassLoader().getResource("messages.zip").getFile();

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
                default:
                    fail("Unknown table name " + tableName);
            }
        }
    }

    private void assertProjectsPaasDay(List<List<ValueData>> sectionData) {
    }

    private void assertProjectsTypesDay(List<List<ValueData>> sectionData) {
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
            System.out.println("================> " + stringValueData.getAsString() + "/n" + valueData.getAsString());
        }
    }

    private void assertAuthenticationsDay(List<List<ValueData>> sectionData) {
    }

    private void assertUserSessionsDay(List<List<ValueData>> sectionData) {
    }

    private void assertWorkspaceUsageDay(List<List<ValueData>> sectionData) {
        assertEquals(new StringValueData("Total"), sectionData.get(1).get(0));
        assertEquals(new StringValueData("64"), sectionData.get(1).get(1));

        assertEquals(new StringValueData("New Active Workspaces"), sectionData.get(2).get(0));
        assertEquals(new StringValueData("54"), sectionData.get(2).get(1));

        assertEquals(new StringValueData("Returning Active Workspaces"), sectionData.get(3).get(0));
        assertEquals(new StringValueData("449"), sectionData.get(3).get(1));

        assertEquals(new StringValueData("Non-Active Workspaces"), sectionData.get(4).get(0));
        assertEquals(new StringValueData("-439"), sectionData.get(4).get(1));
    }

    private void assertUsageTimeDay(List<List<ValueData>> sectionData) {
    }

    private void assertIdeUsageDay(List<List<ValueData>> sectionData) {
    }

    private void assertUsersDay(List<List<ValueData>> sectionData) {
        assertEquals(new StringValueData("Total Created"), sectionData.get(1).get(0));
        assertEquals(new StringValueData("59"), sectionData.get(1).get(1));

        assertEquals(new StringValueData("Created From Factory"), sectionData.get(2).get(0));
        assertEquals(new StringValueData("15"), sectionData.get(2).get(1));

        assertEquals(new StringValueData("Created From Form / oAuth"), sectionData.get(3).get(0));
        assertEquals(new StringValueData("44"), sectionData.get(3).get(1));

        assertEquals(new StringValueData("Created From Form / oAuth"), sectionData.get(4).get(0));
        assertEquals(new StringValueData("79"), sectionData.get(4).get(1));
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
    }

    private void assertWorkspacesDay(List<List<ValueData>> sectionData) {
        assertEquals(new StringValueData("Created"), sectionData.get(1).get(0));
        assertEquals(new StringValueData("54"), sectionData.get(1).get(1));

        assertEquals(new StringValueData("Destroyed"), sectionData.get(2).get(0));
        assertEquals(new StringValueData(""), sectionData.get(2).get(1));

        assertEquals(new StringValueData("Total"), sectionData.get(3).get(0));
        assertEquals(new StringValueData("64"), sectionData.get(3).get(1));
    }

    private void assertTimeSpentDay(List<List<ValueData>> sectionData) {
    }

    private void assertInvitationsDay(List<List<ValueData>> sectionData) {
    }
}
