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
package com.codenvy.analytics.pig.scripts;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.UsersStatistics;
import com.codenvy.analytics.pig.PigServer;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mongodb.util.MyAsserts.assertEquals;
import static com.mongodb.util.MyAsserts.fail;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestUsersData extends BaseTest {

    private Map<String, String> params;

    @BeforeClass
    public void prepare() throws IOException {
        params = Utils.newContext();

        List<Event> events = new ArrayList<>();

        events.add(Event.Builder.createUserUpdateProfile("user1@gmail.com", "f_1", "l_1", "", "", "")
                        .withDate("2013-11-01").build());
        events.add(Event.Builder.createUserUpdateProfile("user2@gmail.com", "f_2", "l_2", "", "", "")
                        .withDate("2013-11-01").build());

        events.add(Event.Builder.createSessionStartedEvent("user1@gmail.com", "ws1", "ide", "1").withDate("2013-11-01")
                        .withTime("20:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user1@gmail.com", "ws1", "ide", "1").withDate("2013-11-01")
                        .withTime("20:05:00").build());
        events.add(Event.Builder.createSessionStartedEvent("user3@gmail.com", "ws1", "ide", "2").withDate("2013-11-01")
                        .withTime("20:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user3@gmail.com", "ws1", "ide", "2").withDate("2013-11-01")
                        .withTime("20:02:00").build());

        events.add(Event.Builder.createProjectCreatedEvent("user1@gmail.com", "ws1", "s", "", "")
                        .withDate("2013-11-01").build());
        events.add(Event.Builder.createProjectDeployedEvent("user4@gmail.com", "ws1", "s", "", "", "")
                        .withDate("2013-11-01").build());

        File log = LogGenerator.generateLog(events);

        Parameters.FROM_DATE.put(params, "20131101");
        Parameters.TO_DATE.put(params, "20131101");
        Parameters.USER.put(params, Parameters.USER_TYPES.REGISTERED.name());
        Parameters.WS.put(params, Parameters.WS_TYPES.ANY.name());
        Parameters.STORAGE_TABLE.put(params, "testuserupdateprofile-profiles");
        Parameters.STORAGE_TABLE_USERS_STATISTICS.put(params, "testusersdata");
        Parameters.LOG.put(params, log.getAbsolutePath());

        PigServer.execute(ScriptType.USER_UPDATE_PROFILE, params);

        Parameters.USER.put(params, Parameters.USER_TYPES.ANY.name());
        Parameters.WS.put(params, Parameters.WS_TYPES.ANY.name());
        Parameters.STORAGE_TABLE.put(params, "testuserupdateprofile-sessions");
        Parameters.STORAGE_TABLE_USERS_STATISTICS.put(params, "testusersdata");
        PigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, params);

        Parameters.USER.put(params, Parameters.USER_TYPES.REGISTERED.name());
        Parameters.WS.put(params, Parameters.WS_TYPES.ANY.name());
        Parameters.STORAGE_TABLE.put(params, "testusersdata");
        PigServer.execute(ScriptType.USERS_STATISTICS, params);
    }

    @Test
    public void testSingleProfile() throws Exception {
        Map<String, String> context = Utils.newContext();

        Metric metric = new TestUsersStatistics();
        ListValueData value = (ListValueData)metric.getValue(context);

        assertEquals(value.size(), 4);

        for (ValueData object : value.getAll()) {
            MapValueData valueData = (MapValueData)object;

            Map<String, ValueData> all = valueData.getAll();
            String user = all.get("user_email").getAsString();

            switch (user) {
                case "user1@gmail.com":
                    assertEquals(all.size(), 9);
                    assertEquals(all.get("user_first_name").getAsString(), "f_1");
                    assertEquals(all.get("user_last_name").getAsString(), "l_1");
                    assertEquals(all.get("user_company").getAsString(), "");
                    assertEquals(all.get("user_phone").getAsString(), "");
                    assertEquals(all.get("projects").getAsString(), "1");
                    assertEquals(all.get("deploys").getAsString(), "0");
                    assertEquals(all.get("builds").getAsString(), "0");
                    assertEquals(all.get("time").getAsString(), "300");

                    break;
                case "user2@gmail.com":
                    assertEquals(all.size(), 5);
                    assertEquals(all.get("user_first_name").getAsString(), "f_2");
                    assertEquals(all.get("user_last_name").getAsString(), "l_2");
                    assertEquals(all.get("user_company").getAsString(), "");
                    assertEquals(all.get("user_phone").getAsString(), "");

                    break;
                case "user3@gmail.com":
                    assertEquals(all.size(), 2);
                    assertEquals(all.get("time").getAsString(), "120");

                    break;
                case "user4@gmail.com":
                    assertEquals(all.size(), 4);
                    assertEquals(all.get("projects").getAsString(), "0");
                    assertEquals(all.get("deploys").getAsString(), "1");
                    assertEquals(all.get("builds").getAsString(), "1");

                    break;
                default:
                    fail("unknown user" + user);
                    break;
            }
        }
    }

    public class TestUsersStatistics extends UsersStatistics {

        @Override
        public String getStorageTable() {
            return "testusersdata";
        }
    }
}
