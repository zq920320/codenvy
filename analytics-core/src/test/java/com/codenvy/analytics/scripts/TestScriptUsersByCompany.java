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
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestScriptUsersByCompany extends BaseTest {

    @Test
    public void testExecute() throws Exception {
        List<Event> events = new ArrayList<Event>();
        events.add(Event.Builder.createUserUpdateProfile("user2@gmail.com", "f2", "l2", "company", "1", "1")
                        .withDate("2010-10-01").build());
        events.add(Event.Builder.createUserUpdateProfile("user1@gmail.com", "f2", "l2", "company", "1", "1")
                        .withDate("2010-10-02").build());
        File log = LogGenerator.generateLog(events);

        Map<String, String> context = Utils.newContext();
        context.put(MetricParameter.RESULT_DIR.name(), BASE_DIR);
        context.put(MetricParameter.FROM_DATE.name(), "20101001");
        context.put(MetricParameter.TO_DATE.name(), "20101002");

        runUsersProfileLogPreparationScript(log, context, 2);

        events = new ArrayList<Event>();
        events.add(Event.Builder.createUserUpdateProfile("user1@gmail.com", "f3", "l3", "company", "1", "1")
                        .withDate("2010-10-03").build());
        events.add(Event.Builder.createUserUpdateProfile("user3@gmail.com", "f4", "l4", "zompany", "1", "1")
                        .withDate("2010-10-04").build());
        log = LogGenerator.generateLog(events);

        context.put(MetricParameter.FROM_DATE.name(), "20101003");
        context.put(MetricParameter.TO_DATE.name(), "20101004");

        runUsersProfileLogPreparationScript(log, context, 0);

        context.put(MetricParameter.PARAM.name(), "company");
        ListStringValueData valueData = (ListStringValueData) executeAndReturnResult(ScriptType.USERS_BY_COMPANY, log, context);

        assertEquals(valueData.size(), 2);
        assertTrue(valueData.getAll().contains("user1@gmail.com"));
        assertTrue(valueData.getAll().contains("user2@gmail.com"));

        context.put(MetricParameter.PARAM.name(), "cOmpany");
        valueData = (ListStringValueData) executeAndReturnResult(ScriptType.USERS_BY_COMPANY, log, context);

        assertEquals(valueData.size(), 2);
        assertTrue(valueData.getAll().contains("user1@gmail.com"));
        assertTrue(valueData.getAll().contains("user2@gmail.com"));

        context.put(MetricParameter.PARAM.name(), "c?mpany");
        valueData = (ListStringValueData) executeAndReturnResult(ScriptType.USERS_BY_COMPANY, log, context);

        assertEquals(valueData.size(), 2);
        assertTrue(valueData.getAll().contains("user1@gmail.com"));
        assertTrue(valueData.getAll().contains("user2@gmail.com"));

        context.put(MetricParameter.PARAM.name(), "?ompany");
        valueData = (ListStringValueData) executeAndReturnResult(ScriptType.USERS_BY_COMPANY, log, context);

        assertEquals(valueData.size(), 3);
        assertTrue(valueData.getAll().contains("user1@gmail.com"));
        assertTrue(valueData.getAll().contains("user2@gmail.com"));
        assertTrue(valueData.getAll().contains("user3@gmail.com"));

        context.put(MetricParameter.PARAM.name(), "Ompany");
        valueData = (ListStringValueData) executeAndReturnResult(ScriptType.USERS_BY_COMPANY, log, context);

        assertEquals(valueData.size(), 3);
        assertTrue(valueData.getAll().contains("user1@gmail.com"));
        assertTrue(valueData.getAll().contains("user2@gmail.com"));
        assertTrue(valueData.getAll().contains("user3@gmail.com"));

    }

    private void runUsersProfileLogPreparationScript(File log, Map<String, String> context, long completedProfiles)
            throws IOException {
        File srcDir = new File(BASE_DIR, "PROFILES");
        File destDir = new File(BASE_DIR, "PREV_PROFILES");

        if (destDir.exists()) {
            FileUtils.deleteDirectory(destDir);
        }

        if (srcDir.exists()) {
            FileUtils.moveDirectory(srcDir, destDir);
        } else {
            destDir.mkdirs();
            File.createTempFile("prefix", "suffix", destDir);
        }

        execute(ScriptType.USERS_PROFILE_LOG_PREPARATION, log, context);
    }
}
