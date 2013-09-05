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

package com.codenvy.analytics.scripts;


import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.DataProcessing;
import com.codenvy.analytics.metrics.MetricParameter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.metrics.value.MapStringListListStringValueData;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestScriptFactorySessions extends BaseTest {

    private File                log;
    private Map<String, String> context;

    @BeforeClass
    public void prepare() throws Exception {
        List<Event> events = new ArrayList<>();

        events.add(Event.Builder.createSessionFactoryStartedEvent("id1", "tmp-1", "user1", "true", "brType")
                        .withDate("2013-02-10").withTime("10:00:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id1", "tmp-1", "user1")
                        .withDate("2013-02-10").withTime("10:05:00").build());

        events.add(Event.Builder.createSessionFactoryStartedEvent("id2", "tmp-1", "user1", "true", "brType")
                        .withDate("2013-02-10").withTime("10:20:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id2", "tmp-1", "user1")
                        .withDate("2013-02-10").withTime("10:30:00").build());

        events.add(Event.Builder.createSessionFactoryStartedEvent("id3", "tmp-1", "anonymoususer_1", "false", "brType")
                        .withDate("2013-02-10").withTime("11:00:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id3", "tmp-1", "anonymoususer_1")
                        .withDate("2013-02-10").withTime("11:15:00").build());

        events.add(Event.Builder.createFactoryProjectImportedEvent("tmp-1", "user1", "project", "type")
                        .withDate("2013-02-10").withTime("10:05:00").build());

        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-1", "factoryUrl1", "referrer1")
                        .withDate("2013-02-10").build());

        log = LogGenerator.generateLog(events);

        context = Utils.newContext();
        MetricParameter.TO_DATE.put(context, "20130210");
        MetricParameter.FROM_DATE.put(context, "20130210");
        MetricParameter.USER.put(context, MetricParameter.USER_TYPES.ANY.name());
        MetricParameter.WS.put(context, MetricParameter.WS_TYPES.TEMPORARY.name());
        MetricParameter.LOG.put(context, log.getAbsolutePath());

        DataProcessing.calculateAndStore(MetricType.FACTORY_URL_ACCEPTED, context);

        log = LogGenerator.generateLog(events);
    }

    @Test
    public void testSessionsList() throws Exception {
        MetricParameter.LOAD_DIR.put(context, Utils.getLoadDirFor(MetricType.FACTORY_URL_ACCEPTED));

        ListListStringValueData valueData =
                (ListListStringValueData)executeAndReturnResult(ScriptType.FACTORY_SESSIONS, log, context);

        assertEquals(valueData.size(), 3);
        assertTrue(valueData.getAll().contains(
                new ListStringValueData(
                        Arrays.asList(new String[]{"300", "factoryUrl1", "referrer1", "true", "true"}))));
        assertTrue(valueData.getAll().contains(
                new ListStringValueData(
                        Arrays.asList(new String[]{"600", "factoryUrl1", "referrer1", "true", "false"}))));
        assertTrue(valueData.getAll().contains(
                new ListStringValueData(
                        Arrays.asList(new String[]{"900", "factoryUrl1", "referrer1", "false", "false"}))));
    }

    @Test
    public void testSessionsListByWs() throws Exception {
        MetricParameter.LOAD_DIR.put(context, Utils.getLoadDirFor(MetricType.FACTORY_URL_ACCEPTED));

        MapStringListListStringValueData valueData =
                (MapStringListListStringValueData)executeAndReturnResult(ScriptType.FACTORY_SESSIONS_BY_WS, log,
                                                                         context);

        assertEquals(valueData.size(), 1);
        assertTrue(valueData.getAll().containsKey("tmp-1"));

        assertTrue(valueData.getAll().get("tmp-1").getAll().contains(
                new ListStringValueData(
                        Arrays.asList(new String[]{"300", "factoryUrl1", "referrer1", "true", "true"}))));
        assertTrue(valueData.getAll().get("tmp-1").getAll().contains(
                new ListStringValueData(
                        Arrays.asList(new String[]{"600", "factoryUrl1", "referrer1", "true", "false"}))));
        assertTrue(valueData.getAll().get("tmp-1").getAll().contains(
                new ListStringValueData(
                        Arrays.asList(new String[]{"900", "factoryUrl1", "referrer1", "false", "false"}))));
    }
}

