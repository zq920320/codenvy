/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.view;

import com.codenvy.analytics.metrics.TimeUnit;
import com.codenvy.analytics.scripts.executor.pig.PigScriptExecutor;
import com.codenvy.analytics.server.TimeLineViewServiceImpl;

import org.testng.annotations.Test;

import java.util.Date;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestTimeLineView {

    @Test
    public void testRun() throws Exception {
        System.setProperty(PigScriptExecutor.ANALYTICS_LOGS_DIRECTORY_PROPERTY, PigScriptExecutor.RESULT_DIRECTORY);

        new TimeLineViewServiceImpl().updateTimelineView(new Date(), TimeUnit.DAY);
        new TimeLineViewServiceImpl().updateTimelineView(new Date(), TimeUnit.WEEK);
        new TimeLineViewServiceImpl().updateTimelineView(new Date(), TimeUnit.MONTH);
    }
}
