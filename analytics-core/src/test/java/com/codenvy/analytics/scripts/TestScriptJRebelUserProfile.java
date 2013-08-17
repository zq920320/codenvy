/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */

package com.codenvy.analytics.scripts;

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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestScriptJRebelUserProfile extends BaseTest {

    @Test
    public void testEventFound() throws Exception {
        List<Event> events = new ArrayList<Event>();
        events.add(Event.Builder.createJRebelUserProfileInfo("user1", "ws1", "userId1", "first1", "last1", "phone1").withDate("2013-01-01")
                                .build());
        events.add(Event.Builder.createJRebelUserProfileInfo("user2", "ws2", "userId2", "", "last2", "phone2").withDate("2013-01-01")
                                .build());
        File log = LogGenerator.generateLog(events);

        Map<String, String> context = Utils.newContext();
        context.put(MetricParameter.FROM_DATE.name(), "20130101");
        context.put(MetricParameter.TO_DATE.name(), "20130101");

        ListListStringValueData valueData =
                                            (ListListStringValueData)executeAndReturnResult(ScriptType.JREBEL_USER_PROFILE_INFO, log,
                                                                                            context);
        List<ListStringValueData> all = valueData.getAll();

        assertEquals(all.size(), 2);
        assertTrue(all.contains(new ListStringValueData(Arrays.asList("user1", "first1", "last1", "phone1"))));
        assertTrue(all.contains(new ListStringValueData(Arrays.asList("user2", "", "last2", "phone2"))));
    }
}
