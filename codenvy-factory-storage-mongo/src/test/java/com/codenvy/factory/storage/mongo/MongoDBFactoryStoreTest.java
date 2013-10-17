/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2013] Codenvy, S.A.
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
package com.codenvy.factory.storage.mongo;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
//import de.flapdoodle.embedmongo.MongoDBRuntime;
//import de.flapdoodle.embedmongo.MongodExecutable;
//import de.flapdoodle.embedmongo.MongodProcess;
//import de.flapdoodle.embedmongo.config.MongodConfig;
//import de.flapdoodle.embedmongo.distribution.Version;
//import de.flapdoodle.embedmongo.runtime.Network;

import com.codenvy.api.factory.AdvancedFactoryUrl;
import com.codenvy.api.factory.FactoryImage;
import com.codenvy.api.factory.FactoryStore;
import com.codenvy.commons.lang.NameGenerator;
import com.mongodb.*;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;


/**
 *
 */
public class MongoDBFactoryStoreTest {

    private static final String DB_NAME   = "test1";
    private static final String COLL_NAME = "factory1";
//    private MongodExecutable mongodExe;
//    private MongodProcess    mongod;
//    FactoryStore store;
//    private Mongo        mongo;
//    private DB           db;
//    private DBCollection collection;

    private DBCollection collection;
    private MongoClient  client;
    private MongoServer  server;
    FactoryStore store;

    @BeforeMethod
    public void setUp() throws Exception {

//        MongoDBRuntime runtime = MongoDBRuntime.getDefaultInstance();
//        mongodExe = runtime.prepare(new MongodConfig(Version.V2_1_0, 12345,
//                                                     Network.localhostIsIPv6()));
//        mongod = mongodExe.start();
//
//        mongo = new Mongo("localhost", 12345);
//        db = mongo.getDB(DB_NAME);
//        collection = db.getCollection(COLL_NAME);

        server = new MongoServer(new MemoryBackend());

        // bind on a random local port
        InetSocketAddress serverAddress = server.bind();

        client = new MongoClient(new ServerAddress(serverAddress));
        collection = client.getDB(DB_NAME).getCollection(COLL_NAME);

        store = new MongoDBFactoryStore(serverAddress.getHostName(), serverAddress.getPort(), DB_NAME, COLL_NAME);
    }

    @AfterMethod
    public void tearDown() throws Exception {
//        mongod.stop();
//        mongodExe.cleanup();
        client.close();
        server.shutdownNow();
    }

    @Test
    public void testSaveFactory() throws Exception {

        Set<FactoryImage> images = new HashSet<>();
        Map<String, String> attrs = new HashMap<>();
        attrs.put("testattr1", "testVaue1");
        attrs.put("testattr2", "testVaue2");
        attrs.put("testattr3", "testVaue3");

        AdvancedFactoryUrl factoryUrl = new AdvancedFactoryUrl();
        factoryUrl.setAuthor("someAuthor");
        factoryUrl.setContactmail("test@test.com");
        factoryUrl.setDescription("testDescription");
        factoryUrl.setProjectattributes(attrs);
        factoryUrl.setStyle("testStyle");
        factoryUrl.setAction("openfile");
        factoryUrl.setAffiliateid("testaffiliate123");
        factoryUrl.setCommitid("commit12345");
        factoryUrl.setVcsinfo(true);
        factoryUrl.setV("1.1");
        factoryUrl.setVcs("http://testvscurl.com");

        String id = store.saveFactory(factoryUrl, images);

        DBObject query = new BasicDBObject();
        query.put("_id", id);
        DBObject res = collection.findOne(query);
        BasicDBObject props =  (BasicDBObject)((BasicDBObject)res.get("factoryurl")).get("projectattributes");

        assertNotNull(res);
        assertEquals(((BasicDBObject)res.get("factoryurl")).get("author"), "someAuthor");
        assertEquals(((BasicDBObject)res.get("factoryurl")).get("commitid"), "commit12345");
        assertEquals(props.toMap(), attrs);

    }

