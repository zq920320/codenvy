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

import com.codenvy.analytics.services.configuration.ConfigurationManager;
import com.codenvy.analytics.services.configuration.XmlConfigurationManager;
import com.mongodb.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Utility class to perform MongoDB index management operations like dropping or ensuring indexes based on
 * configuration defined in collections configuration file.
 *
 * @author <a href="mailto:dnochevnov@codenvy.com">Dmytro Nochevnov</a>
 */
public class CollectionsManagement {

    private static final Logger LOG = LoggerFactory.getLogger(CollectionsManagement.class);

    private final static String CONFIGURATION         = "collections.xml";
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
