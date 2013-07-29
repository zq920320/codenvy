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

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.MetricParameter;
import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestUsersSSOLoggedIn extends BaseTest {

    @Test
    public void testExecute() throws Exception {
        List<Event> events = new ArrayList<Event>();
        events.add(Event.Builder.createUserSSOLoggedInEvent("user1", "google").withDate("2010-10-01").build());
        events.add(Event.Builder.createUserSSOLoggedInEvent("user1", "github").withDate("2010-10-01").build());
        events.add(Event.Builder.createUserSSOLoggedInEvent("user2", "google").withDate("2010-10-01").build());
        events.add(Event.Builder.createUserSSOLoggedInEvent("user3", "jaas").withDate("2010-10-01").build());

        File log = LogGenerator.generateLog(events);

        Map<String, String> params = new HashMap<String, String>();
        params.put(MetricParameter.FROM_DATE.name(), "20101001");
        params.put(MetricParameter.TO_DATE.name(), "20101001");

        ListListStringValueData value = (ListListStringValueData)executeAndReturnResult(ScriptType.USERS_SSO_LOGGED_IN, log, params);
        List<ListStringValueData> all = value.getAll();

        assertEquals(all.size(), 4);
        assertTrue(all.contains(new ListStringValueData(Arrays.asList("user1", "google"))));
        assertTrue(all.contains(new ListStringValueData(Arrays.asList("user1", "github"))));
        assertTrue(all.contains(new ListStringValueData(Arrays.asList("user2", "google"))));
        assertTrue(all.contains(new ListStringValueData(Arrays.asList("user3", "jaas"))));
    }
}
