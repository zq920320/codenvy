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
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsList;
import static org.testng.Assert.assertEquals;

/**
 * @author Alexander Reshetnyak
 */
public class TestCreatedList extends BaseTest {

    @BeforeClass
    public void prepare() throws Exception {

        List<Event> events = new ArrayList<>();

        events.add(Event.Builder.createUserCreatedEvent(UID3, "user2@gmail.com", "user2@gmail.com").withDate("2013-01-01").build());
        events.add(Event.Builder.createUserCreatedEvent(UID4, "user3@gmail.com", "user3@gmail.com").withDate("2013-01-01").build());
        events.add(Event.Builder.createUserCreatedEvent(UID5, "user5@gmail.com", "user5@gmail.com").withDate("2013-01-01").build());
        events.add(Event.Builder.createUserCreatedEvent(AUID1, "anonymoususer_1", "anonymoususer_1").withDate("2013-01-01").build());

        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.USERS_PROFILES, MetricType.USERS_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.CREATED_USERS_SET).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());
    }

    @Test
    public void listOfAllUsers() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.CREATED_USERS_LIST);
        ListValueData lvd = getAsList(metric, Context.EMPTY);

        assertEquals(lvd.size(), 3);
    }

    @Test
    public void listOfUsersWithFilterByUsers() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_ID, UID3 + " OR " + UID5);

        Metric metric = MetricFactory.getMetric(MetricType.CREATED_USERS_LIST);
        ListValueData lvd = getAsList(metric, builder.build());

        assertEquals(lvd.size(), 2);
    }
}