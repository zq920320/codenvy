/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
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
import com.codenvy.analytics.datamodel.ValueDataUtil;
import com.codenvy.analytics.metrics.*;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertNotNull;
import static org.testng.AssertJUnit.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestUsersActivity extends BaseTest {

    @BeforeClass
    public void prepare() throws Exception {
        List<Event> events = new ArrayList<>();

        events.add(Event.Builder.createUserCreatedEvent("uid1", "user1","[user1]").withDate("2013-01-01").build());
        events.add(Event.Builder.createUserCreatedEvent("uid2", "user2","[user2]").withDate("2013-01-01").build());
        events.add(Event.Builder.createUserCreatedEvent("uid3", "user3","[user3]").withDate("2013-01-01").build());

        events.add(Event.Builder.createWorkspaceCreatedEvent("ws1", "wsid1", "user1").withDate("2013-01-01").build());
        events.add(Event.Builder.createWorkspaceCreatedEvent("ws2", "wsid2", "user2").withDate("2013-01-01").build());

        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.USERS_PROFILES, MetricType.USERS_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.WORKSPACES_PROFILES, MetricType.WORKSPACES_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.WORKSPACES_PROFILES, builder.build());


        events = new ArrayList<>();
        events.add(Event.Builder.createIDEUsageEvent("user1",
                                                     "ws1",
                                                     "action1",
                                                     "src1",
                                                     "project1",
                                                     "type1",
                                                     "p1=v1,p2=v2").withDate("2013-01-01").build());
        events.add(Event.Builder.createIDEUsageEvent("user2",
                                                     "ws2",
                                                     "action2",
                                                     "src2",
                                                     "project2",
                                                     "type2",
                                                     null).withDate("2013-01-01").build());
        events.add(Event.Builder.createIDEUsageEvent("user3",
                                                     null,
                                                     null,
                                                     null,
                                                     "project2",
                                                     "type2",
                                                     "p2=v2").withDate("2013-01-01").build());

        log = LogGenerator.generateLog(events);

        builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        builder.putAll(scriptsManager.getScript(ScriptType.USERS_ACTIVITY, MetricType.USERS_ACTIVITY_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_ACTIVITY, builder.build());
    }

    @Test
    public void shouldStoreAllParametersFromMessage() throws Exception {
        DBObject filter = new BasicDBObject("user", "uid1");

        DBCollection collection = mongoDb.getCollection(MetricType.USERS_ACTIVITY.toString().toLowerCase());
        DBCursor cursor = collection.find(filter);

        assertEquals(1, cursor.size());

        DBObject dbObject = cursor.next();
        assertEquals(14, dbObject.keySet().size());
        assertEquals("ide-usage", dbObject.get("event"));
        assertEquals("uid1", dbObject.get("user"));
        assertEquals("wsid1", dbObject.get("ws"));
        assertEquals(1, dbObject.get("registered_user"));
        assertEquals(1, dbObject.get("persistent_ws"));
        assertEquals("action1", dbObject.get("action"));
        assertEquals("src1", dbObject.get("source"));
        assertEquals("project1", dbObject.get("project"));
        assertEquals("type1", dbObject.get("type"));
        assertEquals("v1", dbObject.get("p1"));
        assertEquals("v2", dbObject.get("p2"));
        assertNotNull(dbObject.get("_id"));
        assertNotNull(dbObject.get("date"));
        assertNotNull(dbObject.get("message"));
    }

    @Test(dataProvider = "parametersFilterProvider")
    public void testParametersFilter(String parameters, Integer sizeOfResult) throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.USERS_ACTIVITY_LIST);

        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.PARAMETERS, parameters);

        ListValueData list = ValueDataUtil.getAsList(metric, builder.build());

        Assert.assertEquals(sizeOfResult.intValue(), list.size());
    }

    @Test(dataProvider = "actionFilterProvider")
    public void testActionFilter(String action, Integer sizeOfResult) throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.USERS_ACTIVITY_LIST);

        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.ACTION, action);

        ListValueData list = ValueDataUtil.getAsList(metric, builder.build());

        Assert.assertEquals(sizeOfResult.intValue(), list.size());
    }


    @DataProvider(name = "parametersFilterProvider")
    public Object[][] parametersFilterProvider() {
        return new Object[][]{{"project=project2,type=type2", 2},
                              {"project=project2,type=type3", 0},
                              {"p2=v2", 2},
                              {"", 3}};
    }

    @DataProvider(name = "actionFilterProvider")
    public Object[][] actionFilterProvider() {
        return new Object[][]{{"action1", 1},
                              {"IDE usage", 1}};
    }
}
