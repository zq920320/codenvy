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
package com.codenvy.analytics.services.marketo;

import com.codenvy.analytics.Injector;
import com.codenvy.analytics.metrics.AbstractMetric;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.users.UsersStatisticsList;
import com.codenvy.analytics.services.AbstractUsersActivityTest;
import com.codenvy.analytics.services.view.CSVFileHolder;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
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

    @BeforeClass
    public void prepare() throws Exception {
    }

    @BeforeMethod
    public void clearDatabase() {
        super.clearDatabase();
    }

    @Test
    public void testWholePeriod() throws Exception {
        computeStatistics("20131101");
        computeStatistics("20131102");

        MarketoReportGenerator job = Injector.getInstance(MarketoReportGenerator.class);
        CSVFileHolder cleaner = Injector.getInstance(CSVFileHolder.class);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131102");

        File jobFile = cleaner.createNewFile();

        job.prepareReport(jobFile, builder.build(), Context.EMPTY, false);

        Map<String, Map<String, String>> content = read(jobFile);

        assertEquals(5, content.size());

        // verify
        Map<String, String> headData = content.get("_HEAD");
        assertEquals(HEADERS.size(), headData.size());
        for (String column : HEADERS.values()) {
            assertEquals(column, headData.get(column));
        }

        // verify "user1@gmail.com" data
        Map<String, String> user1Data = content.get("user1@gmail.com");
        assertEquals(HEADERS.size(), user1Data.size());
        assertEquals("user1@gmail.com", user1Data.get(HEADERS.get(AbstractMetric.ID)));
        assertEquals("1", user1Data.get(HEADERS.get(UsersStatisticsList.BUILDS)));
        assertEquals("0", user1Data.get(HEADERS.get(UsersStatisticsList.DEPLOYS)));
        assertEquals("true", user1Data.get(HEADERS.get(MarketoReportGenerator.PROFILE_COMPLETED)));
        assertEquals("2", user1Data.get(HEADERS.get(UsersStatisticsList.PROJECTS)));
        assertEquals("0", user1Data.get(HEADERS.get(UsersStatisticsList.RUNS)));
        assertEquals("5", user1Data.get(HEADERS.get(UsersStatisticsList.TIME)));
        assertEquals("0", user1Data.get(HEADERS.get(UsersStatisticsList.LOGINS)));
        assertEquals("", user1Data.get(HEADERS.get(MarketoReportGenerator.LAST_PRODUCT_LOGIN)));
        assertEquals("31", user1Data.get(HEADERS.get(MarketoReportGenerator.POINTS)));
        assertEquals("0.0533", user1Data.get(HEADERS.get(AbstractMetric.GIGABYTE_RAM_HOURS)));
        assertEquals("2013-11-01 10:00:00", user1Data.get(HEADERS.get(MarketoReportGenerator.SING_UP_DATE)));
        assertEquals("false", user1Data.get(HEADERS.get(MarketoReportGenerator.ACCOUNT_LOCKDOWN)));
        assertEquals("false", user1Data.get(HEADERS.get(MarketoReportGenerator.CC_ADDED)));
        assertEquals("", user1Data.get(HEADERS.get(MarketoReportGenerator.ON_PREM_SUB_ADDED)));
        assertEquals("", user1Data.get(HEADERS.get(MarketoReportGenerator.ON_PREM_SUB_REMOVED)));
        //assertEquals("0", user1Data.get(HEADERS.get(MarketoReportGenerator.NEW_USER)));

        // verify "user2@gmail.com" data
        Map<String, String> user2Data = content.get("user2@gmail.com");
        assertEquals(HEADERS.size(), user2Data.size());
        assertEquals("user2@gmail.com", user2Data.get(HEADERS.get(AbstractMetric.ID)));
        assertEquals("1", user2Data.get(HEADERS.get(UsersStatisticsList.BUILDS)));
        assertEquals("6", user2Data.get(HEADERS.get(UsersStatisticsList.DEPLOYS)));
        assertEquals("false", user2Data.get(HEADERS.get(MarketoReportGenerator.PROFILE_COMPLETED)));
        assertEquals("1", user2Data.get(HEADERS.get(UsersStatisticsList.PROJECTS)));
        assertEquals("1", user2Data.get(HEADERS.get(UsersStatisticsList.RUNS)));
        assertEquals("10", user2Data.get(HEADERS.get(UsersStatisticsList.TIME)));
        assertEquals("2", user2Data.get(HEADERS.get(UsersStatisticsList.LOGINS)));
        assertEquals("2013-11-01 10:10:30", user2Data.get(HEADERS.get(MarketoReportGenerator.LAST_PRODUCT_LOGIN)));        
        assertEquals("82", user2Data.get(HEADERS.get(MarketoReportGenerator.POINTS)));
        assertEquals("0.0084", user2Data.get(HEADERS.get(AbstractMetric.GIGABYTE_RAM_HOURS)));
        assertEquals("2013-11-01 10:00:00", user2Data.get(HEADERS.get(MarketoReportGenerator.SING_UP_DATE)));
        assertEquals("true", user2Data.get(HEADERS.get(MarketoReportGenerator.ACCOUNT_LOCKDOWN)));
        assertEquals("true", user2Data.get(HEADERS.get(MarketoReportGenerator.CC_ADDED)));
        assertEquals("", user2Data.get(HEADERS.get(MarketoReportGenerator.ON_PREM_SUB_ADDED)));
        assertEquals("2013-11-02 11:15:07", user2Data.get(HEADERS.get(MarketoReportGenerator.ON_PREM_SUB_REMOVED)));
        //assertEquals("0", user2Data.get(HEADERS.get(MarketoReportGenerator.NEW_USER)));

        // verify "user3@gmail.com" data
        Map<String, String> user3Data = content.get("user3@gmail.com");
        assertEquals(HEADERS.size(), user3Data.size());
        assertEquals("user3@gmail.com", user3Data.get(HEADERS.get(AbstractMetric.ID)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.BUILDS)));
        assertEquals("1", user3Data.get(HEADERS.get(UsersStatisticsList.DEPLOYS)));
        assertEquals("false", user3Data.get(HEADERS.get(MarketoReportGenerator.PROFILE_COMPLETED)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.PROJECTS)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.RUNS)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.TIME)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.LOGINS)));
        assertEquals("", user3Data.get(HEADERS.get(MarketoReportGenerator.LAST_PRODUCT_LOGIN)));       
        assertEquals("10", user3Data.get(HEADERS.get(MarketoReportGenerator.POINTS)));
        assertEquals("0.0000", user3Data.get(HEADERS.get(AbstractMetric.GIGABYTE_RAM_HOURS)));
        assertEquals("2013-11-01 10:00:00", user3Data.get(HEADERS.get(MarketoReportGenerator.SING_UP_DATE)));
        assertEquals("false", user3Data.get(HEADERS.get(MarketoReportGenerator.ACCOUNT_LOCKDOWN)));
        assertEquals("true", user3Data.get(HEADERS.get(MarketoReportGenerator.CC_ADDED)));
        assertEquals("2013-11-01 11:15:03", user3Data.get(HEADERS.get(MarketoReportGenerator.ON_PREM_SUB_ADDED)));
        assertEquals("", user3Data.get(HEADERS.get(MarketoReportGenerator.ON_PREM_SUB_REMOVED)));
        //assertEquals("0", user3Data.get(HEADERS.get(MarketoReportGenerator.NEW_USER)));

        // verify "user5@gmail.com" data
        Map<String, String> user5Data = content.get("user5@gmail.com");
        assertEquals(HEADERS.size(), user3Data.size());
        assertEquals("user5@gmail.com", user5Data.get(HEADERS.get(AbstractMetric.ID)));
        assertEquals("0", user5Data.get(HEADERS.get(UsersStatisticsList.BUILDS)));
        assertEquals("0", user5Data.get(HEADERS.get(UsersStatisticsList.DEPLOYS)));
        assertEquals("false", user5Data.get(HEADERS.get(MarketoReportGenerator.PROFILE_COMPLETED)));
        assertEquals("2", user5Data.get(HEADERS.get(UsersStatisticsList.PROJECTS)));
        assertEquals("0", user5Data.get(HEADERS.get(UsersStatisticsList.RUNS)));
        assertEquals("0", user5Data.get(HEADERS.get(UsersStatisticsList.TIME)));
        assertEquals("0", user5Data.get(HEADERS.get(UsersStatisticsList.LOGINS)));
        assertEquals("", user5Data.get(HEADERS.get(MarketoReportGenerator.LAST_PRODUCT_LOGIN)));
        assertEquals("4", user5Data.get(HEADERS.get(MarketoReportGenerator.POINTS)));
        assertEquals("0.0000", user5Data.get(HEADERS.get(AbstractMetric.GIGABYTE_RAM_HOURS)));
        assertEquals("2013-11-02 10:00:00", user5Data.get(HEADERS.get(MarketoReportGenerator.SING_UP_DATE)));
        assertEquals("false", user5Data.get(HEADERS.get(MarketoReportGenerator.ACCOUNT_LOCKDOWN)));
        assertEquals("false", user5Data.get(HEADERS.get(MarketoReportGenerator.CC_ADDED)));
        assertEquals("", user5Data.get(HEADERS.get(MarketoReportGenerator.ON_PREM_SUB_ADDED)));
        assertEquals("", user5Data.get(HEADERS.get(MarketoReportGenerator.ON_PREM_SUB_REMOVED)));
        //assertEquals("0", user5Data.get(HEADERS.get(MarketoReportGenerator.NEW_USER)));
    }

    @Test
    public void testUpdate() throws Exception {
        computeStatistics("20131101");
        computeStatistics("20131102");

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

        assertEquals(4, content.size());

        // verify
        Map<String, String> headData = content.get("_HEAD");
        assertEquals(HEADERS.size(), headData.size());
        for (String column : HEADERS.values()) {
            assertEquals(column, headData.get(column));
        }

        // verify "user3@gmail.com" data
        Map<String, String> user3Data = content.get("user3@gmail.com");
        assertEquals(HEADERS.size(), user3Data.size());
        assertEquals("user3@gmail.com", user3Data.get(HEADERS.get(AbstractMetric.ID)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.BUILDS)));
        assertEquals("2", user3Data.get(HEADERS.get(UsersStatisticsList.DEPLOYS)));
        assertEquals("false", user3Data.get(HEADERS.get(MarketoReportGenerator.PROFILE_COMPLETED)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.PROJECTS)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.RUNS)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.TIME)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.LOGINS)));
        assertEquals("", user3Data.get(HEADERS.get(MarketoReportGenerator.LAST_PRODUCT_LOGIN)));
        assertEquals("20", user3Data.get(HEADERS.get(MarketoReportGenerator.POINTS)));
        assertEquals("0.0000", user3Data.get(HEADERS.get(AbstractMetric.GIGABYTE_RAM_HOURS)));
        assertEquals("2013-11-01 10:00:00", user3Data.get(HEADERS.get(MarketoReportGenerator.SING_UP_DATE)));
        assertEquals("false", user3Data.get(HEADERS.get(MarketoReportGenerator.ACCOUNT_LOCKDOWN)));
        assertEquals("true", user3Data.get(HEADERS.get(MarketoReportGenerator.CC_ADDED)));
        assertEquals("2013-11-01 11:15:03", user3Data.get(HEADERS.get(MarketoReportGenerator.ON_PREM_SUB_ADDED)));
        assertEquals("", user3Data.get(HEADERS.get(MarketoReportGenerator.ON_PREM_SUB_REMOVED)));
        //assertEquals("0", user3Data.get(HEADERS.get(MarketoReportGenerator.NEW_USER)));

        // verify "user4@gmail.com" data
        Map<String, String> user4Data = content.get("user4@gmail.com");
        assertEquals(HEADERS.size(), user4Data.size());
        assertEquals("user4@gmail.com", user4Data.get(HEADERS.get(AbstractMetric.ID)));
        assertEquals("0", user4Data.get(HEADERS.get(UsersStatisticsList.BUILDS)));
        assertEquals("0", user4Data.get(HEADERS.get(UsersStatisticsList.DEPLOYS)));
        assertEquals("false", user4Data.get(HEADERS.get(MarketoReportGenerator.PROFILE_COMPLETED)));
        assertEquals("0", user4Data.get(HEADERS.get(UsersStatisticsList.PROJECTS)));
        assertEquals("0", user4Data.get(HEADERS.get(UsersStatisticsList.RUNS)));
        assertEquals("0", user4Data.get(HEADERS.get(UsersStatisticsList.TIME)));
        assertEquals("0", user4Data.get(HEADERS.get(UsersStatisticsList.LOGINS)));
        assertEquals("", user4Data.get(HEADERS.get(MarketoReportGenerator.LAST_PRODUCT_LOGIN)));
        assertEquals("0", user4Data.get(HEADERS.get(MarketoReportGenerator.POINTS)));
        assertEquals("0.0000", user4Data.get(HEADERS.get(AbstractMetric.GIGABYTE_RAM_HOURS)));
        assertEquals("2013-11-03 10:00:00", user4Data.get(HEADERS.get(MarketoReportGenerator.SING_UP_DATE)));
        assertEquals("false", user4Data.get(HEADERS.get(MarketoReportGenerator.ACCOUNT_LOCKDOWN)));
        assertEquals("false", user4Data.get(HEADERS.get(MarketoReportGenerator.CC_ADDED)));
        assertEquals("", user4Data.get(HEADERS.get(MarketoReportGenerator.ON_PREM_SUB_ADDED)));
        assertEquals("", user4Data.get(HEADERS.get(MarketoReportGenerator.ON_PREM_SUB_REMOVED)));
        //assertEquals("1", user4Data.get(HEADERS.get(MarketoReportGenerator.NEW_USER)));


        // verify "user5@gmail.com" data
        Map<String, String> user5Data = content.get("user5@gmail.com");
        assertEquals(HEADERS.size(), user3Data.size());
        assertEquals("user5@gmail.com", user5Data.get(HEADERS.get(AbstractMetric.ID)));
        assertEquals("0", user5Data.get(HEADERS.get(UsersStatisticsList.BUILDS)));
        assertEquals("0", user5Data.get(HEADERS.get(UsersStatisticsList.DEPLOYS)));
        assertEquals("false", user5Data.get(HEADERS.get(MarketoReportGenerator.PROFILE_COMPLETED)));
        assertEquals("3", user5Data.get(HEADERS.get(UsersStatisticsList.PROJECTS)));
        assertEquals("0", user5Data.get(HEADERS.get(UsersStatisticsList.RUNS)));
        assertEquals("0", user5Data.get(HEADERS.get(UsersStatisticsList.TIME)));
        assertEquals("0", user5Data.get(HEADERS.get(UsersStatisticsList.LOGINS)));
        assertEquals("", user5Data.get(HEADERS.get(MarketoReportGenerator.LAST_PRODUCT_LOGIN)));
        assertEquals("6", user5Data.get(HEADERS.get(MarketoReportGenerator.POINTS)));
        assertEquals("0.0000", user5Data.get(HEADERS.get(AbstractMetric.GIGABYTE_RAM_HOURS)));
        assertEquals("2013-11-02 10:00:00", user5Data.get(HEADERS.get(MarketoReportGenerator.SING_UP_DATE)));
        assertEquals("false", user5Data.get(HEADERS.get(MarketoReportGenerator.ACCOUNT_LOCKDOWN)));
        assertEquals("false", user5Data.get(HEADERS.get(MarketoReportGenerator.CC_ADDED)));
        assertEquals("", user5Data.get(HEADERS.get(MarketoReportGenerator.ON_PREM_SUB_ADDED)));
        assertEquals("", user5Data.get(HEADERS.get(MarketoReportGenerator.ON_PREM_SUB_REMOVED)));
        //assertEquals("0", user5Data.get(HEADERS.get(MarketoReportGenerator.NEW_USER)));
    }
}
