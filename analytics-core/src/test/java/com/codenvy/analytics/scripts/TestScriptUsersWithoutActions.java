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
public class TestScriptUsersWithoutActions extends BaseTest {

    /** Run script which find all events. */
    @Test
    public void testUsersWithoutProjects() throws Exception {
        List<Event> events = new ArrayList<Event>();
        events.add(Event.Builder.createUserCreatedEvent("user", "user1").withDate("2012-10-01").build());
        events.add(Event.Builder.createUserCreatedEvent("user", "user2").withDate("2012-10-01").build());
        events.add(Event.Builder.createUserCreatedEvent("user", "user3").withDate("2012-10-02").build());

        events.add(Event.Builder.createProjectCreatedEvent("user1", "ws", "session", "project", "type").withDate("2012-10-01")
                        .build());
        events.add(Event.Builder.createProjectCreatedEvent("user3", "ws", "session", "project", "type").withDate("2012-10-03")
                        .build());

        File log = LogGenerator.generateLog(events);

        Map<String, String> params = new HashMap<String, String>();
        putFromDate(params, "20121001");
        putToDate(params, "20121003");

        ListListStringValueData value = (ListListStringValueData)executeAndReturnResult(ScriptType.USERS_WITHOUT_PROJECTS, log, params);
        List<ListStringValueData> all = value.getAll();

        assertEquals(all.size(), 1);
        assertTrue(all.contains(new ListStringValueData(Arrays.asList("user2"))));
    }

    /** Run script which find all events. */
    @Test
    public void testUsersWithoutInvites() throws Exception {
        List<Event> events = new ArrayList<Event>();
        events.add(Event.Builder.createUserCreatedEvent("user", "user1").withDate("2012-10-01").build());
        events.add(Event.Builder.createUserCreatedEvent("user", "user2").withDate("2012-10-01").build());
        events.add(Event.Builder.createUserCreatedEvent("user", "user3").withDate("2012-10-02").build());

        events.add(Event.Builder.createUserInviteEvent("user1", "ws", "session", "user4").withDate("2012-10-01")
                        .build());
        events.add(Event.Builder.createUserInviteEvent("user2", "ws", "session", "user4").withDate("2012-10-01").build());

        File log = LogGenerator.generateLog(events);

        Map<String, String> params = new HashMap<String, String>();
        putFromDate(params, "20121001");
        putToDate(params, "20121003");

        ListListStringValueData value = (ListListStringValueData)executeAndReturnResult(ScriptType.USERS_WITHOUT_INVITES, log, params);
        List<ListStringValueData> all = value.getAll();

        assertEquals(all.size(), 1);
        assertTrue(all.contains(new ListStringValueData(Arrays.asList("user3"))));
    }

    /** Run script which find all events. */
    @Test
    public void testUsersWithoutBuilds() throws Exception {
        List<Event> events = new ArrayList<Event>();
        events.add(Event.Builder.createProjectCreatedEvent("user1", "ws", "session", "project", "type").withDate("2012-10-01")
                        .build());
        events.add(Event.Builder.createProjectCreatedEvent("user2", "ws", "session", "project", "type").withDate("2012-10-03")
                        .build());
        events.add(Event.Builder.createProjectCreatedEvent("user3", "ws", "session", "project", "type").withDate("2012-10-01")
                        .build());
        events.add(Event.Builder.createProjectCreatedEvent("user4", "ws", "session", "project", "type").withDate("2012-10-03")
                        .build());

        events.add(Event.Builder.createProjectBuiltEvent("user1", "ws", "session", "project", "type")
                        .withDate("2012-10-01").build());
        events.add(Event.Builder.createProjectDeployedEvent("user2", "ws", "session", "project", "type", "paas")
                        .withDate("2012-10-03").build());
        events.add(Event.Builder.createApplicationCreatedEvent("user3", "ws", "session", "project", "type", "paas")
                        .withDate("2012-10-01").build());

        File log = LogGenerator.generateLog(events);

        Map<String, String> params = new HashMap<String, String>();
        putFromDate(params, "20121001");
        putToDate(params, "20121003");

        ListListStringValueData value = (ListListStringValueData)executeAndReturnResult(ScriptType.USERS_WITHOUT_BUILDS, log, params);
        List<ListStringValueData> all = value.getAll();

        assertEquals(all.size(), 1);
        assertTrue(all.contains(new ListStringValueData(Arrays.asList("user4"))));
    }

    /** Run script which find all events. */
    @Test
    public void testUsersWithoutDeployes() throws Exception {
        List<Event> events = new ArrayList<Event>();
        events.add(Event.Builder.createProjectCreatedEvent("user2", "ws", "session", "project", "type").withDate("2012-10-03")
                                .build());
        events.add(Event.Builder.createProjectCreatedEvent("user3", "ws", "session", "project", "type").withDate("2012-10-01")
                                .build());
        events.add(Event.Builder.createProjectCreatedEvent("user4", "ws", "session", "project", "type").withDate("2012-10-03")
                                .build());
        events.add(Event.Builder.createProjectDeployedEvent("user2", "ws", "session", "project", "type", "paas").withDate("2012-10-03")
                                .build());
        events.add(Event.Builder.createApplicationCreatedEvent("user3", "ws", "session", "project", "type", "paas").withDate("2012-10-01")
                                .build());

        File log = LogGenerator.generateLog(events);

        Map<String, String> params = new HashMap<String, String>();
        putFromDate(params, "20121001");
        putToDate(params, "20121003");

        ListListStringValueData value = (ListListStringValueData)executeAndReturnResult(ScriptType.USERS_WITHOUT_DEPLOYES, log, params);
        List<ListStringValueData> all = value.getAll();

        assertEquals(all.size(), 1);
        assertTrue(all.contains(new ListStringValueData(Arrays.asList("user4"))));
    }
}
