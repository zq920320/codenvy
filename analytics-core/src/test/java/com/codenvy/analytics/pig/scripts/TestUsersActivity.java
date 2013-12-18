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
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.UsersActivity;
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

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestUsersActivity extends BaseTest {

    private Map<String, String> params;

    @BeforeClass
    public void prepare() throws IOException {
        params = Utils.newContext();

        List<Event> events = new ArrayList<>();

        events.add(Event.Builder.createSessionStartedEvent("user1@gmail.com", "ws1", "ide", "1").withDate("2013-11-01")
                        .withTime("20:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user1@gmail.com", "ws1", "ide", "1").withDate("2013-11-01")
                        .withTime("20:05:00").build());

        File log = LogGenerator.generateLog(events);

        Parameters.FROM_DATE.put(params, "20131101");
        Parameters.TO_DATE.put(params, "20131101");
        Parameters.USER.put(params, Parameters.USER_TYPES.REGISTERED.name());
        Parameters.WS.put(params, Parameters.WS_TYPES.ANY.name());
        Parameters.STORAGE_TABLE.put(params, "testusersactivity");
        Parameters.LOG.put(params, log.getAbsolutePath());

        PigServer.execute(ScriptType.USERS_ACTIVITY, params);
    }

    @Test
    public void testActivity() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20131101");
        Parameters.TO_DATE.put(context, "20131101");

        Metric metric = new TestListValueResulted();
        ListValueData value = (ListValueData)metric.getValue(context);

        assertEquals(value.size(), 2);
    }

    public class TestListValueResulted extends UsersActivity {

        @Override
        public String getStorageTable() {
            return "testusersactivity";
        }

        @Override
        public String getDescription() {
            return null;
        }
    }
}
