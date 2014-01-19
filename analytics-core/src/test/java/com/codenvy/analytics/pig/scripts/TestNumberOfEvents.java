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
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestNumberOfEvents extends BaseTest {

    private Map<String, String> params;

    @BeforeClass
    public void init() throws Exception {
        params = Utils.newContext();

        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createTenantCreatedEvent("ws1", "user1@gmail.com")
                        .withDate("2013-01-01")
                        .withTime("10:00:00")
                        .build());
        events.add(Event.Builder.createTenantCreatedEvent("ws1", "user1@yahoo.com")
                        .withDate("2013-01-01")
                        .withTime("10:00:01")
                        .build());
        File log = LogGenerator.generateLog(events);

        Parameters.FROM_DATE.put(params, "20130101");
        Parameters.TO_DATE.put(params, "20130101");
        Parameters.USER.put(params, Parameters.USER_TYPES.REGISTERED.name());
        Parameters.WS.put(params, Parameters.WS_TYPES.PERSISTENT.name());
        Parameters.EVENT.put(params, "tenant-created");
        Parameters.STORAGE_TABLE.put(params, "testnumberofevents");
        Parameters.LOG.put(params, log.getAbsolutePath());

        pigServer.execute(ScriptType.EVENTS, params);

        events = new ArrayList<>();
        events.add(Event.Builder.createTenantCreatedEvent("ws2", "user1@gmail.com")
                        .withDate("2013-01-02")
                        .withTime("10:00:00")
                        .build());
        events.add(Event.Builder.createTenantCreatedEvent("ws2", "user1@yahoo.com")
                        .withDate("2013-01-02")
                        .withTime("10:00:01")
                        .build());
        log = LogGenerator.generateLog(events);

        Parameters.FROM_DATE.put(params, "20130102");
        Parameters.TO_DATE.put(params, "20130102");
        Parameters.LOG.put(params, log.getAbsolutePath());

        pigServer.execute(ScriptType.EVENTS, params);
    }

    @Test
    public void testExecute() throws Exception {
        pigServer.executeAndReturn(ScriptType.EVENTS, params);
    }
}
