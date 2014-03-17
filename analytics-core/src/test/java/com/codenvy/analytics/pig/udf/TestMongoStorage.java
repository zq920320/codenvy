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
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;
import com.mongodb.*;

import org.apache.pig.data.Tuple;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.mongodb.util.MyAsserts.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestMongoStorage extends BaseTest {

    private DBCollection dbCollection;
    private Context      context;

    @BeforeClass
    public void prepare() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createTenantCreatedEvent("ws1", "user1@gmail.com")
                                .withDate("2013-01-02")
                                .withTime("00:00:00")
                                .build());

        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130102");
        builder.put(Parameters.TO_DATE, "20130102");
        builder.put(Parameters.USER, Parameters.USER_TYPES.REGISTERED.name());
        builder.put(Parameters.WS, Parameters.WS_TYPES.PERSISTENT.name());
        builder.put(Parameters.EVENT, "tenant-created");
        builder.put(Parameters.STORAGE_TABLE, "testmongostorage");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        context = builder.build();

        DB db = mongoDataStorage.getDb();
        dbCollection = db.getCollection("testmongostorage");
    }

    @Test
    public void testExecute() throws Exception {
        pigServer.execute(ScriptType.EVENTS, context);

        BasicDBObject dbObject = new BasicDBObject();
        dbObject.put("date", dateFormat.parse("20130102").getTime());

        DBCursor dbCursor = dbCollection.find(dbObject);
        assertEquals(dbCursor.size(), 1);

        DBObject next = dbCursor.next();
        assertEquals(next.get("ws"), "ws1");
        assertEquals(next.get("user"), "user1@gmail.com");
        assertEquals(next.get("value"), 1L);

        Iterator<Tuple> iterator = pigServer.executeAndReturn(ScriptType.TEST_MONGO_LOADER, context);
        assertTrue(iterator.hasNext());

        Tuple tuple = iterator.next();
        assertEquals(tuple.get(0), 1L);
    }
}
