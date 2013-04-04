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

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestScriptProductUsageTime extends BasePigTest {

    @Test
    public void testEventFound() throws Exception {
        List<Event> events = new ArrayList<Event>();

        // 7 min
        events.add(Event.Builder.createUserSSOLoggedInEvent("user1", "google").withDate("2010-10-01")
                        .withTime("20:00:00").build());
        events.add(Event.Builder.createUserSSOLoggedInEvent("user1", "google").withDate("2010-10-01")
                        .withTime("20:05:00").build());
        events.add(Event.Builder.createUserSSOLoggedInEvent("user1", "google").withDate("2010-10-01")
                        .withTime("20:07:00").build());

        // 4 min
        events.add(Event.Builder.createUserSSOLoggedInEvent("user1", "google").withDate("2010-10-01")
                        .withTime("20:25:00").build());
        events.add(Event.Builder.createUserSSOLoggedInEvent("user1", "google").withDate("2010-10-01")
                        .withTime("20:29:00").build());

        File log = LogGenerator.generateLog(events);

        Map<String, String> params = new HashMap<String, String>();
        params.put(ScriptParameters.FROM_DATE.getName(), "20101001");
        params.put(ScriptParameters.TO_DATE.getName(), "20101003");
        params.put(ScriptParameters.INACTIVE_INTERVAL.getName(), "10");

        executePigScript(ScriptType.PRODUCT_USAGE_TIME, log, params);

        FileObject fileObject = ScriptType.PRODUCT_USAGE_TIME.createFileObject(BASE_DIR, params);
        Assert.assertEquals(fileObject.getValue(), 11L);
    }
}
