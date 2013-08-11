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
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestScriptExtractUser extends BaseTest {

    @Test
    public void testExecute() throws Exception {
        List<Event> events = new ArrayList<>();

        events.add(new Event.Builder().withParam("EVENT", "fake").withParam("USER", "user1").withDate("2010-10-01").build());
        events.add(new Event.Builder().withParam("EVENT", "fake").withParam("ALIASES", "[user2,user3]").withDate("2010-10-01").build());
        events.add(new Event.Builder().withParam("EVENT", "fake").withParam("ALIASES", "user4").withDate("2010-10-01").build());
        events.add(new Event.Builder().withParam("EVENT" ,"fake").withDate("2010-10-01").build());

        File log = LogGenerator.generateLog(events);

        Map<String, String> context = new HashMap<>();
        context.put(MetricParameter.FROM_DATE.name(), "20101001");
        context.put(MetricParameter.TO_DATE.name(), "20101001");

        ListStringValueData valueData =
                (ListStringValueData)executeAndReturnResult(ScriptType.TEST_EXTRACT_USER, log, context);
        assertEquals(valueData.size(), 5);
        assertTrue(valueData.getAll().contains("user1"));
        assertTrue(valueData.getAll().contains("user2"));
        assertTrue(valueData.getAll().contains("user3"));
        assertTrue(valueData.getAll().contains("user4"));
        assertTrue(valueData.getAll().contains("default"));
    }
}
