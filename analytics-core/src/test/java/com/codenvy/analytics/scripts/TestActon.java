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
import com.codenvy.analytics.metrics.MetricParameter;
import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestActon extends BaseTest {

    @Test
    public void testExecute() throws Exception {
        List<Event> events = new ArrayList<Event>();
        events.add(Event.Builder.createProjectCreatedEvent("user1", "ws1", "session", "project1", "type1")
                .withDate("2010-10-01").withTime("20:00:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user1", "ws1", "session", "project1", "type1")
                .withDate("2010-10-01").withTime("20:05:00").build());
        events.add(Event.Builder.createApplicationCreatedEvent("user1", "ws1", "session", "project1", "type1", "paas1")
                .withDate("2010-10-01").withTime("20:10:00").build());
        events.add(Event.Builder.createUserInviteEvent("user2", "ws1", "session", "email")
                .withDate("2010-10-02").withTime("20:00:00").build());
        events.add(Event.Builder.createUserInviteEvent("user2", "ws1", "session", "email")
                .withDate("2010-10-02").withTime("20:05:00").build());
        events.add(Event.Builder.createProjectDeployedEvent("user3", "ws4", "session", "project4", "type2", "local")
                .withDate("2010-10-02").build());

        File log = LogGenerator.generateLog(events);

        HashMap<String,String> params = new HashMap<String, String>();
        params.put(MetricParameter.RESULT_DIR.name(), BASE_DIR);
        params.put(MetricParameter.FROM_DATE.name(), "20101001");
        params.put(MetricParameter.TO_DATE.name(), "20101002");

        ListListStringValueData valueData = (ListListStringValueData) executeAndReturnResult(ScriptType.ACTON, log, params);
        List<ListStringValueData> all = valueData.getAll();

        assertEquals(all.size(), 3);
        assertTrue(all.contains(new ListStringValueData(Arrays.asList("user1", "1", "2", "1", "10"))));
        assertTrue(all.contains(new ListStringValueData(Arrays.asList("user2", "0", "0", "0", "5"))));
        assertTrue(all.contains(new ListStringValueData(Arrays.asList("user3", "0", "1", "1", "0"))));
    }
}
