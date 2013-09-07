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

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.MetricParameter;
import com.codenvy.analytics.metrics.value.MapStringLongValueData;
import com.codenvy.analytics.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
public class TestScriptErrorEventsByErrorType extends BaseTest {

    private File                log;
    private Map<String, String> context;

    @BeforeClass
    public void prepare() throws Exception {
        String baseMsg = "127.0.0.1 2013-09-02 08:00:32,109[main]";

        List<String> strings = new ArrayList<>();
        strings.add(baseMsg + " [ERROR] [org.d.ErrorClass 1]  [][][] - error message 1");
        strings.add(baseMsg + " [ERROR] [org.d.ErrorClass 1]  [][][] - error message 3");
        strings.add(baseMsg + " [INFO ] [o.e.ide.ErrorClass 70]  [][][] - information 1");
        strings.add(baseMsg + " [ERROR] [ErrorClass 2]  [][][] - error message 2");
        strings.add(baseMsg + " [ERROR] [ErrorClass 2]  [][][] - error message 5");
        strings.add(baseMsg + " [ERROR] [ErrorClass 3]  [][][] - error message 1");
        strings.add(baseMsg + " [ERROR] [C.[.[.[.[ErrorClass 20]  [][][] - error message 4");

        log = LogGenerator.generateLogByStrings(strings);

        context = new HashMap<>();
        MetricParameter.FROM_DATE.put(context, "20130902");
        MetricParameter.TO_DATE.put(context, "20130902");
    }

    @Test
    public void testExecute() throws Exception {
        MapStringLongValueData value =
                (MapStringLongValueData)executeAndReturnResult(ScriptType.ERROR_TYPES, log, context);

        assertEquals(value.getAll().size(), 4);
        assertTrue(value.getAll().containsKey("org.d.ErrorClass 1"));
        assertTrue(value.getAll().containsKey("ErrorClass 2"));
        assertTrue(value.getAll().containsKey("ErrorClass 3"));
        assertTrue(value.getAll().containsKey("C.[.[.[.[ErrorClass 20"));

        assertEquals(value.getAll().get("org.d.ErrorClass 1").longValue(), 2L);
        assertEquals(value.getAll().get("ErrorClass 2").longValue(), 2L);
        assertEquals(value.getAll().get("ErrorClass 3").longValue(), 1L);
        assertEquals(value.getAll().get("C.[.[.[.[ErrorClass 20").longValue(), 1L);
    }
}
