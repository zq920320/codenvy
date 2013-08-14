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
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.scripts.executor.pig.PigScriptExecutor;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestScriptEntityByFactoryUrl extends BaseTest {

    @Test
    public void testExecute() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(
                Event.Builder.createFactoryCreatedEvent("ws1", "user1", "project1", "type1", "repoUrl1", "factoryUrl1")
                     .withDate("2010-10-01").build());
        events.add(
                Event.Builder.createFactoryCreatedEvent("ws2", "user1", "project2", "type1", "repoUrl1", "factoryUrl1")
                     .withDate("2010-10-01").build());
        events.add(
                Event.Builder.createFactoryCreatedEvent("ws3", "user2", "project3", "type1", "repoUrl1", "factoryUrl2")
                     .withDate("2010-10-01").build());
        events.add(
                Event.Builder.createFactoryCreatedEvent("ws4", "user2", "project4", "type2", "repoUrl1", "factoryUrl2")
                     .withDate("2010-10-01").build());
        events.add(
                Event.Builder.createFactoryCreatedEvent("ws5", "user2", "project5", "type2", "repoUrl3", "factoryUrl3")
                     .withDate("2010-10-01").build());
        File log = LogGenerator.generateLog(events);

        Map<String, String> context = Utils.newContext();
        context.put(MetricParameter.FROM_DATE.name(), "20101001");
        context.put(MetricParameter.TO_DATE.name(), "20101001");
        context.put(PigScriptExecutor.LOG, log.getAbsolutePath());
        Utils.putLoadDir(context, MetricType.FACTORY_CREATED);
        Utils.putStoreDir(context, MetricType.FACTORY_CREATED);

        DataProcessing.calculateAndStore(MetricType.FACTORY_CREATED, context);

        context.put(MetricParameter.FIELD.name(), "ws");
        context.put(MetricParameter.PARAM.name(), "ws1");

        Utils.prepareDirectories(MetricType.FACTORY_CREATED);
        ListStringValueData valueData =
                (ListStringValueData)executeAndReturnResult(ScriptType.ENTITY_BY_FACTORYURL, log, context);

        assertEquals(valueData.size(), 1);
        assertTrue(valueData.getAll().contains("factoryUrl1"));
    }
}
