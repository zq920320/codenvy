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
public class TestScriptDetails extends BasePigTest {

    @Test
    public void testScriptDetailsUserAddedToWs() throws Exception {
        List<Event> events = new ArrayList<Event>();
        events.add(Event.Builder.createUserAddedToWsEvent("user1", "ws1", "session", "ws1", "user1", "website")
                        .withDate("2010-10-01").build());
        events.add(Event.Builder.createUserAddedToWsEvent("user2", "ws1", "session", "ws1", "user2", "website")
                        .withDate("2010-10-01").build());
        events.add(Event.Builder.createUserAddedToWsEvent("user3", "ws1", "session", "ws1", "user3", "invite")
                        .withDate("2010-10-01").build());

        File log = LogGenerator.generateLog(events);

        Map<String, String> params = new HashMap<String, String>();
        params.put(ScriptParameters.FROM_DATE.getName(), "20101001");
        params.put(ScriptParameters.TO_DATE.getName(), "20101001");

        Map<String, Long> value = (Map<String, Long>)executeAndReturnResult(ScriptType.DETAILS_USER_ADDED_TO_WS, log, params);

        Assert.assertEquals(value.get("website"), Long.valueOf(2));
        Assert.assertEquals(value.get("invite"), Long.valueOf(1));
    }

    @Test
    public void testScriptDetailsUserSsoLoggedIn() throws Exception {
        List<Event> events = new ArrayList<Event>();
        events.add(Event.Builder.createUserSSOLoggedInEvent("user1", "google").withDate("2010-10-01").build());
        events.add(Event.Builder.createUserSSOLoggedInEvent("user1", "github").withDate("2010-10-01").build());
        events.add(Event.Builder.createUserSSOLoggedInEvent("user2", "google").withDate("2010-10-01").build());
        events.add(Event.Builder.createUserSSOLoggedInEvent("user3", "jaas").withDate("2010-10-01").build());

        File log = LogGenerator.generateLog(events);

        Map<String, String> params = new HashMap<String, String>();
        params.put(ScriptParameters.FROM_DATE.getName(), "20101001");
        params.put(ScriptParameters.TO_DATE.getName(), "20101001");

        Map<String, Long> value = (Map<String, Long>)executeAndReturnResult(ScriptType.DETAILS_USER_SSO_LOGGED_IN, log, params);
        Assert.assertEquals(value.get("google"), Long.valueOf(2));
        Assert.assertEquals(value.get("github"), Long.valueOf(1));
        Assert.assertEquals(value.get("jaas"), Long.valueOf(1));
    }

    @Test
    public void testScriptDetailsProjectCreatedTypes() throws Exception {
        List<Event> events = new ArrayList<Event>();
        events.add(Event.Builder.createProjectCreatedEvent("user", "ws", "session", "project1")
                        .withParam("TYPE", "type1").withDate("2010-10-01").build());
        events.add(Event.Builder.createProjectCreatedEvent("user", "ws", "session", "project2")
                        .withParam("TYPE", "type1").withDate("2010-10-01").build());
        events.add(Event.Builder.createProjectCreatedEvent("user", "ws", "session", "project").withParam("TYPE", "type2")
                        .withDate("2010-10-01").build());
        events.add(Event.Builder.createProjectCreatedEvent("user", "ws", "session", "project").withParam("TYPE", "type3")
                        .withDate("2010-10-01").build());

        File log = LogGenerator.generateLog(events);

        Map<String, String> params = new HashMap<String, String>();
        params.put(ScriptParameters.FROM_DATE.getName(), "20101001");
        params.put(ScriptParameters.TO_DATE.getName(), "20101001");

        Map<String, Long> value = (Map<String, Long>)executeAndReturnResult(ScriptType.DETAILS_PROJECT_CREATED_TYPES, log, params);
        Assert.assertEquals(value.get("type1"), Long.valueOf(2));
        Assert.assertEquals(value.get("type2"), Long.valueOf(1));
        Assert.assertEquals(value.get("type3"), Long.valueOf(1));
    }

    @Test
    public void testScriptDetailsApplicationCreatedPaas() throws Exception {
        List<Event> events = new ArrayList<Event>();
        events.add(Event.Builder.createApplicationCreatedEvent("user", "ws", "session", "project1", "type", "paas1")
                        .withDate("2010-10-01").build());
        events.add(Event.Builder.createApplicationCreatedEvent("user", "ws", "session", "project2", "type", "paas3")
                        .withDate("2010-10-01").build());
        events.add(Event.Builder.createApplicationCreatedEvent("user", "ws", "session", "project3", "type", "paas3")
                        .withDate("2010-10-01").build());
        events.add(Event.Builder.createApplicationCreatedEvent("user", "ws", "session", "project4", "type", "paas3")
                        .withDate("2010-10-01").build());
        events.add(Event.Builder.createProjectDeployedEvent("user", "ws", "session", "project4", "type", "local")
                        .withDate("2010-10-01").build());


        File log = LogGenerator.generateLog(events);

        Map<String, String> params = new HashMap<String, String>();
        params.put(ScriptParameters.FROM_DATE.getName(), "20101001");
        params.put(ScriptParameters.TO_DATE.getName(), "20101001");

        Map<String, Long> value = (Map<String, Long>)executeAndReturnResult(ScriptType.DETAILS_APPLICATION_CREATED_PAAS, log, params);
        Assert.assertEquals(value.get("paas1"), Long.valueOf(1));
        Assert.assertEquals(value.get("paas3"), Long.valueOf(3));
        Assert.assertEquals(value.get("local"), Long.valueOf(1));
    }
}
