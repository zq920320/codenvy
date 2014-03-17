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
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.users.UsersProfilesList;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.util.MyAsserts.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestPagination extends BaseTest {

    @BeforeClass
    public void prepare() throws Exception {
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

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.USER, Parameters.USER_TYPES.REGISTERED.name());
        builder.put(Parameters.WS, Parameters.WS_TYPES.ANY.name());
        builder.put(Parameters.STORAGE_TABLE, "testpagination");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        pigServer.execute(ScriptType.USERS_UPDATE_PROFILES, builder.build());
    }

    @Test
    public void testSortAsc() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.SORT, "+_id");

        Metric metric = new TestUsersProfilesList();

        ListValueData value = (ListValueData)metric.getValue(builder.build());
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
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.SORT, "-_id");

        Metric metric = new TestUsersProfilesList();

        ListValueData value = (ListValueData)metric.getValue(builder.build());
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
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.SORT, "+_id");
        builder.put(Parameters.PAGE, 1);
        builder.put(Parameters.PER_PAGE, 1);

        Metric metric = new TestUsersProfilesList();

        ListValueData value = (ListValueData)metric.getValue(builder.build());
        assertEquals(value.size(), 1);

        List<ValueData> all = value.getAll();

        MapValueData item = (MapValueData)all.get(0);
        assertEquals(item.getAll().get("_id").getAsString(), "user1@gmail.com");
    }

    @Test
    public void testPage3() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.SORT, "+_id");
        builder.put(Parameters.PAGE, 3);
        builder.put(Parameters.PER_PAGE, 1);

        Metric metric = new TestUsersProfilesList();

        ListValueData value = (ListValueData)metric.getValue(builder.build());
        assertEquals(value.size(), 1);

        List<ValueData> all = value.getAll();

        MapValueData item = (MapValueData)all.get(0);
        assertEquals(item.getAll().get("_id").getAsString(), "user3@gmail.com");
    }

    @Test
    public void testPage5() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.SORT, "+_id");
        builder.put(Parameters.PAGE, 5);
        builder.put(Parameters.PER_PAGE, 1);

        Metric metric = new TestUsersProfilesList();

        ListValueData value = (ListValueData)metric.getValue(builder.build());
        assertEquals(value.size(), 1);

        List<ValueData> all = value.getAll();

        MapValueData item = (MapValueData)all.get(0);
        assertEquals(item.getAll().get("_id").getAsString(), "user5@gmail.com");
    }

    @Test
    public void testUnExistedPage() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.SORT, "+_id");
        builder.put(Parameters.PAGE, 6);
        builder.put(Parameters.PER_PAGE, 1);


        Metric metric = new TestUsersProfilesList();

        ListValueData value = (ListValueData)metric.getValue(builder.build());
        assertEquals(value.size(), 0);
    }

    public class TestUsersProfilesList extends UsersProfilesList {

        @Override
        public String getStorageCollectionName() {
            return "testpagination";
        }
    }
}
