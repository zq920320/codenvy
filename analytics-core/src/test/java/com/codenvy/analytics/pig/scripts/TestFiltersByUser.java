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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Anatoliy Bazko
 */
public class TestFiltersByUser extends BaseTest {

    @BeforeClass
    public void prepare() throws Exception {
        List<Event> events = new ArrayList<>();

        events.add(Event.Builder.createUserCreatedEvent(UID1, "u1", "u1@u.com").withDate("2013-01-01").build());
        events.add(Event.Builder.createUserCreatedEvent(UID2, "u2", "u2@u.com").withDate("2013-01-01").build());
        events.add(Event.Builder.createUserCreatedEvent(UID3, "anonymoususer_3", "anonymoususer_3").withDate("2013-01-01").build());
        events.add(Event.Builder.createUserCreatedEvent(UID4, "anonymoususer_4", "anonymoususer_4").withDate("2013-01-01").build());

        events.add(Event.Builder.createUserUpdateProfile(UID1, "u1@u.com", "u1@u.com", "f1", "l1", "company1", "", "")
                                .withDate("2013-01-01").build());
        events.add(Event.Builder.createUserUpdateProfile(UID2, "u2@u.com", "u2@u.com", "f2", "l2", "company2", "", "")
                                .withDate("2013-01-01").build());


        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.USERS_PROFILES, MetricType.USERS_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.USERS_ACTIVITY, MetricType.USERS_ACTIVITY).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_ACTIVITY, builder.build());
    }

    @Test(dataProvider = "metricsToTest")
    public void testFilterByID(Metric metric, String userIdField) throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_ID, UID1);

        ListValueData l = getAsList(metric, builder.build());

        assertResult(l, userIdField, UID1);
    }

    @Test(dataProvider = "metricsToTest")
    public void testSeveralFilterByID(Metric metric, String userIdField) throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_ID, UID1 + " OR " + UID3);

        ListValueData l = getAsList(metric, builder.build());

        assertResult(l, userIdField, UID1, UID3);
    }

    @Test(dataProvider = "metricsToTest")
    public void testFilterByWSUseCase1(Metric metric, String wsIdField) throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER, UID1);

        ListValueData l = getAsList(metric, builder.build());

        assertResult(l, wsIdField, UID1);
    }

    @Test(dataProvider = "metricsToTest")
    public void testFilterByWSUseCase2(Metric metric, String wsIdField) throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER, UID1 + " OR u2@u.com");

        ListValueData l = getAsList(metric, builder.build());

        assertResult(l, wsIdField, UID1, UID2);
    }

    @Test(dataProvider = "metricsToTest")
    public void testFilterByWSUseCase3(Metric metric, String wsIdField) throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER, "~ " + UID1 + " OR u2@u.com");

        ListValueData l = getAsList(metric, builder.build());

        assertResult(l, wsIdField, UID3, UID4);
    }

    @Test(dataProvider = "metricsToTest")
    public void testNegotiatedFilterByID(Metric metric, String userIdField) throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_ID, "~ " + UID1);

        ListValueData l = getAsList(metric, builder.build());

        assertResult(l, userIdField, UID2, UID3, UID4);
    }

    @Test(dataProvider = "metricsToTest")
    public void testFilterByNameContainsID(Metric metric, String userIdField) throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER, UID1);

        ListValueData l = getAsList(metric, builder.build());

        assertResult(l, userIdField, UID1);
    }


    @Test(dataProvider = "metricsToTest")
    public void testFilterByName(Metric metric, String userIdField) throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER, "u1@u.com");

        ListValueData l = getAsList(metric, builder.build());

        assertResult(l, userIdField, UID1);
    }

    @Test(dataProvider = "metricsToTest")
    public void testSeveralFilterByName(Metric metric, String userIdField) throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER, "u1@u.com OR anonymoususer_3");

        ListValueData l = getAsList(metric, builder.build());

        assertResult(l, userIdField, UID1, UID3);
    }

    @Test(dataProvider = "metricsToTest")
    public void testNegotiatedFilterByName(Metric metric, String userIdField) throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER, "~ u1@u.com");

        ListValueData l = getAsList(metric, builder.build());

        assertResult(l, userIdField, UID2, UID3, UID4);
    }


    @Test(dataProvider = "metricsToTest")
    public void testMixFiltersUseCase1(Metric metric, String userIdField) throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER, "u1@u.com OR u2@u.com OR anonymoususer_4");
        builder.put(MetricFilter.USER_ID, UID1 + " OR " + UID2);

        ListValueData l = getAsList(metric, builder.build());

        assertResult(l, userIdField, UID1, UID2);
    }

    @Test(dataProvider = "metricsToTest")
    public void testMixFiltersUseCase2(Metric metric, String userIdField) throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER, "u1@u.com OR u2@u.com OR anonymoususer_4");
        builder.put(MetricFilter.USER_ID, "~ " + UID1);

        ListValueData l = getAsList(metric, builder.build());

        assertResult(l, userIdField, UID2, UID4);
    }

    @Test(dataProvider = "metricsToTest")
    public void testMixFiltersUseCase3(Metric metric, String userIdField) throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER, "~ u2@u.com");
        builder.put(MetricFilter.USER_ID, "~ " + UID1);

        ListValueData l = getAsList(metric, builder.build());

        assertResult(l, userIdField, UID3, UID4);
    }

    @Test(dataProvider = "metricsToTest")
    public void testMixFiltersUseCase4(Metric metric, String userIdField) throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER, Parameters.USER_TYPES.ANY.toString());

        ListValueData l = getAsList(metric, builder.build());

        assertResult(l, userIdField, UID1, UID2, UID3, UID4);
    }

    @Test(dataProvider = "metricsToTest")
    public void testMixFiltersUseCase5(Metric metric, String userIdField) throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER, Parameters.USER_TYPES.REGISTERED.toString());

        ListValueData l = getAsList(metric, builder.build());

        assertResult(l, userIdField, UID1, UID2);
    }

    @Test(dataProvider = "metricsToTest")
    public void testMixFiltersUseCase6(Metric metric, String userIdField) throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER, Parameters.USER_TYPES.ANONYMOUS.toString());

        ListValueData l = getAsList(metric, builder.build());

        assertResult(l, userIdField, UID3, UID4);
    }

    @Test(dataProvider = "metricsToTest")
    public void testMixFiltersUseCase7(Metric metric, String userIdField) throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER, "u1@u.com");
        builder.put(MetricFilter.USER_ID, UID2);

        ListValueData l = getAsList(metric, builder.build());
        assertTrue(l.isEmpty());
    }

    @Test(dataProvider = "metricsToTest")
    public void testMixFiltersUseCase8(Metric metric, String wsIdField) throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER, "u1@u.com OR " + UID2);
        builder.put(MetricFilter.USER_ID, UID2);

        ListValueData l = getAsList(metric, builder.build());

        assertResult(l, wsIdField, UID2);
    }

    private void assertResult(ListValueData l, String userIdField, String... users) {
        Set<String> m = listToMap(l, userIdField).keySet();

        assertEquals(m.size(), users.length);
        assertEquals(m, new HashSet<>(Arrays.asList(users)));
    }

    @DataProvider(name = "metricsToTest")
    public static Object[][] metricsToTest() {
        return new Object[][]{{MetricFactory.getMetric(MetricType.USERS_ACTIVITY_LIST), AbstractMetric.USER},
                              {MetricFactory.getMetric(MetricType.USERS_PROFILES_LIST), AbstractMetric.ID}};
    }
}
