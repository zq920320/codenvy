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
package com.codenvy.analytics.pig.scripts;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.projects.AbstractProjectPaas;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestDeploymentsByTypes extends BaseTest {

    private static final String COLLECTION        = TestDeploymentsByTypes.class.getSimpleName().toLowerCase();
    private static final String RESOURCE_DIR      = BASE_DIR + "/test-classes/" + TestDeploymentsByTypes.class.getSimpleName();
    private static final String LOG_TILL_20140506 = RESOURCE_DIR + "/messages20140506.log";
    private static final String LOG               = RESOURCE_DIR + "/messages.log";

    @BeforeClass
    public void init() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.STORAGE_TABLE, COLLECTION);
        builder.put(Parameters.LOG, new File(LOG_TILL_20140506).getAbsolutePath());
        pigServer.execute(ScriptType.DEPLOYMENTS_BY_TYPES, builder.build());

        builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20140507");
        builder.put(Parameters.TO_DATE, "20140507");
        builder.put(Parameters.STORAGE_TABLE, COLLECTION);
        builder.put(Parameters.LOG, new File(LOG).getAbsolutePath());
        pigServer.execute(ScriptType.DEPLOYMENTS_BY_TYPES, builder.build());
    }

    @Test
    public void testReturnDataByComplexFilter() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(MetricFilter.USER, "user1@gmail.com OR user2@gmail.com");
        builder.put(MetricFilter.WS, "ws3");

        Metric metric = new TestAbstractProjectPaas(new String[]{"paas1"});
        assertEquals(metric.getValue(builder.build()), new LongValueData(1));
    }

    // -------------------------> Tested Metrics

    private class TestAbstractProjectPaas extends AbstractProjectPaas {
        protected TestAbstractProjectPaas(String[] types) {
            super(COLLECTION, types);
        }

        @Override
        public String getStorageCollectionName() {
            return COLLECTION;
        }

        @Override
        public String getDescription() {
            return null;
        }
    }
}
