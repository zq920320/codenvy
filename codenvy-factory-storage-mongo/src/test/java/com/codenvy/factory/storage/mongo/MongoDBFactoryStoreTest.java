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
package com.codenvy.factory.storage.mongo;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.factory.FactoryBuilder;
import org.eclipse.che.api.factory.FactoryImage;
import org.eclipse.che.api.factory.dto.Action;
import org.eclipse.che.api.factory.dto.Author;
import org.eclipse.che.api.factory.dto.Factory;
import org.eclipse.che.api.factory.dto.Ide;
import org.eclipse.che.api.factory.dto.OnAppLoaded;
import org.eclipse.che.api.factory.dto.OnProjectOpened;
import org.eclipse.che.api.project.shared.dto.ImportSourceDescriptor;
import org.eclipse.che.api.project.shared.dto.NewProject;
import org.eclipse.che.api.project.shared.dto.RunnerConfiguration;
import org.eclipse.che.api.project.shared.dto.RunnersDescriptor;
import org.eclipse.che.api.project.shared.dto.Source;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.dto.server.DtoFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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
    private DBCollection   collection;
    private MongoClient    client;
    private MongoServer    server;
    private MongoDBFactoryStore   store;
    private FactoryBuilder factoryBuilder;

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
        Factory factory = DtoFactory.getInstance().createDto(Factory.class);
        factory.setV("2.1");

        factory.setCreator(DtoFactory.getInstance().createDto(Author.class)
                                     .withName("someAuthor")
                                     .withAccountId("acc1")
                                     .withCreated(777777777L)
                                     .withEmail("test@test.com"));


        Map<String, RunnerConfiguration> mapConfigs = new HashMap<>();
        RunnerConfiguration runnerConfiguration = DtoFactory.getInstance().createDto(RunnerConfiguration.class);
        mapConfigs.put("this.is.my.project$configuration", runnerConfiguration);
        RunnersDescriptor runnersDescriptor = DtoFactory.getInstance().createDto(RunnersDescriptor.class).withConfigs(mapConfigs);

        factory.setProject(DtoFactory.getInstance().createDto(NewProject.class)
                                     .withName("projectName")
                                     .withType("maven")
                                     .withDescription("Description of project")
                                     .withRunners(runnersDescriptor));

        factory.setSource(DtoFactory.getInstance().createDto(Source.class)
                                    .withProject(DtoFactory.getInstance().createDto(ImportSourceDescriptor.class)
                                                           .withType("git")
                                                           .withLocation("gitUrl")));

        Ide ide = DtoFactory.getInstance().createDto(Ide.class)
                            .withOnAppLoaded(DtoFactory.getInstance().createDto(OnAppLoaded.class))
                            .withOnProjectOpened(DtoFactory.getInstance().createDto(OnProjectOpened.class));

        Action welcomePage = DtoFactory.getInstance().createDto(Action.class).withId("openWelcomePage");
        Map<String, String> welcomePageProperties = new HashMap<>();
        welcomePageProperties.put("authenticatedContentUrl", "content");
        welcomePageProperties.put("authenticatedTitle", "title");
        welcomePageProperties.put("authenticatedNotification", "notification");
        welcomePageProperties.put("nonAuthenticatedContentUrl", "content");
        welcomePageProperties.put("nonAuthenticatedTitle", "title");
        welcomePageProperties.put("nonAuthenticatedNotification", "notification");
        welcomePage.setProperties(welcomePageProperties);

        ide.getOnAppLoaded().getActions().add(welcomePage);

        Action findReplace = DtoFactory.getInstance().createDto(Action.class).withId("findReplace");
        Map<String, String> findReplaceProperties = new HashMap<>();
        findReplaceProperties.put("in", "content");
        findReplaceProperties.put("find", "title");
        findReplaceProperties.put("replace", "notification");
        findReplaceProperties.put("replaceMode", "content");
        findReplace.setProperties(findReplaceProperties);

        ide.getOnProjectOpened().getActions().add(findReplace);

        Set<FactoryImage> images = new HashSet<>();
        String id = store.saveFactory(factory, images);


        Factory result = store.getFactory(id);
        factory.setId(id);

        assertEquals(result, factory);
    }

    /**
     * Checks we can't save a null factory
     * Expects a server exception
     * @throws Exception
     */
    @Test(expectedExceptions = ServerException.class)
    public void testSaveNulFactory() throws Exception {
        store.saveFactory(null, null);

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

    @Test
    public void testGetFactoryByAttributes() throws Exception {

        Set<FactoryImage> images = new HashSet<>();

        Factory factoryUrl1 = DtoFactory.getInstance().createDto(Factory.class)
                                        .withCreator(DtoFactory.getInstance().createDto(Author.class)
                                                               .withUserId("userOK"));

        Factory factoryUrl2 = DtoFactory.getInstance().createDto(Factory.class)
                                        .withCreator(DtoFactory.getInstance().createDto(Author.class)
                                                               .withUserId("userOK"))
                                        .withProject(DtoFactory.getInstance().createDto(NewProject.class)
                                                               .withName("projectName"));

        Factory factoryUrl3 = DtoFactory.getInstance().createDto(Factory.class)
                                        .withCreator(DtoFactory.getInstance().createDto(Author.class)
                                                               .withUserId("userOK"))
                                        .withProject(DtoFactory.getInstance().createDto(NewProject.class)
                                                               .withName("projectName")
                                                               .withType("projectType"));

        store.saveFactory(factoryUrl1, images);
        store.saveFactory(factoryUrl2, images);
        store.saveFactory(factoryUrl3, images);

        assertEquals(3, store.findByAttribute(Pair.of("creator.userId", "userOK")).size());
        assertEquals(2, store.findByAttribute(Pair.of("project.name", "projectName")).size());
        assertEquals(1, store.findByAttribute(Pair.of("project.name", "projectName"), Pair.of("project.type", "projectType")).size());
        assertEquals(1, store.findByAttribute(Pair.of("creator.userId", "userOK"), Pair.of("project.type", "projectType")).size());
    }

    /**
     * Checks that we can update a factory that has images and images are unchanged after the update
     * @throws Exception if there is failure
     */
    @Test
    public void testUpdateFactory() throws Exception {
        Factory factory = DtoFactory.getInstance().createDto(Factory.class);
        factory.setV("2.1");

        factory.setCreator(DtoFactory.getInstance().createDto(Author.class)
                                     .withName("Florent")
                                     .withAccountId("acc1")
                                     .withCreated(System.currentTimeMillis())
                                     .withEmail("test@codenvy.com"));

        factory.setProject(DtoFactory.getInstance().createDto(NewProject.class)
                                     .withName("projectName")
                                     .withType("AngularJS")
                                     .withDescription("Description of project"));

        factory.setSource(DtoFactory.getInstance().createDto(Source.class)
                                    .withProject(DtoFactory.getInstance().createDto(ImportSourceDescriptor.class)
                                                           .withType("git")
                                                           .withLocation("gitUrl")));


        // new Factory
        Factory updatedFactory = DtoFactory.getInstance().clone(factory);
        updatedFactory.getSource().getProject().setLocation("gitUrlChanged");


        Set<FactoryImage> images = new HashSet<>();
        FactoryImage image = new FactoryImage();
        byte[] b = new byte[4096];
        new Random().nextBytes(b);
        image.setName("test123.jpg");
        image.setMediaType("image/jpeg");
        image.setImageData(b);
        images.add(image);
        // First save a factory
        String id = store.saveFactory(factory, images);

        // now update factory
        store.updateFactory(id, updatedFactory);


        DBObject query = new BasicDBObject("_id", id);
        DBObject res = (DBObject)collection.findOne(query).get("factoryurl");

        Factory result = DtoFactory.getInstance().createDtoFromJson(res.toString(), Factory.class);

        // check factory has been modified
        assertEquals(result.getSource().getProject().getLocation(), "gitUrlChanged");


        // check images have not been modified
        Set<FactoryImage> newImages = store.getFactoryImages(id, null);
        assertNotNull(newImages);
        FactoryImage newImage = newImages.iterator().next();

        assertTrue(newImage.getName().endsWith(image.getName()));
        assertEquals(newImage.getMediaType(), image.getMediaType());
        assertEquals(newImage.getImageData(), image.getImageData());

    }

    /**
     * Check that exception is thrown when using an unknown id
     * @throws Exception the NotFoundException expectd one
     */
    @Test(expectedExceptions = NotFoundException.class)
    public void testUpdateUnknownFactory() throws Exception {
        Factory factory = DtoFactory.getInstance().createDto(Factory.class);
        factory.setV("2.1");

        // new Factory
        Factory updatedFactory = DtoFactory.getInstance().clone(factory);

        // now update factory
        store.updateFactory("1234", updatedFactory);

    }

    /**
     * Check encoding with dot
     */
    @Test
    public void testEncodedDot() {
        String original = "hello.my.string";
        String encoded = store.encode(original);
        Assert.assertEquals("hello" + MongoDBFactoryStore.ESCAPED_DOT + "my" + MongoDBFactoryStore.ESCAPED_DOT + "string", encoded);
        String decoded = store.decode(encoded);
        Assert.assertEquals(original, decoded);
    }

    /**
     * Check encoding with dollar
     */
    @Test
    public void testEncodedDollar() {
        String original = "hello$my$string";
        String encoded = store.encode(original);
        Assert.assertEquals("hello" + MongoDBFactoryStore.ESCAPED_DOLLAR + "my" + MongoDBFactoryStore.ESCAPED_DOLLAR + "string", encoded);
        String decoded = store.decode(encoded);
        Assert.assertEquals(original, decoded);
    }

    /**
     * Check encoding with dot and dollar
     */
    @Test
    public void testEncodedDotDollar() {
        String original = "hello.my$string";
        String encoded = store.encode(original);
        Assert.assertEquals("hello" + MongoDBFactoryStore.ESCAPED_DOT + "my" + MongoDBFactoryStore.ESCAPED_DOLLAR + "string", encoded);
        String decoded = store.decode(encoded);
        Assert.assertEquals(original, decoded);
    }

}
