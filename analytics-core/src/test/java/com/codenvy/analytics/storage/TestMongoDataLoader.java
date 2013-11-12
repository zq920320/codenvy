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

package com.codenvy.analytics.storage;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.ReadBasedMetric;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.testng.AssertJUnit.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestMongoDataLoader extends BaseTest {

    private DataLoader dataLoader;

    @BeforeClass
    public void prepare() throws Exception {
        MongoClient mongoClient = new MongoClient(MONGO_CLIENT_URI);
        DB db = mongoClient.getDB(MONGO_CLIENT_URI.getDatabase());
        DBCollection dbCollection = db.getCollection(MONGO_CLIENT_URI.getCollection());

        BasicDBObject dbObject = new BasicDBObject();
        dbObject.put("_id", dateFormat.parse("20130910").getTime());
        dbObject.put("value", 100L);
        dbCollection.save(dbObject);

        dbObject.put("_id", dateFormat.parse("20130911").getTime());
        dbCollection.save(dbObject);

        mongoClient.close();

        dataLoader = DataLoaderFactory.createDataLoader();
    }

    @Test
    public void testSingleDay() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130910");
        Parameters.TO_DATE.put(context, "20130910");
        ValueData valueData = dataLoader.loadValue(new TestMetric(), context);

        AssertJUnit.assertEquals(new LongValueData(100), valueData);
    }

    @Test
    public void testPeriod() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130910");
        Parameters.TO_DATE.put(context, "20130911");
        ValueData valueData = dataLoader.loadValue(new TestMetric(), context);

        assertEquals(new LongValueData(200), valueData);
    }

    private class TestMetric extends ReadBasedMetric {

        private TestMetric() {
            super("test");
        }

        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return LongValueData.class;
        }

        @Override
        public Set<Parameters> getParams() {
            return Collections.emptySet();
        }

        @Override
        public String getDescription() {
            return null;
        }
    }
}
