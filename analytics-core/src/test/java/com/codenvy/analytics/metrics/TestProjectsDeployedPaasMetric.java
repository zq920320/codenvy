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

package com.codenvy.analytics.metrics;

import static org.testng.Assert.assertEquals;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.value.DoubleValueData;
import com.codenvy.analytics.metrics.value.MapStringLongValueData;
import com.codenvy.analytics.scripts.executor.pig.PigScriptExecutor;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestProjectsDeployedPaasMetric extends BaseTest {

    @Test
    public void testScriptDetailsProjectCreatedTypes() throws Exception {
        List<Event> events = new ArrayList<Event>();
        events.add(Event.Builder.createApplicationCreatedEvent("user1", "ws1", "session", "project1", "type1", "paas1")
                                .withDate("2010-10-01").build());
        events.add(Event.Builder.createApplicationCreatedEvent("user1", "ws2", "session", "project2", "type1", "paas3")
                                .withDate("2010-10-01").build());
        events.add(Event.Builder.createApplicationCreatedEvent("user2", "ws3", "session", "project3", "type2", "paas3")
                                .withDate("2010-10-01").build());
        events.add(Event.Builder.createApplicationCreatedEvent("user3", "ws3", "session", "project4", "type2", "paas3")
                                .withDate("2010-10-01").build());
        events.add(Event.Builder.createProjectDeployedEvent("user3", "ws4", "session", "project4", "type2", "LOCAL")
                                .withDate("2010-10-01").build());

        File log = LogGenerator.generateLog(events);

        Map<String, String> context = Utils.newContext();
        context.put(MetricParameter.FROM_DATE.name(), "20101001");
        context.put(MetricParameter.TO_DATE.name(), "20101001");
        context.put(PigScriptExecutor.LOG, log.getParent());

        Metric metric = MetricFactory.createMetric(MetricType.PAAS_DEPLOYMENT_TYPES);
        
        MapStringLongValueData valueData = (MapStringLongValueData)metric.getValue(context);
        Map<String, Long> all = valueData.getAll();
        
        assertEquals(all.get("paas1"), Long.valueOf(1));
        assertEquals(all.get("paas3"), Long.valueOf(3));
        assertEquals(all.get("LOCAL"), Long.valueOf(1));

        metric = MetricFactory.createMetric(MetricType.PAAS_DEPLOYMENT_TYPE_LOCAL_NUMBER);
        assertEquals(metric.getValue(context), new DoubleValueData(1));
    }
}
