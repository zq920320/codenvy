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
package com.codenvy.analytics.pig.scripts;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.AbstractTopSessions;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.ProductUsageFactorySessionsList;
import com.codenvy.analytics.pig.PigServer;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

/** @author <a href="mailto:dnochevnov@codenvy.com">Dmytro Nochevnov</a> */
public class TestTopMetrics extends BaseTest {

    private Map<String, String> params;

    @BeforeClass
    public void init() throws IOException {
        params = Utils.newContext();

        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createSessionFactoryStartedEvent("id1", "tmp-1", "user1", "true", "brType")
                        .withDate("2013-02-10").withTime("10:00:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id1", "tmp-1", "user1")
                        .withDate("2013-02-10").withTime("10:05:00").build());

        events.add(Event.Builder.createSessionFactoryStartedEvent("id2", "tmp-2", "user1", "true", "brType")
                        .withDate("2013-02-10").withTime("10:20:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id2", "tmp-2", "user1")
                        .withDate("2013-02-10").withTime("10:30:00").build());

        events.add(Event.Builder.createSessionFactoryStartedEvent("id3", "tmp-3", "anonymoususer_1", "false", "brType")
                        .withDate("2013-02-10").withTime("11:00:00").build());
        events.add(Event.Builder.createSessionFactoryStoppedEvent("id3", "tmp-3", "anonymoususer_1")
                        .withDate("2013-02-10").withTime("11:15:00").build());

        events.add(Event.Builder.createFactoryProjectImportedEvent("tmp-1", "user1", "project", "type")
                        .withDate("2013-02-10").withTime("10:05:00").build());

        events.add(
                Event.Builder.createFactoryUrlAcceptedEvent("tmp-1", "factoryUrl1", "referrer1", "org1", "affiliate1")
                     .withDate("2013-02-10").withTime("11:00:00").build());
        events.add(
                Event.Builder.createFactoryUrlAcceptedEvent("tmp-2", "factoryUrl1", "referrer2", "org2", "affiliate1")
                     .withDate("2013-02-10").withTime("11:00:01").build());
        events.add(
                Event.Builder.createFactoryUrlAcceptedEvent("tmp-3", "factoryUrl1", "referrer3", "org3", "affiliate2")
                     .withDate("2013-02-10").withTime("11:00:02").build());

        events.add(Event.Builder.createTenantCreatedEvent("tmp-1", "user1")
                        .withDate("2013-02-10").withTime("12:00:00").build());
        events.add(Event.Builder.createTenantCreatedEvent("tmp-2", "user1")
                        .withDate("2013-02-10").withTime("12:01:00").build());

        // run event for session #1
        events.add(Event.Builder.createRunStartedEvent("user1", "tmp-1", "project", "type")
                        .withDate("2013-02-10").withTime("10:03:00").build());

        File log = LogGenerator.generateLog(events);

        Parameters.FROM_DATE.put(params, "20130210");
        Parameters.TO_DATE.put(params, "20130210");
        Parameters.USER.put(params, Parameters.USER_TYPES.ANY.name());
        Parameters.WS.put(params, Parameters.WS_TYPES.ANY.name());
        Parameters.STORAGE_TABLE.put(params, "testproductusagefactorysessionslist_acceptedfactories");
        Parameters.LOG.put(params, log.getAbsolutePath());
        PigServer.execute(ScriptType.ACCEPTED_FACTORIES, params);

        Parameters.WS.put(params, Parameters.WS_TYPES.TEMPORARY.name());
        Parameters.STORAGE_TABLE.put(params, "testproductusagefactorysessionslist");
        PigServer.execute(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, params);
    }

    @Test
    public void testAbstractTopSessions() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130210");
        Parameters.TO_DATE.put(context, "20130210");

        AbstractTopSessions metric = new TestAbstractTopSessions(MetricType.TOP_FACTORY_SESSIONS_BY_LIFETIME, -1);

        ListValueData value = (ListValueData)metric.getValue(context);

        assertEquals(value.size(), 3);

        System.out.println("testAbstractTopSessions: " + value.getAsString());
        
        List<ValueData> all = value.getAll();       
        checkTopSessionDataItem((MapValueData)all.get(0), "900", "factoryUrl1", "referrer3", "0", "0");
        checkTopSessionDataItem((MapValueData)all.get(1), "600", "factoryUrl1", "referrer2", "0", "1");
        checkTopSessionDataItem((MapValueData)all.get(2), "300", "factoryUrl1", "referrer1", "1", "1");
    }

    private void checkTopSessionDataItem(MapValueData item, String time, String factory, String referrer, String convertedSession, String authenticatedSession) {
        assertEquals(item.getAll().get(ProductUsageFactorySessionsList.TIME).getAsString(), time);
        assertEquals(item.getAll().get(ProductUsageFactorySessionsList.FACTORY).getAsString(), factory);
        assertEquals(item.getAll().get(ProductUsageFactorySessionsList.REFERRER).getAsString(), referrer);
        assertEquals(item.getAll().get(ProductUsageFactorySessionsList.CONVERTED_SESSION).getAsString(), convertedSession);
        assertEquals(item.getAll().get(ProductUsageFactorySessionsList.AUTHENTICATED_SESSION).getAsString(), authenticatedSession);
    }
    
    private class TestAbstractTopSessions extends AbstractTopSessions {

        public TestAbstractTopSessions(MetricType factoryMetricType, int dayCount) {
            super(factoryMetricType, dayCount);
        }

        @Override
        public String getDescription() {
            return null;
        }
        
        
        @Override
        public String getStorageCollectionName() {
            return "testproductusagefactorysessionslist";
        }
    }
}
