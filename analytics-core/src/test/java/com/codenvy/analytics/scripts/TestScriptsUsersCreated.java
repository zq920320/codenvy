/*
 *    Copyright (C) 2013 Codenvy.
 *
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

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestScriptsUsersCreated extends BaseTest {

    @Test
    public void testExecute() throws Exception {
        List<Event> events = new ArrayList<Event>();
        events.add(Event.Builder.createUserCreatedEvent("user1", "user@user1").withDate("2010-10-01").build());
        events.add(Event.Builder.createUserCreatedEvent("user2", "user@user2").withDate("2010-10-01").build());
        events.add(Event.Builder.createUserCreatedEvent("user3", "user@user3").withDate("2010-10-01").build());
        File log = LogGenerator.generateLog(events);

        Map<String, String> params = new HashMap<String, String>();
        params.put(MetricParameter.FROM_DATE.name(), "20101001");
        params.put(MetricParameter.TO_DATE.name(), "20101001");

        ListListStringValueData value = (ListListStringValueData)executeAndReturnResult(ScriptType.USERS_CREATED, log, params);
        List<ListStringValueData> all = value.getAll();

        assertEquals(all.size(), 3);
        assertTrue(all.contains(new ListStringValueData(Arrays.asList("user@user1"))));
        assertTrue(all.contains(new ListStringValueData(Arrays.asList("user@user2"))));
        assertTrue(all.contains(new ListStringValueData(Arrays.asList("user@user3"))));
    }
}
