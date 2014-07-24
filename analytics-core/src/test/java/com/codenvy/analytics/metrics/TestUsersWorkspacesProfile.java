/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;
import com.codenvy.analytics.services.DataComputationFeature;

import org.quartz.JobExecutionException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;

import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsList;
import static org.testng.AssertJUnit.assertEquals;

/**
 * @author Alexander Reshetnyak
 */
public class TestUsersWorkspacesProfile extends BaseTest {

    @BeforeClass
    public void init() throws Exception {
        File log = LogGenerator.generateLogByStrings(Arrays.asList(new String[]
            {"127.0.0.1 2013-01-01 00:04:10,791[]  [] []    [][][] - EVENT#user-created# USER#user1@domain.com# USER-ID#user7me0azc5tgh8gqmx# EMAILS#[]#",
             "127.0.0.1 2013-01-01 07:08:56,754[]  [] []    [][][] - EVENT#user-created# USER#user2@domain.com# USER-ID#user04a07xvgry4xvwya# EMAILS#[]#",
             "127.0.0.1 2013-01-01 07:08:56,755[]  [] []    [][][] - EVENT#user-created# USER#user3@domain.com# USER-ID#user04a07xvgry4xvwyb# EMAILS##",
             "127.0.0.1 2013-01-01 07:08:56,756[]  [] []    [][][] - EVENT#user-created# USER#AnonymousUser_rl3qni# USER-ID#Userz6rx8c35gewat48o# EMAILS#[]#",
             "127.0.0.1 2013-01-01 07:08:56,757[]  [] []    [][][] - EVENT#workspace-created# WS#tmp-workspaceks4vox9# WS-ID#workspacex44fjnhzcft7a298# USER#AnonymousUser_rl3qni#",
             "127.0.0.1 2013-01-01 07:08:56,758[]  [] []    [AnonymousUser_rl3qni][tmp-workspaceks4vox9][4AF63763BBF9280294F2F82E9CBBB6CD] - EVENT#project-created# WS#workspacex44fjnhzcft7a298# USER#Userz6rx8c35gewat48o# PROJECT#Sample-AngularJS# TYPE#JavaScript# PAAS#null#"}));

        computeStatistics(log, "20130101");
    }

    private void computeStatistics(File log, String date) throws IOException, ParseException, JobExecutionException {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, date);
        builder.put(Parameters.TO_DATE, date);
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.USERS_PROFILES, MetricType.USERS_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.WORKSPACES_PROFILES, MetricType.WORKSPACES_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.WORKSPACES_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.USERS_STATISTICS, MetricType.USERS_STATISTICS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_STATISTICS, builder.build());

        DataComputationFeature dataComputationFeature = new DataComputationFeature();
        dataComputationFeature.forceExecute(builder.build());
    }

    @Test
    public void testUsersProfiles() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");

        Metric metric = MetricFactory.getMetric(MetricType.USERS_PROFILES_LIST);

        ListValueData value = (ListValueData)metric.getValue(builder.build());

        assertEquals(value.getAll().size(), 4);
        assertEquals("[user1@domain.com]", ((MapValueData)value.getAll().get(0)).getAll().get("aliases").getAsString());
        assertEquals("[user2@domain.com]", ((MapValueData)value.getAll().get(1)).getAll().get("aliases").getAsString());
        assertEquals("[user3@domain.com]", ((MapValueData)value.getAll().get(2)).getAll().get("aliases").getAsString());
        assertEquals("[anonymoususer_rl3qni]", ((MapValueData)value.getAll().get(3)).getAll().get("aliases").getAsString());

        metric = MetricFactory.getMetric(MetricType.USERS_STATISTICS_LIST_PRECOMPUTED);
        value = getAsList(metric, builder.build());

        assertEquals(0, value.size());
    }

    @Test
    public void testTmpWorkspacesProfiles() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");

        Metric metric = MetricFactory.getMetric(MetricType.WORKSPACES_PROFILES_LIST);

        ListValueData value = (ListValueData)metric.getValue(builder.build());

        assertEquals(value.getAll().size(), 1);
        assertEquals("tmp-workspaceks4vox9", ((MapValueData)value.getAll().get(0)).getAll().get(ReadBasedMetric.WS_NAME).getAsString());

        metric = MetricFactory.getMetric(MetricType.USERS_STATISTICS_LIST_PRECOMPUTED);
        value = getAsList(metric, builder.build());

        assertEquals(0, value.size());
    }
}