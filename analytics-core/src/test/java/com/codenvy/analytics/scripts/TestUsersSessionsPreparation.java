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

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.MetricParameter;
import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.MapStringListListStringValueData;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestUsersSessionsPreparation extends BaseTest {

    @Test
    public void testExecute() throws Exception {
        List<Event> events = new ArrayList<Event>();

        // 7 min, user1 session #1
        events.add(Event.Builder.createProjectBuiltEvent("user1", "ws1", "", "", "").withDate("2010-10-01")
                                .withTime("20:00:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user1", "ws1", "", "", "").withDate("2010-10-01")
                                .withTime("20:05:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user1", "ws1", "", "", "").withDate("2010-10-01")
                                .withTime("20:07:00").build());

        // 4 min, user2 session #1
        events.add(Event.Builder.createProjectBuiltEvent("user2", "ws1", "", "", "").withDate("2010-10-01")
                                .withTime("20:25:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user2", "ws1", "", "", "").withDate("2010-10-01")
                                .withTime("20:29:00").build());

        // 7 min, user1 session #2
        events.add(Event.Builder.createProjectBuiltEvent("user1", "ws1", "", "", "").withDate("2010-10-01")
                                .withTime("21:00:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user1", "ws1", "", "", "").withDate("2010-10-01")
                                .withTime("21:05:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user1", "ws1", "", "", "").withDate("2010-10-01")
                                .withTime("21:07:00").build());

        // 0 min, user3 session #1
        events.add(Event.Builder.createProjectBuiltEvent("user3", "ws1", "", "", "").withDate("2010-10-01")
                                .withTime("20:25:00").build());

        File log = LogGenerator.generateLog(events);

        Map<String, String> params = new HashMap<String, String>();
        params.put(MetricParameter.FROM_DATE.name(), "20101001");
        params.put(MetricParameter.TO_DATE.name(), "20101001");

        MapStringListListStringValueData value =
                                                 (MapStringListListStringValueData)executeAndReturnResult(ScriptType.USERS_ACTIVITY_PREPARATION,
                                                                                                          log, params);

        Map<String, ListListStringValueData> all = value.getAll();
        assertEquals(all.size(), 3);
    }
}
