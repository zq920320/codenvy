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
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.*;
import com.codenvy.analytics.metrics.ide_usage.BuildAction;
import com.codenvy.analytics.metrics.ide_usage.CodeCompletionsBasedOnIdeUsage;
import com.codenvy.analytics.metrics.ide_usage.OpenProjectAction;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertNotNull;
import static org.testng.AssertJUnit.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestIdeUsageEvents extends BaseTest {

    @BeforeClass
    public void prepare() throws Exception {
        List<Event> events = new ArrayList<>();

        events.add(Event.Builder.createUserCreatedEvent("uid1", "user1@gmail.com","[user1@gmail.com]").withDate("2013-01-01").build());
        events.add(Event.Builder.createUserCreatedEvent("uid2", "user2@gmail.com","[user2@gmail.com]").withDate("2013-01-01").build());
        events.add(Event.Builder.createUserCreatedEvent("uid3", "user3@gmail.com","[user3@gmail.com]").withDate("2013-01-01").build());
        events.add(Event.Builder.createUserCreatedEvent("uid4", "user4@gmail.com","[user4@gmail.com]").withDate("2013-01-01").build());
        events.add(Event.Builder.createUserCreatedEvent("uid5", "user5@gmail.com","[user5@gmail.com]").withDate("2013-01-01").build());

        events.add(Event.Builder.createWorkspaceCreatedEvent("ws1", "wsid1", "user1@gmail.com").withDate("2013-01-01").build());

        events.add(Event.Builder.createIDEUsageEvent("user1@gmail.com", "ws1", "action1", "src1", "project1", "type1", "p1=v1,p2=v2")
                                .withDate("2013-01-01").build());
        events.add(Event.Builder.createIDEUsageEvent("user2@gmail.com", null, null, null, null, null, null)
                                .withDate("2013-01-01").build());
        events.add(Event.Builder.createIDEUsageEvent("user3@gmail.com", null, CodeCompletionsBasedOnIdeUsage.ACTION_ID, null, null, null, null)
                                .withDate("2013-01-01").build());
        events.add(Event.Builder.createIDEUsageEvent("user4@gmail.com", null, CodeCompletionsBasedOnIdeUsage.ACTION_ID, null, null, null, null)
                                .withDate("2013-01-01").build());
        events.add(Event.Builder.createIDEUsageEvent("user5@gmail.com", null, CodeCompletionsBasedOnIdeUsage.ACTION_ID, null, null, null, null)
                                .withDate("2013-01-01").build());
        events.add(Event.Builder.createIDEUsageEvent("user5@gmail.com", "testWs", OpenProjectAction.ACTION_ID, null, "testProject", null, null)
                                .withDate("2013-01-01").withTime("11:00:00").build());
        events.add(Event.Builder.createIDEUsageEvent("user5@gmail.com", "testWs", BuildAction.ACTION_ID, null, "testProject", "spring", null)
                                .withDate("2013-01-01").withTime("11:01:00").build());
        events.add(Event.Builder.createIDEUsageEvent("user5@gmail.com", "testWs", BuildAction.ACTION_ID, null, "testProject", "spring", null)
                                .withDate("2013-01-01").withTime("11:02:00").build());

        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.USERS_PROFILES, MetricType.USERS_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.WORKSPACES_PROFILES, MetricType.WORKSPACES_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.WORKSPACES_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.IDE_USAGE_EVENTS, MetricType.IDE_USAGES).getParamsAsMap());
        pigServer.execute(ScriptType.IDE_USAGE_EVENTS, builder.build());
    }

    @Test
    public void scriptShouldStoreNotNullParameters() throws Exception {
        DBObject filter = new BasicDBObject("user", "uid1");

        DBCollection collection = mongoDb.getCollection(MetricType.IDE_USAGES.toString().toLowerCase());
        DBCursor cursor = collection.find(filter);

        assertEquals(1, cursor.size());

        DBObject dbObject = cursor.next();
        assertEquals(13, dbObject.keySet().size());
        assertEquals("uid1", dbObject.get("user"));
        assertEquals("wsid1", dbObject.get("ws"));
        assertEquals(1, dbObject.get("registered_user"));
        assertEquals(1, dbObject.get("persistent_ws"));
        assertEquals("action1", dbObject.get("action"));
        assertEquals("src1", dbObject.get("source"));
        assertEquals("project1", dbObject.get("project"));
        assertEquals("uid1/wsid1/project1", dbObject.get("project_id"));
        assertEquals("type1", dbObject.get("project_type"));
        assertEquals("v1", dbObject.get("p1"));
        assertEquals("v2", dbObject.get("p2"));
        assertNotNull(dbObject.get("_id"));
        assertNotNull(dbObject.get("date"));
    }

    @Test
    public void scriptShouldNotStoreNullParameters() throws Exception {
        DBObject filter = new BasicDBObject("user", "uid2");

        DBCollection collection = mongoDb.getCollection(MetricType.IDE_USAGES.toString().toLowerCase());
        DBCursor cursor = collection.find(filter);

        assertEquals(1, cursor.size());

        DBObject dbObject = cursor.next();
        assertEquals(6, dbObject.keySet().size());
        assertEquals("uid2", dbObject.get("user"));
        assertEquals("default", dbObject.get("ws"));
        assertEquals(1, dbObject.get("registered_user"));
        assertEquals(0, dbObject.get("persistent_ws"));
        assertNotNull(dbObject.get("_id"));
        assertNotNull(dbObject.get("date"));
    }

    @Test
    public void testEditorSingleActions() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.CODE_COMPLETIONS_BASED_ON_IDE_USAGES);
        Assert.assertEquals(metric.getValue(Context.EMPTY), LongValueData.valueOf(3));
    }

    @Test
    public void testIDEMenuSingleActions() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.BUILD_ACTION);
        Assert.assertEquals(metric.getValue(Context.EMPTY), LongValueData.valueOf(2));

        ListValueData data = (ListValueData) ((Expandable) metric).getExpandedValue(Context.EMPTY);
        assertEquals(data.getAll().size(), 1);

        Map<String, ValueData> record = ((MapValueData)data.getAll().get(0)).getAll();
        assertEquals("uid5/testWs/testProject", record.get(AbstractMetric.PROJECT_ID).getAsString());
    }

    @Test
    public void testGetDescriptionOfIdeUsageMetric() {
        Metric metric = MetricFactory.getMetric(MetricType.ADD_TO_INDEX_ACTION);
        Assert.assertEquals(metric.getDescription(), "Add To Index");


        metric = MetricFactory.getMetric(MetricType.BUILD_ACTION);
        Assert.assertEquals(metric.getDescription(), "Build");
    }
}