    @Test
    public void testRemoveFactory() throws Exception {

        String id = "123412341";
        DBObject obj = new BasicDBObject("_id", id).append("key", "value");
        collection.insert(obj);

        store.removeFactory(id);

        DBObject query = new BasicDBObject();
        query.put("_id", id);
        assertNull(collection.findOne(query));

    }

    @Test
    public void testGetFactory() throws Exception {

        String id = "testid1234";

        Map<String, String> attrs = new HashMap<>();
        attrs.put("testattr1", "testVaue1");
        attrs.put("testattr2", "testVaue2");
        attrs.put("testattr3", "testVaue3");

        byte[] b = new byte[4096];
        new Random().nextBytes(b);

        BasicDBObjectBuilder attributes = BasicDBObjectBuilder.start(attrs);

        List<DBObject> imageList = new ArrayList<>();

        BasicDBObjectBuilder factoryURLbuilder = new BasicDBObjectBuilder();
        factoryURLbuilder.add("v", "1.1")
                         .add("vcs", "git")
                         .add("vcsurl", "http://vcsurl")
                         .add("commitid", "commit123456")
                         .add("action", "openfile")
                         .add("openfile", "true")
                         .add("vcsinfo", true)
//                         .add("style", factoryUrl.getStyle())
//                         .add("description", factoryUrl.getDescription())
//                         .add("contactmail", factoryUrl.getContactmail())
//                         .add("author", factoryUrl.getAuthor())
//                         .add("orgid", factoryUrl.getOrgid())
//                         .add("affiliateid", factoryUrl.getAffiliateid())
//                         .add("vcsbranch", factoryUrl.getVcsbranch())
                         .add("projectattributes", attributes.get());

        BasicDBObjectBuilder factoryDatabuilder = new BasicDBObjectBuilder();
        factoryDatabuilder.add("_id", id);
        factoryDatabuilder.add("factoryurl", factoryURLbuilder.get());
        factoryDatabuilder.add("images", imageList);

        collection.save(factoryDatabuilder.get());

        AdvancedFactoryUrl factoryUrl = store.getFactory(id);
        assertNotNull(factoryUrl);
        assertEquals(factoryUrl.getProjectattributes(), attrs);

    }

    @Test
    public void testGetFactoryImages() throws Exception {

        String id = "testid1234314";

        Set<FactoryImage> images = new HashSet<>();
        FactoryImage image = new FactoryImage();
        byte[] b = new byte[4096];
        new Random().nextBytes(b);
        image.setName("test123.jpg");
        image.setMediaType("image/jpeg");
        image.setImageData(b);
        images.add(image);

        List<DBObject> imageList = new ArrayList<>();
        for (FactoryImage one : images) {
            imageList.add(new BasicDBObjectBuilder().add("name", NameGenerator.generate("", 16) + one.getName())
                                                    .add("type", one.getMediaType())
                                                    .add("data", one.getImageData()).get());
        }

        BasicDBObjectBuilder factoryURLbuilder = new BasicDBObjectBuilder();

        BasicDBObjectBuilder factoryDatabuilder = new BasicDBObjectBuilder();
        factoryDatabuilder.add("_id", id);
        factoryDatabuilder.add("factoryurl", factoryURLbuilder.get());
        factoryDatabuilder.add("images", imageList);

        collection.save(factoryDatabuilder.get());

        DBObject query = new BasicDBObject();
        query.put("_id", id);
        Set<FactoryImage> newImages =  store.getFactoryImages(id, null);
        assertNotNull(newImages);
        FactoryImage newImage = newImages.iterator().next();

        assertTrue(newImage.getName().endsWith(image.getName()));
        assertEquals(newImage.getMediaType(), image.getMediaType());
        assertEquals(newImage.getImageData(), image.getImageData());
    }
}
