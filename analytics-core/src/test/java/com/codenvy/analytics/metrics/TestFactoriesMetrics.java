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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mortbay.log.Log;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.metrics.sessions.factory.TotalFactories;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

/** @author <a href="mailto:dnochevnov@codenvy.com">Dmytro Nochevnov</a> */
public class TestFactoriesMetrics extends BaseTest {

    /** @see analytics.properties -> initial.value.metric.total_factories */
    private static final long INITIAL_VALUE_OF_TOTAL_FACTORIES = 40;
    
    @BeforeClass
    public void init() throws Exception {
        Map<String, String> params = Utils.newContext();

        List<Event> events = new ArrayList<>();
        events.add(Event.Builder
                   .createFactoryCreatedEvent("ws1", "user1", "project1", "type1", "repo1", "factory1",
                                              "", "").withDate("2013-02-10").withTime("13:00:00").build());

        events.add(Event.Builder
                   .createFactoryCreatedEvent("ws1", "user1", "project2", "type1", "repo2", "factory2",
                                              "", "").withDate("2013-02-11").withTime("13:00:00").build());


        File log = LogGenerator.generateLog(events);

        Parameters.USER.put(params, Parameters.USER_TYPES.ANY.name());
        Parameters.WS.put(params, Parameters.WS_TYPES.ANY.name());
        Parameters.STORAGE_TABLE.put(params, "created_factories_set");
        Parameters.LOG.put(params, log.getAbsolutePath());

        Parameters.FROM_DATE.put(params, "20130210");
        Parameters.TO_DATE.put(params, "20130210");        
        pigServer.execute(ScriptType.CREATED_FACTORIES, params);
        
        Parameters.FROM_DATE.put(params, "20130211");
        Parameters.TO_DATE.put(params, "20130211");        
        pigServer.execute(ScriptType.CREATED_FACTORIES, params);
    }

    @Test
    public void testTotalFactories() throws Exception {
        Map<String, String> context = Utils.newContext();
        TotalFactories metric = (TotalFactories) MetricFactory.getMetric(MetricType.TOTAL_FACTORIES);
        
        // total factories for one day             
        Parameters.TO_DATE.put(context, "20130210");        
        LongValueData value = (LongValueData) metric.getValue(context);
        assertEquals(value.getAsLong(),INITIAL_VALUE_OF_TOTAL_FACTORIES + 1);
        
        // total factories for two days
        Parameters.TO_DATE.put(context, "20130211");        
        value = (LongValueData)metric.getValue(context);
        assertEquals(value.getAsLong(), INITIAL_VALUE_OF_TOTAL_FACTORIES + 2);

    }
}