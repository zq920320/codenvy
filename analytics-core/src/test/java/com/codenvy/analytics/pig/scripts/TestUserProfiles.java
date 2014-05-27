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
import com.codenvy.analytics.datamodel.*;
import com.codenvy.analytics.metrics.*;
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
import java.util.regex.Pattern;

import static com.mongodb.util.MyAsserts.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Anatoliy Bazko
 */
public class TestUserProfiles extends BaseTest {

    @BeforeClass
    public void prepare() throws Exception {
        List<Event> events = new ArrayList<>();

        events.add(Event.Builder.createUserCreatedEvent("id1", "user1@gmail.com", "user1@gmail.com")
                                .withDate("2013-01-01").withTime("10:00:00,000").build());
        events.add(Event.Builder.createUserUpdateProfile("id1", "user1@gmail.com", "user1@gmail.com", "f2", "l2", "company1", "11", "1")
                                .withDate("2013-01-01").withTime("10:10:00,000").build());

        events.add(Event.Builder.createUserCreatedEvent("id2", "user2@gmail.com", "user2@gmail.com")
                                .withDate("2013-01-01").withTime("11:00:00,000").build());
        events.add(Event.Builder.createUserUpdateProfile("id2", "user2@gmail.com", "user2@gmail.com", "f2", "l2", "company2", "11", "1")
                                .withDate("2013-01-01").withTime("11:00:00,000").build());
        events.add(Event.Builder.createUserUpdatedEvent("id2", "user2@yahoo.com", "user2@yahoo.com")
                                .withDate("2013-01-01").withTime("12:00:00,000").build());
        events.add(Event.Builder.createUserUpdatedEvent("id2", "user2@ukr.net", "user2@ukr.net")
                                .withDate("2013-01-01").withTime("13:00:00,000").build());
        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(scriptsManager.getScript(ScriptType.USERS_PROFILES, MetricType.USERS_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_PROFILES, builder.build());

        events.add(Event.Builder.createUserUpdateProfile("id1", "user1@gmail.com", "user1@gmail.com", "f3", "l3", "company1", "22", "2")
                                .withDate("2013-01-02").build());

        events.add(Event.Builder.createUserCreatedEvent("id3", "user3@gmail.com", "user3@gmail.com")
                                .withDate("2013-01-02").withTime("12:00:00,000").build());
        events.add(Event.Builder.createUserUpdateProfile("id3", "user3@gmail.com", "user3@gmail.com", "f4", "l4", "company3", "22", "2")
                                .withDate("2013-01-02").withTime("12:10:00,000").build());

        events.add(Event.Builder.createUserCreatedEvent("id4", "user4@gmail.com", "user4@gmail.com")
                                .withDate("2013-01-02").withTime("13:00:00,000").build());
        events.add(Event.Builder.createUserUpdateProfile("id4", "user4@gmail.com", "user4@gmail.com", "f4", "l4", "company4 :)", "22", "")
                                .withDate("2013-01-02").withTime("13:10:00,000").build());
        log = LogGenerator.generateLog(events);

        builder.put(Parameters.FROM_DATE, "20130102");
        builder.put(Parameters.TO_DATE, "20130102");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        pigServer.execute(ScriptType.USERS_PROFILES, builder.build());
    }

    @Test
    public void shouldReturnAllProfiles() throws Exception {
        Metric metric = new UsersProfiles();
        assertEquals(LongValueData.valueOf(4), metric.getValue(Context.EMPTY));

        metric = new UsersProfilesList();
        ListValueData value = ValueDataUtil.getAsList(metric, Context.EMPTY);
        assertEquals(value.size(), 4);

        Map<String, Map<String, ValueData>> m = listToMap(value, "_id");

        assertEquals(m.size(), 4);
        assertTrue(m.containsKey("id1"));
        assertTrue(m.containsKey("id2"));
        assertTrue(m.containsKey("id3"));
        assertTrue(m.containsKey("id4"));

        assertProfile(m.get("id1"),
                      "f3",
                      "l3",
                      "company1",
                      "22",
                      "Other",
                      "[user1@gmail.com]",
                      fullDateFormat.parse("2013-01-01 10:00:00").getTime());
        assertProfile(m.get("id2"),
                      "f2",
                      "l2",
                      "company2",
                      "11",
                      "Other",
                      "[user2@ukr.net]",
                      fullDateFormat.parse("2013-01-01 11:00:00").getTime());
        assertProfile(m.get("id3"),
                      "f4",
                      "l4",
                      "company3",
                      "22",
                      "Other",
                      "[user3@gmail.com]",
                      fullDateFormat.parse("2013-01-02 12:00:00").getTime());
        assertProfile(m.get("id4"),
                      "f4",
                      "l4",
                      "company4 :)",
                      "22",
                      "",
                      "[user4@gmail.com]",
                      fullDateFormat.parse("2013-01-02 13:00:00").getTime());
    }

    private void assertProfile(Map<String, ValueData> profile,
                               String firstName,
                               String lastName,
                               String company,
                               String phone,
                               String job,
                               String aliases,
                               long creationDate) {
        assertEquals(StringValueData.valueOf(firstName), profile.get("user_first_name"));
        assertEquals(StringValueData.valueOf(lastName), profile.get("user_last_name"));
        assertEquals(StringValueData.valueOf(company), profile.get("user_company"));
        assertEquals(StringValueData.valueOf(phone), profile.get("user_phone"));
        assertEquals(StringValueData.valueOf(job), profile.get("user_job"));
        assertEquals(StringValueData.valueOf(aliases), profile.get("aliases"));
        assertEquals(LongValueData.valueOf(creationDate), profile.get("date"));
    }

    @Test
    public void testFilterByUserAsString() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER, "id1");

