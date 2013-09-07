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


package com.codenvy.analytics.server;

import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.TimeUnit;
import com.codenvy.analytics.metrics.value.FSValueDataManager;
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
        System.setProperty(PigScriptExecutor.ANALYTICS_LOGS_DIRECTORY_PROPERTY, FSValueDataManager.RESULT_DIRECTORY);
        
        Map<String, String> filters = new HashMap<>();
        filters.put(MetricFilter.USER.name(), "gmail.com");

        TimeLineServiceImpl service = new TimeLineServiceImpl();
        service.getData(TimeUnit.DAY, filters);
    }
}
