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
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestDeploymentsByTypes extends BaseTest {

    private static final String RESOURCE_DIR      = BASE_DIR + "/test-classes/" + TestDeploymentsByTypes.class.getSimpleName();
    private static final String LOG_TILL_20140506 = RESOURCE_DIR + "/messages20140506.log";
    private static final String LOG               = RESOURCE_DIR + "/messages.log";

    @BeforeClass
    public void init() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.LOG, new File(LOG_TILL_20140506).getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.WORKSPACES_PROFILES, MetricType.WORKSPACES_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.WORKSPACES_PROFILES, builder.build());

        builder.putAll(
                scriptsManager.getScript(ScriptType.DEPLOYMENTS_BY_TYPES, MetricType.PROJECT_PAASES).getParamsAsMap());
        pigServer.execute(ScriptType.DEPLOYMENTS_BY_TYPES, builder.build());

        builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20140507");
        builder.put(Parameters.TO_DATE, "20140507");
        builder.put(Parameters.LOG, new File(LOG).getAbsolutePath());


        builder.putAll(scriptsManager.getScript(ScriptType.DEPLOYMENTS_BY_TYPES, MetricType.PROJECT_PAASES).getParamsAsMap());
        pigServer.execute(ScriptType.DEPLOYMENTS_BY_TYPES, builder.build());
    }

    @Test
    public void testReturnDataByComplexFilter() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20140507");
        builder.put(Parameters.TO_DATE, "20140507");
        builder.put(MetricFilter.USER_ID, "user1@gmail.com OR user2@gmail.com");
        builder.put(MetricFilter.WS_ID, "wsid3");

        Metric metric = MetricFactory.getMetric(MetricType.PROJECT_PAAS_GAE);
        assertEquals(metric.getValue(builder.build()), new LongValueData(1));

        metric = MetricFactory.getMetric(MetricType.PROJECT_PAAS_ANY);
        assertEquals(metric.getValue(builder.build()), new LongValueData(1));
    }
}
