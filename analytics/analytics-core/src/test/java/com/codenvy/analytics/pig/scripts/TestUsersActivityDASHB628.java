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
package com.codenvy.analytics.pig.scripts;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
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
 * This test is reproduce problem that describe in issue https://jira.codenvycorp.com/browse/DASHB-628
 *
 * Main problem in this issue isn't show session events for specific session.
 * Cause that problem is bag in method com.codenvy.analytics.Utils.isUserID.
 *
 * @author Alexander Reshetnyak
 */
public class TestUsersActivityDASHB628 extends BaseTest {

    @BeforeClass
    public void init() throws Exception {
        File log = LogGenerator.generateLogByStrings(Arrays.asList(new String[]
                                                                           {
                                                                                   "10.142.176.188 2015-01-27 04:09:30,576[io-8080-exec-87]  [INFO ] [c.c.api.dao.ldap.UserDaoImpl 528]    [ide3][][][] - EVENT#user-created# USER#AnonymousUser_yfgzgx# USER-ID#User55abphy8ie89vlgs# EMAILS#[AnonymousUser_yfgzgx]#",
                                                                                   "10.142.176.188 2015-01-27 04:09:30,675[io-8080-exec-62]  [INFO ] [c.c.a.dao.mongo.MemberDaoImpl 284]   [ide3][][][] - EVENT#user-added-to-ws# USER#AnonymousUser_yfgzgx# WS#tmp-workspace2t5dhce# FROM#website#",
                                                                                   "10.142.176.188 2015-01-27 04:09:30,675[io-8080-exec-62]  [INFO ] [c.c.a.w.s.WorkspaceService 286]      [ide3][][][] - EVENT#workspace-created# WS#tmp-workspace2t5dhce# WS-ID#workspaces9eysbb8crd3f9qi# USER#User55abphy8ie89vlgs#",
                                                                                   "10.142.176.188 2015-01-27 04:09:30,676[io-8080-exec-47]  [INFO ] [c.c.c.f.CloudIdeFactoryServlet 236]  [ide3][][][] - EVENT#factory-url-accepted# WS#tmp-workspace2t5dhce# REFERRER## FACTORY-URL#https://codenvy.com/factory/?id=c5v5vbe41eiov3ji# ORG-ID#accountya9vidwqbifhqd9p# AFFILIATE-ID##",
                                                                                   "10.142.176.188 2015-01-27 04:09:39,332[cs Event Logger]  [INFO ] [c.c.a.a.logger.EventLogger 209]      [ide3][][][] - EVENT#session-usage# WS#tmp-workspace2t5dhce# USER#AnonymousUser_yfgzgx# PARAMETERS#SESSION-ID=EE8E3B02-1697-4F0A-A524-F254E5432B5B#",
                                                                                   "10.142.176.188 2015-01-27 04:09:39,338[o-8080-exec-146]  [INFO ] [c.c.a.w.s.WorkspaceService 455]      [ide3][][][] - EVENT#workspace-updated# WS#tmp-workspace2t5dhce# WS-ID#workspaces9eysbb8crd3f9qi#",
                                                                                   "10.142.176.188 2015-01-27 04:09:39,353[cs Event Logger]  [INFO ] [c.c.a.a.logger.EventLogger 209]      [ide3][][][] - EVENT#session-factory-usage# WS#tmp-workspace2t5dhce# USER#AnonymousUser_yfgzgx# PARAMETERS#SESSION-ID=EE8E3B02-1697-4F0A-A524-F254E5432B5B#",
                                                                                   "10.142.176.188 2015-01-27 04:09:40,231[ronousJobPool34]  [INFO ] [c.c.a.p.server.ProjectService 1222]  [ide3][][][] - EVENT#project-created# PROJECT#php# TYPE#php# WS#workspaces9eysbb8crd3f9qi# USER#User55abphy8ie89vlgs# PAAS#default#",
                                                                                   "10.142.176.188 2015-01-27 04:09:57,416[cs Event Logger]  [INFO ] [c.c.a.a.logger.EventLogger 209]      [ide3][][][] - EVENT#ide-usage# WS#tmp-workspace2t5dhce# USER#AnonymousUser_yfgzgx# PROJECT#php# TYPE#php# SOURCE#com.codenvy.ide.actions.ShowPreferencesAction# PARAMETERS##",
                                                                                   "10.142.176.188 2015-01-27 04:10:39,502[cs Event Logger]  [INFO ] [c.c.a.a.logger.EventLogger 209]      [ide3][][][] - EVENT#session-usage# WS#tmp-workspace2t5dhce# USER#AnonymousUser_yfgzgx# PROJECT#php# TYPE#php# PARAMETERS#SESSION-ID=EE8E3B02-1697-4F0A-A524-F254E5432B5B#",
                                                                                   "10.142.176.188 2015-01-27 04:10:39,523[cs Event Logger]  [INFO ] [c.c.a.a.logger.EventLogger 209]      [ide3][][][] - EVENT#session-factory-usage# WS#tmp-workspace2t5dhce# USER#AnonymousUser_yfgzgx# PROJECT#php# TYPE#php# PARAMETERS#SESSION-ID=EE8E3B02-1697-4F0A-A524-F254E5432B5B#",
                                                                                   "10.142.176.188 2015-01-27 04:11:11,753[cs Event Logger]  [INFO ] [c.c.a.a.logger.EventLogger 209]      [ide3][][][] - EVENT#ide-usage# WS#tmp-workspace2t5dhce# USER#AnonymousUser_yfgzgx# PROJECT#php# TYPE#php# SOURCE#com.codenvy.ide.ext.datasource.client.action.EditDatasourcesAction# PARAMETERS##",
                                                                                   "10.142.176.188 2015-01-27 04:11:15,774[cs Event Logger]  [INFO ] [c.c.a.a.logger.EventLogger 209]      [ide3][][][] - EVENT#ide-usage# WS#tmp-workspace2t5dhce# USER#AnonymousUser_yfgzgx# PROJECT#php# TYPE#php# SOURCE#com.codenvy.ide.ext.datasource.client.newdatasource.NewDatasourceWizardAction# PARAMETERS##",
                                                                                   "10.142.176.188 2015-01-27 04:12:19,906[cs Event Logger]  [INFO ] [c.c.a.a.logger.EventLogger 209]      [ide3][][][] - EVENT#session-usage# WS#tmp-workspace2t5dhce# USER#AnonymousUser_yfgzgx# PROJECT#php# TYPE#php# PARAMETERS#SESSION-ID=EE8E3B02-1697-4F0A-A524-F254E5432B5B#",
                                                                                   "10.142.176.188 2015-01-27 04:12:19,926[cs Event Logger]  [INFO ] [c.c.a.a.logger.EventLogger 209]      [ide3][][][] - EVENT#session-factory-usage# WS#tmp-workspace2t5dhce# USER#AnonymousUser_yfgzgx# PROJECT#php# TYPE#php# PARAMETERS#SESSION-ID=EE8E3B02-1697-4F0A-A524-F254E5432B5B#",
                                                                                   "10.142.176.188 2015-01-27 13:07:56,054[cs Event Logger]  [INFO ] [c.c.a.a.logger.EventLogger 209]      [ide3][][][] - EVENT#session-usage# WS#tmp-workspace2t5dhce# USER#AnonymousUser_yfgzgx# PROJECT#php# TYPE#php# PARAMETERS#SESSION-ID=EE8E3B02-1697-4F0A-A524-F254E5432B5B#",
                                                                                   "10.142.176.188 2015-01-27 13:07:56,074[cs Event Logger]  [INFO ] [c.c.a.a.logger.EventLogger 209]      [ide3][][][] - EVENT#session-factory-usage# WS#tmp-workspace2t5dhce# USER#AnonymousUser_yfgzgx# PROJECT#php# TYPE#php# PARAMETERS#SESSION-ID=EE8E3B02-1697-4F0A-A524-F254E5432B5B#",
                                                                                   "10.142.176.188 2015-01-27 15:28:02,326[cs Event Logger]  [INFO ] [c.c.a.a.logger.EventLogger 209]      [ide3][][][] - EVENT#session-usage# WS#tmp-workspace2t5dhce# USER#AnonymousUser_yfgzgx# PROJECT#php# TYPE#php# PARAMETERS#SESSION-ID=346EA3BA-ABE1-4F97-81C9-CF74CE95F90E#",
                                                                                   "10.142.176.188 2015-01-27 15:28:02,347[cs Event Logger]  [INFO ] [c.c.a.a.logger.EventLogger 209]      [ide3][][][] - EVENT#session-factory-usage# WS#tmp-workspace2t5dhce# USER#AnonymousUser_yfgzgx# PROJECT#php# TYPE#php# PARAMETERS#SESSION-ID=346EA3BA-ABE1-4F97-81C9-CF74CE95F90E#",
                                                                                   "10.142.176.188 2015-01-27 19:50:59,726[o-8080-exec-161]  [INFO ] [c.c.a.d.mongo.WorkspaceDaoImpl 193]  [ide3][][][] - EVENT#workspace-destroyed# WS#tmp-workspace2t5dhce# WS-ID#workspaces9eysbb8crd3f9qi#",
                                                                                   "10.142.176.188 2015-01-27 19:50:59,755[o-8080-exec-161]  [INFO ] [c.c.api.dao.ldap.UserDaoImpl 528]    [ide3][][][] - EVENT#user-removed# USER#AnonymousUser_yfgzgx# USER-ID#User55abphy8ie89vlgs# EMAILS#[AnonymousUser_yfgzgx]#",
                                                                           }));

        computeStatistics(log, "20150127");
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

        builder.putAll(
                scriptsManager.getScript(ScriptType.USERS_ACTIVITY, MetricType.USERS_ACTIVITY_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_ACTIVITY, builder.build());

        DataComputationFeature dataComputationFeature = new DataComputationFeature();
        dataComputationFeature.forceExecute(builder.build());
    }

    @Test
    public void testUsersActivity() throws Exception {
        Metric  metric = MetricFactory.getMetric(MetricType.USERS_ACTIVITY_LIST);
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20150127");
        builder.put(Parameters.TO_DATE, "20150127");
        builder.put(Parameters.USER, "User55abphy8ie89vlgs");


        ListValueData value = (ListValueData)metric.getValue(builder.build());
        assertEquals(value.size(), 8);
    }
}