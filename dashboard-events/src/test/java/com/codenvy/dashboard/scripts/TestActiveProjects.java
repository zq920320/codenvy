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
public class TestActiveProjects extends BasePigTest
{
    @Test
    public void testEventFound() throws Exception
    {
        List<Event> events = new ArrayList<Event>();

        // 2 active projects
        events.add(Event.Builder.createProjectCreatedEvent("user2", "ws2", "session", "project1").withDate("2010-10-02")
                                .build());
        events.add(Event.Builder.createProjectCreatedEvent("user2", "ws2", "session", "project2").withDate("2010-10-02")
                                .build());

        // project already mentioned
        events.add(Event.Builder.createProjectCreatedEvent("user3", "ws2", "session", "project1").withDate("2010-10-02")
                                .build());

        // events should be ignored
        events.add(Event.Builder.createProjectDestroyedEvent("user2", "ws2", "session", "project3")
                                .withDate("2010-10-02").build());

        File log = LogGenerator.generateLog(events);

        Map<String, String> params = new HashMap<String, String>();
        params.put(ScriptParameters.FROM_DATE.getName(), "20101002");
        params.put(ScriptParameters.TO_DATE.getName(), "20101002");

        executePigScript(ScriptType.ACTIVE_PROJECTS, log, params);

        FileObject fileObject = ScriptType.ACTIVE_PROJECTS.createFileObject(BASE_DIR, params);

        Assert.assertEquals(fileObject.getValue(), 2L);
    }
}
