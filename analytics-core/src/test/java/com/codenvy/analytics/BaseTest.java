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
package com.codenvy.analytics;

import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.AbstractMetric;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.persistent.CollectionsManagement;
import com.codenvy.analytics.persistent.MongoDataStorage;
import com.codenvy.analytics.pig.PigServer;
import com.codenvy.analytics.services.integrity.TasksIntegrity;
import com.codenvy.analytics.services.metrics.DataComputation;
import com.codenvy.analytics.services.pig.ScriptsManager;
import com.codenvy.commons.lang.NameGenerator;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;

import org.apache.pig.data.TupleFactory;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:abazko@exoplatform.com">Anatoliy Bazko</a> */
public class BaseTest {
    public static final String BASE_DIR = "target";

    protected static final String UID1 = NameGenerator.generate("user_1_", com.codenvy.api.user.server.Constants.ID_LENGTH - 3);
    protected static final String UID2 = NameGenerator.generate("user_2_", com.codenvy.api.user.server.Constants.ID_LENGTH - 3);
    protected static final String UID3 = NameGenerator.generate("user_3_", com.codenvy.api.user.server.Constants.ID_LENGTH - 3);
    protected static final String UID4 = NameGenerator.generate("user_4_", com.codenvy.api.user.server.Constants.ID_LENGTH - 3);
    protected static final String UID5 = NameGenerator.generate("user_5_", com.codenvy.api.user.server.Constants.ID_LENGTH - 3);

    protected static final String AUID1 = NameGenerator.generate("user_a1_", com.codenvy.api.user.server.Constants.ID_LENGTH - 4);
    protected static final String AUID2 = NameGenerator.generate("user_a2_", com.codenvy.api.user.server.Constants.ID_LENGTH - 4);

    protected static final String WID1  = NameGenerator.generate("workspace_1_", com.codenvy.api.workspace.server.Constants.ID_LENGTH - 3);
    protected static final String WID2  = NameGenerator.generate("workspace_2_", com.codenvy.api.workspace.server.Constants.ID_LENGTH - 3);
    protected static final String WID3  = NameGenerator.generate("workspace_3_", com.codenvy.api.workspace.server.Constants.ID_LENGTH - 3);
    protected static final String WID4  = NameGenerator.generate("workspace_4_", com.codenvy.api.workspace.server.Constants.ID_LENGTH - 3);
    protected static final String TWID1 = NameGenerator.generate("workspace_t1_", com.codenvy.api.workspace.server.Constants.ID_LENGTH - 4);
    protected static final String TWID2 = NameGenerator.generate("workspace_t2_", com.codenvy.api.workspace.server.Constants.ID_LENGTH - 4);
    protected static final String TWID3 = NameGenerator.generate("workspace_t3_", com.codenvy.api.workspace.server.Constants.ID_LENGTH - 4);
    protected static final String TWID4 = NameGenerator.generate("workspace_t4_", com.codenvy.api.workspace.server.Constants.ID_LENGTH - 4);

    protected static final Logger LOG = LoggerFactory.getLogger(BaseTest.class);

    protected final TupleFactory tupleFactory    = TupleFactory.getInstance();
    protected final DateFormat   dateFormat      = new SimpleDateFormat("yyyyMMdd");
    protected final DateFormat   shortDateFormat = new SimpleDateFormat("HH:mm:ss");

    public static final DateFormat fullDateFormat     = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final DateFormat fullDateFormatMils = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");

    protected final Configurator          configurator;
    protected final PigServer             pigServer;
    protected final MongoDataStorage      mongoDataStorage;
    protected final DB                    mongoDb;
    protected final ScriptsManager        scriptsManager;
    protected final CollectionsManagement collectionsManagement;

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
        this.scriptsManager = Injector.getInstance(ScriptsManager.class);
        this.collectionsManagement = Injector.getInstance(CollectionsManagement.class);
    }

    protected Map<String, Map<String, ValueData>> listToMap(ListValueData valueData, String key) {
        Map<String, Map<String, ValueData>> result = new HashMap<>();

        for (ValueData item : valueData.getAll()) {
            MapValueData row = (MapValueData)item;
            result.put(row.getAll().get(key).getAsString(), row.getAll());
        }

        return result;
    }

    protected void makeAllUsersRegistered(String collection) {
        mongoDb.getCollection(collection).update(new BasicDBObject(),
                                                 new BasicDBObject("$set", new BasicDBObject(AbstractMetric.REGISTERED_USER, 1L)),
                                                 false,
                                                 true);
    }

    protected void makeAllWsPersisted(String collection) {
        mongoDb.getCollection(collection).update(new BasicDBObject(),
                                                 new BasicDBObject("$set", new BasicDBObject(AbstractMetric.PERSISTENT_WS, 1L)),
                                                 false,
                                                 true);
    }

    protected void addRegisteredUser(String id, String alias) {
        mongoDb.getCollection(MetricType.USERS_PROFILES.toString().toLowerCase()).insert(
                new BasicDBObject(AbstractMetric.REGISTERED_USER, 1L)
                        .append(AbstractMetric.ID, id)
                        .append(AbstractMetric.ALIASES, Utils.toArray(alias)));
    }

    protected void addPersistentWs(String id, String name) {
        mongoDb.getCollection(MetricType.WORKSPACES_PROFILES.toString().toLowerCase()).insert(
                new BasicDBObject(AbstractMetric.PERSISTENT_WS, 1L)
                        .append(AbstractMetric.ID, id)
                        .append(AbstractMetric.WS_NAME, name));
    }

    protected void addTemporaryWs(String id, String name) {
        mongoDb.getCollection(MetricType.WORKSPACES_PROFILES.toString().toLowerCase()).insert(
                new BasicDBObject(AbstractMetric.PERSISTENT_WS, 0L)
                        .append(AbstractMetric.ID, id)
                        .append(AbstractMetric.WS_NAME, name));
    }

    protected void doComputation(String date) throws IOException, JobExecutionException {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, date);
        builder.put(Parameters.TO_DATE, date);
        Context context = builder.build();

        DataComputation computation = new DataComputation(configurator, collectionsManagement);
        computation.forceExecute(context);
    }

    protected void doIntegrity(String date) throws IOException {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, date);
        builder.put(Parameters.TO_DATE, date);
        Context context = builder.build();

        TasksIntegrity integrity = new TasksIntegrity(collectionsManagement);
        integrity.doCompute(context);
    }
}
