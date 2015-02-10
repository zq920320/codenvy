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

import com.codenvy.analytics.Utils;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.ReadBasedMetric;
import com.codenvy.analytics.services.configuration.XmlConfigurationManager;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Utility class to perform MongoDB index management operations like dropping or ensuring indexes based on
 * configuration defined in collections configuration file.
 *
 * @author <a href="mailto:dnochevnov@codenvy.com">Dmytro Nochevnov</a>
 */
@Singleton
public class CollectionsManagement {

    private static final Logger LOG = LoggerFactory.getLogger(CollectionsManagement.class);

    private final static String CONFIGURATION         = "collections.xml";
    private static final String BACKUP_SUFFIX         = "_backup";
    private final static int    ASCENDING_INDEX_MARK  = 1;
    private final static int    DESCENDING_INDEX_MARK = -1;

    private final DB                                   db;
    private final Map<String, CollectionConfiguration> configuration;

    @Inject
    public CollectionsManagement(MongoDataStorage mongoDataStorage,
                                 XmlConfigurationManager confManager) throws IOException {
        CollectionsConfiguration conf = confManager.loadConfiguration(CollectionsConfiguration.class, CONFIGURATION);

        this.db = mongoDataStorage.getDb();
        this.configuration = conf.getAsMap();
    }

    /**
     * @return true if collection exists in configuration
     */
    public boolean exists(String name) {
        return configuration.containsKey(name);
    }

    public Collection<String> getNames() {
        return configuration.keySet();
    }

