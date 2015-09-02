/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.SetValueData;
import com.codenvy.analytics.datamodel.StringValueData;
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

import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

/**
 * @author Alexander Reshetnyak
 */
public class TestConfirmationSingupByEmail extends BaseTest {

    @BeforeClass
    public void init() throws Exception {
        File log = LogGenerator.generateLogByStrings(Arrays.asList(new String[]
            {"127.0.0.1 2013-01-01 00:01:10,791[]  [] []    [][][] - EVENT#singup-validation-email-send# EMAIL#user1@domain.com#",
             "127.0.0.1 2013-01-01 00:04:10,791[]  [] []    [][][] - EVENT#user-created# USER#user1@domain.com# USER-ID#user7me0azc5tgh8gqmx# EMAILS#[]#",
             "127.0.0.1 2013-01-01 06:00:57,754[]  [] []    [][][] - EVENT#singup-validation-email-send# EMAIL#user2@domain.com#",
             "127.0.0.1 2013-01-01 07:00:58,755[]  [] []    [][][] - EVENT#singup-validation-email-send# EMAIL#user3@domain.com#",
             "127.0.0.1 2013-01-01 07:08:58,755[]  [] []    [][][] - EVENT#user-created# USER#user3@domain.com# USER-ID#user04a07xvgry4xvwyb# EMAILS##",
             "127.0.0.1 2013-01-01 07:00:58,755[]  [] []    [][][] - EVENT#singup-validation-email-send# EMAIL#user4@domain.com#"}));

        computeStatistics(log, "20130101");
    }

    private void computeStatistics(File log, String date) throws IOException, ParseException, JobExecutionException {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, date);
        builder.put(Parameters.TO_DATE, date);
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.USERS_PROFILES, MetricType.USERS_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.SEND_VERIFICATION_EMAIL, MetricType.EMAIL_VALIDATION_SEND).getParamsAsMap());
        pigServer.execute(ScriptType.SEND_VERIFICATION_EMAIL, builder.build());

        DataComputationFeature dataComputationFeature = new DataComputationFeature();
        dataComputationFeature.forceExecute(builder.build());
    }

    @Test
    public void testSingupConfirmation() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");

        Metric metric = MetricFactory.getMetric(MetricType.SINGUP_VALIDATION_EMAIL_SEND);
        LongValueData value = (LongValueData)metric.getValue(builder.build());
        assertEquals(4, value.getAsLong());

        metric = MetricFactory.getMetric(MetricType.SINGUP_VALIDATION_EMAIL_SEND_SET);
        SetValueData set = (SetValueData)metric.getValue(builder.build());
        assertEquals(4, set.getAll().size());
        assertTrue(set.getAll().contains(StringValueData.valueOf("user1@domain.com")));
        assertTrue(set.getAll().contains(StringValueData.valueOf("user2@domain.com")));
        assertTrue(set.getAll().contains(StringValueData.valueOf("user3@domain.com")));
        assertTrue(set.getAll().contains(StringValueData.valueOf("user4@domain.com")));

        metric = MetricFactory.getMetric(MetricType.SINGUP_VALIDATION_EMAIL_CONFIRMED);
        value = (LongValueData)metric.getValue(builder.build());
        assertEquals(2, value.getAsLong());

        metric = MetricFactory.getMetric(MetricType.SINGUP_VALIDATION_EMAIL_NOT_CONFIRMED);
        value = (LongValueData)metric.getValue(builder.build());
        assertEquals(2, value.getAsLong());

        metric = MetricFactory.getMetric(MetricType.SINGUP_VALIDATION_EMAIL_CONFIRMED_SET);
        set = (SetValueData)metric.getValue(builder.build());
        assertEquals(2, set.getAll().size());
        assertTrue(set.getAll().contains(StringValueData.valueOf("user1@domain.com")));
        assertTrue(set.getAll().contains(StringValueData.valueOf("user3@domain.com")));

        metric = MetricFactory.getMetric(MetricType.SINGUP_VALIDATION_EMAIL_NOT_CONFIRMED_SET);
        set = (SetValueData)metric.getValue(builder.build());
        assertEquals(2, set.getAll().size());
        assertTrue(set.getAll().contains(StringValueData.valueOf("user2@domain.com")));
        assertTrue(set.getAll().contains(StringValueData.valueOf("user4@domain.com")));
    }
}
