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
package com.codenvy.analytics.collection;

import com.codenvy.analytics.services.configuration.ConfigurationManager;
import com.codenvy.analytics.services.configuration.XmlConfigurationManager;
import com.codenvy.analytics.storage.MongoDataStorage;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Utililty class to perform MongoDB index management operations like dropping or ensuring indexes based on configuration defined in collections configuration file.
 * 
 * 
 * * @author <a href="mailto:dnochevnov@codenvy.com">Dmytro Nochevnov</a> */
public class IndexOperations {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(IndexOperations.class);

    private static final ConfigurationManager<DBCollectionsConfiguration> configurationManager;

    private static DB db;

    private final static int ASCENDING_INDEX_MARK = 1;
    private final static int DESCENDING_INDEX_MARK = -1;
    
    private final static String CONFIGURATION_FILE_NAME = "db-collections.xml";
    
    static {
        configurationManager = new XmlConfigurationManager<>(DBCollectionsConfiguration.class);
        db = MongoDataStorage.getDb();
    }

    /**
     * Drop all indexes defined in index configuration file db-indexes.xml
     * @throws IOException
     */
    public static void dropIndexes() throws IOException {
        LOG.info("Start dropping indexing...");

        try {
            DBCollectionsConfiguration configuration = configurationManager.loadConfiguration(CONFIGURATION_FILE_NAME);

            for (CollectionConfiguration indexerConfiguration: configuration.getCollections()) {
                String collectionName = indexerConfiguration.getName();
                List<CompoundIndexConfiguration> compoundIndexes = indexerConfiguration.getCompoundIndexes();

                for (CompoundIndexConfiguration compoundIndex: compoundIndexes) {
                    String indexName = compoundIndex.getName();                    

                    dropIndex(collectionName, indexName);
                }
            }
        } finally {
            LOG.info("Finish dropping indexes.");
        }
    }

    /**
     * Ensure all indexes defined in index configuration file db-indexes.xml  
     * @throws IOException
     */
    public static void ensureIndexes() throws IOException {
        LOG.info("Start ensuring indexes...");

        try {
            DBCollectionsConfiguration configuration = configurationManager.loadConfiguration(CONFIGURATION_FILE_NAME);

            for (CollectionConfiguration indexerConfiguration: configuration.getCollections()) {
                String collectionName = indexerConfiguration.getName();
                List<CompoundIndexConfiguration> compoundIndexes = indexerConfiguration.getCompoundIndexes();

                for (CompoundIndexConfiguration compoundIndex: compoundIndexes) {
                    String indexName = compoundIndex.getName();                    
                    List<IndexFieldConfiguration> fields = compoundIndex.getFields();
                    
                    ensureCompoundIndex(collectionName, indexName, fields);
                }
                
            }
        } finally {
            LOG.info("Finish ensuring indexes.");
        }
    }


    /**
     * Ensure compound index in collection. 
     * @param collectionName
     * @param indexName
     * @param fields
     */
    private static void ensureCompoundIndex(String collectionName, String indexName, List<IndexFieldConfiguration> fields) {
        DBCollection dbCollection = db.getCollection(collectionName);
        
        if (dbCollection != null) {           
            BasicDBObject index = new BasicDBObject();
            for (IndexFieldConfiguration field: fields) {
                index.put(field.getField(), ASCENDING_INDEX_MARK);
            }
    
            dbCollection.ensureIndex(index, indexName);
        }
    }
    /**
     * Drop index in collection.
     * @param collectionName
     * @param indexName
     */
    private static void dropIndex(String collectionName, String indexName) {
        DBCollection dbCollection = db.getCollection(collectionName);
        if (dbCollection != null) {
            try {
                dbCollection.dropIndex(indexName);
            } catch(MongoException me) {
                // ignore "index not found" exception
                LOG.info(me.getMessage());
            }
        }
    }
}
