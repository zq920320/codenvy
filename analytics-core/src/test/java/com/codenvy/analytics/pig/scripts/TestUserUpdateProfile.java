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
import com.codenvy.analytics.metrics.*;
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

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestUserUpdateProfile extends BaseTest {

    private Map<String, String> params;

    @BeforeClass
    public void prepare() throws IOException {
        params = Utils.newContext();

        List<Event> events = new ArrayList<>();

        events.add(Event.Builder.createUserUpdateProfile("user2@gmail.com", "f2", "l2", "company", "11", "1")
                        .withDate("2013-01-01").build());
        events.add(Event.Builder.createUserUpdateProfile("user1@gmail.com", "f2", "l2", "company", "11", "1")
                        .withDate("2013-01-01").build());
        File log = LogGenerator.generateLog(events);

        Parameters.FROM_DATE.put(params, "20130101");
        Parameters.TO_DATE.put(params, "20130101");
        Parameters.USER.put(params, Parameters.USER_TYPES.REGISTERED.name());
        Parameters.WS.put(params, Parameters.WS_TYPES.ANY.name());
        Parameters.STORAGE_TABLE.put(params, "testuserupdateprofile");
        Parameters.LOG.put(params, log.getAbsolutePath());

        PigServer.execute(ScriptType.USERS_UPDATE_PROFILES, params);


        events.add(Event.Builder.createUserUpdateProfile("user1@gmail.com", "f3", "l3", "company-2", "22", "2")
                        .withDate("2013-01-02").build());
        events.add(Event.Builder.createUserUpdateProfile("user3@gmail.com", "f4", "l4", "company-2", "22", "2")
                        .withDate("2013-01-02").build());
        events.add(Event.Builder.createUserUpdateProfile("user4@gmail.com", "f4", "l4", "company", "22", "")
                        .withDate("2013-01-02").build());
        log = LogGenerator.generateLog(events);

        Parameters.FROM_DATE.put(params, "20130102");
        Parameters.TO_DATE.put(params, "20130102");
        Parameters.LOG.put(params, log.getAbsolutePath());

        PigServer.execute(ScriptType.USERS_UPDATE_PROFILES, params);
    }

    @Test
    public void testAllProfiles() throws Exception {
        Map<String, String> context = Utils.newContext();

        Metric metric = new TestUsersProfilesList();

        ListValueData value = (ListValueData)metric.getValue(context);
        assertEquals(value.size(), 4);

        for (ValueData object : value.getAll()) {
            MapValueData item = (MapValueData)object;
            Map<String, ValueData> all = item.getAll();

            ValueData userEmail = all.get("_id");

            if (userEmail.getAsString().equals("user1@gmail.com")) {
                assertEquals(all.get("user_last_name").getAsString(), "l3");
                assertEquals(all.get("user_company").getAsString(), "company-2");
                assertEquals(all.get("user_phone").getAsString(), "22");
                assertEquals(all.get("user_job").getAsString(), "2");

            } else if (userEmail.getAsString().equals("user2@gmail.com")) {
                assertEquals(all.get("user_first_name").getAsString(), "f2");
                assertEquals(all.get("user_last_name").getAsString(), "l2");
                assertEquals(all.get("user_company").getAsString(), "company");
                assertEquals(all.get("user_phone").getAsString(), "11");
                assertEquals(all.get("user_job").getAsString(), "1");

            } else if (userEmail.getAsString().equals("user3@gmail.com")) {
                assertEquals(all.get("user_first_name").getAsString(), "f4");
                assertEquals(all.get("user_last_name").getAsString(), "l4");
                assertEquals(all.get("user_company").getAsString(), "company-2");
                assertEquals(all.get("user_phone").getAsString(), "22");
                assertEquals(all.get("user_job").getAsString(), "2");

            } else if (userEmail.getAsString().equals("user4@gmail.com")) {
                assertEquals(all.get("user_first_name").getAsString(), "f4");
                assertEquals(all.get("user_last_name").getAsString(), "l4");
                assertEquals(all.get("user_company").getAsString(), "company");
                assertEquals(all.get("user_phone").getAsString(), "22");
                assertEquals(all.get("user_job").getAsString(), "");
            }
        }

        metric = new TestUsersProfiles();
        assertEquals(metric.getValue(context).getAsString(), "4");
    }

    @Test
    public void testSingleProfile() throws Exception {
        Map<String, String> context = Utils.newContext();
        MetricFilter.USER.put(context, "user1@gmail.com");

        Metric metric = new TestUsersProfilesList();

        ListValueData value = (ListValueData)metric.getValue(context);
        assertEquals(value.size(), 1);

        MapValueData item = (MapValueData)value.getAll().get(0);
        Map<String, ValueData> all = item.getAll();

        assertEquals(all.get("_id").getAsString(), "user1@gmail.com");
        assertEquals(all.get("user_first_name").getAsString(), "f3");
        assertEquals(all.get("user_last_name").getAsString(), "l3");
        assertEquals(all.get("user_company").getAsString(), "company-2");
        assertEquals(all.get("user_phone").getAsString(), "22");
        assertEquals(all.get("user_job").getAsString(), "2");
    }

    @Test
    public void testUsersByCompany() throws Exception {
        Map<String, String> context = Utils.newContext();
        MetricFilter.USER_COMPANY.put(context, "company");

        Metric metric = new TestUsersProfilesList();

        ListValueData value = (ListValueData)metric.getValue(context);
        assertEquals(value.size(), 2);

        for (ValueData object : value.getAll()) {
            MapValueData item = (MapValueData)object;
            Map<String, ValueData> all = item.getAll();

            if (all.get("_id").getAsString().equals("user2@gmail.com")) {
                assertEquals(all.get("user_first_name").getAsString(), "f2");
                assertEquals(all.get("user_last_name").getAsString(), "l2");
                assertEquals(all.get("user_company").getAsString(), "company");
                assertEquals(all.get("user_phone").getAsString(), "11");
                assertEquals(all.get("user_job").getAsString(), "1");

            } else if (all.get("_id").getAsString().equals("user4@gmail.com")) {
                assertEquals(all.get("user_first_name").getAsString(), "f4");
                assertEquals(all.get("user_last_name").getAsString(), "l4");
                assertEquals(all.get("user_company").getAsString(), "company");
                assertEquals(all.get("user_phone").getAsString(), "22");
                assertEquals(all.get("user_job").getAsString(), "");
            }
        }
    }

    public class TestUsersProfiles extends UsersProfiles {
        @Override
        public String getStorageCollectionName() {
            return "testuserupdateprofile";
        }
    }


    public class TestUsersProfilesList extends UsersProfilesList {

        @Override
        public String getStorageCollectionName() {
            return "testuserupdateprofile";
        }
    }
}
