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
import com.codenvy.analytics.persistent.MongoDataStorage;
import com.codenvy.analytics.pig.PigServer;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;
import com.mongodb.*;

import org.apache.pig.data.Tuple;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.mongodb.util.MyAsserts.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestMongoStorage extends BaseTest {

    private DBCollection dbCollection;
    private Map<String, String> params = new HashMap<>();

    @BeforeClass
    public void prepare() throws IOException {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createTenantCreatedEvent("ws1", "user1@gmail.com")
                        .withDate("2013-01-02")
                        .withTime("00:00:00")
                        .build());

        File log = LogGenerator.generateLog(events);

        Parameters.FROM_DATE.put(params, "20130102");
        Parameters.TO_DATE.put(params, "20130102");
        Parameters.USER.put(params, Parameters.USER_TYPES.REGISTERED.name());
        Parameters.WS.put(params, Parameters.WS_TYPES.PERSISTENT.name());
        Parameters.EVENT.put(params, "tenant-created");
        Parameters.STORAGE_TABLE.put(params, "testmongostorage");
        Parameters.LOG.put(params, log.getAbsolutePath());

        DB db = MongoDataStorage.getDb();
        dbCollection = db.getCollection("testmongostorage");
    }

    @Test
    public void testExecute() throws Exception {
        PigServer.execute(ScriptType.EVENTS, params);

        BasicDBObject dbObject = new BasicDBObject();
        dbObject.put("date", dateFormat.parse("20130102").getTime());

        DBCursor dbCursor = dbCollection.find(dbObject);
        assertEquals(dbCursor.size(), 1);

        DBObject next = dbCursor.next();
        assertEquals(next.get("ws"), "ws1");
        assertEquals(next.get("user"), "user1@gmail.com");
        assertEquals(next.get("value"), 1L);

        Iterator<Tuple> iterator = PigServer.executeAndReturn(ScriptType.TEST_MONGO_LOADER, params);
        assertTrue(iterator.hasNext());

        Tuple tuple = iterator.next();
        assertEquals(tuple.get(0), 1L);
    }
}
