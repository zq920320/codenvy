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
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.metrics.value.FSValueDataManager;
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestScriptNumberOfActiveUsers extends BaseTest {

    @Test
    public void testSetOfActiveUsers() throws Exception {
        FileUtils.deleteDirectory(new File(FSValueDataManager.SCRIPT_LOAD_DIRECTORY));
        FileUtils.deleteDirectory(new File(FSValueDataManager.SCRIPT_STORE_DIRECTORY));

        Map<String, String> context = new HashMap<>();
        MetricParameter.LOAD_DIR.put(context, Utils.getLoadDirFor(MetricType.USERS_CREATED_PROJECT_ONCE));
        MetricParameter.STORE_DIR.put(context, Utils.getStoreDirFor(MetricType.USERS_CREATED_PROJECT_ONCE));

        Utils.initLoadStoreDirectories(context);

        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createProjectCreatedEvent("user1@gmail.com", "ws1", "session", "project1", "type")
                        .withDate("2013-01-01").build());
        events.add(Event.Builder.createProjectCreatedEvent("user2@gmail.com", "ws2", "session", "project1", "type")
                        .withDate("2013-01-01").build());
        events.add(Event.Builder.createProjectCreatedEvent("user2@gmail.com", "ws2", "session", "project2", "type")
                        .withDate("2013-01-02").build());
        events.add(Event.Builder.createProjectCreatedEvent("", "", "session", "project1", "type").withDate("2013-01-02")
                        .build());
        File log = LogGenerator.generateLog(events);

        MetricParameter.FROM_DATE.put(context, "20130101");
        MetricParameter.TO_DATE.put(context, "20130101");
        MetricParameter.EVENT.put(context, EventType.PROJECT_CREATED.toString());
        LongValueData valueData = (LongValueData)executeAndReturnResult(ScriptType.NUMBER_ACTIVE_USERS, log, context);

        assertEquals(valueData.getAsLong(), 2);

        Utils.initLoadStoreDirectories(context);

        context = new HashMap<>();
        MetricParameter.FROM_DATE.put(context, "20130102");
        MetricParameter.TO_DATE.put(context, "20130102");
        MetricParameter.LOAD_DIR.put(context, Utils.getLoadDirFor(MetricType.USERS_CREATED_PROJECT_ONCE));
        MetricParameter.STORE_DIR.put(context, Utils.getStoreDirFor(MetricType.USERS_CREATED_PROJECT_ONCE));
        MetricParameter.EVENT.put(context, EventType.PROJECT_CREATED.toString());
        valueData = (LongValueData)executeAndReturnResult(ScriptType.NUMBER_ACTIVE_USERS, log, context);

        assertEquals(valueData.getAsLong(), 3);
    }
}
