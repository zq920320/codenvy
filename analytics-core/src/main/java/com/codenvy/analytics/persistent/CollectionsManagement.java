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
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.ReadBasedMetric;
import com.codenvy.analytics.services.configuration.ConfigurationManager;
import com.codenvy.analytics.services.configuration.XmlConfigurationManager;
import com.mongodb.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class CollectionsManagement {

    private static final Logger LOG = LoggerFactory.getLogger(CollectionsManagement.class);

    private final static String CONFIGURATION         = "collections.xml";
    private static final String BACKUP_SUFFIX         = "_backup";
    private final static int    ASCENDING_INDEX_MARK  = 1;
    private final static int    DESCENDING_INDEX_MARK = -1;

    private final DB                                             db;
    private final ConfigurationManager<CollectionsConfiguration> configurationManager;

    public CollectionsManagement() {
        this.db = MongoDataStorage.getDb();
        this.configurationManager = new XmlConfigurationManager<>(CollectionsConfiguration.class, CONFIGURATION);
    }

    public CollectionsManagement(ConfigurationManager<CollectionsConfiguration> configurationManager) {
        this.db = MongoDataStorage.getDb();
        this.configurationManager = configurationManager;
    }

    /** @return true if collection exists in configuration */
    public boolean isCollectionExists(String collectionName) throws IOException {
        CollectionsConfiguration configuration = configurationManager.loadConfiguration();

        for (CollectionConfiguration collectionConfiguration : configuration.getCollections()) {
            if (collectionConfiguration.getName().equals(collectionName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Drop all indexes defined in collections configuration file
     *
     * @throws IOException
     */
    public void dropIndexes() throws IOException {
        long start = System.currentTimeMillis();
        LOG.info("Start dropping indexing...");

        try {
            CollectionsConfiguration configuration = configurationManager.loadConfiguration();

            for (CollectionConfiguration collectionConfiguration : configuration.getCollections()) {
                String collectionName = collectionConfiguration.getName();

                IndexesConfiguration indexesConfiguration = collectionConfiguration.getIndexes();
                List<IndexConfiguration> indexes = indexesConfiguration.getIndexes();

                for (IndexConfiguration indexConfiguration : indexes) {
                    dropIndex(collectionName, indexConfiguration);
                }
            }
        } finally {
            LOG.info("Finish dropping indexes in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }

    /**
     * Ensure all indexes defined in collections configuration file
     *
     * @throws IOException
     */
    public void ensureIndexes() throws IOException {
        long start = System.currentTimeMillis();

        LOG.info("Start ensuring indexes...");

        try {
            CollectionsConfiguration configuration = configurationManager.loadConfiguration();

            for (CollectionConfiguration collectionConfiguration : configuration.getCollections()) {
                String collectionName = collectionConfiguration.getName();

                IndexesConfiguration indexesConfiguration = collectionConfiguration.getIndexes();
                List<IndexConfiguration> indexes = indexesConfiguration.getIndexes();

                for (IndexConfiguration indexConfiguration : indexes) {
                    ensureIndex(collectionName, indexConfiguration);
                }

            }
        } finally {
            LOG.info("Finish ensuring indexes in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }

    /**
     * Removes data from all collection satisfying given date interval. The date interval is represented by two
     * parameters: {@link Parameters#FROM_DATE} and {@link Parameters#TO_DATE}.
     *
     * @throws IOException
     */
    public void removeData(Map<String, String> context) throws IOException, ParseException {
        long start = System.currentTimeMillis();

        LOG.info("Start removing data...");

        try {
            CollectionsConfiguration configuration = configurationManager.loadConfiguration();
            DBObject dateFilter = getDateFilter(context);

            for (CollectionConfiguration collectionConfiguration : configuration.getCollections()) {
                String collectionName = collectionConfiguration.getName();
                db.getCollection(collectionName).remove(dateFilter);
            }
        } finally {
            LOG.info("Finish removing data in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }

    /**
     * Copy data from collectionName to collectionName_backup.
     *
     * @param collectionName
     *         the collection name to backup data from
     * @throws IOException
     */
    public void backup(String collectionName) throws IOException {
        DBCollection src = db.getCollection(collectionName);
        DBCollection dst = db.getCollection(collectionName + BACKUP_SUFFIX);

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

    private DBObject getDateFilter(Map<String, String> context) throws ParseException {
        DBObject dateFilter = new BasicDBObject();
        dateFilter.put("$gte", Parameters.FROM_DATE.exists(context)
                               ? Utils.getFromDate(context).getTimeInMillis()
                               : 0);
        dateFilter.put("$lt", Parameters.TO_DATE.exists(context)
                              ? Utils.getToDate(context).getTimeInMillis() + ReadBasedMetric.DAY_IN_MILLISECONDS
                              : Long.MAX_VALUE);

        return new BasicDBObject(ReadBasedMetric.DATE, dateFilter);
    }

    /**
     * Ensures index in collection.
     *
     * @param collectionName
     *         the collection name to create index in
     * @param indexConfiguration
     *         the index configuration
     */
    private void ensureIndex(String collectionName, IndexConfiguration indexConfiguration) {
        if (db.collectionExists(collectionName)) {
            DBCollection dbCollection = db.getCollection(collectionName);
            String name = indexConfiguration.getName();
            DBObject index = createIndex(indexConfiguration.getFields());

            dbCollection.ensureIndex(index, name);
        } else {
            LOG.error("Collection " + collectionName + " doesn't exist");
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
