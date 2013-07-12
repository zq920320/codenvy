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
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestScriptJRebelUserProfileGahtering extends BaseTest {

    @Test
    public void testEventFound() throws Exception {
        List<Event> events = new ArrayList<Event>();
        events.add(Event.Builder.createJRebelUserProfileInfo("user1", "ws1", "userId1", "first1", "last1", "phone1").withDate("2010-10-01")
                                .build());
        events.add(Event.Builder.createJRebelUserProfileInfo("user2", "ws2", "userId2", "", "last2", "phone2").withDate("2010-10-01")
                                .build());
        File log = LogGenerator.generateLog(events);

        Map<String, String> context = Utils.newContext();
        context.put(MetricParameter.FROM_DATE.name(), "20101001");
        context.put(MetricParameter.TO_DATE.name(), "20101001");

        ListListStringValueData valueData =
                                            (ListListStringValueData)executeAndReturnResult(ScriptType.JREBEL_USER_PROFILE_GATHERING, log,
                                                                                            context);
        List<ListStringValueData> all = valueData.getAll();

        assertEquals(all.size(), 2);
        assertTrue(all.contains(new ListStringValueData(Arrays.asList("user1", "first1", "last1", "phone1"))));
        assertTrue(all.contains(new ListStringValueData(Arrays.asList("user2", "", "last2", "phone2"))));
    }
}
