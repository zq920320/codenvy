/*
 *    Copyright (C) 2013 eXo Platform SAS.
 *
 *    This is free software; you can redistribute it and/or modify it
 *    under the terms of the GNU Lesser General Public License as
 *    published by the Free Software Foundation; either version 2.1 of
 *    the License, or (at your option) any later version.
 *
 *    This software is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this software; if not, write to the Free
 *    Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *    02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.codenvy.analytics.scripts;


import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.MetricParameter;
import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestScriptEventCount extends BaseTest {
    @Test
    public void testEventCountTenantCreated() throws Exception {
        List<Event> events = new ArrayList<Event>();
        events.add(Event.Builder.createTenantCreatedEvent("ws1", "user1").withDate("2010-10-01").build());
        events.add(Event.Builder.createTenantCreatedEvent("ws2", "user2").withDate("2010-10-01").build());
        events.add(Event.Builder.createTenantCreatedEvent("ws3", "user2").withDate("2010-10-02").build());
        File log = LogGenerator.generateLog(events);

        Map<String, String> params = new HashMap<String, String>();
        params.put(MetricParameter.FROM_DATE.getName(), "20101001");
        params.put(MetricParameter.TO_DATE.getName(), "20101001");

        LongValueData value = (LongValueData)executeAndReturnResult(ScriptType.EVENT_COUNT_WORKSPACE_CREATED, log, params);
        Assert.assertEquals(value.getAsLong(), 2L);
    }

    @Test
    public void testEventCountTenantDestroyed() throws Exception {
        List<Event> events = new ArrayList<Event>();
        events.add(Event.Builder.createTenantDestroyedEvent("ws1").withDate("2010-10-01").build());
        events.add(Event.Builder.createTenantDestroyedEvent("ws2").withDate("2010-10-01").build());
        File log = LogGenerator.generateLog(events);

        Map<String, String> params = new HashMap<String, String>();
        params.put(MetricParameter.FROM_DATE.getName(), "20101001");
        params.put(MetricParameter.TO_DATE.getName(), "20101001");

        LongValueData value = (LongValueData)executeAndReturnResult(ScriptType.EVENT_COUNT_WORKSPACE_DESTROYED, log, params);
        Assert.assertEquals(value.getAsLong(), 2L);
    }


    @Test
    public void testEventCountProjectDestroyed() throws Exception {
        List<Event> events = new ArrayList<Event>();
        events.add(Event.Builder.createProjectDestroyedEvent("user", "ws", "session", "project", "type").withDate("2010-10-01")
                        .build());
        File log = LogGenerator.generateLog(events);

        Map<String, String> params = new HashMap<String, String>();
        params.put(MetricParameter.FROM_DATE.getName(), "20101001");
        params.put(MetricParameter.TO_DATE.getName(), "20101001");

        LongValueData value = (LongValueData)executeAndReturnResult(ScriptType.EVENT_COUNT_PROJECT_DESTROYED, log, params);
        Assert.assertEquals(value.getAsLong(), 1L);
    }

    @Test
    public void testEventCountUserInvite() throws Exception {
        List<Event> events = new ArrayList<Event>();
        events.add(Event.Builder.createUserInviteEvent("user", "ws", "session", "email").withDate("2010-10-01").build());
        File log = LogGenerator.generateLog(events);

        Map<String, String> params = new HashMap<String, String>();
        params.put(MetricParameter.FROM_DATE.getName(), "20101001");
        params.put(MetricParameter.TO_DATE.getName(), "20101001");

        ListListStringValueData value = (ListListStringValueData)executeAndReturnResult(ScriptType.USERS_INVITATIONS, log, params);

        assertEquals(value.size(), 1L);
        assertTrue(value.getAll().contains(new ListStringValueData(Arrays.asList("ws", "user", "email"))));
    }
}
