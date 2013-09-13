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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestScriptProductUsageSessionsFactory extends BaseTest {

    private File                log;
    private Map<String, String> context;

    @BeforeClass
    public void prepare() throws Exception {
        List<Event> events = new ArrayList<>();

        events.add(Event.Builder.createSessionFactoryStartedEvent("id1", "tmp-1", "user", "true", "brType")
                        .withDate("2013-02-10").withTime("10:00:00").build());
        events.add(Event.Builder.createSessionFactoryStartedEvent("id2", "tmp-2", "user", "true", "brType")
                        .withDate("2013-02-10").withTime("10:00:00").build());

        events.add(Event.Builder.createSessionFactoryStoppedEvent("id1", "tmp-1", "user")
                        .withDate("2013-02-10").withTime("10:05:00").build());

        events.add(Event.Builder.createSessionFactoryStoppedEvent("id1", "tmp-1", "user")
                        .withDate("2013-02-10").withTime("10:10:00").build());

        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-1", "factory1", "ref1")
                        .withDate("2013-02-10").withTime("10:10:00").build());
        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-2", "factory1", "ref2")
                        .withDate("2013-02-10").withTime("10:10:00").build());

        log = LogGenerator.generateLog(events);

        context = new HashMap<>();
        MetricParameter.FROM_DATE.put(context, "20130210");
        MetricParameter.TO_DATE.put(context, "20130210");
        MetricParameter.USER.put(context, MetricParameter.USER_TYPES.ANY.name());
        MetricParameter.WS.put(context, MetricParameter.WS_TYPES.TEMPORARY.name());
        MetricParameter.LOAD_DIR.put(context, Utils.getLoadDirFor(MetricType.FACTORY_URL_ACCEPTED));
        MetricParameter.LOG.put(context, log.getAbsolutePath());

        DataProcessing.calculateAndStore(MetricType.FACTORY_URL_ACCEPTED, context);
    }

    @Test
    public void testEventFound() throws Exception {
        ListListStringValueData value =
                (ListListStringValueData)executeAndReturnResult(ScriptType.PRODUCT_USAGE_SESSIONS_FACTORY,
                                                                log,
                                                                context);
        List<ListStringValueData> all = value.getAll();

        assertEquals(all.size(), 1);

        long total = 0;
        for (ListStringValueData item : all) {
            total += Long.valueOf(item.getAll().get(3));
        }

        assertEquals(total, 5 * 60);
    }

    @Test
    public void testEventFoundByWs() throws Exception {
        MapStringListListStringValueData value =
                (MapStringListListStringValueData)executeAndReturnResult(
                        ScriptType.PRODUCT_USAGE_SESSIONS_FACTORY_BY_WS, log, context);

        assertEquals(value.size(), 1);
        assertTrue(value.getAll().containsKey("tmp-1"));

        List<String> list = value.getAll().get("tmp-1").getAll().get(0).getAll();
        assertEquals(list.get(0), "tmp-1");
        assertEquals(list.get(1), "user");
        assertTrue(list.get(2).contains("2013-02-10"));
        assertTrue(list.get(2).contains("10:00:00"));
        assertEquals(list.get(3), "300");
    }

    @Test
    public void testEventFoundByUrl() throws Exception {
        MapStringListListStringValueData value =
                (MapStringListListStringValueData)executeAndReturnResult(
                        ScriptType.PRODUCT_USAGE_SESSIONS_FACTORY_BY_REFERRER_URL, log, context);

        assertEquals(value.size(), 1);
        assertTrue(value.getAll().containsKey("ref1"));

        List<String> list = value.getAll().get("ref1").getAll().get(0).getAll();
        assertTrue(list.contains("tmp-1"));
        assertEquals(list.get(0), "tmp-1");
        assertTrue(list.contains("user"));
        assertEquals(list.get(1), "user");
        assertTrue(list.get(2).contains("2013-02-10"));
        assertTrue(list.get(2).contains("10:00:00"));
        assertTrue(list.contains("300"));
        assertEquals(list.get(3), "300");
    }
}

