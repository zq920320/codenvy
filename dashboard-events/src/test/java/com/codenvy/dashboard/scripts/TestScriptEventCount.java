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
package com.codenvy.dashboard.scripts;

import com.codenvy.dashboard.scripts.util.Event;
import com.codenvy.dashboard.scripts.util.LogGenerator;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestScriptEventCount extends BasePigTest
{
    @Test
    public void testEventCountTenantCreated() throws Exception
    {
        List<Event> events = new ArrayList<Event>();
        events.add(Event.Builder.createTenantCreatedEvent("ws1", "user1").withDate("2010-10-01").build());
        events.add(Event.Builder.createTenantCreatedEvent("ws2", "user2").withDate("2010-10-01").build());
        events.add(Event.Builder.createTenantCreatedEvent("ws3", "user2").withDate("2010-10-02").build());
        File log = LogGenerator.generateLog(events);

        Map<String, String> params = new HashMap<String, String>();
        params.put(ScriptParameters.FROM_DATE.getName(), "20101001");
        params.put(ScriptParameters.TO_DATE.getName(), "20101001");

        executePigScript(ScriptType.EVENT_COUNT_WORKSPACE_CREATED, log, params);

        FileObject fileObject = ScriptType.EVENT_COUNT_WORKSPACE_CREATED.createFileObject(BASE_DIR, params);

        Assert.assertEquals(fileObject.getValue(), 2L);
    }

    @Test
    public void testEventCountTenantDestroyed() throws Exception
    {
        List<Event> events = new ArrayList<Event>();
        events.add(Event.Builder.createTenantDestroyedEvent("ws1").withDate("2010-10-01").build());
        events.add(Event.Builder.createTenantDestroyedEvent("ws2").withDate("2010-10-01").build());
        File log = LogGenerator.generateLog(events);

        Map<String, String> params = new HashMap<String, String>();
        params.put(ScriptParameters.FROM_DATE.getName(), "20101001");
        params.put(ScriptParameters.TO_DATE.getName(), "20101001");

        executePigScript(ScriptType.EVENT_COUNT_WORKSPACE_DESTROYED, log, params);

        FileObject fileObject = ScriptType.EVENT_COUNT_WORKSPACE_DESTROYED.createFileObject(BASE_DIR, params);

        Assert.assertEquals(fileObject.getValue(), 2L);
    }

    @Test
    public void testEventCountUserCreated() throws Exception
    {
        List<Event> events = new ArrayList<Event>();
        events.add(Event.Builder.createUserCreatedEvent("user1", "user@user1").withDate("2010-10-01").build());
        events.add(Event.Builder.createUserCreatedEvent("user2", "user@user2").withDate("2010-10-01").build());
        events.add(Event.Builder.createUserCreatedEvent("user3", "user@user3").withDate("2010-10-01").build());
        File log = LogGenerator.generateLog(events);

        Map<String, String> params = new HashMap<String, String>();
        params.put(ScriptParameters.FROM_DATE.getName(), "20101001");
        params.put(ScriptParameters.TO_DATE.getName(), "20101001");

        executePigScript(ScriptType.EVENT_COUNT_USER_CREATED, log, params);

        FileObject fileObject = ScriptType.EVENT_COUNT_USER_CREATED.createFileObject(BASE_DIR, params);

        Assert.assertEquals(fileObject.getValue(), 3L);
    }

    @Test
    public void testEventCountUserRemoved() throws Exception
    {
        List<Event> events = new ArrayList<Event>();
        events.add(Event.Builder.createUserRemovedEvent("user1").withDate("2010-10-01").build());
        File log = LogGenerator.generateLog(events);

        Map<String, String> params = new HashMap<String, String>();
        params.put(ScriptParameters.FROM_DATE.getName(), "20101001");
        params.put(ScriptParameters.TO_DATE.getName(), "20101001");

        executePigScript(ScriptType.EVENT_COUNT_USER_REMOVED, log, params);

        FileObject fileObject = ScriptType.EVENT_COUNT_USER_REMOVED.createFileObject(BASE_DIR, params);

        Assert.assertEquals(fileObject.getValue(), 1L);
    }

    @Test
    public void testEventCountProjectCreated() throws Exception
    {
        List<Event> events = new ArrayList<Event>();
        events.add(Event.Builder.createProjectCreatedEvent("user", "ws", "session", "project").withDate("2010-10-01")
                                .build());
        File log = LogGenerator.generateLog(events);

        Map<String, String> params = new HashMap<String, String>();
        params.put(ScriptParameters.FROM_DATE.getName(), "20101001");
        params.put(ScriptParameters.TO_DATE.getName(), "20101001");

        executePigScript(ScriptType.EVENT_COUNT_PROJECT_CREATED, log, params);

        FileObject fileObject = ScriptType.EVENT_COUNT_PROJECT_CREATED.createFileObject(BASE_DIR, params);

        Assert.assertEquals(fileObject.getValue(), 1L);
    }

    @Test
    public void testEventCountProjectDestroyed() throws Exception
    {
        List<Event> events = new ArrayList<Event>();
        events.add(Event.Builder.createProjectDestroyedEvent("user", "ws", "session", "project").withDate("2010-10-01")
                                .build());
        File log = LogGenerator.generateLog(events);

        Map<String, String> params = new HashMap<String, String>();
        params.put(ScriptParameters.FROM_DATE.getName(), "20101001");
        params.put(ScriptParameters.TO_DATE.getName(), "20101001");

        executePigScript(ScriptType.EVENT_COUNT_PROJECT_DESTROYED, log, params);

        FileObject fileObject = ScriptType.EVENT_COUNT_PROJECT_DESTROYED.createFileObject(BASE_DIR, params);

        Assert.assertEquals(fileObject.getValue(), 1L);
    }

    @Test
    public void testEventCountProjectBuild() throws Exception
    {
        List<Event> events = new ArrayList<Event>();
        events.add(Event.Builder.createProjectBuiltEvent("user1", "ws1", "ses", "project1", "type")
                                .withDate("2010-10-01").build());
        events.add(Event.Builder.createApplicationCreatedEvent("user1", "ws1", "ses", "project2", "type", "paas")
                                .withDate("2010-10-01").build());
        events.add(Event.Builder.createProjectDeployedEvent("user1", "ws1", "ses", "project3", "type", "paas")
                                .withDate("2010-10-01").build());
        File log = LogGenerator.generateLog(events);

        Map<String, String> params = new HashMap<String, String>();
        params.put(ScriptParameters.FROM_DATE.getName(), "20101001");
        params.put(ScriptParameters.TO_DATE.getName(), "20101001");

        executePigScript(ScriptType.EVENT_COUNT_DIST_PROJECT_BUILD, log, params);

        FileObject fileObject = ScriptType.EVENT_COUNT_DIST_PROJECT_BUILD.createFileObject(BASE_DIR, params);

        Assert.assertEquals(fileObject.getValue(), 3L);
    }

    @Test
    public void testEventCountUserInvite() throws Exception
    {
        List<Event> events = new ArrayList<Event>();
        events.add(Event.Builder.createUserInviteEvent("user", "ws", "session", "email").withDate("2010-10-01").build());
        File log = LogGenerator.generateLog(events);

        Map<String, String> params = new HashMap<String, String>();
        params.put(ScriptParameters.FROM_DATE.getName(), "20101001");
        params.put(ScriptParameters.TO_DATE.getName(), "20101001");

        executePigScript(ScriptType.EVENT_COUNT_USER_INVITE, log, params);

        FileObject fileObject = ScriptType.EVENT_COUNT_USER_INVITE.createFileObject(BASE_DIR, params);

        Assert.assertEquals(fileObject.getValue(), 1L);
    }
}
