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
import com.codenvy.analytics.scripts.EventType;
import com.codenvy.analytics.scripts.executor.pig.PigScriptExecutor;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

import org.testng.annotations.Test;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestDataProcessing {

    private final DateFormat df          = new SimpleDateFormat("yyyy-MM-dd");
    private final String     currentDate = "2010-10-03";
    private final String     nextDate    = "2010-10-04";

    @Test
    public void testCheckFilesForNumberOfEvents() throws Exception {
        MetricType metricType = MetricType.USER_CODE_REFACTOR;
        Map<String, String> context = prepareContext(currentDate);
        DataProcessing.calculateAndStore(metricType, context);

        String baseDir = getBaseDir(metricType);

        assertTrue(new File(baseDir).exists());
        assertTrue(new File(baseDir + "value").exists());

        assertTrue(new File(baseDir + "users/u/s/e/r@gmail.com/value").exists());
        assertFalse(new File(baseDir + "users/d/e/f/ault/value").exists());

        assertTrue(new File(baseDir + "domains/g/m/a/il.com/value").exists());
        assertFalse(new File(getBaseDirNextDay(metricType)).exists());
    }

    @Test
    public void testCheckFilesForSetOfUsers() throws Exception {
        MetricType metricType = MetricType.ACTIVE_USERS_SET;
        Map<String, String> context = prepareContext(currentDate);
        DataProcessing.calculateAndStore(metricType, context);

        String baseDir = getBaseDir(metricType);

        assertTrue(new File(baseDir + "value").exists());
        assertTrue(new File(baseDir + "users/u/s/e/r@gmail.com/value").exists());
        assertFalse(new File(baseDir + "users/d/e/f/ault/value").exists());
        assertTrue(new File(baseDir + "domains/g/m/a/il.com/value").exists());
        assertFalse(new File(getBaseDirNextDay(metricType)).exists());
    }

    @Test
    public void testCheckFilesForSetOfWs() throws Exception {
        MetricType metricType = MetricType.ACTIVE_WS_SET;
        Map<String, String> context = prepareContext(currentDate);
        DataProcessing.calculateAndStore(metricType, context);

        String baseDir = getBaseDir(metricType);

        assertTrue(new File(baseDir + "value").exists());

        assertTrue(new File(baseDir + "users/u/s/e/r@gmail.com/value").exists());
        assertFalse(new File(baseDir + "users/d/e/f/ault/value").exists());

        assertTrue(new File(baseDir + "domains/g/m/a/il.com/value").exists());
        assertFalse(new File(getBaseDirNextDay(metricType)).exists());
    }

    @Test
    public void testCheckFilesForNumberOfEventsWithType() throws Exception {
        MetricType metricType = MetricType.USER_SSO_LOGGED_IN;
        Map<String, String> context = prepareContextUserSSOLoggedInEvents(currentDate);
        DataProcessing.calculateAndStore(metricType, context);

        String baseDir = getBaseDir(metricType);

        assertFalse(new File(baseDir + "value").exists());

        assertTrue(new File(baseDir + "/google/value").exists());
        assertTrue(new File(baseDir + "/github/value").exists());
        assertTrue(new File(baseDir + "/jaas/value").exists());

        assertTrue(new File(baseDir + "/google/users/u/s/e/r1@gmail.com/value").exists());
        assertTrue(new File(baseDir + "/github/users/u/s/e/r1@gmail.com/value").exists());
        assertTrue(new File(baseDir + "/google/users/u/s/e/r2@gmail.com/value").exists());
        assertTrue(new File(baseDir + "/jaas/users/u/s/e/r3@gmail.com/value").exists());

        assertTrue(new File(baseDir + "/google/domains/g/m/a/il.com/value").exists());
        assertTrue(new File(baseDir + "/github/domains/g/m/a/il.com/value").exists());
        assertTrue(new File(baseDir + "/jaas/domains/g/m/a/il.com/value").exists());
        assertFalse(new File(getBaseDirNextDay(metricType)).exists());
    }

    @Test
    public void testProductUsageTime() throws Exception {
        MetricType metricType = MetricType.PRODUCT_USAGE_TIME;
        Map<String, String> context = prepareContextProductUsateTimeEvents(currentDate);
        DataProcessing.calculateAndStore(metricType, context);

        String baseDir = getBaseDir(metricType);

        assertTrue(new File(baseDir + "value").exists());
        assertTrue(new File(baseDir + "number").exists());
        assertTrue(new File(baseDir + "/users/u/s/e/r1@gmail.com/value").exists());
        assertTrue(new File(baseDir + "/users/u/s/e/r1@gmail.com/number").exists());
        assertTrue(new File(baseDir + "/domains/g/m/a/il.com/value").exists());
        assertTrue(new File(baseDir + "/domains/g/m/a/il.com/number").exists());
        assertFalse(new File(getBaseDirNextDay(metricType)).exists());
    }

    private Map<String, String> prepareContext(String date) throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createUserCodeRefactorEvent("ws", "user@gmail.com", "project1", "type", "feature")
                        .withDate(date).build());
        events.add(Event.Builder.createUserCodeRefactorEvent("ws", "user@gmail.com", "project2", "type", "feature")
                        .withDate(date).build());
        events.add(Event.Builder.createUserCodeRefactorEvent("", "", "project2", "type", "feature").withDate(date)
                        .build());
        File log = LogGenerator.generateLog(events);

        Map<String, String> context = new HashMap<>();
        context.put(PigScriptExecutor.LOG, log.getAbsolutePath());
        Utils.putFromDate(context, date.replace("-", ""));
        Utils.putToDate(context, date.replace("-", ""));

        return context;
    }

    private Map<String, String> prepareContextUserSSOLoggedInEvents(String date) throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createUserSSOLoggedInEvent("user1@gmail.com", "google").withDate(date).build());
        events.add(Event.Builder.createUserSSOLoggedInEvent("user1@gmail.com", "github").withDate(date).build());
        events.add(Event.Builder.createUserSSOLoggedInEvent("user2@gmail.com", "google").withDate(date).build());
        events.add(Event.Builder.createUserSSOLoggedInEvent("user3@gmail.com", "jaas").withDate(date).build());
        events.add(Event.Builder.createUserSSOLoggedInEvent("user1@gmail.com", "google").withDate(date).build());
        events.add(Event.Builder.createUserSSOLoggedInEvent("", "google").withDate(date).build());
        File log = LogGenerator.generateLog(events);

        Map<String, String> context = new HashMap<>();
        context.put(PigScriptExecutor.LOG, log.getAbsolutePath());
        Utils.putFromDate(context, date.replace("-", ""));
        Utils.putToDate(context, date.replace("-", ""));

        return context;
    }

    private Map<String, String> prepareContextProductUsateTimeEvents(String date) throws Exception {
        List<Event> events = new ArrayList<>();
        // session started and session finished [5m]
        events.add(Event.Builder.createSessionStartedEvent("user1@gmail.com", "ws1", "ide", "1").withDate(date)
                        .withTime("20:00:00").build());
        events.add(Event.Builder.createSessionFinishedEvent("user1@gmail.com", "ws1", "ide", "1").withDate(date)
                        .withTime("20:05:00").build());
        File log = LogGenerator.generateLog(events);

        Map<String, String> context = new HashMap<>();
        context.put(PigScriptExecutor.LOG, log.getAbsolutePath());
        Utils.putFromDate(context, date.replace("-", ""));
        Utils.putToDate(context, date.replace("-", ""));

        return context;
    }

    private String getBaseDir(MetricType metricType) {
        return getBaseDir(metricType, currentDate);
    }

    private String getBaseDirNextDay(MetricType metricType) {
        return getBaseDir(metricType, nextDate);
    }

    private String getBaseDir(MetricType metricType, String date) {
        return FSValueDataManager.RESULT_DIRECTORY + File.separator +
               metricType.name().toLowerCase() + File.separator +
               date.replace("-", File.separator) + File.separator +
               date.replace("-", "") + File.separator;
    }
}
