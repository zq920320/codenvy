/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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
package com.codenvy.analytics.persistent;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.Injector;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.AbstractLongValueResulted;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Parameters;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.testng.AssertJUnit.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestMongoDataLoader extends BaseTest {

    private DataLoader dataLoader;

    @BeforeClass
    public void prepare() throws Exception {
        MongoDataStorage mongoDataStorage = Injector.getInstance(MongoDataStorage.class);
        dataLoader = mongoDataStorage.createdDataLoader();

        DB db = mongoDataStorage.getDb();
        DBCollection dbCollection = db.getCollection("test");

        BasicDBObject dbObject = new BasicDBObject();
        dbObject.put("_id", UUID.randomUUID().toString());
        dbObject.put("date", dateFormat.parse("20130910").getTime());
        dbObject.put("value", 100L);
        dbCollection.save(dbObject);

        dbObject.put("_id", UUID.randomUUID().toString());
        dbObject.put("date", dateFormat.parse("20130911").getTime());
        dbCollection.save(dbObject);
    }

    @Test
    public void testSingleDay() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130910");
        builder.put(Parameters.TO_DATE, "20130910");

        ValueData valueData = dataLoader.loadValue(new TestLongValueResulted(), builder.build());

        AssertJUnit.assertEquals(new LongValueData(100), valueData);
    }

    @Test
    public void testPeriod() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130910");
        builder.put(Parameters.TO_DATE, "20130911");

        ValueData valueData = dataLoader.loadValue(new TestLongValueResulted(), builder.build());

        assertEquals(new LongValueData(200), valueData);
    }

    private class TestLongValueResulted extends AbstractLongValueResulted {

        private TestLongValueResulted() {
            super("test", null);
        }

        @Override
        public String getDescription() {
            return null;
        }
    }
}
