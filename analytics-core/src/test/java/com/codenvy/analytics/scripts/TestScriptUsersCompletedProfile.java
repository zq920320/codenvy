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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

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
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
public class TestScriptUsersCompletedProfile extends BaseTest {

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
    public void testScriptUsersCompletedProfile() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createUserUpdateProfile("us2@gmail.com", "f2", "l2", "company1", "t1", "jt1")
                                .withDate("2013-01-01").build());
        events.add(Event.Builder.createUserUpdateProfile("us1@gmail.com", "f2", "l2", "company1", "t1", "jt1")
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
        executeAndReturnResult(ScriptType.UPDATE_PROFILE_BY_USERS, log, context);

        // check completed profile
        Map<String, String> props = Utils.newContext();
        MetricParameter.LOAD_DIR.put(props, storage);
        LongValueData completed =
                                  (LongValueData)executeAndReturnResult(ScriptType.USERS_COMPLETED_PROFILE,
                                                                        LogGenerator.generateLog(new ArrayList<Event>()), props);

        assertEquals(completed.getAsLong(), 2L);
    }
}
