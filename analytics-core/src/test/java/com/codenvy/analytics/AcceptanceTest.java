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
package com.codenvy.analytics;

import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.services.pig.PigRunner;
import com.codenvy.analytics.services.view.ViewBuilder;

import org.quartz.JobExecutionException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class AcceptanceTest extends BaseTest {

    @BeforeClass
    public void prepare() throws Exception {
        runScript();
        buildView();
    }

    private void buildView() throws JobExecutionException {
        ViewBuilder viewBuilder = new ViewBuilder();
        viewBuilder.forceExecute(Utils.newContext());
    }

    private void runScript() throws JobExecutionException {
        PigRunner pigRunner = new PigRunner();

        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20131120");
        Parameters.TO_DATE.put(context, "20131120");
        Parameters.LOG.put(context, getClass().getClassLoader().getResource("messages").getFile());

        pigRunner.forceExecute(context);

        Parameters.FROM_DATE.put(context, "20131121");
        Parameters.TO_DATE.put(context, "20131121");

        pigRunner.forceExecute(context);
    }

    @Test
    public void test() throws Exception {
    }
}
