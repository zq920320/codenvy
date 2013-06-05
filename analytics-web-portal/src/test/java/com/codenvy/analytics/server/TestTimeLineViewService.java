/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server;

import com.codenvy.analytics.client.TimeLineService;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.TimeUnit;
import com.codenvy.analytics.scripts.executor.pig.PigScriptExecutor;

import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestTimeLineViewService {

    @Test
    public void testRun() throws Exception {
        System.setProperty(PigScriptExecutor.ANALYTICS_LOGS_DIRECTORY_PROPERTY, PigScriptExecutor.RESULT_DIRECTORY);
        
        Map<String, String> filters = new HashMap<String, String>();
        filters.put(MetricFilter.FILTER_USER.name(), "gmail.com");

        TimeLineService service = new TimeLineServiceImpl();
        service.getData(TimeUnit.DAY, "gmail.com");
    }
}
