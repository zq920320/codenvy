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

import org.apache.pig.data.Tuple;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.mongodb.util.MyAsserts.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestMongoStorage extends BaseTest {

    private Map<String, String> params = new HashMap<>();
    private MongoClient  mongoClient;
    private DBCollection dbCollection;
    private DBCollection dbCollectionRaw;

    @BeforeClass
    public void prepare() throws IOException {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createTenantCreatedEvent("ws1", "user1@gmail.com").withDate("2013-01-02").build());

        File log = LogGenerator.generateLog(events);

        Parameters.FROM_DATE.put(params, "20130102");
        Parameters.TO_DATE.put(params, "20130102");
        Parameters.USER.put(params, Parameters.USER_TYPES.REGISTERED.name());
        Parameters.WS.put(params, Parameters.WS_TYPES.PERSISTENT.name());
        Parameters.EVENT.put(params, EventType.TENANT_CREATED.toString());
        Parameters.METRIC.put(params, "TestMongoStorage");
        Parameters.LOG.put(params, log.getAbsolutePath());

        mongoClient = new MongoClient(MONGO_CLIENT_URI);
        DB db = mongoClient.getDB(MONGO_CLIENT_URI.getDatabase());
        dbCollection = db.getCollection("TestMongoStorage");
        dbCollectionRaw = db.getCollection("TestMongoStorage-raw");
    }

    @AfterClass
    public void cleanup() throws IOException {
        mongoClient.close();
    }

    @Test
    public void testExecute() throws Exception {
        PigServer.execute(ScriptType.NUMBER_OF_EVENTS, params);

        BasicDBObject dbObject = new BasicDBObject();
        dbObject.put("_id", 20130102);

        DBCursor dbCursor = dbCollection.find(dbObject);
        assertEquals(dbCursor.size(), 1);

        DBObject next = dbCursor.next();
        assertEquals(next.get("value"), 1L);

        dbCursor = dbCollectionRaw.find(dbObject);
        assertEquals(dbCursor.size(), 1);

        next = dbCursor.next();
        assertEquals(next.get("ws"), "ws1");
        assertEquals(next.get("user"), "user1@gmail.com");
        assertEquals(next.get("domain"), "gmail.com");
        assertEquals(next.get("value"), 1L);

        Iterator<Tuple> iterator = PigServer.executeAndReturn(ScriptType.TEST_MONGO_LOADER, params);
        assertTrue(iterator.hasNext());

        Tuple tuple = iterator.next();
        assertEquals(tuple.get(0), 20130102L);

        Tuple innerTuple = (Tuple)tuple.get(1);
        assertEquals(innerTuple.get(0), "value");
        assertEquals(innerTuple.get(1), 1L);
    }
}
