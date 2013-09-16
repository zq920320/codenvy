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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.DataProcessing;
import com.codenvy.analytics.metrics.MetricParameter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.metrics.value.FSValueDataManager;
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.MapStringListValueData;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
public class TestScriptUpdateProfileByUsers extends BaseTest {

    @BeforeMethod
    public void setUp() throws Exception {
        FileUtils.deleteDirectory(new File(FSValueDataManager.SCRIPT_LOAD_DIRECTORY));
        FileUtils.deleteDirectory(new File(FSValueDataManager.SCRIPT_STORE_DIRECTORY));
        
        Map<String, String> context = new HashMap<>();
        MetricParameter.LOAD_DIR.put(context, Utils.getLoadDirFor(MetricType.USER_UPDATE_PROFILE));
        MetricParameter.STORE_DIR.put(context, Utils.getStoreDirFor(MetricType.USER_UPDATE_PROFILE));

        Utils.initLoadStoreDirectories(context);
    }
    
    @Test
    public void testScriptUpdateProfileByUsers() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createUserUpdateProfile("u2@gmail.com", "f2", "l2", "company", "t1", "jt1")
                        .withDate("2013-01-01").build());
        events.add(Event.Builder.createUserUpdateProfile("u1@gmail.com", "f2", "l2", "company", "t1", "jt1")
                        .withDate("2013-01-01").build());
        File log = LogGenerator.generateLog(events);

        Map<String, String> context = Utils.newContext();
        context.put(MetricParameter.FROM_DATE.name(), "20130101");
        context.put(MetricParameter.TO_DATE.name(), "20130101");
        MetricParameter.LOG.put(context, log.getAbsolutePath());
        MetricParameter.LOAD_DIR.put(context, Utils.getLoadDirFor(MetricType.USER_UPDATE_PROFILE));
        String storage = Utils.getStoreDirFor(MetricType.USER_UPDATE_PROFILE);
        MetricParameter.STORE_DIR.put(context, storage);
        MetricParameter.USER.put(context, MetricParameter.USER_TYPES.ANY.name());
        MetricParameter.WS.put(context, MetricParameter.WS_TYPES.ANY.name());
        
        DataProcessing.calculateAndStore(MetricType.USER_UPDATE_PROFILE, context);

        MapStringListValueData value =
            (MapStringListValueData)executeAndReturnResult(ScriptType.UPDATE_PROFILE_BY_USERS, log, context);
        
        assertEquals(value.getAll().size(), 2);
        assertEquals(value.getAll().containsKey("u1@gmail.com"), true);
        assertEquals(value.getAll().containsKey("u2@gmail.com"), true);
        
        List<String> list = (List<String>)value.getAll().get("u1@gmail.com").getAll();
        assertTrue(list.contains("u1@gmail.com"));
        assertEquals(list.get(0), "u1@gmail.com");
        assertTrue(list.contains("f2"));
        assertEquals(list.get(1), "f2");
        assertTrue(list.contains("l2"));
        assertEquals(list.get(2), "l2");
        assertTrue(list.contains("company"));
        assertEquals(list.get(3), "company");
        assertTrue(list.contains("t1"));
        assertEquals(list.get(4), "t1");
        assertTrue(list.contains("jt1"));
        assertEquals(list.get(5), "jt1");
        
        list = (List<String>)value.getAll().get("u2@gmail.com").getAll();
        assertTrue(list.contains("u2@gmail.com"));
        assertEquals(list.get(0), "u2@gmail.com");
        assertTrue(list.contains("f2"));
        assertEquals(list.get(1), "f2");
        assertTrue(list.contains("l2"));
        assertEquals(list.get(2), "l2");
        assertTrue(list.contains("company"));
        assertEquals(list.get(3), "company");
        assertTrue(list.contains("t1"));
        assertEquals(list.get(4), "t1");
        assertTrue(list.contains("jt1"));
        assertEquals(list.get(5), "jt1");
        
        //check completed profile
        Map<String, String> props = Utils.newContext();
        MetricParameter.LOAD_DIR.put(props, storage);
        LongValueData completed =
            (LongValueData)executeAndReturnResult(ScriptType.USERS_COMPLETED_PROFILE, LogGenerator.generateLog(new ArrayList<Event>()), props);
        
        assertEquals(completed.getAsLong(), 2L);
        
        // get all users
        props = Utils.newContext();
        props.put(MetricParameter.PARAM.name(), "company");
        MetricParameter.LOAD_DIR.put(props, storage);
        ListStringValueData valueData = (ListStringValueData) executeAndReturnResult(ScriptType.USERS_BY_COMPANY, LogGenerator.generateLog(new ArrayList<Event>()), props);

        assertEquals(valueData.size(), 2);
        assertTrue(valueData.getAll().contains("u1@gmail.com"));
        assertTrue(valueData.getAll().contains("u2@gmail.com"));
        
        events = new ArrayList<>();
        events.add(Event.Builder.createUserUpdateProfile("u1@gmail.com", "f3", "l3", "company", "t1", "jt2")
                        .withDate("2013-01-02").build());
        events.add(Event.Builder.createUserUpdateProfile("u3@gmail.com", "f33", "l33", "company", "t1", "jt1")
                   .withDate("2013-01-02").build());
        log = LogGenerator.generateLog(events);

        MetricParameter.LOAD_DIR.put(context, storage);
        String storage2 = storage + "2";
        MetricParameter.STORE_DIR.put(context, storage2);
        context.put(MetricParameter.FROM_DATE.name(), "20130102");
        context.put(MetricParameter.TO_DATE.name(), "20130102");
        MetricParameter.LOG.put(context, log.getAbsolutePath());

        value = (MapStringListValueData)executeAndReturnResult(ScriptType.UPDATE_PROFILE_BY_USERS, log, context);
        
        assertEquals(value.getAll().size(), 2);
        assertEquals(value.getAll().containsKey("u1@gmail.com"), true);
        
        list = (List<String>)value.getAll().get("u1@gmail.com").getAll();
        assertTrue(list.contains("u1@gmail.com"));
        assertEquals(list.get(0), "u1@gmail.com");
        assertTrue(list.contains("f3"));
        assertEquals(list.get(1), "f3");
        assertTrue(list.contains("l3"));
        assertEquals(list.get(2), "l3");
        assertTrue(list.contains("company"));
        assertEquals(list.get(3), "company");
        assertTrue(list.contains("t1"));
        assertEquals(list.get(4), "t1");
        assertTrue(list.contains("jt2"));
        assertEquals(list.get(5), "jt2");
        
        list = (List<String>)value.getAll().get("u3@gmail.com").getAll();
        assertTrue(list.contains("u3@gmail.com"));
        assertEquals(list.get(0), "u3@gmail.com");
        assertTrue(list.contains("f33"));
        assertEquals(list.get(1), "f33");
        assertTrue(list.contains("l33"));
        assertEquals(list.get(2), "l33");
        assertTrue(list.contains("company"));
        assertEquals(list.get(3), "company");
        assertTrue(list.contains("t1"));
        assertEquals(list.get(4), "t1");
        assertTrue(list.contains("jt1"));
        assertEquals(list.get(5), "jt1");
        
        
        // get all users
        props = Utils.newContext();
        props.put(MetricParameter.PARAM.name(), "company");
        MetricParameter.LOAD_DIR.put(props, storage2);
        valueData = (ListStringValueData) executeAndReturnResult(ScriptType.USERS_BY_COMPANY, LogGenerator.generateLog(new ArrayList<Event>()), props);
        
        assertEquals(valueData.size(), 3);
        assertTrue(valueData.getAll().contains("u1@gmail.com"));
        assertTrue(valueData.getAll().contains("u2@gmail.com"));
        assertTrue(valueData.getAll().contains("u3@gmail.com"));
    }
}
