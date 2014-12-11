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

import static org.testng.AssertJUnit.assertEquals;

/**
 * @author Alexander Reshetnyak
 */
public class TestFactorySessionsInTmpWs extends BaseTest {

    @BeforeClass
    public void init() throws Exception {
        File log = LogGenerator.generateLogByStrings(Arrays.asList(new String[]
        {  //Events from user which was used not supported browser
           "127.0.0.1 2014-12-08 14:17:52,159[]  [ ] []  [][][] - EVENT#user-created# USER#AnonymousUser_2vsur8# USER-ID#Userc8mqap7aau1vqgqy# EMAILS#[AnonymousUser_2vsur8]#",
            "127.0.0.1 2014-12-08 14:17:52,486[]  [ ] []  [][][] - EVENT#user-added-to-ws# USER#AnonymousUser_2vsur8# WS#tmp-workspacel4nbgx4# FROM#website#",
            "127.0.0.1 2014-12-08 14:17:52,487[]  [ ] []  [][][] - EVENT#workspace-created# WS#tmp-workspacel4nbgx4# WS-ID#workspacev3ebqp87tgzppmfy# USER#Userc8mqap7aau1vqgqy#",
            "127.0.0.1 2014-12-08 14:17:52,489[]  [ ] []  [][][] - EVENT#factory-url-accepted# WS#tmp-workspacel4nbgx4# REFERRER## FACTORY-URL#https://codenvy.com/factory/?id=oxumdbmtuhy87jqb#  AFFILIATE-ID## ORG-ID#accountya9vidwqbifhqd9p#",

            //Events from user which was used supported browser
            "127.0.0.1 2014-12-08 14:10:55,562[]  [ ] []  [][][] - EVENT#user-created# USER#AnonymousUser_zlnh4z# USER-ID#Usertn8qmf6zhcf1a4v4# EMAILS#[AnonymousUser_zlnh4z]#",
            "127.0.0.1 2014-12-08 14:10:55,564[]  [ ] []  [][][] - Temporary user AnonymousUser_zlnh4z created",
            "127.0.0.1 2014-12-08 14:10:55,905[]  [ ] []  [][][] - EVENT#user-added-to-ws# USER#AnonymousUser_zlnh4z# WS#tmp-workspace32e4k1s# FROM#website#",
            "127.0.0.1 2014-12-08 14:10:55,905[]  [ ] []  [][][] - EVENT#workspace-created# WS#tmp-workspace32e4k1s# WS-ID#workspacewdzvro7kgx2xy407# USER#Usertn8qmf6zhcf1a4v4#",
            "127.0.0.1 2014-12-08 14:10:55,907[]  [ ] []  [][][] - EVENT#factory-url-accepted# WS#tmp-workspace32e4k1s# REFERRER## FACTORY-URL#https://codenvy.com/factory/?id=oxumdbmtuhy87jqb#  AFFILIATE-ID## ORG-ID#accountya9vidwqbifhqd9p#",
            "127.0.0.1 2014-12-08 14:11:02,572[]  [ ] []  [][][] - EVENT#workspace-updated# WS#tmp-workspace32e4k1s# WS-ID#workspacewdzvro7kgx2xy407#",
            "127.0.0.1 2014-12-08 14:11:04,065[]  [ ] []  [][][] - EVENT#session-usage# WS#tmp-workspace32e4k1s# USER#AnonymousUser_zlnh4z# PARAMETERS#SESSION-ID=DCB787A0-CD1B-433B-8777-AF04ADBA5391#",
            "127.0.0.1 2014-12-08 14:11:04,085[]  [ ] []  [][][] - EVENT#session-factory-usage# WS#tmp-workspace32e4k1s# USER#AnonymousUser_zlnh4z# PARAMETERS#SESSION-ID=DCB787A0-CD1B-433B-8777-AF04ADBA5391#",
            "127.0.0.1 2014-12-08 14:11:07,537[]  [ ] []  [][][] - EVENT#project-created# PROJECT#spring-petclinic# TYPE#maven# WS#tmp-workspace32e4k1s# USER#AnonymousUser_zlnh4z# PAAS#default#",
        }));

        computeStatistics(log, "20141208");
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

        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_SESSIONS, MetricType.PRODUCT_USAGE_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_SESSIONS, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, builder.build());

        DataComputationFeature dataComputationFeature = new DataComputationFeature();
        dataComputationFeature.forceExecute(builder.build());
    }

    @Test
    public void testFactorySessionOnWsFromNotSupportedBrowser() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20141208");
        builder.put(Parameters.TO_DATE, "20141208");
        builder.put(Parameters.WS, "workspacev3ebqp87tgzppmfy");

        Metric metric = MetricFactory.getMetric(MetricType.PRODUCT_USAGE_SESSIONS_LIST);

        ListValueData value = (ListValueData)metric.getValue(builder.build());

        assertEquals(value.size(), 1);

        MapValueData m = (MapValueData)value.getAll().get(0);
        assertEquals(m.getAll().get("ws").toString(), "workspacev3ebqp87tgzppmfy");
        assertEquals(m.getAll().get("user").toString(), "Userc8mqap7aau1vqgqy");
    }

    @Test
    public void testFactorySessionOnWsFromSupportedBrowser() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20141208");
        builder.put(Parameters.TO_DATE, "20141208");
        builder.put(Parameters.WS, "workspacewdzvro7kgx2xy407");

        Metric metric = MetricFactory.getMetric(MetricType.PRODUCT_USAGE_SESSIONS_LIST);

        ListValueData value = (ListValueData)metric.getValue(builder.build());

        assertEquals(value.size(), 1);

        MapValueData m = (MapValueData)value.getAll().get(0);
        assertEquals(m.getAll().get("session_id").toString(), "DCB787A0-CD1B-433B-8777-AF04ADBA5391");
        assertEquals(m.getAll().get("ws").toString(), "workspacewdzvro7kgx2xy407");
        assertEquals(m.getAll().get("user").toString(), "Usertn8qmf6zhcf1a4v4");
    }
}