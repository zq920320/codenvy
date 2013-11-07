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
package com.codenvy.analytics.pig.udf;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.pig.PigServer;
import com.codenvy.analytics.pig.scripts.EventType;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;
import com.mongodb.*;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mongodb.util.MyAsserts.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestMongoStorage extends BaseTest {

    private Map<String, String> params = new HashMap<>();

    @BeforeClass
    public void prepare() throws IOException {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createTenantCreatedEvent("ws1", "user1").withDate("2013-01-01").build());
        events.add(Event.Builder.createTenantCreatedEvent("ws2", "user1").withDate("2013-01-01").build());

        File log = LogGenerator.generateLog(events);

        Parameters.FROM_DATE.put(params, "20130101");
        Parameters.TO_DATE.put(params, "20130101");
        Parameters.USER.put(params, Parameters.USER_TYPES.REGISTERED.name());
        Parameters.WS.put(params, Parameters.WS_TYPES.PERSISTENT.name());
        Parameters.EVENT.put(params, EventType.TENANT_CREATED.toString());
        Parameters.METRIC.put(params, "test");
        Parameters.LOG.put(params, log.getAbsolutePath());
    }

    @Test
    public void testExecute() throws Exception {
        PigServer.execute(ScriptType.NUMBER_OF_EVENTS, params);

        MongoClient mongoClient = new MongoClient(MONGO_CLIENT_URI);
        DB db = mongoClient.getDB(MONGO_CLIENT_URI.getDatabase());
        DBCollection dbCollection = db.getCollection(MONGO_CLIENT_URI.getCollection());

        BasicDBObject dbObject = new BasicDBObject();
        dbObject.put("_id", 20130101);

        DBCursor dbCursor = dbCollection.find(dbObject);
        assertEquals(dbCursor.size(), 1);

        DBObject next = dbCursor.next();
        assertEquals(next.get("value"), 2L);

        mongoClient.close();
    }
}