        Metric metric = new UsersProfiles();
        assertEquals(LongValueData.valueOf(1), metric.getValue(builder.build()));

        metric = new UsersProfilesList();
        ListValueData value = ValueDataUtil.getAsList(metric, builder.build());
        assertEquals(value.size(), 1);

        Map<String, Map<String, ValueData>> m = listToMap(value, "_id");
        assertEquals(m.size(), 1);
        assertTrue(m.containsKey("id1"));
    }

    @Test
    public void testFilterByUserAsStringSeveralValues() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER, "id1 OR id2");

        Metric metric = new UsersProfiles();
        assertEquals(LongValueData.valueOf(2), metric.getValue(builder.build()));

        metric = new UsersProfilesList();
        ListValueData value = ValueDataUtil.getAsList(metric, builder.build());
        assertEquals(value.size(), 2);

        Map<String, Map<String, ValueData>> m = listToMap(value, "_id");
        assertEquals(m.size(), 2);
        assertTrue(m.containsKey("id1"));
        assertTrue(m.containsKey("id2"));
    }

    @Test
    public void testFilterByUserAsStringSeveralValuesWithExclusion() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER, "~ id1 OR id2");

        Metric metric = new UsersProfiles();
        assertEquals(LongValueData.valueOf(2), metric.getValue(builder.build()));

        metric = new UsersProfilesList();
        ListValueData value = ValueDataUtil.getAsList(metric, builder.build());
        assertEquals(value.size(), 2);

        Map<String, Map<String, ValueData>> m = listToMap(value, "_id");
        assertEquals(m.size(), 2);
        assertTrue(m.containsKey("id3"));
        assertTrue(m.containsKey("id4"));
    }

    @Test
    public void testFilterByUserAsStringArray() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER, new String[]{"id1"});

        Metric metric = new UsersProfiles();
        assertEquals(LongValueData.valueOf(1), metric.getValue(builder.build()));

        metric = new UsersProfilesList();
        ListValueData value = ValueDataUtil.getAsList(metric, builder.build());
        assertEquals(value.size(), 1);

        Map<String, Map<String, ValueData>> m = listToMap(value, "_id");
        assertEquals(m.size(), 1);
        assertTrue(m.containsKey("id1"));
    }

    @Test
    public void testFilterByUserAsPatternArray() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER, new Pattern[]{Pattern.compile("id1")});

        Metric metric = new UsersProfiles();
        assertEquals(LongValueData.valueOf(1), metric.getValue(builder.build()));

        metric = new UsersProfilesList();
        ListValueData value = ValueDataUtil.getAsList(metric, builder.build());
        assertEquals(value.size(), 1);

        Map<String, Map<String, ValueData>> m = listToMap(value, "_id");
        assertEquals(m.size(), 1);
        assertTrue(m.containsKey("id1"));
    }

    @Test
    public void testFilterByCompanyAsString() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_COMPANY, "company1");

        Metric metric = new UsersProfiles();
        assertEquals(LongValueData.valueOf(1), metric.getValue(builder.build()));

        metric = new UsersProfilesList();
        ListValueData value = ValueDataUtil.getAsList(metric, builder.build());
        assertEquals(value.size(), 1);
    }

    @Test
    public void testFilterByCompanyAsStringSeveralValues() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_COMPANY, "company1 OR company2");

        Metric metric = new UsersProfiles();
        assertEquals(LongValueData.valueOf(2), metric.getValue(builder.build()));

        metric = new UsersProfilesList();
        ListValueData value = ValueDataUtil.getAsList(metric, builder.build());
        assertEquals(value.size(), 2);
    }

    @Test
    public void testFilterByCompanyAsStringSeveralValuesExclusion() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_COMPANY, "~ company1 OR company2");

        Metric metric = new UsersProfiles();
        assertEquals(LongValueData.valueOf(2), metric.getValue(builder.build()));

        metric = new UsersProfilesList();
        ListValueData value = ValueDataUtil.getAsList(metric, builder.build());
        assertEquals(value.size(), 2);
    }

    @Test
    public void testFilterByCompanyAsStringArray() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_COMPANY, new String[]{"company1"});

        Metric metric = new UsersProfiles();
        assertEquals(LongValueData.valueOf(1), metric.getValue(builder.build()));

        metric = new UsersProfilesList();
        ListValueData value = ValueDataUtil.getAsList(metric, builder.build());
        assertEquals(value.size(), 1);
    }

    @Test
    public void testFilterByCompanyAsPatternArray() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_COMPANY, new Pattern[]{Pattern.compile("company1")});

        Metric metric = new UsersProfiles();
        assertEquals(LongValueData.valueOf(1), metric.getValue(builder.build()));

        metric = new UsersProfilesList();
        ListValueData value = ValueDataUtil.getAsList(metric, builder.build());
        assertEquals(value.size(), 1);
    }

    @Test
    public void testFilterByCompanyPartName() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_COMPANY, "company");

        Metric metric = new UsersProfiles();
        assertEquals(LongValueData.valueOf(4), metric.getValue(builder.build()));

        metric = new UsersProfilesList();
        ListValueData value = ValueDataUtil.getAsList(metric, builder.build());
        assertEquals(value.size(), 4);
    }

    @Test
    public void testInsensitiveFilterByCompany() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_COMPANY, "COMPANY2");

        Metric metric = new UsersProfiles();
        assertEquals(LongValueData.valueOf(1), metric.getValue(builder.build()));

        metric = new UsersProfilesList();
        ListValueData value = ValueDataUtil.getAsList(metric, builder.build());
        assertEquals(value.size(), 1);
    }

    @Test
    public void testFilterByCompanySpecialCharacters() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_COMPANY, "company4 :)");

        Metric metric = new UsersProfiles();
        assertEquals(LongValueData.valueOf(1), metric.getValue(builder.build()));

        metric = new UsersProfilesList();
        ListValueData value = ValueDataUtil.getAsList(metric, builder.build());
        assertEquals(value.size(), 1);
    }

    @Test
    public void testFilterByFirstName() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_FIRST_NAME, "f4");

        Metric metric = new UsersProfiles();
        assertEquals(LongValueData.valueOf(2), metric.getValue(builder.build()));

        metric = new UsersProfilesList();
        ListValueData value = ValueDataUtil.getAsList(metric, builder.build());
        assertEquals(value.size(), 2);

        Map<String, Map<String, ValueData>> m = listToMap(value, "_id");
        assertEquals(m.size(), 2);
        assertTrue(m.containsKey("id3"));
        assertTrue(m.containsKey("id4"));
    }

    @Test
    public void testFilterByLastName() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_LAST_NAME, "l4");

        Metric metric = new UsersProfiles();
        assertEquals(LongValueData.valueOf(2), metric.getValue(builder.build()));


        metric = new UsersProfilesList();
        ListValueData value = ValueDataUtil.getAsList(metric, builder.build());
        assertEquals(value.size(), 2);

        Map<String, Map<String, ValueData>> m = listToMap(value, "_id");
        assertEquals(m.size(), 2);
        assertTrue(m.containsKey("id3"));
        assertTrue(m.containsKey("id4"));
    }

    @Test
    public void testCompletedProfiles() throws Exception {
        Metric metric = new CompletedProfiles();

        ValueData value = metric.getValue(Context.EMPTY);
        assertEquals(value, LongValueData.valueOf(3));
    }
}
