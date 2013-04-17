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

import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestScriptUsersWithoutActions extends BasePigTest {

    /** Run script which find all events. */
    @Test
    public void testUsersWithoutProjects() throws Exception {
        List<Event> events = new ArrayList<Event>();
        events.add(Event.Builder.createUserCreatedEvent("user", "user1").withDate("2012-10-01").build());
        events.add(Event.Builder.createUserCreatedEvent("user", "user2").withDate("2012-10-01").build());
        events.add(Event.Builder.createUserCreatedEvent("user", "user3").withDate("2012-10-02").build());

        events.add(Event.Builder.createProjectCreatedEvent("user1", "ws", "session", "project").withDate("2012-10-01")
                        .build());
        events.add(Event.Builder.createProjectCreatedEvent("user3", "ws", "session", "project").withDate("2012-10-03")
                        .build());

        File log = LogGenerator.generateLog(events);

        Map<String, String> params = new HashMap<String, String>();

        List<String> value = (List<String>)executeAndReturnResult(ScriptType.USERS_WITHOUT_PROJECTS, log, params);
        Assert.assertEquals(value.size(), 1);
        Assert.assertEquals(value.get(0), "user2");
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

        List<String> value = (List<String>)executeAndReturnResult(ScriptType.USERS_WITHOUT_INVITES, log, params);
        Assert.assertEquals(value.size(), 1);
        Assert.assertEquals(value.get(0), "user3");
    }

    /** Run script which find all events. */
    @Test
    public void testUsersWithoutBuilds() throws Exception {
        List<Event> events = new ArrayList<Event>();
        events.add(Event.Builder.createProjectCreatedEvent("user1", "ws", "session", "project").withDate("2012-10-01")
                        .build());
        events.add(Event.Builder.createProjectCreatedEvent("user2", "ws", "session", "project").withDate("2012-10-03")
                        .build());
        events.add(Event.Builder.createProjectCreatedEvent("user3", "ws", "session", "project").withDate("2012-10-01")
                        .build());
        events.add(Event.Builder.createProjectCreatedEvent("user4", "ws", "session", "project").withDate("2012-10-03")
                        .build());

        events.add(Event.Builder.createProjectBuiltEvent("user1", "ws", "session", "project", "type")
                        .withDate("2012-10-01").build());
        events.add(Event.Builder.createProjectDeployedEvent("user2", "ws", "session", "project", "type", "paas")
                        .withDate("2012-10-03").build());
        events.add(Event.Builder.createApplicationCreatedEvent("user3", "ws", "session", "project", "type", "paas")
                        .withDate("2012-10-01").build());

        File log = LogGenerator.generateLog(events);

        Map<String, String> params = new HashMap<String, String>();

        List<String> value = (List<String>)executeAndReturnResult(ScriptType.USERS_WITHOUT_BUILDS, log, params);
        Assert.assertEquals(value.size(), 1);
        Assert.assertEquals(value.get(0), "user4");
    }

    /** Run script which find all events. */
    // @Test
    public void testUsersWithoutDeployes() throws Exception {
        List<Event> events = new ArrayList<Event>();
        events.add(Event.Builder.createProjectCreatedEvent("user2", "ws", "session", "project").withDate("2012-10-03")
                        .build());
        events.add(Event.Builder.createProjectCreatedEvent("user3", "ws", "session", "project").withDate("2012-10-01")
                        .build());
        events.add(Event.Builder.createProjectCreatedEvent("user4", "ws", "session", "project").withDate("2012-10-03")
                        .build());

        events.add(Event.Builder.createProjectDeployedEvent("user2", "ws", "session", "project", "type", "paas")
                        .withDate("2010-10-03").build());
        events.add(Event.Builder.createApplicationCreatedEvent("user3", "ws", "session", "project", "type", "paas")
                        .withDate("2010-10-01").build());

        File log = LogGenerator.generateLog(events);

        Map<String, String> params = new HashMap<String, String>();

        List<String> value = (List<String>)executeAndReturnResult(ScriptType.USERS_WITHOUT_DEPLOYS, log, params);
        Assert.assertEquals(value.size(), 1);
        Assert.assertEquals(value.get(0), "user4");
    }
}
