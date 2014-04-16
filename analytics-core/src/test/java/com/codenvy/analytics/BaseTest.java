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

package com.codenvy.analytics;

import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.persistent.MongoDataStorage;
import com.codenvy.analytics.pig.PigServer;
import com.mongodb.DB;

import org.apache.pig.data.TupleFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:abazko@exoplatform.com">Anatoliy Bazko</a> */
public class BaseTest {
    public static final    String BASE_DIR = "target";
    protected static final Logger LOG      = LoggerFactory.getLogger(BaseTest.class);

    protected final TupleFactory tupleFactory    = TupleFactory.getInstance();
    protected final DateFormat   dateFormat      = new SimpleDateFormat("yyyyMMdd");
    protected final DateFormat   shortDateFormat = new SimpleDateFormat("HH:mm:ss");

    protected final DateFormat fullDateFormat     = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    protected final DateFormat fullDateFormatMils = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");

    protected final Configurator     configurator;
    protected final PigServer        pigServer;
    protected final MongoDataStorage mongoDataStorage;
    protected final DB               mongoDb;

    @BeforeClass
    public void clearDatabase() {
        for (String collectionName : mongoDb.getCollectionNames()) {
            if (collectionName.startsWith("system.")) {           // don't drop system collections
                continue;
            }

            mongoDb.getCollection(collectionName).drop();
        }
    }

    public BaseTest() {
        this.configurator = Injector.getInstance(Configurator.class);
        this.pigServer = Injector.getInstance(PigServer.class);
        this.mongoDataStorage = Injector.getInstance(MongoDataStorage.class);
        this.mongoDb = mongoDataStorage.getDb();
    }

    protected Map<String, Map<String, ValueData>> listToMap(ListValueData valueData, String key) {
        Map<String, Map<String, ValueData>> result = new HashMap<>();

        for (ValueData item : valueData.getAll()) {
            MapValueData row = (MapValueData)item;
            result.put(row.getAll().get(key).getAsString(), row.getAll());
        }

        return result;
    }
}
