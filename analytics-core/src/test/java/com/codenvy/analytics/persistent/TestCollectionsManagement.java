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

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.services.configuration.XmlConfigurationManager;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Testing ensuring and dropping indexes in collections. It is needed to take in account existing index by '_id' field.
 *
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestCollectionsManagement extends BaseTest {

    private static final String FILE            = BASE_DIR + "/resource";
    private static final String COLLECTION_NAME = "test_collection_";
    private static final String CONFIGURATION   = "<collections>" +
                                                  "   <collection name=\"test_collection_\">" +
                                                  "      <indexes>" +
                                                  "         <index name=\"index1\">" +
                                                  "            <field>_id</field>" +
                                                  "            <field>field1</field>" +
                                                  "         </index>" +
                                                  "         <index name=\"index2\">" +
                                                  "            <field>field2</field>" +
                                                  "         </index>" +
                                                  "      </indexes>" +
                                                  "   </collection>" +
                                                  "</collections>";

    private DB db;

    @BeforeClass
    public void prepare() {
        db = MongoDataStorage.getDb();
    }

    @Test
    public void shouldNotCreateIndexesIfCollectionNotExists() throws Exception {
        String collectionSuffix = "1";

        ensureIndexes(collectionSuffix);

        assertIndexesNumber(collectionSuffix, 0);
    }

    @Test
    public void shouldCreateIndexesIfCollectionExists() throws Exception {
        String collectionSuffix = "2";

        createCollection(collectionSuffix);
        ensureIndexes(collectionSuffix);

        assertIndexesNumber(collectionSuffix, 3);
    }

    @Test
    public void shouldNotDropIndexesIfCollectionNotExists() throws Exception {
        String collectionSuffix = "3";

        dropIndexes(collectionSuffix);
        assertIndexesNumber(collectionSuffix, 0);
    }

    @Test
    public void shouldDropIndexesIfCollectionExists() throws Exception {
        String collectionSuffix = "4";

        createCollection(collectionSuffix);
        ensureIndexes(collectionSuffix);

        assertIndexesNumber(collectionSuffix, 3);

        dropIndexes(collectionSuffix);

        assertIndexesNumber(collectionSuffix, 1);
    }

    @Test
    public void shouldNotCreateIndexesIfIndexesAlreadyExist() throws Exception {
        String collectionSuffix = "5";

        createCollection(collectionSuffix);
        ensureIndexes(collectionSuffix);

        assertIndexesNumber(collectionSuffix, 3);

        ensureIndexes(collectionSuffix);

        assertIndexesNumber(collectionSuffix, 3);
    }

    @Test
    public void shouldThrowExceptionsIfIndexesNotExist() throws Exception {
        String collectionSuffix = "6";

        createCollection(collectionSuffix);
        ensureIndexes(collectionSuffix);

        assertIndexesNumber(collectionSuffix, 3);

        dropIndexes(collectionSuffix);

        assertIndexesNumber(collectionSuffix, 1);

        dropIndexes(collectionSuffix);

        assertIndexesNumber(collectionSuffix, 1);
    }

    private void createCollection(String collectionSuffix) {
        DBCollection dbCollection = db.getCollection(COLLECTION_NAME + collectionSuffix);
        dbCollection.save(new BasicDBObject());
    }

    private void ensureIndexes(String collectionSuffix) throws Exception {
        CollectionsManagement collectionsManagement = initCollectionManagement(collectionSuffix);
        collectionsManagement.ensureIndexes();
    }

    private void dropIndexes(String collectionSuffix) throws Exception {
        CollectionsManagement collectionsManagement = initCollectionManagement(collectionSuffix);
        collectionsManagement.dropIndexes();
    }

    private void assertIndexesNumber(String collectionSuffix, int indexNumber) {
        DBCollection collection = db.getCollection(COLLECTION_NAME + collectionSuffix);
        assertEquals(collection.getIndexInfo().size(), indexNumber);
    }

    private CollectionsManagement initCollectionManagement(String collectionSuffix) throws Exception {
        XmlConfigurationManager<CollectionsConfiguration> configurationManager =
                getConfigurationManager(collectionSuffix);
        return new CollectionsManagement(configurationManager);
    }

    private XmlConfigurationManager<CollectionsConfiguration> getConfigurationManager(String collectionSuffix)
            throws Exception {

        String configuration = CONFIGURATION.replace(COLLECTION_NAME, COLLECTION_NAME + collectionSuffix);
        String file = FILE + "_" + collectionSuffix;

        try (BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
            out.write(configuration);
        }

        return new XmlConfigurationManager<>(CollectionsConfiguration.class, file);
    }
}
