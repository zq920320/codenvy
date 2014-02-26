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

import com.codenvy.api.factory.*;
import com.codenvy.api.factory.dto.*;
import com.codenvy.commons.lang.NameGenerator;
import com.codenvy.dto.server.DtoFactory;
import com.mongodb.*;

import org.everrest.core.impl.provider.json.*;
import org.testng.annotations.*;

import java.io.ByteArrayInputStream;
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
        Map<String, String> attrs = new HashMap<>();
        attrs.put("testattr1", "testValue1");
        attrs.put("testattr2", "testValue2");
        attrs.put("testattr3", "testValue3");

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

        AdvancedFactoryUrl factoryUrl = DtoFactory.getInstance().createDto(AdvancedFactoryUrl.class);
        factoryUrl.setAuthor("someAuthor");
        factoryUrl.setContactmail("test@test.com");
        factoryUrl.setDescription("testDescription");
        factoryUrl.setProjectattributes(attrs);
        factoryUrl.setStyle("testStyle");
        factoryUrl.setAction("openfile");
        factoryUrl.setOrgid("org123456");
        factoryUrl.setAffiliateid("testaffiliate123");
        factoryUrl.setCommitid("commit12345");
        factoryUrl.setVcsinfo(true);
        factoryUrl.setV("1.1");
        factoryUrl.setVcs("http://testvscurl.com");
        factoryUrl.setOpenfile("index.php");
        factoryUrl.setVcsbranch("master");
        factoryUrl.setVcsurl("http://testvscurl.com");
        factoryUrl.setWelcome(welcomePage);

        String id = store.saveFactory(factoryUrl, images);

        DBObject query = new BasicDBObject();
        query.put("_id", id);
        DBObject res = (DBObject)collection.findOne(query).get("factoryurl");
        JsonParser jsonParser = new JsonParser();
        jsonParser.parse(new ByteArrayInputStream(res.toString().getBytes("UTF-8")));
        JsonValue jsonValue = jsonParser.getJsonObject();
        AdvancedFactoryUrl result = ObjectBuilder.createObject(AdvancedFactoryUrl.class, jsonValue);
        result.setId(id);
        factoryUrl.setId(id);
        compareFactories(result, factoryUrl);

    }

    @Test
    public void testSaveFactoryWithoutWelcome() throws Exception {

        Set<FactoryImage> images = new HashSet<>();
        Map<String, String> attrs = new HashMap<>();
        attrs.put("testattr1", "testValue1");
        attrs.put("testattr2", "testValue2");
        attrs.put("testattr3", "testValue3");

        WelcomePage welcomePage = null;

        AdvancedFactoryUrl factoryUrl = DtoFactory.getInstance().createDto(AdvancedFactoryUrl.class);
        factoryUrl.setAuthor("someAuthor");
        factoryUrl.setContactmail("test@test.com");
        factoryUrl.setDescription("testDescription");
        factoryUrl.setProjectattributes(attrs);
        factoryUrl.setStyle("testStyle");
        factoryUrl.setAction("openfile");
        factoryUrl.setOrgid("org123456");
        factoryUrl.setAffiliateid("testaffiliate123");
        factoryUrl.setCommitid("commit12345");
        factoryUrl.setVcsinfo(true);
        factoryUrl.setV("1.1");
        factoryUrl.setVcs("http://testvscurl.com");
        factoryUrl.setOpenfile("index.php");
        factoryUrl.setVcsbranch("master");
        factoryUrl.setVcsurl("http://testvscurl.com");
        factoryUrl.setWelcome(welcomePage);

        String id = store.saveFactory(factoryUrl, images);

        DBObject query = new BasicDBObject();
        query.put("_id", id);
        DBObject res = (DBObject)collection.findOne(query).get("factoryurl");
        JsonParser jsonParser = new JsonParser();
        jsonParser.parse(new ByteArrayInputStream(res.toString().getBytes("UTF-8")));
        JsonValue jsonValue = jsonParser.getJsonObject();
        AdvancedFactoryUrl result = ObjectBuilder.createObject(AdvancedFactoryUrl.class, jsonValue);
        result.setId(id);
        factoryUrl.setId(id);
        compareFactories(result, factoryUrl);

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
        attrs.put("testattr1", "testValue1");
        attrs.put("testattr2", "testValue2");
        attrs.put("testattr3", "testValue3");

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

        byte[] b = new byte[4096];
        new Random().nextBytes(b);

        BasicDBObjectBuilder attributes = BasicDBObjectBuilder.start(attrs);

        Variable variable = DtoFactory.getInstance().createDto(Variable.class);
        Replacement replacement = DtoFactory.getInstance().createDto(Replacement.class);
        replacement.setFind("find");
        replacement.setReplacemode("replace");
        replacement.setReplacemode("text_multipass");
        variable.setFiles(Collections.singletonList("glob"));
        variable.setEntries(Collections.<Replacement>singletonList(replacement));
        List<Variable> variables = Collections.<Variable>singletonList(variable);

        List<DBObject> imageList = new ArrayList<>();

        BasicDBObject welcomeDBObject = new BasicDBObject();

        WelcomeConfiguration authConfiguration = welcomePage.getAuthenticated();
        BasicDBObject authDBOWelcome = new BasicDBObject();
        authDBOWelcome.put("title", authConfiguration.getTitle());
        authDBOWelcome.put("iconurl", authConfiguration.getIconurl());
        authDBOWelcome.put("contenturl", authConfiguration.getContenturl());

        WelcomeConfiguration nonAuthConfiguration = welcomePage.getNonauthenticated();
        BasicDBObject nonAuthDBOWelcome = new BasicDBObject();
        nonAuthDBOWelcome.put("title", nonAuthConfiguration.getTitle());
        nonAuthDBOWelcome.put("iconurl", nonAuthConfiguration.getIconurl());
        nonAuthDBOWelcome.put("contenturl", nonAuthConfiguration.getContenturl());

        welcomeDBObject.put("authenticated", authDBOWelcome);
        welcomeDBObject.put("nonauthenticated", nonAuthDBOWelcome);

        BasicDBObjectBuilder factoryURLbuilder = new BasicDBObjectBuilder();
        factoryURLbuilder.add("v", "1.1")
                         .add("vcs", "git")
                         .add("vcsurl", "http://vcsurl")
                         .add("commitid", "commit123456")
                         .add("action", "openfile")
                         .add("openfile", "true")
                         .add("vcsinfo", true)
                         .add("style", "testStyle")
                         .add("description", "testDescription")
                         .add("contactmail", "test@test.com")
                         .add("author", "someAuthor")
                         .add("orgid", "org123456")
                         .add("affiliateid", "testaffiliate123")
                         .add("vcsbranch", "master")
                         .add("userid", "123456798")
                         .add("validsince", System.currentTimeMillis())
                         .add("validuntil", System.currentTimeMillis())
                         .add("created", System.currentTimeMillis())
                         .add("projectattributes", attributes.get())
                         .add("variables", VariableHelper.toBasicDBFormat(variables))
                         .add("welcome", welcomeDBObject);

        BasicDBObjectBuilder factoryDatabuilder = new BasicDBObjectBuilder();
        factoryDatabuilder.add("_id", id);
        factoryDatabuilder.add("factoryurl", factoryURLbuilder.get());
        factoryDatabuilder.add("images", imageList);

        collection.save(factoryDatabuilder.get());

        AdvancedFactoryUrl result = store.getFactory(id);
        assertNotNull(result);

        JsonParser jsonParser = new JsonParser();
        jsonParser.parse(new ByteArrayInputStream(factoryURLbuilder.get().toString().getBytes("UTF-8")));
        JsonValue jsonValue = jsonParser.getJsonObject();
        AdvancedFactoryUrl source = ObjectBuilder.createObject(AdvancedFactoryUrl.class, jsonValue);
        source.setId(id);
        source.setVariables(variables);
        source.setWelcome(welcomePage);

        compareFactories(source, result);
    }

    @Test
    public void testGetFactoryWithoutWelcome() throws Exception {

        String id = "testid1234";

        Map<String, String> attrs = new HashMap<>();
        attrs.put("testattr1", "testValue1");
        attrs.put("testattr2", "testValue2");
        attrs.put("testattr3", "testValue3");


        byte[] b = new byte[4096];
        new Random().nextBytes(b);

        BasicDBObjectBuilder attributes = BasicDBObjectBuilder.start(attrs);

        Variable variable = DtoFactory.getInstance().createDto(Variable.class);
        Replacement replacement = DtoFactory.getInstance().createDto(Replacement.class);
        replacement.setFind("find");
        replacement.setReplacemode("replace");
        replacement.setReplacemode("text_multipass");
        variable.setFiles(Collections.singletonList("glob"));
        variable.setEntries(Collections.<Replacement>singletonList(replacement));
        List<Variable> variables = Collections.<Variable>singletonList(variable);

        List<DBObject> imageList = new ArrayList<>();


        BasicDBObjectBuilder factoryURLbuilder = new BasicDBObjectBuilder();
        factoryURLbuilder.add("v", "1.1")
                         .add("vcs", "git")
                         .add("vcsurl", "http://vcsurl")
                         .add("commitid", "commit123456")
                         .add("action", "openfile")
                         .add("openfile", "true")
                         .add("vcsinfo", true)
                         .add("style", "testStyle")
                         .add("description", "testDescription")
                         .add("contactmail", "test@test.com")
                         .add("author", "someAuthor")
                         .add("orgid", "org123456")
                         .add("affiliateid", "testaffiliate123")
                         .add("vcsbranch", "master")
                         .add("userid", "123456798")
                         .add("validsince", System.currentTimeMillis())
                         .add("validuntil", System.currentTimeMillis())
                         .add("created", System.currentTimeMillis())
                         .add("projectattributes", attributes.get())
                         .add("variables", VariableHelper.toBasicDBFormat(variables))
                         .add("welcome", null);

        BasicDBObjectBuilder factoryDatabuilder = new BasicDBObjectBuilder();
        factoryDatabuilder.add("_id", id);
        factoryDatabuilder.add("factoryurl", factoryURLbuilder.get());
        factoryDatabuilder.add("images", imageList);

        collection.save(factoryDatabuilder.get());

        AdvancedFactoryUrl result = store.getFactory(id);
        assertNotNull(result);

        JsonParser jsonParser = new JsonParser();
        jsonParser.parse(new ByteArrayInputStream(factoryURLbuilder.get().toString().getBytes("UTF-8")));
        JsonValue jsonValue = jsonParser.getJsonObject();
        AdvancedFactoryUrl source = ObjectBuilder.createObject(AdvancedFactoryUrl.class, jsonValue);
        source.setId(id);
        source.setVariables(variables);

        compareFactories(result, source);
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

    private void compareFactories(AdvancedFactoryUrl source, AdvancedFactoryUrl result) {
        //need specific comparison, beacause proxy object generated by json helper doesn't have equals method
        assertEquals(source.getId(), result.getId());
        assertEquals(source.getAuthor(), result.getAuthor());
        assertEquals(source.getContactmail(), result.getContactmail());
        assertEquals(source.getCreated(), result.getCreated());
        assertEquals(source.getDescription(), result.getDescription());
        assertEquals(source.getStyle(), result.getStyle());
        assertEquals(source.getV(), result.getV());
        assertEquals(source.getVcs(), result.getVcs());
        assertEquals(source.getVcsurl(), result.getVcsurl());
        assertEquals(source.getProjectattributes(), result.getProjectattributes());
        assertEquals(source.getCommitid(), result.getCommitid());
        assertEquals(source.getVcsbranch(), result.getVcsbranch());
        assertEquals(source.getVcsinfo(), result.getVcsinfo());
        assertEquals(source.getAction(), result.getAction());
        assertEquals(source.getOpenfile(), result.getOpenfile());
    }
}
