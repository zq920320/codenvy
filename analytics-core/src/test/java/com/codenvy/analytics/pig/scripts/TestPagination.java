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
package com.codenvy.analytics.pig.scripts;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.UsersProfiles;
import com.codenvy.analytics.pig.PigServer;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mongodb.util.MyAsserts.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestPagination extends BaseTest {

    private Map<String, String> params;

    @BeforeClass
    public void prepare() throws IOException {
        params = Utils.newContext();

        List<Event> events = new ArrayList<>();

        events.add(Event.Builder.createUserUpdateProfile("user1@gmail.com", "f2", "l2", "company", "11", "1")
                        .withDate("2013-01-01").build());
        events.add(Event.Builder.createUserUpdateProfile("user2@gmail.com", "f2", "l2", "company", "11", "1")
                        .withDate("2013-01-01").build());
        events.add(Event.Builder.createUserUpdateProfile("user3@gmail.com", "f2", "l2", "company", "11", "1")
                        .withDate("2013-01-01").build());
        events.add(Event.Builder.createUserUpdateProfile("user4@gmail.com", "f2", "l2", "company", "11", "1")
                        .withDate("2013-01-01").build());
        events.add(Event.Builder.createUserUpdateProfile("user5@gmail.com", "f2", "l2", "company", "11", "1")
                        .withDate("2013-01-01").build());
        File log = LogGenerator.generateLog(events);

        Parameters.FROM_DATE.put(params, "20130101");
        Parameters.TO_DATE.put(params, "20130101");
        Parameters.USER.put(params, Parameters.USER_TYPES.REGISTERED.name());
        Parameters.WS.put(params, Parameters.WS_TYPES.ANY.name());
        Parameters.STORAGE_TABLE.put(params, "testpagination");
        Parameters.LOG.put(params, log.getAbsolutePath());

        PigServer.execute(ScriptType.USER_UPDATE_PROFILE, params);
    }

    @Test
    public void testSortAsc() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.SORT.put(context, "+_id");

        Metric metric = new TestUserProfile();

        ListValueData value = (ListValueData)metric.getValue(context);
        assertEquals(value.size(), 5);

        List<ValueData> all = value.getAll();

        MapValueData item = (MapValueData)all.get(0);
        assertEquals(item.getAll().get("_id").getAsString(), "user1@gmail.com");

        item = (MapValueData)all.get(1);
        assertEquals(item.getAll().get("_id").getAsString(), "user2@gmail.com");

        item = (MapValueData)all.get(2);
        assertEquals(item.getAll().get("_id").getAsString(), "user3@gmail.com");

        item = (MapValueData)all.get(3);
        assertEquals(item.getAll().get("_id").getAsString(), "user4@gmail.com");

        item = (MapValueData)all.get(4);
        assertEquals(item.getAll().get("_id").getAsString(), "user5@gmail.com");
    }

    @Test
    public void testSortDesc() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.SORT.put(context, "-_id");

        Metric metric = new TestUserProfile();

        ListValueData value = (ListValueData)metric.getValue(context);
        assertEquals(value.size(), 5);

        List<ValueData> all = value.getAll();

        MapValueData item = (MapValueData)all.get(0);
        assertEquals(item.getAll().get("_id").getAsString(), "user5@gmail.com");

        item = (MapValueData)all.get(1);
        assertEquals(item.getAll().get("_id").getAsString(), "user4@gmail.com");

        item = (MapValueData)all.get(2);
        assertEquals(item.getAll().get("_id").getAsString(), "user3@gmail.com");

        item = (MapValueData)all.get(3);
        assertEquals(item.getAll().get("_id").getAsString(), "user2@gmail.com");

        item = (MapValueData)all.get(4);
        assertEquals(item.getAll().get("_id").getAsString(), "user1@gmail.com");
    }

    @Test
    public void testPage1() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.SORT.put(context, "+_id");
        Parameters.PAGE.put(context, "1");
        Parameters.PER_PAGE.put(context, "1");

        Metric metric = new TestUserProfile();

        ListValueData value = (ListValueData)metric.getValue(context);
        assertEquals(value.size(), 1);

        List<ValueData> all = value.getAll();

        MapValueData item = (MapValueData)all.get(0);
        assertEquals(item.getAll().get("_id").getAsString(), "user1@gmail.com");
    }

    @Test
    public void testPage3() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.SORT.put(context, "+_id");
        Parameters.PAGE.put(context, "3");
        Parameters.PER_PAGE.put(context, "1");

        Metric metric = new TestUserProfile();

        ListValueData value = (ListValueData)metric.getValue(context);
        assertEquals(value.size(), 1);

        List<ValueData> all = value.getAll();

        MapValueData item = (MapValueData)all.get(0);
        assertEquals(item.getAll().get("_id").getAsString(), "user3@gmail.com");
    }

    @Test
    public void testPage5() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.SORT.put(context, "+_id");
        Parameters.PAGE.put(context, "5");
        Parameters.PER_PAGE.put(context, "1");

        Metric metric = new TestUserProfile();

        ListValueData value = (ListValueData)metric.getValue(context);
        assertEquals(value.size(), 1);

        List<ValueData> all = value.getAll();

        MapValueData item = (MapValueData)all.get(0);
        assertEquals(item.getAll().get("_id").getAsString(), "user5@gmail.com");
    }

    @Test
    public void testUnExistedPage() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.SORT.put(context, "+_id");
        Parameters.PAGE.put(context, "6");
        Parameters.PER_PAGE.put(context, "1");

        Metric metric = new TestUserProfile();

        ListValueData value = (ListValueData)metric.getValue(context);
        assertEquals(value.size(), 0);
    }

    public class TestUserProfile extends UsersProfiles {

        @Override
        public String getStorageTable() {
            return "testpagination";
        }
    }
}
