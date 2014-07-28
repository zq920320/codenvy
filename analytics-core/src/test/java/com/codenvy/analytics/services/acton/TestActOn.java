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

package com.codenvy.analytics.services.acton;

import com.codenvy.analytics.Injector;
import com.codenvy.analytics.metrics.AbstractMetric;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.users.UsersStatisticsList;
import com.codenvy.analytics.services.AbstractUsersActivityTest;

import org.testng.annotations.Test;

import java.io.File;
import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestActOn extends AbstractUsersActivityTest {

    private static final Map<String, String> HEADERS = ActOn.headers;

    @Override
    protected Map<String, String> getHeaders() {
        return ActOn.headers;
    }

    @Test
    public void testWholePeriod() throws Exception {
        ActOn job = Injector.getInstance(ActOn.class);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131101");
        builder.put(Parameters.TO_DATE, "20131102");

        File jobFile = job.prepareFile(builder.build());
        assertEquals(jobFile.getName(), ActOn.FILE_NAME);

        Map<String, Map<String, String>> content = read(jobFile);

        assertEquals(content.size(), 4);

        // verify head of FTP data
        Map<String, String> headData = content.get("_HEAD");
        assertEquals(HEADERS.size(), headData.size());
        for (String column : HEADERS.values()) {
            assertEquals(column, headData.get(column));
        }

        // verify "user1@gmail.com" data
        Map<String, String> user1Data = content.get("user1@gmail.com");
        assertEquals(HEADERS.size(), user1Data.size());
        assertEquals("user1@gmail.com", user1Data.get(HEADERS.get(AbstractMetric.ID)));
        assertEquals("f", user1Data.get(HEADERS.get(AbstractMetric.USER_FIRST_NAME)));
        assertEquals("l", user1Data.get(HEADERS.get(AbstractMetric.USER_LAST_NAME)));
        assertEquals("phone", user1Data.get(HEADERS.get(AbstractMetric.USER_PHONE)));
        assertEquals("company", user1Data.get(HEADERS.get(AbstractMetric.USER_COMPANY)));
        assertEquals("2013-11-01", user1Data.get(HEADERS.get(ActOn.CREATION_DATE)));
        assertEquals("2", user1Data.get(HEADERS.get(UsersStatisticsList.PROJECTS)));
        assertEquals("0", user1Data.get(HEADERS.get(UsersStatisticsList.BUILDS)));
        assertEquals("0", user1Data.get(HEADERS.get(UsersStatisticsList.RUNS)));
        assertEquals("0", user1Data.get(HEADERS.get(UsersStatisticsList.DEPLOYS)));
        assertEquals("5", user1Data.get(HEADERS.get(UsersStatisticsList.TIME)));
        assertEquals("true", user1Data.get(HEADERS.get(ActOn.ACTIVE)));
        assertEquals("1", user1Data.get(HEADERS.get(UsersStatisticsList.INVITES)));
        assertEquals("1", user1Data.get(HEADERS.get(UsersStatisticsList.FACTORIES)));
        assertEquals("0", user1Data.get(HEADERS.get(UsersStatisticsList.DEBUGS)));
        assertEquals("0", user1Data.get(HEADERS.get(UsersStatisticsList.LOGINS)));
        assertEquals("120", user1Data.get(HEADERS.get(UsersStatisticsList.BUILD_TIME)));
        assertEquals("0", user1Data.get(HEADERS.get(UsersStatisticsList.RUN_TIME)));
        assertEquals("true", user1Data.get(HEADERS.get(ActOn.PROFILE_COMPLETED)));
        assertEquals("0", user1Data.get(HEADERS.get(UsersStatisticsList.PAAS_DEPLOYS)));
        assertEquals("29", user1Data.get(HEADERS.get(ActOn.POINTS)));

        // verify "user2@gmail.com" data
        Map<String, String> user2Data = content.get("user2@gmail.com");
        assertEquals(HEADERS.size(), user2Data.size());
        assertEquals("user2@gmail.com", user2Data.get(HEADERS.get(AbstractMetric.ID)));
        assertEquals("", user2Data.get(HEADERS.get(AbstractMetric.USER_FIRST_NAME)));
        assertEquals("", user2Data.get(HEADERS.get(AbstractMetric.USER_LAST_NAME)));
        assertEquals("", user2Data.get(HEADERS.get(AbstractMetric.USER_PHONE)));
        assertEquals("", user2Data.get(HEADERS.get(AbstractMetric.USER_COMPANY)));
        assertEquals("2013-11-01", user2Data.get(HEADERS.get(ActOn.CREATION_DATE)));
        assertEquals("1", user2Data.get(HEADERS.get(UsersStatisticsList.PROJECTS)));
        assertEquals("1", user2Data.get(HEADERS.get(UsersStatisticsList.BUILDS)));
        assertEquals("1", user2Data.get(HEADERS.get(UsersStatisticsList.RUNS)));
        assertEquals("6", user2Data.get(HEADERS.get(UsersStatisticsList.DEPLOYS)));
        assertEquals("10", user2Data.get(HEADERS.get(UsersStatisticsList.TIME)));
        assertEquals("true", user2Data.get(HEADERS.get(ActOn.ACTIVE)));
        assertEquals("0", user2Data.get(HEADERS.get(UsersStatisticsList.INVITES)));
        assertEquals("0", user2Data.get(HEADERS.get(UsersStatisticsList.FACTORIES)));
        assertEquals("1", user2Data.get(HEADERS.get(UsersStatisticsList.DEBUGS)));
        assertEquals("1", user2Data.get(HEADERS.get(UsersStatisticsList.LOGINS)));
        assertEquals("0", user2Data.get(HEADERS.get(UsersStatisticsList.BUILD_TIME)));
        assertEquals("120", user2Data.get(HEADERS.get(UsersStatisticsList.RUN_TIME)));
        assertEquals("false", user2Data.get(HEADERS.get(ActOn.PROFILE_COMPLETED)));
        assertEquals("6", user2Data.get(HEADERS.get(UsersStatisticsList.PAAS_DEPLOYS)));
        assertEquals("92", user2Data.get(HEADERS.get(ActOn.POINTS)));

        // verify "user3@gmail.com" data
        Map<String, String> user3Data = content.get("user3@gmail.com");
        assertEquals(HEADERS.size(), user3Data.size());
        assertEquals("user3@gmail.com", user3Data.get(HEADERS.get(AbstractMetric.ID)));
        assertEquals("", user3Data.get(HEADERS.get(AbstractMetric.USER_FIRST_NAME)));
        assertEquals("", user3Data.get(HEADERS.get(AbstractMetric.USER_LAST_NAME)));
        assertEquals("", user3Data.get(HEADERS.get(AbstractMetric.USER_PHONE)));
        assertEquals("", user3Data.get(HEADERS.get(AbstractMetric.USER_COMPANY)));
        assertEquals("2013-11-01", user3Data.get(HEADERS.get(ActOn.CREATION_DATE)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.PROJECTS)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.BUILDS)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.RUNS)));
        assertEquals("1", user3Data.get(HEADERS.get(UsersStatisticsList.DEPLOYS)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.TIME)));
        assertEquals("true", user3Data.get(HEADERS.get(ActOn.ACTIVE)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.INVITES)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.FACTORIES)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.DEBUGS)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.LOGINS)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.BUILD_TIME)));
        assertEquals("0", user3Data.get(HEADERS.get(UsersStatisticsList.RUN_TIME)));
        assertEquals("false", user3Data.get(HEADERS.get(ActOn.PROFILE_COMPLETED)));
        assertEquals("1", user3Data.get(HEADERS.get(UsersStatisticsList.PAAS_DEPLOYS)));
        assertEquals("12", user3Data.get(HEADERS.get(ActOn.POINTS)));
    }
}
