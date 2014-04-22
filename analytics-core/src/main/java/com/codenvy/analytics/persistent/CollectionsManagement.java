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
package com.codenvy.analytics.persistent;

import com.codenvy.analytics.Utils;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.services.configuration.XmlConfigurationManager;
import com.mongodb.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.text.ParseException;
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

    private final static String CONFIGURATION        = "collections.xml";
    private static final String BACKUP_SUFFIX        = "_backup";
    private final static int    ASCENDING_INDEX_MARK = 1;

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
                    dropIndex(name, indexConf);
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
        if (db.collectionExists(name)) {
            CollectionConfiguration collectionConf = configuration.get(name);

            IndexesConfiguration indexesConf = collectionConf.getIndexes();
            List<IndexConfiguration> indexes = indexesConf.getIndexes();

            for (IndexConfiguration indexConf : indexes) {
                ensureIndex(name, indexConf);
            }
        } else {
            LOG.warn("Collection " + name + " doesn't exist in " + CONFIGURATION);
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
     * @param name
     *         the collection name to create index in
     * @param indexConfiguration
     *         the index configuration
     */
    private void ensureIndex(String name, IndexConfiguration indexConfiguration) {
        if (exists(name)) {
            DBCollection dbCollection = getOrCreate(name);
            String indexName = indexConfiguration.getName();
            DBObject index = createIndex(indexConfiguration.getFields());

            dbCollection.ensureIndex(index, indexName);
        } else {
            LOG.warn("Collection " + name + " doesn't exist");
        }
    }

    private DBObject createIndex(List<FieldConfiguration> fields) {
        BasicDBObject index = new BasicDBObject();
        for (FieldConfiguration field : fields) {
            index.put(field.getField(), ASCENDING_INDEX_MARK);
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
}
