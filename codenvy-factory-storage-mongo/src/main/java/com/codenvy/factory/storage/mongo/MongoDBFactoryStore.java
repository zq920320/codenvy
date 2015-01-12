/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
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


import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.factory.FactoryImage;
import com.codenvy.api.factory.FactoryStore;
import com.codenvy.api.factory.dto.Factory;
import com.codenvy.commons.lang.NameGenerator;
import com.codenvy.commons.lang.Pair;
import com.codenvy.dto.server.DtoFactory;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;

/** Implementation of the MongoDB factory storage. */

@Singleton
public class MongoDBFactoryStore implements FactoryStore {
    private static final Logger LOG = LoggerFactory.getLogger(MongoDBFactoryStore.class);

    private static final String HOST       = "factory.mongo.host";
    private static final String PORT       = "factory.mongo.port";
    private static final String DATABASE   = "factory.mongo.database";
    private static final String COLLECTION = "factory.mongo.collection";
    private static final String USERNAME   = "factory.mongo.username";
    private static final String PASSWORD   = "factory.mongo.password";


    DBCollection factories;

    @Inject
    public MongoDBFactoryStore(@Named(HOST) String host, @Named(PORT) int port, @Named(DATABASE) String dbName,
                               @Named(COLLECTION) String collectionName, @Named(USERNAME) String username,
                               @Named(PASSWORD) String password) {
        MongoClient mongoClient;
        DB db;
        if (dbName == null || dbName.isEmpty() || collectionName == null || collectionName.isEmpty()) {
            throw new RuntimeException("Parameters 'database' and 'collection' can't be null or empty.");
        }

        try {
            mongoClient = new MongoClient(host, port);
            db = mongoClient.getDB(dbName);

            if (username != null && password != null) {
                if (!db.authenticate(username, password.toCharArray())) {
                    throw new RuntimeException("Wrong MongoDB credentians, authentication failed.");
                }
            }
            factories = db.getCollection(collectionName);

        } catch (UnknownHostException e) {
            throw new RuntimeException("Can't connect to MongoDB.");
        }
    }

    @Override
    public String saveFactory(Factory factoryUrl, Set<FactoryImage> images) throws ApiException {

        if (factoryUrl == null) {
            throw new ServerException("The factory shouldn't be null");
        }

        factoryUrl.setId(NameGenerator.generate("", 16));

        List<DBObject> imageList = new ArrayList<>();
        for (FactoryImage one : images) {
            imageList.add(new BasicDBObjectBuilder().add("name", one.getName())
                                                    .add("type", one.getMediaType())
                                                    .add("data", one.getImageData()).get());
        }

        BasicDBObjectBuilder factoryDatabuilder = new BasicDBObjectBuilder();
        factoryDatabuilder.add("_id", factoryUrl.getId());
        factoryDatabuilder.add("factoryurl", JSON.parse(DtoFactory.getInstance().toJson(factoryUrl)));
        factoryDatabuilder.add("images", imageList);

        factories.save(factoryDatabuilder.get());
        return factoryUrl.getId();
    }

    @Override
    public void removeFactory(String id) throws ApiException {
        factories.remove(new BasicDBObject("_id", id));
    }

    @Override
    public Factory getFactory(String id) throws ApiException {
        DBObject res = factories.findOne(new BasicDBObject("_id", id));
        if (res == null) {
            return null;
        }

        // Processing factory
        Factory factoryUrl = DtoFactory.getInstance().createDtoFromJson(res.get("factoryurl").toString(), Factory.class);

        factoryUrl.setId((String)res.get("_id"));

        return factoryUrl;
    }

    @Override
    public List<Factory> findByAttribute(Pair<String, String>... attributes) throws ApiException {
        List<Factory> result = new ArrayList<>();
        BasicDBObject query = new BasicDBObject();
        for (Pair<String, String> one : attributes) {
            query.append(format("factoryurl.%s", one.first), one.second);
        }
        DBCursor cursor = factories.find(query);
        for (DBObject one :cursor) {
            Factory factoryUrl = DtoFactory.getInstance().createDtoFromJson(one.get("factoryurl").toString(), Factory.class);
            factoryUrl.setId((String)one.get("_id"));
            result.add(factoryUrl);
        }
        return result;
    }

    @Override
    public Set<FactoryImage> getFactoryImages(String factoryId, String imageId) throws ApiException {
        Set<FactoryImage> images = new HashSet<>();

        DBObject res = factories.findOne(new BasicDBObject("_id", factoryId));
        if (res == null) {
            return Collections.emptySet();
        }

        BasicDBList imagesAsDbObject = (BasicDBList)res.get("images");
        for (Object obj : imagesAsDbObject) {
            BasicDBObject dbobj = (BasicDBObject)obj;
            try {
                if (imageId == null || dbobj.get("name").equals(imageId)) {
                    FactoryImage image = new FactoryImage();
                    image.setName((String)dbobj.get("name"));
                    image.setMediaType((String)dbobj.get("type"));
                    image.setImageData((byte[])dbobj.get("data"));
                    images.add(image);
                }
            } catch (IOException e) {
                LOG.error("Wrong image data found for image " + dbobj.get("name"), e);
            }
        }

        return images;
    }


    /**
     * Update factory at storage.
     *
     * @param factoryId
     *         - factory information
     * @param factory
     *         - factory information
     * @return - if of stored factory
     * @throws com.codenvy.api.core.NotFoundException if the given factory ID is not found
     * @throws com.codenvy.api.core.ServerException if factory is null
     */
    @Override
    public String updateFactory(String factoryId, Factory factory) throws NotFoundException, ServerException {

        if (factory == null) {
            throw new ServerException("The factory shouldn't be null");
        }

        DBObject query = new BasicDBObject("_id", factoryId);
        long res = factories.count(query);
        if (res == 0) {
            throw new NotFoundException(format("The factory with ID %s has not been found.", factoryId));
        }
        final Factory clonedFactory = DtoFactory.getInstance().clone(factory);
        clonedFactory.setId(factoryId);

        BasicDBObject factoryReplacement = new BasicDBObject("$set",
                                                             new BasicDBObject("factoryurl",
                                                                               JSON.parse(DtoFactory.getInstance().toJson(clonedFactory))));

        factories.update(query, factoryReplacement);

        // return the factory ID
        return factory.getId();
    }
}
