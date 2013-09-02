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
import com.codenvy.analytics.metrics.DataProcessing;
import com.codenvy.analytics.metrics.MetricParameter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.metrics.value.FSValueDataManager;
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.metrics.value.MapStringListValueData;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestScriptUsersProfiles extends BaseTest {

    @BeforeMethod
    public void setUp() throws Exception {
        FileUtils.deleteDirectory(new File(FSValueDataManager.SCRIPT_LOAD_DIRECTORY));
        FileUtils.deleteDirectory(new File(FSValueDataManager.SCRIPT_STORE_DIRECTORY));

        Map<String, String> props = new HashMap<>();
        MetricParameter.LOAD_DIR.put(props, Utils.getLoadDirFor(MetricType.USER_UPDATE_PROFILE));
        MetricParameter.STORE_DIR.put(props, Utils.getStoreDirFor(MetricType.USER_UPDATE_PROFILE));
        Utils.initLoadStoreDirectories(props);
        
        List<Event> events = new ArrayList<>();
        events.add(
                Event.Builder.createUserUpdateProfile("user1", "f1", "l1", "c1", "p1", "j1").withDate("2013-01-01")
                     .build());
        events.add(
                Event.Builder.createUserUpdateProfile("user2", "", "", "", "", "").withDate("2013-01-01")
                     .build());
        File log = LogGenerator.generateLog(events);

        Map<String, String> context = Utils.newContext();
        MetricParameter.FROM_DATE.put(context, "20130101");
        MetricParameter.TO_DATE.put(context, "20130101");
        MetricParameter.USER.put(context, MetricParameter.USER_TYPES.REGISTERED.name());
        MetricParameter.WS.put(context, MetricParameter.WS_TYPES.ANY.name());
        MetricParameter.LOG.put(context, log.getAbsolutePath());

        DataProcessing.calculateAndStore(MetricType.USER_UPDATE_PROFILE, context);
    }

    @Test
    public void testEventFound() throws Exception {
        Map<String, String> context = Utils.newContext();
        MetricParameter.LOAD_DIR.put(context, Utils.getLoadDirFor(MetricType.USER_UPDATE_PROFILE));

        MapStringListValueData valueData =
                (MapStringListValueData)executeAndReturnResult(ScriptType.USERS_PROFILES,
                                                               new File(BASE_DIR),
                                                               context);

        Map<String, ListStringValueData> all = valueData.getAll();

        assertEquals(all.size(), 2);
        assertEquals(all.get("user1"), new ListStringValueData(Arrays.asList("user1", "f1", "l1", "c1", "p1", "j1")));
        assertEquals(all.get("user2"), new ListStringValueData(Arrays.asList("user2", "", "", "", "", "")));
    }
}
