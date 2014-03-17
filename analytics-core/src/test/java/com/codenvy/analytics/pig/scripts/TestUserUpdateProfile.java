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
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.users.CompletedProfiles;
import com.codenvy.analytics.metrics.users.UsersProfiles;
import com.codenvy.analytics.metrics.users.UsersProfilesList;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mongodb.util.MyAsserts.assertEquals;
import static org.testng.AssertJUnit.fail;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestUserUpdateProfile extends BaseTest {

    private static final String COLLECTION = TestUserUpdateProfile.class.getSimpleName().toLowerCase();

    @BeforeClass
    public void prepare() throws Exception {

        List<Event> events = new ArrayList<>();

        events.add(Event.Builder.createUserUpdateProfile("user2@gmail.com", "f2", "l2", "company2", "11", "1")
                                .withDate("2013-01-01").build());
        events.add(Event.Builder.createUserUpdateProfile("user1@gmail.com", "f2", "l2", "company1", "11", "1")
                                .withDate("2013-01-01").build());
        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.USER, Parameters.USER_TYPES.REGISTERED.name());
        builder.put(Parameters.WS, Parameters.WS_TYPES.ANY.name());
        builder.put(Parameters.STORAGE_TABLE, COLLECTION);
        builder.put(Parameters.LOG, log.getAbsolutePath());
        pigServer.execute(ScriptType.USERS_UPDATE_PROFILES, builder.build());

        events.add(Event.Builder.createUserUpdateProfile("user1@gmail.com", "f3", "l3", "company1", "22", "2")
                                .withDate("2013-01-02").build());
        events.add(Event.Builder.createUserUpdateProfile("user3@gmail.com", "f4", "l4", "company3", "22", "2")
                                .withDate("2013-01-02").build());
        events.add(Event.Builder.createUserUpdateProfile("user4@gmail.com", "f4", "l4", "company4 :)", "22", "")
                                .withDate("2013-01-02").build());
        log = LogGenerator.generateLog(events);

        builder.put(Parameters.FROM_DATE, "20130102");
        builder.put(Parameters.TO_DATE, "20130102");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        pigServer.execute(ScriptType.USERS_UPDATE_PROFILES, builder.build());
    }

    @Test
    public void testAllProfiles() throws Exception {
        Context.Builder builder = new Context.Builder();

        Metric metric = new TestedUsersProfilesList();

        ListValueData value = (ListValueData)metric.getValue(builder.build());
        assertEquals(value.size(), 4);

        for (ValueData object : value.getAll()) {
            MapValueData item = (MapValueData)object;
            Map<String, ValueData> all = item.getAll();

            ValueData userEmail = all.get("_id");

            if (userEmail.getAsString().equals("user1@gmail.com")) {
                assertEquals(all.get("user_last_name").getAsString(), "l3");
                assertEquals(all.get("user_company").getAsString(), "company1");
                assertEquals(all.get("user_phone").getAsString(), "22");
                assertEquals(all.get("user_job").getAsString(), "Other");

            } else if (userEmail.getAsString().equals("user2@gmail.com")) {
                assertEquals(all.get("user_first_name").getAsString(), "f2");
                assertEquals(all.get("user_last_name").getAsString(), "l2");
                assertEquals(all.get("user_company").getAsString(), "company2");
                assertEquals(all.get("user_phone").getAsString(), "11");
                assertEquals(all.get("user_job").getAsString(), "Other");

            } else if (userEmail.getAsString().equals("user3@gmail.com")) {
                assertEquals(all.get("user_first_name").getAsString(), "f4");
                assertEquals(all.get("user_last_name").getAsString(), "l4");
                assertEquals(all.get("user_company").getAsString(), "company3");
                assertEquals(all.get("user_phone").getAsString(), "22");
                assertEquals(all.get("user_job").getAsString(), "Other");

            } else if (userEmail.getAsString().equals("user4@gmail.com")) {
                assertEquals(all.get("user_first_name").getAsString(), "f4");
                assertEquals(all.get("user_last_name").getAsString(), "l4");
                assertEquals(all.get("user_company").getAsString(), "company4 :)");
                assertEquals(all.get("user_phone").getAsString(), "22");
                assertEquals(all.get("user_job").getAsString(), "");

            } else {
                fail("Unknown user" + userEmail);
            }

        }

        metric = new TestedUsersProfiles();
        assertEquals(metric.getValue(builder.build()).getAsString(), "4");
    }

    @Test
    public void testSingleProfile() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER, "user1@gmail.com");

        Metric metric = new TestedUsersProfilesList();

        ListValueData value = (ListValueData)metric.getValue(builder.build());
        assertEquals(value.size(), 1);

        MapValueData item = (MapValueData)value.getAll().get(0);
        Map<String, ValueData> all = item.getAll();

        assertEquals(all.get("_id").getAsString(), "user1@gmail.com");
        assertEquals(all.get("user_first_name").getAsString(), "f3");
        assertEquals(all.get("user_last_name").getAsString(), "l3");
        assertEquals(all.get("user_company").getAsString(), "company1");
        assertEquals(all.get("user_phone").getAsString(), "22");
        assertEquals(all.get("user_job").getAsString(), "Other");
    }

    @Test
    public void testSearchUsersByCompany() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_COMPANY, "company1");

        Metric metric = new TestedUsersProfilesList();

        ListValueData value = (ListValueData)metric.getValue(builder.build());
        assertEquals(value.size(), 1);
    }

    @Test
    public void testSearchUsersByCompanyUseCase1() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_COMPANY, "company");

        Metric metric = new TestedUsersProfilesList();

        ListValueData value = (ListValueData)metric.getValue(builder.build());
        assertEquals(value.size(), 4);
    }

    @Test
    public void testSearchUsersByCompanyUseCase2() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_COMPANY, "company1,company2");

        Metric metric = new TestedUsersProfilesList();

        ListValueData value = (ListValueData)metric.getValue(builder.build());
        assertEquals(value.size(), 2);
    }

    @Test
    public void testSearchUsersByCompanyUseCase3() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_COMPANY, "company1,company4");

        Metric metric = new TestedUsersProfilesList();

        ListValueData value = (ListValueData)metric.getValue(builder.build());
        assertEquals(value.size(), 2);
    }

    @Test
    public void testSearchUsersByCompanyUseCase4() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_COMPANY, "ompan");

        Metric metric = new TestedUsersProfilesList();

        ListValueData value = (ListValueData)metric.getValue(builder.build());
        assertEquals(value.size(), 4);
    }

    @Test
    public void testSearchUsersByCompanyUseCase5() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_COMPANY, "company4");

        Metric metric = new TestedUsersProfilesList();

        ListValueData value = (ListValueData)metric.getValue(builder.build());
        assertEquals(value.size(), 1);
    }

    @Test
    public void testSearchUsersByCompanyCaseInsensitive() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_COMPANY, "COMPANY2");

        Metric metric = new TestedUsersProfilesList();

        ListValueData value = (ListValueData)metric.getValue(builder.build());
        assertEquals(value.size(), 1);
    }


    @Test
    public void testSearchUsersByCompanyWithSpecialCharacters() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_COMPANY, "company4 :)");

        Metric metric = new TestedUsersProfilesList();

        ListValueData value = (ListValueData)metric.getValue(builder.build());
        assertEquals(value.size(), 1);
    }

    @Test
    public void testSearchUsersByFirstName() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_FIRST_NAME, "f4");

        Metric metric = new TestedUsersProfilesList();

        ListValueData value = (ListValueData)metric.getValue(builder.build());
        assertEquals(value.size(), 2);

        List<ValueData> items = value.getAll();
        MapValueData entry = (MapValueData)items.get(0);
        assertEquals(entry.getAll().get(UsersProfilesList.ID).getAsString(), "user3@gmail.com");

        entry = (MapValueData)items.get(1);
        assertEquals(entry.getAll().get(UsersProfilesList.ID).getAsString(), "user4@gmail.com");
    }

    @Test
    public void testSearchUsersByLastName() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_LAST_NAME, "l4");

        Metric metric = new TestedUsersProfilesList();

        ListValueData value = (ListValueData)metric.getValue(builder.build());
        assertEquals(value.size(), 2);

        List<ValueData> items = value.getAll();
        MapValueData entry = (MapValueData)items.get(0);
        assertEquals(entry.getAll().get(UsersProfilesList.ID).getAsString(), "user3@gmail.com");

        entry = (MapValueData)items.get(1);
        assertEquals(entry.getAll().get(UsersProfilesList.ID).getAsString(), "user4@gmail.com");
    }

    @Test
    public void testCompletedProfiles() throws Exception {
        Metric metric = new TestedCompletedProfiles();

        ValueData value = metric.getValue(new Context.Builder().build());
        assertEquals(value, LongValueData.valueOf(3));
    }

    // ----------------------> Tested metrics

    private class TestedCompletedProfiles extends CompletedProfiles {
        @Override
        public String getStorageCollectionName() {
            return COLLECTION;
        }
    }

    private class TestedUsersProfiles extends UsersProfiles {
        @Override
        public String getStorageCollectionName() {
            return COLLECTION;
        }
    }

    private class TestedUsersProfilesList extends UsersProfilesList {
        @Override
        public String getStorageCollectionName() {
            return COLLECTION;
        }
    }
}
