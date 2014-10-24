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
package com.codenvy.analytics.pig.scripts;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.datamodel.ValueDataUtil;
import com.codenvy.analytics.metrics.AbstractMetric;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
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
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/** @author Anatoliy Bazko */
public class TestUsersProfiles extends BaseTest {

    private static final Metric METRIC = MetricFactory.getMetric(MetricType.USERS_PROFILES_LIST);

    @BeforeClass
    public void prepare() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createUserCreatedEvent(UID1, "u1", "u1@u.com").withDate("2013-01-01", "10:00:00").build());
        events.add(Event.Builder.createUserCreatedEvent(UID2, "u2", "u2@u.com").withDate("2013-01-01", "10:00:00").build());
        events.add(Event.Builder.createUserCreatedEvent(UID3, "u3", "u3@u.com").withDate("2013-01-01", "10:00:00").build());
        events.add(Event.Builder.createUserCreatedEvent(AUID1, "a1", "anonymoususer_1").withDate("2013-01-01", "10:00:00").build());

        events.add(Event.Builder.createUserUpdateProfile(UID1, "u1@u.com", "u1@u.com", "f1", "l1", "company1", "phone", "job")
                                .withDate("2013-01-01").build());
        events.add(Event.Builder.createUserUpdateProfile(UID2, "u2@u.com", "u2@u.com", "f2", "l2", "company2", "", "")
                                .withDate("2013-01-01").build());

        // user2 changes company
        events.add(Event.Builder.createUserUpdateProfile(UID2, "u2@u.com", "u2@u.com", "f2", "l2", "company3", "", "")
                                .withDate("2013-01-02").build());
        events.add(Event.Builder.createUserUpdateProfile(UID3, "u3@u.com", "u3@u.com", "f3", "l3", "company3 :)", "", "")
                                .withDate("2013-01-02").build());

        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.putAll(scriptsManager.getScript(ScriptType.USERS_PROFILES, MetricType.USERS_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_PROFILES, builder.build());

        builder.put(Parameters.FROM_DATE, "20130102");
        builder.put(Parameters.TO_DATE, "20130102");
        builder.putAll(scriptsManager.getScript(ScriptType.USERS_PROFILES, MetricType.USERS_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_PROFILES, builder.build());
    }

    @Test
    public void testAllProfiles() throws Exception {
        ListValueData l = ValueDataUtil.getAsList(METRIC, Context.EMPTY);
        Map<String, Map<String, ValueData>> m = listToMap(l, AbstractMetric.ID);

        assertEquals(m.size(), 4);
        assertTrue(m.containsKey(UID1));
        assertTrue(m.containsKey(UID2));
        assertTrue(m.containsKey(UID3));
        assertTrue(m.containsKey(AUID1));

        assertProfile(m.get(UID1), "f1", "l1", "company1", "phone", "job", "[u1@u.com]", fullDateFormat.parse("2013-01-01 10:00:00").getTime());
        assertProfile(m.get(UID2), "f2", "l2", "company3", "", "", "[u2@u.com]", fullDateFormat.parse("2013-01-01 10:00:00").getTime());
        assertProfile(m.get(UID3), "f3", "l3", "company3 :)", "", "", "[u3@u.com]", fullDateFormat.parse("2013-01-01 10:00:00").getTime());
        assertProfile(m.get(AUID1), null, null, null, null, null, "[anonymoususer_1]", fullDateFormat.parse("2013-01-01 10:00:00").getTime());
    }

    private void assertProfile(Map<String, ValueData> profile,
                               String firstName,
                               String lastName,
                               String company,
                               String phone,
                               String job,
                               String aliases,
                               long creationDate) {
        assertField(profile, "user_first_name", firstName);
        assertField(profile, "user_last_name", lastName);
        assertField(profile, "user_company", company);
        assertField(profile, "user_phone", phone);
        assertField(profile, "user_job", job);
        assertField(profile, "aliases", aliases);
        assertEquals(LongValueData.valueOf(creationDate), profile.get("date"));
    }

    private void assertField(Map<String, ValueData> profile, String field, String expectedValue) {
        ValueData actualValue = profile.get(field);
        if (actualValue == null) {
            assertNull(expectedValue);
        } else {
            assertEquals(StringValueData.valueOf(expectedValue), actualValue);
        }
    }

    @Test
    public void testFilterByUserAsStringArray() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_ID, new String[]{UID1});

        ListValueData l = ValueDataUtil.getAsList(METRIC, builder.build());
        Map<String, Map<String, ValueData>> m = listToMap(l, AbstractMetric.ID);

        assertEquals(m.size(), 1);
        assertTrue(m.containsKey(UID1));
    }

    @Test
    public void testFilterByCompanyAsString() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_COMPANY, "company1");

        ListValueData l = ValueDataUtil.getAsList(METRIC, builder.build());
        Map<String, Map<String, ValueData>> m = listToMap(l, AbstractMetric.ID);

        assertEquals(m.size(), 1);
        assertTrue(m.containsKey(UID1));
    }

    @Test
    public void testFilterByCompanyAsStringSeveralValues() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_COMPANY, "company1 OR company3");

        ListValueData l = ValueDataUtil.getAsList(METRIC, builder.build());
        Map<String, Map<String, ValueData>> m = listToMap(l, AbstractMetric.ID);

        assertEquals(m.size(), 3);
        assertTrue(m.containsKey(UID1));
        assertTrue(m.containsKey(UID2));
        assertTrue(m.containsKey(UID3));
    }

    @Test
    public void testFilterByCompanyAsStringExclusion() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_COMPANY, "~ company1");

        ListValueData l = ValueDataUtil.getAsList(METRIC, builder.build());
        Map<String, Map<String, ValueData>> m = listToMap(l, AbstractMetric.ID);

        assertEquals(m.size(), 3);
        assertTrue(m.containsKey(UID2));
        assertTrue(m.containsKey(UID3));
        assertTrue(m.containsKey(AUID1));
    }

    @Test
    public void testFilterByCompanyAsStringArray() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_COMPANY, new String[]{"company1"});

        ListValueData l = ValueDataUtil.getAsList(METRIC, builder.build());
        Map<String, Map<String, ValueData>> m = listToMap(l, AbstractMetric.ID);

        assertEquals(m.size(), 1);
        assertTrue(m.containsKey(UID1));
    }

    @Test
    public void testFilterByCompanyAsPatternArray() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_COMPANY, new Pattern[]{Pattern.compile("company1")});

        ListValueData l = ValueDataUtil.getAsList(METRIC, builder.build());
        Map<String, Map<String, ValueData>> m = listToMap(l, AbstractMetric.ID);

        assertEquals(m.size(), 1);
        assertTrue(m.containsKey(UID1));
    }

    @Test
    public void testFilterByCompanyPartName() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_COMPANY, "company");

        ListValueData l = ValueDataUtil.getAsList(METRIC, builder.build());
        Map<String, Map<String, ValueData>> m = listToMap(l, AbstractMetric.ID);

        assertEquals(m.size(), 3);
        assertTrue(m.containsKey(UID1));
        assertTrue(m.containsKey(UID2));
        assertTrue(m.containsKey(UID3));
    }

    @Test
    public void testInsensitiveFilterByCompany() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_COMPANY, "COMPANY1");

        ListValueData l = ValueDataUtil.getAsList(METRIC, builder.build());
        Map<String, Map<String, ValueData>> m = listToMap(l, AbstractMetric.ID);

        assertEquals(m.size(), 1);
        assertTrue(m.containsKey(UID1));
    }

    @Test
    public void testFilterByCompanySpecialCharacters() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_COMPANY, "company3 :)");

        ListValueData l = ValueDataUtil.getAsList(METRIC, builder.build());
        Map<String, Map<String, ValueData>> m = listToMap(l, AbstractMetric.ID);

        assertEquals(m.size(), 1);
        assertTrue(m.containsKey(UID3));
    }

    @Test
    public void testFilterByFirstName() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_FIRST_NAME, "f1");

        ListValueData l = ValueDataUtil.getAsList(METRIC, builder.build());
        Map<String, Map<String, ValueData>> m = listToMap(l, AbstractMetric.ID);

        assertEquals(m.size(), 1);
        assertTrue(m.containsKey(UID1));
    }

    @Test
    public void testFilterByLastName() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_LAST_NAME, "l1");

        ListValueData l = ValueDataUtil.getAsList(METRIC, builder.build());
        Map<String, Map<String, ValueData>> m = listToMap(l, AbstractMetric.ID);

        assertEquals(m.size(), 1);
        assertTrue(m.containsKey(UID1));
    }

    @Test
    public void testCompletedProfiles() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.COMPLETED_PROFILES);

        LongValueData l = ValueDataUtil.getAsLong(metric, Context.EMPTY);
        assertEquals(l.getAsLong(), 1);
    }
}
