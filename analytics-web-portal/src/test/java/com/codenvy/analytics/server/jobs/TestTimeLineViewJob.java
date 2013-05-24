/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.jobs;

import com.codenvy.analytics.scripts.executor.pig.PigScriptExecutor;

import org.testng.annotations.Test;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestTimeLineViewJob {

    @Test
    public void testRun() throws Exception {
        System.setProperty(PigScriptExecutor.ANALYTICS_LOGS_DIRECTORY_PROPERTY, PigScriptExecutor.RESULT_DIRECTORY);

        TimeLineViewJob job = new TimeLineViewJob();
        job.execute(null);
    }
}
