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
package com.codenvy.analytics.modules.pigexecutor.config;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.assertNotNull;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.testng.annotations.Test;


/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
public class TestPigSciptsExecutorConfig {

    public File testConfig;
    
    @Test
    public void testParsingConfig() throws Exception {
        URL configURL = getClass().getClassLoader().getResource("test-pig-script-executor-config.xml");
        testConfig = new File(configURL.getPath());
        
        IBindingFactory factory = BindingDirectory.getFactory(PigScriptsExecutorConfiguration.class);
        IUnmarshallingContext uctx = factory.createUnmarshallingContext();
        PigScriptsExecutorConfiguration conf =
                 (PigScriptsExecutorConfiguration) uctx.unmarshalDocument(new FileInputStream(testConfig), null);
        
        assertNotNull(conf);
        assertEquals(2, conf.getExecutions().size());
        
        ExecutionEntry executionEntry = conf.getExecutions().get(0);
        assertNotNull(executionEntry);
        assertEquals("0 15 10 ? * *", executionEntry.getSchedule());
        assertEquals(2, executionEntry.getScripts().size());
        
        ScriptEntry scriptEntry = executionEntry.getScripts().get(0);
        assertNotNull(scriptEntry);
        assertEquals("action.pig", scriptEntry.getName());
        assertEquals(5, scriptEntry.getParameters().size());
        assertEquals("LOG", scriptEntry.getParameters().get(0).getName());
        assertEquals("...", scriptEntry.getParameters().get(0).getValue());
        assertEquals("FROM_DATE", scriptEntry.getParameters().get(1).getName());
        assertEquals("...", scriptEntry.getParameters().get(1).getValue());
        assertEquals("TO_DATE", scriptEntry.getParameters().get(2).getName());
        assertEquals("...", scriptEntry.getParameters().get(2).getValue());
        assertEquals("USER", scriptEntry.getParameters().get(3).getName());
        assertEquals("*", scriptEntry.getParameters().get(3).getValue());
        assertEquals("WS", scriptEntry.getParameters().get(4).getName());
        assertEquals("*", scriptEntry.getParameters().get(4).getValue());
        
        
        scriptEntry = executionEntry.getScripts().get(1);
        assertNotNull(scriptEntry);
        assertEquals("set_active_by_users.pig", scriptEntry.getName());
        assertEquals(7, scriptEntry.getParameters().size());
        assertEquals("LOG", scriptEntry.getParameters().get(0).getName());
        assertEquals("...", scriptEntry.getParameters().get(0).getValue());
        assertEquals("FROM_DATE", scriptEntry.getParameters().get(1).getName());
        assertEquals("...", scriptEntry.getParameters().get(1).getValue());
        assertEquals("TO_DATE", scriptEntry.getParameters().get(2).getName());
        assertEquals("...", scriptEntry.getParameters().get(2).getValue());
        assertEquals("USER", scriptEntry.getParameters().get(3).getName());
        assertEquals("*", scriptEntry.getParameters().get(3).getValue());
        assertEquals("WS", scriptEntry.getParameters().get(4).getName());
        assertEquals("*", scriptEntry.getParameters().get(4).getValue());
        assertEquals("EVENT", scriptEntry.getParameters().get(5).getName());
        assertEquals("build", scriptEntry.getParameters().get(5).getValue());
        assertEquals("FIELD", scriptEntry.getParameters().get(6).getName());
        assertEquals("field_value", scriptEntry.getParameters().get(6).getValue());
        
        executionEntry = conf.getExecutions().get(1);
        assertNotNull(executionEntry);
        assertEquals("0 15 10 ? * 6L", executionEntry.getSchedule());
        assertEquals(1, executionEntry.getScripts().size());
        
        scriptEntry = executionEntry.getScripts().get(0);
        assertNotNull(scriptEntry);
        assertEquals("product_usage_time_domains.pig", scriptEntry.getName());
        assertEquals(5, scriptEntry.getParameters().size());
        assertEquals("LOG", scriptEntry.getParameters().get(0).getName());
        assertEquals("...", scriptEntry.getParameters().get(0).getValue());
        assertEquals("FROM_DATE", scriptEntry.getParameters().get(1).getName());
        assertEquals("...", scriptEntry.getParameters().get(1).getValue());
        assertEquals("TO_DATE", scriptEntry.getParameters().get(2).getName());
        assertEquals("...", scriptEntry.getParameters().get(2).getValue());
        assertEquals("USER", scriptEntry.getParameters().get(3).getName());
        assertEquals("*", scriptEntry.getParameters().get(3).getValue());
        assertEquals("WS", scriptEntry.getParameters().get(4).getName());
        assertEquals("*", scriptEntry.getParameters().get(4).getValue());
    }
    
    
}
