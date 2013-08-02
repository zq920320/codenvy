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


package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.FSValueDataManager;
import com.codenvy.analytics.scripts.executor.pig.PigScriptExecutor;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestDataProcessing {

    private HashMap<String, String> context;

    @BeforeTest
    public void setUp() throws Exception {
        List<Event> events = new ArrayList<Event>();
        events.add(Event.Builder.createUserCodeRefactorEvent("ws", "user@gmail.com", "project1", "type", "feature").withDate("2010-10-01").build());
        events.add(Event.Builder.createUserCodeRefactorEvent("ws", "user@gmail.com", "project2", "type", "feature").withDate("2010-10-01").build());
        events.add(Event.Builder.createUserCodeRefactorEvent("", "", "project2", "type", "feature").withDate("2010-10-01").build());
        File log = LogGenerator.generateLog(events);

        context = new HashMap<>();
        context.put(PigScriptExecutor.LOG, log.getAbsolutePath());
        Utils.putFromDate(context, "20101001");
        Utils.putToDate(context, "20101001");
        Utils.putEvent(context, "user-code-refactor");

        DataProcessing.calculateForWsUser(MetricType.USER_CODE_REFACTOR, context);
    }

    @Test
    public void testCheckFiles() {
        String baseDir = FSValueDataManager.RESULT_DIRECTORY + File.separator +
                MetricType.USER_CODE_REFACTOR.name().toLowerCase() + File.separator +
                "2010/10/01/20101001/";

        assertTrue(new File(baseDir + "value").exists());

        assertTrue(new File(baseDir + "users/u/s/e/r@gmail.com/value").exists());
        assertFalse(new File(baseDir + "users/d/e/f/ault/value").exists());

        assertTrue(new File(baseDir + "domains/g/m/a/il.com/value").exists());

        assertTrue(new File(baseDir + "ws/ws/value").exists());
   }
}
