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

import com.codenvy.api.factory.FactoryImage;
import com.codenvy.api.factory.FactoryStore;
import com.codenvy.api.factory.dto.Factory;
import com.codenvy.api.factory.dto.*;
import com.codenvy.commons.lang.NameGenerator;
import com.codenvy.dto.server.DtoFactory;
import com.mongodb.*;

import org.testng.annotations.*;

import java.net.InetSocketAddress;
import java.util.*;

import static org.testng.Assert.*;


/**
 *
 */
public class MongoDBFactoryStoreTest {

    private static final String DB_NAME   = "test1";
    private static final String COLL_NAME = "factory1";
    private DBCollection collection;
    private MongoClient  client;
    private MongoServer  server;
    FactoryStore store;

    @BeforeMethod
    public void setUp() throws Exception {
        server = new MongoServer(new MemoryBackend());

        // bind on a random local port
        InetSocketAddress serverAddress = server.bind();

        client = new MongoClient(new ServerAddress(serverAddress));
        collection = client.getDB(DB_NAME).getCollection(COLL_NAME);

        store = new MongoDBFactoryStore(serverAddress.getHostName(), serverAddress.getPort(), DB_NAME, COLL_NAME, null, null);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        client.close();
        server.shutdownNow();
    }

    @Test
    public void testSaveFactory() throws Exception {

        Set<FactoryImage> images = new HashSet<>();

        WelcomePage welcomePage = DtoFactory.getInstance().createDto(WelcomePage.class);
        WelcomeConfiguration authConf = DtoFactory.getInstance().createDto(WelcomeConfiguration.class);
        WelcomeConfiguration notAuthConf = DtoFactory.getInstance().createDto(WelcomeConfiguration.class);

        authConf.setTitle("title1");
        authConf.setIconurl("url1");
        authConf.setContenturl("content1");
        notAuthConf.setTitle("title2");
        notAuthConf.setIconurl("url2");
        notAuthConf.setContenturl("content2");

        welcomePage.setAuthenticated(authConf);
        welcomePage.setNonauthenticated(notAuthConf);

        Variable variable = DtoFactory.getInstance().createDto(Variable.class);
        Replacement replacement =
                DtoFactory.getInstance().createDto(Replacement.class).withFind("find").withReplace("replace").withReplacemode("mode");
        variable.withFiles(Arrays.asList("file1", "file2")).withEntries(Arrays.asList(replacement, replacement));

        Factory factoryUrl = DtoFactory.getInstance().createDto(Factory.class);
        factoryUrl.setAuthor("someAuthor");
        factoryUrl.setContactmail("test@test.com");
        factoryUrl.setDescription("testDescription");
        factoryUrl.setProjectattributes(DtoFactory.getInstance().createDto(ProjectAttributes.class).withPname("pname").withPtype("ptype"));
        factoryUrl.setStyle("testStyle");
        factoryUrl.setAction("openfile");
        factoryUrl.setOrgid("org123456");
        factoryUrl.setAffiliateid("testaffiliate123");
        factoryUrl.setCommitid("commit12345");
        factoryUrl.setIdcommit("commit12345");
        factoryUrl.setPname("pname");
        factoryUrl.setPtype("ptype");
        factoryUrl.setVcsinfo(true);
        factoryUrl.setV("1.1");
        factoryUrl.setVcs("http://testvscurl.com");
        factoryUrl.setOpenfile("index.php");
        factoryUrl.setVcsbranch("master");
        factoryUrl.setVcsurl("http://testvscurl.com");
        factoryUrl.setWelcome(welcomePage);
        factoryUrl.setWname("wname");
        factoryUrl.setUserid("123456");
        factoryUrl.setCreated(123456);
        factoryUrl.setValidsince(123456);
        factoryUrl.setValiduntil(456789);
        factoryUrl.setVariables(Arrays.asList(variable, variable));
        factoryUrl.setGit(DtoFactory.getInstance().createDto(Git.class).withConfigbranchmerge("configbranchmerge")
                                    .withConfigpushdefault("configpushdefault").withConfigremoteoriginfetch("configremoteoriginfetch"));
        factoryUrl.setRestriction(DtoFactory.getInstance().createDto(Restriction.class).withValidsessioncount(123).withValiduntil(123456798)
                                            .withRefererhostname("host").withValidsince(123).withPassword("password")
                                            .withRestrictbypassword(true));

        String id = store.saveFactory(factoryUrl, images);

        DBObject query = new BasicDBObject();
        query.put("_id", id);
        DBObject res = (DBObject)collection.findOne(query).get("factoryurl");
        Factory result = DtoFactory.getInstance().createDtoFromJson(res.toString(), Factory.class);
        result.setId(id);
        factoryUrl.setId(id);

        //compareFactories(result, factoryUrl);
        assertEquals(result, factoryUrl);
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

        Set<FactoryImage> newImages = store.getFactoryImages(id, null);
        assertNotNull(newImages);
        FactoryImage newImage = newImages.iterator().next();

        assertTrue(newImage.getName().endsWith(image.getName()));
        assertEquals(newImage.getMediaType(), image.getMediaType());
        assertEquals(newImage.getImageData(), image.getImageData());
    }
}
