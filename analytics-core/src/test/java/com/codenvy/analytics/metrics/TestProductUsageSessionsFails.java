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
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsList;
import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsLong;
import static org.testng.Assert.assertEquals;

/** @author Anatoliy Bazko */
public class TestProductUsageSessionsFails extends BaseTest {

    @BeforeClass
    public void setUp() throws Exception {
        prepareData();
    }

    @Test
    public void testNumberOfFailedSessions() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.PRODUCT_USAGE_SESSIONS_FAILS);
        LongValueData l = getAsLong(metric, Context.EMPTY);
        assertEquals(l.getAsLong(), 3L);
    }

    @Test
    public void testFailedSessionsList() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.PRODUCT_USAGE_SESSIONS_FAILS_LIST);
        ListValueData l = getAsList(metric, Context.EMPTY);
        assertEquals(l.size(), 3L);
    }

    @Test
    public void testExpendedValues() throws Exception {
        ReadBasedExpandable metric = (ReadBasedExpandable)MetricFactory.getMetric(MetricType.PRODUCT_USAGE_SESSIONS_FAILS);
        ListValueData l = (ListValueData)metric.getExpandedValue(Context.EMPTY);
        assertEquals(l.size(), 3L);
    }

    @Test
    public void testNumberOfSessions() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.PRODUCT_USAGE_SESSIONS);
        LongValueData l = getAsLong(metric, Context.EMPTY);
        assertEquals(l.getAsLong(), 0L);
    }

    private void prepareData() throws Exception {
        addRegisteredUser(UID1, "user1@gmail.com");
        addPersistentWs(WID1, "ws1");
        addTemporaryWs(TWID1, "tmpws1");
        addTemporaryWs(TWID4, "tmpws4");

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.LOG, initLog().getAbsolutePath());
        builder.put(Parameters.FROM_DATE, "20140101");
        builder.put(Parameters.TO_DATE, "20140101");

        builder.putAll(scriptsManager.getScript(ScriptType.ACCEPTED_FACTORIES, MetricType.FACTORIES_ACCEPTED_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.ACCEPTED_FACTORIES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_SESSIONS, MetricType.PRODUCT_USAGE_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, builder.build());

        builder.putAll(
                scriptsManager.getScript(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, builder.build());
    }


    private File initLog() throws Exception {
        List<Event> events = new ArrayList<>();

        events.add(Event.Builder.createWorkspaceCreatedEvent(TWID1, "tmpws1", "user1@gmail.com").withDate("2014-01-01").build());
        events.add(
                Event.Builder.createFactoryUrlAcceptedEvent("tmpws1", "factory", "referrer", "orgId", "affiliateId").withDate("2014-01-01").build());

        events.add(Event.Builder.createSessionUsageEvent("user1@gmail.com", "tmpws2", "1", true).withDate("2014-01-01").withTime("12:00:00").build());
        events.add(Event.Builder.createSessionUsageEvent("user1@gmail.com", "ws1", "2", false).withDate("2014-01-01").withTime("10:00:00").build());

        events.add(
                Event.Builder.createFactoryUrlAcceptedEvent("tmpws3", "factory", "referrer", "orgId", "affiliateId").withDate("2014-01-01").build());

        return LogGenerator.generateLog(events);
    }

}
