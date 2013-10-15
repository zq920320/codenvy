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
package com.codenvy.analytics.modules.pigexecutor;

import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.codenvy.analytics.modules.pigexecutor.impl.PigExecutorServiceImpl;

/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
public class TestPigScriptsExecutorService {

    @Test
    public void testExecution() throws Exception {
       System.setProperty(PigExecutorService.ANALYTICS_PIG_EXECUTOR_CONFIG_PROPERTY, "test-pig-script-executor-config.xml");

        PigExecutorServiceImpl executorService = PigExecutorServiceImpl.getInstance();
        
        assertEquals(executorService.getConfiguration().getExecutions().size(), 2);

        try {
            // wait 10 seconds minutes to show jobs
            Thread.sleep(10L * 1000L);
            // executing...
        } catch (Exception e) {
        }

        executorService.shutdown();

        assertTrue(executorService.getNumberOfScriptsExecuted() > 0);
    }
}
