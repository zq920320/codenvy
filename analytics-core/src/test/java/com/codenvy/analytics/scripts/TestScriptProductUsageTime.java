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
import com.codenvy.analytics.metrics.value.FSValueDataManager;
import com.codenvy.analytics.metrics.value.FixedListLongValueData;
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.MapStringFixedLongListValueData;
import com.codenvy.analytics.metrics.value.MapStringLongValueData;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;

import java.io.File;
import java.util.*;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestScriptProductUsageTime extends BaseTest {

    @Test
    public void testProductUsageTimeUsers() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createSessionStartedEvent("user1", "ws1", "ide", "1").withDate("2013-01-01")
                        .withTime("19:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user1", "ws1", "ide", "1").withDate("2013-01-01")
                        .withTime("19:06:00").build());

        events.add(Event.Builder.createSessionStartedEvent("user1", "ws1", "ide", "2").withDate("2013-01-01")
                        .withTime("20:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user1", "ws1", "ide", "2").withDate("2013-01-01")
                        .withTime("20:05:00").build());

        events.add(Event.Builder.createSessionStartedEvent("user2", "ws1", "ide", "3").withDate("2013-01-01")
                        .withTime("20:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user2", "ws1", "ide", "3").withDate("2013-01-01")
                        .withTime("20:05:00").build());

        File log = LogGenerator.generateLog(events);

        Map<String, String> params = new HashMap<>();
        params.put(MetricParameter.FROM_DATE.name(), "20130101");
        params.put(MetricParameter.TO_DATE.name(), "20130101");
        MetricParameter.USER.put(params, MetricParameter.USER_TYPES.ANY.name());
        MetricParameter.WS.put(params, MetricParameter.WS_TYPES.PERSISTENT.name());

        MapStringFixedLongListValueData value =
                (MapStringFixedLongListValueData)executeAndReturnResult(ScriptType.PRODUCT_USAGE_TIME_USERS, log, params);
        Map<String, FixedListLongValueData> all = value.getAll();
        assertEquals(all.size(), 2);
        assertEquals(all.get("user1"), new FixedListLongValueData(Arrays.asList(11L, 2L)));
        assertEquals(all.get("user2"), new FixedListLongValueData(Arrays.asList(5L, 1L)));
    }
    
    @Test
    public void testProductUsageTimeByDomains() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createSessionStartedEvent("user1@domain1.com", "ws1", "ide", "1").withDate("2013-01-01")
                        .withTime("19:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user1@domain1.com", "ws1", "ide", "1").withDate("2013-01-01")
                        .withTime("19:06:00").build());

        events.add(Event.Builder.createSessionStartedEvent("user1@domain1.com", "ws1", "ide", "2").withDate("2013-01-01")
                        .withTime("20:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user1@domain1.com", "ws1", "ide", "2").withDate("2013-01-01")
                        .withTime("20:05:00").build());

        events.add(Event.Builder.createSessionStartedEvent("user2@domain2.com", "ws1", "ide", "3").withDate("2013-01-01")
                        .withTime("20:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user2@domain2.com", "ws1", "ide", "3").withDate("2013-01-01")
                        .withTime("20:05:00").build());

        File log = LogGenerator.generateLog(events);

        Map<String, String> params = new HashMap<>();
        params.put(MetricParameter.FROM_DATE.name(), "20130101");
        params.put(MetricParameter.TO_DATE.name(), "20130101");
        MetricParameter.USER.put(params, MetricParameter.USER_TYPES.ANY.name());
        MetricParameter.WS.put(params, MetricParameter.WS_TYPES.PERSISTENT.name());

        MapStringFixedLongListValueData value =
                (MapStringFixedLongListValueData)executeAndReturnResult(ScriptType.PRODUCT_USAGE_TIME_DOMAINS, log, params);
        Map<String, FixedListLongValueData> all = value.getAll();
        assertEquals(all.size(), 2);
        assertEquals(all.get("domain1.com"), new FixedListLongValueData(Arrays.asList(11L, 2L)));
        assertEquals(all.get("domain2.com"), new FixedListLongValueData(Arrays.asList(5L, 1L)));
    }
    
    @Test
    public void testProductUsageTimeByCompanies() throws Exception {
        FileUtils.deleteDirectory(new File(FSValueDataManager.SCRIPT_LOAD_DIRECTORY));
        FileUtils.deleteDirectory(new File(FSValueDataManager.SCRIPT_STORE_DIRECTORY));
        
        // Add the profile for user1 and user2
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createUserUpdateProfile("user1", "f2", "l2", "company1", "11", "1")
                                .withDate("2013-01-01").build());
        events.add(Event.Builder.createUserUpdateProfile("user2", "f2", "l2", "company2", "11", "1")
                                .withDate("2013-01-01").build());
        File log = LogGenerator.generateLog(events);

        Map<String, String> params = new HashMap<>();
        MetricParameter.LOG.put(params, log.getAbsolutePath());
        String storage = Utils.getLoadDirFor(MetricType.USER_UPDATE_PROFILE);
        MetricParameter.LOAD_DIR.put(params, storage);
        MetricParameter.STORE_DIR.put(params, storage);
        params.put(MetricParameter.FROM_DATE.name(), "20130101");
        params.put(MetricParameter.TO_DATE.name(), "20130101");
        MetricParameter.USER.put(params, MetricParameter.USER_TYPES.ANY.name());
        MetricParameter.WS.put(params, MetricParameter.WS_TYPES.PERSISTENT.name());

        DataProcessing.calculateAndStore(MetricType.USER_UPDATE_PROFILE, params);

        // check script PRODUCT_USAGE_TIME_COMPANIES
        events = new ArrayList<>();
        events.add(Event.Builder.createSessionStartedEvent("user1", "ws1", "ide", "1").withDate("2013-01-01")
                                .withTime("19:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user1", "ws1", "ide", "1").withDate("2013-01-01")
                                .withTime("19:06:00").build());

        events.add(Event.Builder.createSessionStartedEvent("user1", "ws1", "ide", "2").withDate("2013-01-01")
                                .withTime("20:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user1", "ws1", "ide", "2").withDate("2013-01-01")
                                .withTime("20:05:00").build());

        events.add(Event.Builder.createSessionStartedEvent("user2", "ws1", "ide", "3").withDate("2013-01-01")
                                .withTime("20:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user2", "ws1", "ide", "3").withDate("2013-01-01")
                                .withTime("20:05:00").build());

        log = LogGenerator.generateLog(events);
        params = new HashMap<>();
        MetricParameter.LOG.put(params, log.getAbsolutePath());
        MetricParameter.LOAD_DIR.put(params, storage);
        params.put(MetricParameter.FROM_DATE.name(), "20130101");
        params.put(MetricParameter.TO_DATE.name(), "20130101");
        MetricParameter.USER.put(params, MetricParameter.USER_TYPES.ANY.name());
        MetricParameter.WS.put(params, MetricParameter.WS_TYPES.PERSISTENT.name());

        MapStringFixedLongListValueData value =
                                                (MapStringFixedLongListValueData)executeAndReturnResult(ScriptType.PRODUCT_USAGE_TIME_COMPANIES,
                                                                                                        log, params);
        Map<String, FixedListLongValueData> all = value.getAll();
        assertEquals(all.size(), 2);
        assertEquals(all.get("company1"), new FixedListLongValueData(Arrays.asList(11L, 2L)));
        assertEquals(all.get("company2"), new FixedListLongValueData(Arrays.asList(5L, 1L)));
    }
    
    @Test
    public void testProductUsageFactory() throws Exception {
        List<Event> events = new ArrayList<>();

        events.add(Event.Builder.createSessionFactoryStartedEvent("id1", "ws1", "user1", "true", "brType")
                        .withDate("2013-01-01").withTime("10:00:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id1", "ws1", "user1")
                        .withDate("2013-01-01").withTime("10:05:00").build());

        events.add(Event.Builder.createSessionFactoryStartedEvent("id2", "ws1", "user2", "true", "brType")
                        .withDate("2013-01-01").withTime("11:00:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id2", "ws1", "user2")
                        .withDate("2013-01-01").withTime("11:05:00").build());

        events.add(Event.Builder.createSessionFactoryStartedEvent("id22", "ws1", "user1", "true", "brType")
                        .withDate("2013-01-01").withTime("11:00:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id22", "ws1", "user1")
                        .withDate("2013-01-01").withTime("11:05:00").build());
        
        File log = LogGenerator.generateLog(events);

        Map<String, String> params = new HashMap<>();
        MetricParameter.LOG.put(params, log.getAbsolutePath());
        params.put(MetricParameter.FROM_DATE.name(), "20130101");
        params.put(MetricParameter.TO_DATE.name(), "20130101");
        MetricParameter.USER.put(params, MetricParameter.USER_TYPES.ANY.name());
        MetricParameter.WS.put(params, MetricParameter.WS_TYPES.PERSISTENT.name());
        
        LongValueData value =
                (LongValueData)executeAndReturnResult(ScriptType.PRODUCT_USAGE_TIME_FACTORY, log, params);
        assertEquals(value.getAsLong(), 900L);
    }
    
    @Test
    public void testProductUsageFactoryByWs() throws Exception {
        List<Event> events = new ArrayList<>();

        events.add(Event.Builder.createSessionFactoryStartedEvent("id1", "ws1", "user1", "true", "brType")
                        .withDate("2013-01-01").withTime("10:00:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id1", "ws1", "user1")
                        .withDate("2013-01-01").withTime("10:05:00").build());

        events.add(Event.Builder.createSessionFactoryStartedEvent("id2", "ws1", "user2", "true", "brType")
                        .withDate("2013-01-01").withTime("11:00:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id2", "ws1", "user2")
                        .withDate("2013-01-01").withTime("11:05:00").build());

        events.add(Event.Builder.createSessionFactoryStartedEvent("id22", "ws1", "user1", "true", "brType")
                        .withDate("2013-01-01").withTime("11:00:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id22", "ws1", "user1")
                        .withDate("2013-01-01").withTime("11:05:00").build());

        events.add(Event.Builder.createSessionFactoryStartedEvent("id32", "ws2", "user2", "true", "brType")
                        .withDate("2013-01-01").withTime("12:00:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id32", "ws2", "user2")
                        .withDate("2013-01-01").withTime("12:05:00").build());

        events.add(Event.Builder.createSessionFactoryStartedEvent("id42", "ws2", "user1", "true", "brType")
                        .withDate("2013-01-01").withTime("12:00:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id42", "ws2", "user1")
                        .withDate("2013-01-01").withTime("12:05:00").build());
        
        File log = LogGenerator.generateLog(events);

        Map<String, String> params = new HashMap<>();
        MetricParameter.LOG.put(params, log.getAbsolutePath());
        params.put(MetricParameter.FROM_DATE.name(), "20130101");
        params.put(MetricParameter.TO_DATE.name(), "20130101");
        MetricParameter.USER.put(params, MetricParameter.USER_TYPES.ANY.name());
        MetricParameter.WS.put(params, MetricParameter.WS_TYPES.PERSISTENT.name());
        
        MapStringLongValueData value =
                (MapStringLongValueData)executeAndReturnResult(ScriptType.PRODUCT_USAGE_TIME_FACTORY_BY_WS, log, params);
        assertEquals(value.size(), 2);
        assertTrue(value.getAll().containsKey("ws1"));
        assertTrue(value.getAll().containsKey("ws2"));
        
        assertEquals(value.getAll().get("ws1").longValue(), 900L);
        assertEquals(value.getAll().get("ws2").longValue(), 600L);
    }
}

