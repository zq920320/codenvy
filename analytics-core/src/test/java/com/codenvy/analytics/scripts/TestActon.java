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

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.MetricParameter;
import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;
import org.testng.annotations.Test;

import java.io.File;
import java.util.*;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestActon extends BaseTest {

    @Test
    public void testExecute() throws Exception {
        List<Event> events = new ArrayList<Event>();
        events.add(Event.Builder.createProjectCreatedEvent("user1", "ws1", "session", "project1", "type1")
                .withDate("2010-10-01").withTime("20:00:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user1", "ws1", "session", "project1", "type1")
                .withDate("2010-10-01").withTime("20:05:00").build());
        events.add(Event.Builder.createApplicationCreatedEvent("user1", "ws1", "session", "project1", "type1", "paas1")
                .withDate("2010-10-01").withTime("20:10:00").build());

        events.add(Event.Builder.createUserInviteEvent("user2", "ws1", "session", "email")
                .withDate("2010-10-01").withTime("20:00:00").build());
        events.add(Event.Builder.createUserInviteEvent("user2", "ws1", "session", "email")
                .withDate("2010-10-01").withTime("20:05:00").build());

        events.add(Event.Builder.createProjectDeployedEvent("user3", "ws4", "session", "project4", "type2", "local")
                .withDate("2010-10-01").build());

        File log = LogGenerator.generateLog(events);

        Map<String, String> params = new HashMap<String, String>();
        params.put(MetricParameter.RESULT_DIR.name(), BASE_DIR);
        params.put(MetricParameter.FROM_DATE.name(), "20101001");
        params.put(MetricParameter.TO_DATE.name(), "20101001");

        ListListStringValueData valueData = (ListListStringValueData) executeAndReturnResult(ScriptType.ACTON, log, params);
        List<ListStringValueData> all = valueData.getAll();

        assertEquals(all.size(), 3);
        assertTrue(all.contains(new ListStringValueData(Arrays.asList("user1", "1", "2", "1", "10"))));
        assertTrue(all.contains(new ListStringValueData(Arrays.asList("user2", "0", "0", "0", "5"))));
        assertTrue(all.contains(new ListStringValueData(Arrays.asList("user3", "0", "1", "1", "0"))));
    }
}
