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

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.distribution.Version;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.ReadBasedMetric;
import com.mongodb.*;

import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.testng.AssertJUnit.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestMongoDataLoader extends BaseTest {

    private DataLoader dataLoader;

    private MongodProcess mongoProcess;

    @BeforeClass
    public void startMongo() throws Exception {
        MongodStarter starter = MongodStarter.getDefaultInstance();
        MongodExecutable mongoExe = starter.prepare(new MongodConfig(Version.V2_3_0, 12345, false));
        mongoProcess = mongoExe.start();

        MongoClientURI uri = new MongoClientURI("mongodb://localhost:12345/test.test");
        MongoClient mongoClient = new MongoClient(uri);
        DB db = mongoClient.getDB(uri.getDatabase());
        DBCollection dbCollection = db.getCollection(uri.getCollection());

        BasicDBObject dbObject = new BasicDBObject();
        dbObject.put("_id", 20130910);
        dbObject.put("value", 100L);
        dbCollection.save(dbObject);

        dbObject.put("_id", 20130911);
        dbCollection.save(dbObject);

        mongoClient.close();

        dataLoader = DataLoaderFactory.createDataLoader();
    }

    @AfterClass
    public void stopMongo() throws Exception {
        mongoProcess.stop();
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
