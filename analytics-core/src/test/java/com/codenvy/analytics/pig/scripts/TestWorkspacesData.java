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

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.WorkspacesStatistics;
import com.codenvy.analytics.pig.PigServer;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mongodb.util.MyAsserts.assertEquals;
import static org.testng.AssertJUnit.fail;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestWorkspacesData extends BaseTest {

    @BeforeClass
    public void prepare() throws IOException {
        Map<String, String> params = Utils.newContext();

        List<Event> events = new ArrayList<>();

        events.add(Event.Builder.createSessionStartedEvent("user1@gmail.com", "ws1", "ide", "1").withDate("2013-11-01")
                        .withTime("20:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user1@gmail.com", "ws1", "ide", "1").withDate("2013-11-01")
                        .withTime("20:05:00").build());
        events.add(Event.Builder.createSessionStartedEvent("user3@gmail.com", "ws2", "ide", "2").withDate("2013-11-01")
                        .withTime("20:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user3@gmail.com", "ws2", "ide", "2").withDate("2013-11-01")
                        .withTime("20:02:00").build());

        events.add(Event.Builder.createTenantCreatedEvent("ws2", "user1@gmail.com").withDate("2013-11-01")
                        .withTime("20:00:00").build());
        events.add(Event.Builder.createTenantCreatedEvent("ws3", "user3@gmail.com").withDate("2013-11-01")
                        .withTime("21:00:00").build());

        File log = LogGenerator.generateLog(events);

        Parameters.FROM_DATE.put(params, "20131101");
        Parameters.TO_DATE.put(params, "20131101");
        Parameters.USER.put(params, Parameters.USER_TYPES.ANY.name());
        Parameters.WS.put(params, Parameters.WS_TYPES.ANY.name());
        Parameters.STORAGE_TABLE.put(params, "testworkspacesdata-sessions");
        Parameters.STORAGE_TABLE_WORKSPACES_STATISTICS.put(params, "testworkspacesdata");
        Parameters.LOG.put(params, log.getAbsolutePath());
        PigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, params);

        Parameters.USER.put(params, Parameters.USER_TYPES.ANY.name());
        Parameters.WS.put(params, Parameters.WS_TYPES.PERSISTENT.name());
        Parameters.STORAGE_TABLE.put(params, "testworkspacesdata");
        PigServer.execute(ScriptType.WORKSPACES_STATISTICS, params);
    }

    @Test
    public void testStatistics() throws Exception {
        Map<String, String> context = Utils.newContext();

        Metric metric = new TestWorkspacesStatistics();
        ListValueData value = (ListValueData)metric.getValue(context);

        assertEquals(value.size(), 3);

        for (ValueData object : value.getAll()) {
            MapValueData valueData = (MapValueData)object;

            Map<String, ValueData> all = valueData.getAll();
            String wsName = all.get("_id").getAsString();

            switch (wsName) {
                case "ws1":
                    assertEquals(all.size(), 3);
                    assertEquals(all.get("time").getAsString(), "300");
                    assertEquals(all.get("sessions").getAsString(), "1");
                    break;

                case "ws2":
                    assertEquals(all.size(), 4);
                    assertEquals(all.get("time").getAsString(), "120");
                    assertEquals(all.get("sessions").getAsString(), "1");
                    assertEquals(all.get("creation_date").getAsString(), "2013-11-01 20:00:00");
                    break;

                case "ws3":
                    assertEquals(all.size(), 2);
                    assertEquals(all.get("creation_date").getAsString(), "2013-11-01 21:00:00");
                    break;

                default:
                    fail("unknown ws " + wsName);
                    break;
            }
        }
    }

    public class TestWorkspacesStatistics extends WorkspacesStatistics {

        @Override
        public String getStorageTable() {
            return "testworkspacesdata";
        }
    }
}