    /**
     * Drops all indexes defined in collections configuration file
     */
    public void dropIndexes() {
        long start = System.currentTimeMillis();
        LOG.info("Start dropping indexing...");

        try {
            for (CollectionConfiguration collectionConf : configuration.values()) {
                String name = collectionConf.getName();

                IndexesConfiguration indexesConf = collectionConf.getIndexes();
                List<IndexConfiguration> indexes = indexesConf.getIndexes();

                for (IndexConfiguration indexConf : indexes) {
                    if (!indexConf.isFixed()) {
                        dropIndex(name, indexConf);
                    }
                }
            }
        } finally {
            LOG.info("Finished dropping indexes in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }

    /**
     * Drops collection.
     */
    public void drop(String collectionName) {
        LOG.info("Dropping collection " + collectionName);
        db.getCollection(collectionName).drop();
    }

    /**
     * Gets collection by name. Creates one if doesn't exist.
     */
    public DBCollection getOrCreate(String collectionName) {
        return db.getCollection(collectionName);
    }

    /**
     * Ensures all indexes.
     */
    public void ensureIndexes(String name) {
        CollectionConfiguration collectionConf = configuration.get(name);

        IndexesConfiguration indexesConf = collectionConf.getIndexes();
        List<IndexConfiguration> indexes = indexesConf.getIndexes();

        for (IndexConfiguration indexConf : indexes) {
            ensureIndex(name, indexConf);
        }
    }

    /**
     * Ensure all indexes.
     */
    public void ensureIndexes() throws IOException {
        long start = System.currentTimeMillis();
        LOG.info("Start ensuring indexes...");

        try {
            for (String name : configuration.keySet()) {
                ensureIndexes(name);
            }
        } finally {
            LOG.info("Finished ensuring indexes in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }

    /**
     * Removes data from all collection satisfying given date interval. The date interval is represented by two
     * parameters: {@link Parameters#FROM_DATE} and {@link Parameters#TO_DATE}.
     */
    public void removeData(Context context) throws ParseException {
        long start = System.currentTimeMillis();
        LOG.info("Start removing data...");

        try {
            DBObject dateFilter = Utils.setDateFilter(context);

            for (CollectionConfiguration collectionConf : configuration.values()) {
                String name = collectionConf.getName();
                db.getCollection(name).remove(dateFilter);
            }
        } finally {
            LOG.info("Finished removing data in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }

    /**
     * Backups data.
     *
     * @param name
     *         the collection name to backup data from
     * @throws IOException
     */
    public void backup(String name) throws IOException {
        DBCollection src = db.getCollection(name);
        DBCollection dst = db.getCollection(name + BACKUP_SUFFIX);

        try {
            dst.drop();
        } catch (MongoException e) {
            throw new IOException("Backup failed. Can't drop " + dst.getName(), e);
        }

        try {
            for (Object o : src.find()) {
                dst.insert((DBObject)o);
            }
        } catch (MongoException e) {
            throw new IOException("Backup failed. Can't copy data from " + src.getName() + " to " + dst.getName(), e);
        }

        if (src.count() != dst.count()) {
            throw new IOException(
                    "Backup failed. Wrong records count between " + src.getName() + " and " + dst.getName());
        }
    }

    /**
     * Ensures index in the collection.
     *
     * @param collectionName
     *         the collection collectionName to create index in
     * @param indexConfiguration
     *         the index configuration
     */
    private void ensureIndex(String collectionName, IndexConfiguration indexConfiguration) {
        if (exists(collectionName)) {
            DBCollection dbCollection = getOrCreate(collectionName);
            String expectedIndexName = indexConfiguration.getName();
            DBObject expectedIndex = createIndex(indexConfiguration.getFields());

            for (DBObject indexInfo : dbCollection.getIndexInfo()) {
                Object indexName = indexInfo.get("collectionName");
                Object index = indexInfo.get("key");

                if (indexName.equals(expectedIndexName)) {
                    if (index.equals(expectedIndex)) {
                        return;
                    } else {
                        dropIndex(collectionName, indexConfiguration);
                    }
                }
            }

            dbCollection.ensureIndex(expectedIndex, expectedIndexName);
        } else {
            LOG.warn("Collection " + collectionName + " doesn't exist");
        }
    }

    private DBObject createIndex(List<FieldConfiguration> fields) {
        BasicDBObject index = new BasicDBObject();
        for (FieldConfiguration field : fields) {
            index.put(field.getField(), field.isDescending() ? DESCENDING_INDEX_MARK : ASCENDING_INDEX_MARK);
        }

        return index;
    }

    /**
     * Drop index in collection.
     *
     * @param collectionName
     *         the collection name to drop index in
     * @param indexConfiguration
     *         the index configuration
     */
    private void dropIndex(String collectionName, IndexConfiguration indexConfiguration) {
        if (db.collectionExists(collectionName)) {
            DBCollection dbCollection = db.getCollection(collectionName);

            try {
                String name = indexConfiguration.getName();
                dbCollection.dropIndex(name);
            } catch (MongoException me) {
                if (!isIndexNotFoundExceptionType(me)) {
                    throw me;
                }
            }
        }
    }

    private static boolean isIndexNotFoundExceptionType(MongoException me) {
        return me.getCode() == -5 && me.getMessage().contains("index not found");
    }

    public void removeAnonymousUsers(Context clauses, int skipLastDays) throws ParseException {
        int removed = removeData(clauses,
                                 skipLastDays,
                                 ((ReadBasedMetric)MetricFactory.getMetric(MetricType.USERS_PROFILES_LIST))
                                         .getStorageCollectionName(),
                                 MetricFilter.REGISTERED_USER,
                                 0);

        if (removed > 0) {
            LOG.info(removed + " anonymous users were removed.");
        }
    }

    public void removeTemporaryWorkspaces(Context clauses, int skipLastDays) throws ParseException {
        int removed = removeData(clauses,
                                 skipLastDays,
                                 ((ReadBasedMetric)MetricFactory.getMetric(MetricType.WORKSPACES_PROFILES_LIST))
                                         .getStorageCollectionName(),
                                 MetricFilter.PERSISTENT_WS,
                                 0);
        if (removed > 0) {
            LOG.info(removed + " temporary workspaces were removed.");
        }
    }

    private int removeData(Context clauses,
                           int skipLastDays,
                           String collection,
                           MetricFilter filter,
                           Object filterValue) throws ParseException {
        Context context = getContextWhereSkipDays(clauses, skipLastDays);

        Calendar toDate = context.getAsDate(Parameters.TO_DATE);
        if (toDate.before(context.getAsDate(Parameters.FROM_DATE))) {
            return -1;
        }

        DBObject filters = Utils.setDateFilter(context);
        filters.put(filter.toString().toLowerCase(), filterValue);

        WriteResult result = db.getCollection(collection).remove(filters);

        return result.getN();
    }


    private Context getContextWhereSkipDays(Context context, int skipDays) throws ParseException {
        Context.Builder builder = new Context.Builder();
        builder.putDefaultValue(Parameters.FROM_DATE);

        Calendar toDate = context.getAsDate(Parameters.TO_DATE);
        toDate.add(Calendar.DAY_OF_MONTH, -skipDays);

        builder.put(Parameters.TO_DATE, toDate);
        return builder.build();
    }
}
