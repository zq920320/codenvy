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

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codenvy.analytics.services.configuration.ConfigurationManager;
import com.codenvy.analytics.services.configuration.XmlConfigurationManager;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoException;

/**
 * Utility class to perform MongoDB index management operations like dropping or ensuring indexes based on configuration defined in collections configuration file.
 * 
 * 
 * * @author <a href="mailto:dnochevnov@codenvy.com">Dmytro Nochevnov</a> */
public class IndexOperations {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(IndexOperations.class);

    private static final ConfigurationManager<CollectionsConfiguration> configurationManager = new XmlConfigurationManager<>(CollectionsConfiguration.class);;

    private static DB db = MongoDataStorage.getDb();

    private final static int ASCENDING_INDEX_MARK = 1;
    private final static int DESCENDING_INDEX_MARK = -1;
    
    private final static String CONFIGURATION_FILE_NAME = "mongo-collections.xml";

    /**
     * Drop all indexes defined in collections configuration file
     * @throws IOException
     */
    public static void dropIndexes() throws IOException {
        long start = System.currentTimeMillis();
        
    	LOG.info("Start dropping indexing...");

        try {
            CollectionsConfiguration configuration = configurationManager.loadConfiguration(CONFIGURATION_FILE_NAME);

            for (CollectionConfiguration collectionConfiguration: configuration.getCollections()) {
                String collectionName = collectionConfiguration.getName();
                
                CompoundIndexesConfiguration compoundIndexesConfiguration = collectionConfiguration.getCompoundIndexes();
                List<CompoundIndexConfiguration> compoundIndexes = compoundIndexesConfiguration.getCompoundIndexes();

                for (CompoundIndexConfiguration compoundIndex: compoundIndexes) {
                    String indexName = compoundIndex.getName();                    

                    dropIndex(collectionName, indexName);
                }
            }
        } finally {
            LOG.info("Finish dropping indexes in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }

    /**
     * Ensure all indexes defined in collections configuration file  
     * @throws IOException
     */
    public static void ensureIndexes() throws IOException {
        long start = System.currentTimeMillis();
        
    	LOG.info("Start ensuring indexes...");

        try {
            CollectionsConfiguration configuration = configurationManager.loadConfiguration(CONFIGURATION_FILE_NAME);

            for (CollectionConfiguration collectionConfiguration: configuration.getCollections()) {
                String collectionName = collectionConfiguration.getName();
                
                CompoundIndexesConfiguration compoundIndexesConfiguration = collectionConfiguration.getCompoundIndexes();
                List<CompoundIndexConfiguration> compoundIndexes = compoundIndexesConfiguration.getCompoundIndexes();

                for (CompoundIndexConfiguration compoundIndex: compoundIndexes) {
                    String indexName = compoundIndex.getName();                    
                    List<FieldConfiguration> fields = compoundIndex.getFields();
                    
                    ensureCompoundIndex(collectionName, indexName, fields);
                }
                
            }
        } finally {
            LOG.info("Finish ensuring indexes in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }


    /**
     * Ensure compound index in collection. 
     * @param collectionName
     * @param indexName
     * @param fields
     */
    private static void ensureCompoundIndex(String collectionName, String indexName, List<FieldConfiguration> fields) {
        DBCollection dbCollection = db.getCollection(collectionName);                  
        BasicDBObject index = new BasicDBObject();
        for (FieldConfiguration field: fields) {
            index.put(field.getField(), ASCENDING_INDEX_MARK);
        }

        dbCollection.ensureIndex(index, indexName);
    }
    /**
     * Drop index in collection.
     * @param collectionName
     * @param indexName
     */
    private static void dropIndex(String collectionName, String indexName) {
        DBCollection dbCollection = db.getCollection(collectionName);
	    try {
	        dbCollection.dropIndex(indexName);
	    } catch(MongoException me) {
	        // ignore "index not found" exception
	        if (isIndexNotFoundExceptionType(me)) {
	            LOG.info(me.getMessage());
	        } else {
	            throw me;
	        }
	    }
    }
    
    private static boolean isIndexNotFoundExceptionType(MongoException me) {
        return me.getCode() == -5 
            && me.getMessage().indexOf("index not found") >= 0;
    }
}
