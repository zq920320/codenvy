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
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestScriptNumberTemporaryWorkspaceCreated extends BaseTest {

    @Test
    public void testNumberOfEvents() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createTenantCreatedEvent("tmp-1", "user1").withDate("2010-10-01").build());
        events.add(Event.Builder.createTenantCreatedEvent("tmp-2", "user2").withDate("2010-10-01").build());
        events.add(Event.Builder.createTenantCreatedEvent("dev-monit", "user2").withDate("2010-10-01").build());
        File log = LogGenerator.generateLog(events);

        Map<String, String> context = new HashMap<>();
        Utils.putFromDate(context, "20101001");
        Utils.putToDate(context, "20101001");

        LongValueData longValueData =
                (LongValueData)executeAndReturnResult(ScriptType.NUMBER_TEMPORARY_WORKSPACE_CREATED, log, context);
        assertEquals(longValueData.getAsLong(), 2);
    }
}
