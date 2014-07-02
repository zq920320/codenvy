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
package com.codenvy.analytics.services.marketo;

import com.codenvy.analytics.Injector;
import com.codenvy.analytics.metrics.AbstractMetric;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.users.UsersStatisticsList;
import com.codenvy.analytics.services.AbstractUsersActivityTest;
import com.codenvy.analytics.services.view.CSVFileHolder;

import org.testng.annotations.Test;

import java.io.File;
import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;

/**
 * @author Alexander Reshetnyak
 */
public class TestMarketoReportGenerator extends AbstractUsersActivityTest {

    private static final Map<String, String> HEADERS = MarketoReportGenerator.headers;

    @Override
    protected Map<String, String> getHeaders() {
        return HEADERS;
    }

    @Test(priority=1)
    public void testWholePeriod() throws Exception {
        MarketoReportGenerator job = Injector.getInstance(MarketoReportGenerator.class);
        CSVFileHolder cleaner = Injector.getInstance(CSVFileHolder.class);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131102");

        File jobFile = cleaner.createNewFile();

        job.prepareReport(jobFile, builder.build(), Context.EMPTY, false);

        Map<String, Map<String, String>> content = read(jobFile);

        assertEquals(4, content.size());

        // verify
        Map<String, String> headData = content.get("_HEAD");
        assertEquals(HEADERS.size(), headData.size());
        for (String column : HEADERS.values()) {
            assertEquals(column, headData.get(column));
        }

        // verify "user1" data
        Map<String, String> user1Data = content.get("user1");
        assertEquals(HEADERS.size(), user1Data.size());
        assertEquals("user1", user1Data.get(HEADERS.get(AbstractMetric.ID)));
        assertEquals("0", user1Data.get(HEADERS.get(UsersStatisticsList.BUILDS)));
        assertEquals("0", user1Data.get(HEADERS.get(UsersStatisticsList.DEPLOYS)));
        assertEquals("true", user1Data.get(HEADERS.get(MarketoReportGenerator.PROFILE_COMPLETED)));
        assertEquals("2", user1Data.get(HEADERS.get(UsersStatisticsList.PROJECTS)));
        assertEquals("0", user1Data.get(HEADERS.get(UsersStatisticsList.RUNS)));
        assertEquals("5", user1Data.get(HEADERS.get(UsersStatisticsList.TIME)));
        assertEquals("0", user1Data.get(HEADERS.get(UsersStatisticsList.LOGINS)));
        assertEquals("29", user1Data.get(HEADERS.get(MarketoReportGenerator.POINTS)));

        // verify "user2" data
        Map<String, String> user2Data = content.get("user2");
        assertEquals(HEADERS.size(), user2Data.size());
        assertEquals("user2", user2Data.get(HEADERS.get(AbstractMetric.ID)));
        assertEquals("1", user2Data.get(HEADERS.get(UsersStatisticsList.BUILDS)));
        assertEquals("6", user2Data.get(HEADERS.get(UsersStatisticsList.DEPLOYS)));
        assertEquals("false", user2Data.get(HEADERS.get(MarketoReportGenerator.PROFILE_COMPLETED)));
        assertEquals("1", user2Data.get(HEADERS.get(UsersStatisticsList.PROJECTS)));
        assertEquals("1", user2Data.get(HEADERS.get(UsersStatisticsList.RUNS)));
        assertEquals("10", user2Data.get(HEADERS.get(UsersStatisticsList.TIME)));
        assertEquals("1", user2Data.get(HEADERS.get(UsersStatisticsList.LOGINS)));
        assertEquals("92", user2Data.get(HEADERS.get(MarketoReportGenerator.POINTS)));

        // verify "user3" data
        Map<String, String> user3Data = content.get("user3");
        assertEquals(HEADERS.size(), user3Data.size());
        assertEquals("user3", user3Data.get(HEADERS.get(AbstractMetric.ID)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.BUILDS)));
        assertEquals("1", user3Data.get(HEADERS.get(UsersStatisticsList.DEPLOYS)));
        assertEquals("false", user3Data.get(HEADERS.get(MarketoReportGenerator.PROFILE_COMPLETED)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.PROJECTS)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.RUNS)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.TIME)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.LOGINS)));
        assertEquals("12", user3Data.get(HEADERS.get(MarketoReportGenerator.POINTS)));
    }

    @Test(priority=2)
    public void testUpdate() throws Exception {
        computeStatistics("20131103");

        MarketoReportGenerator job = Injector.getInstance(MarketoReportGenerator.class);
        CSVFileHolder cleaner = Injector.getInstance(CSVFileHolder.class);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131103");

        Context.Builder builderActiveUsers = new Context.Builder();
        builderActiveUsers.put(Parameters.FROM_DATE, "20131103");
        builderActiveUsers.put(Parameters.TO_DATE, "20131103");

        File jobFile = cleaner.createNewFile();

        job.prepareReport(jobFile, builder.build(), builderActiveUsers.build(), true);

        Map<String, Map<String, String>> content = read(jobFile);

        assertEquals(2, content.size());

        // verify
        Map<String, String> headData = content.get("_HEAD");
        assertEquals(HEADERS.size(), headData.size());
        for (String column : HEADERS.values()) {
            assertEquals(column, headData.get(column));
        }

        // verify "user3" data
        Map<String, String> user3Data = content.get("user3");
        assertEquals(HEADERS.size(), user3Data.size());
        assertEquals("user3", user3Data.get(HEADERS.get(AbstractMetric.ID)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.BUILDS)));
        assertEquals("2", user3Data.get(HEADERS.get(UsersStatisticsList.DEPLOYS)));
        assertEquals("false", user3Data.get(HEADERS.get(MarketoReportGenerator.PROFILE_COMPLETED)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.PROJECTS)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.RUNS)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.TIME)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.LOGINS)));
        assertEquals("24", user3Data.get(HEADERS.get(MarketoReportGenerator.POINTS)));
    }
}
