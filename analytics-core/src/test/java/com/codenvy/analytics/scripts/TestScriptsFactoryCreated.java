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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.DataProcessing;
import com.codenvy.analytics.metrics.MetricParameter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.metrics.value.FSValueDataManager;
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.MapStringLongValueData;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
public class TestScriptsFactoryCreated  extends BaseTest {

    private Map<String, String> context;
    private File                log;

    @BeforeMethod
    public void setUp() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(
                Event.Builder.createFactoryCreatedEvent("ws1", "user1", "project1", "type1", "repoUrl1", "factoryUrl1")
                     .withDate("2013-01-01").build());
        events.add(
                Event.Builder.createFactoryCreatedEvent("ws2", "user1", "project2", "type1", "repoUrl1", "factoryUrl1")
                     .withDate("2013-01-01").build());
        events.add(
                Event.Builder.createFactoryCreatedEvent("ws3", "user2", "project3", "type1", "repoUrl1", "factoryUrl2")
                     .withDate("2013-01-01").build());
        events.add(
                Event.Builder.createFactoryCreatedEvent("ws4", "user2", "project4", "type2", "repoUrl1", "factoryUrl2")
                     .withDate("2013-01-01").build());
        events.add(
                Event.Builder.createFactoryCreatedEvent("ws5", "user2", "project5", "type2", "repoUrl3", "factoryUrl3")
                     .withDate("2013-01-01").build());
        log = LogGenerator.generateLog(events);

        context = Utils.newContext();
        context.put(MetricParameter.FROM_DATE.name(), "20130101");
        context.put(MetricParameter.TO_DATE.name(), "20130101");
    }
    
    @Test
    public void testScriptFactoryCreated() throws Exception {
         
        MetricParameter.LOG.put(context, log.getAbsolutePath());
        MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());
        MetricParameter.WS.put(context, MetricParameter.WS_TYPES.ANY.name());

        MetricParameter.LOAD_DIR.put(context, Utils.getLoadDirFor(MetricType.FACTORY_CREATED));
        MetricParameter.STORE_DIR.put(context, Utils.getStoreDirFor(MetricType.FACTORY_CREATED));

        DataProcessing.calculateAndStore(MetricType.FACTORY_CREATED, context);

        LongValueData result = (LongValueData) executeAndReturnResult(ScriptType.FACTORY_CREATED, log, context);

        assertEquals(result.getAsLong(),5);
    }
    
    @Test
    public void testScriptFactoryCreatedByUrl() throws Exception {
        MetricParameter.LOG.put(context, log.getAbsolutePath());
        MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());
        MetricParameter.WS.put(context, MetricParameter.WS_TYPES.ANY.name());

        MapStringLongValueData result = (MapStringLongValueData) executeAndReturnResult(ScriptType.FACTORY_CREATED_BY_URL, log, context);

        assertEquals(result.size(), 3);
        assertEquals(result.getAll().get("factoryUrl1").longValue(), 2);
        assertEquals(result.getAll().get("factoryUrl2").longValue(), 2);
        assertEquals(result.getAll().get("factoryUrl3").longValue(), 1);        
    }
}
