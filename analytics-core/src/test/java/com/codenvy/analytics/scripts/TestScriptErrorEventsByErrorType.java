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
package com.codenvy.analytics.scripts;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.MetricParameter;
import com.codenvy.analytics.metrics.value.MapStringLongValueData;
import com.codenvy.analytics.scripts.util.LogGenerator;


/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
public class TestScriptErrorEventsByErrorType extends BaseTest {
    
    @Test
    public void testExecute() throws Exception {
        List<String> strings = new ArrayList<String>();
        strings.add("127.0.0.1 2013-09-02 08:00:32,109[manager-cleaner]  [ERROR] [org.d.errortype 1]  error message 1");
        strings.add("127.0.0.1 2013-09-02 08:00:49,929[pool-8-thread-4]  [INFO ] [o.e.ide.IDESessionService 70]  information 1");
        strings.add("127.0.0.1 2013-09-02 09:00:32,109[manager-cleaner]  [ERROR] [errortype 2]  error message 2");
        strings.add("127.0.0.1 2013-09-02 10:00:32,109[manager-cleaner]  [ERROR] [org.d.errortype 1]  error message 3");
        strings.add("127.0.0.1 2013-09-02 11:00:32,109[manager-cleaner]  [ERROR] [errortype 2]  error message 4");
        strings.add("127.0.0.1 2013-09-02 14:00:32,109[manager-cleaner]  [ERROR] [errorType 2]  error message 5");
        strings.add("127.0.0.1 2013-09-03 14:00:32,109[manager-cleaner]  [ERROR] [errortype 3]  error message 1");

        File log = LogGenerator.generateLogByStrings(strings);
        
        Map<String, String> params = new HashMap<String, String>();
        MetricParameter.FROM_DATE.put(params, "20130902");
        MetricParameter.TO_DATE.put(params, "20130902");

        MapStringLongValueData value =
                (MapStringLongValueData)executeAndReturnResult(ScriptType.ERROR_EVENTS_BY_ERROR_TYPE, log, params);

        assertEquals(value.getAll().size(), 3);
        assertTrue(value.getAll().containsKey("org.d.errortype"));
        assertTrue(value.getAll().containsKey("errortype"));
        assertTrue(value.getAll().containsKey("errortype"));
        
        assertEquals(value.getAll().get("org.d.errortype").longValue(), 2L);
        assertEquals(value.getAll().get("errortype").longValue(), 2L);
        assertEquals(value.getAll().get("errorType").longValue(), 1L);
    }

}
