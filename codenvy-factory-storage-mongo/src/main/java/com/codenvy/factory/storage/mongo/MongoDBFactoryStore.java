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


import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.util.JSON;

import org.bson.Document;
import org.bson.types.Binary;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.factory.server.FactoryImage;
import org.eclipse.che.api.factory.server.FactoryStore;
import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;
import static java.lang.String.format;

/**
 * Implementation of the MongoDB factory storage.
 */

@Singleton
public class MongoDBFactoryStore implements FactoryStore {
    private static final Logger LOG = LoggerFactory.getLogger(MongoDBFactoryStore.class);

    /**
     * Escaped dot character as suggested by MongoDB
     */
    protected static final char ESCAPED_DOT = '\uFF0E';

    /**
     * Escaped dollar character as suggested by MongoDB
     */
    protected static final char ESCAPED_DOLLAR = '\uFF04';

    private final MongoCollection<Document> factories;

    @Inject
    public MongoDBFactoryStore(@Named("mongo.db.factory") MongoDatabase db,
                               @Named("factory.storage.db.collection") String collectionName) {
        factories = db.getCollection(collectionName, Document.class);
        factories.createIndex(new Document("_id", 1), new IndexOptions().unique(true));
    }


    @Override
    public String saveFactory(Factory factory, Set<FactoryImage> images) throws ApiException {

        if (factory == null) {
            throw new ServerException("The factory shouldn't be null");
        }
        factory.setId(NameGenerator.generate("", 16));
        final List<Document> imageList = images.stream().map(one -> new Document().append("name", one.getName())
                                                                                  .append("type", one.getMediaType())
                                                                                  .append("data", new Binary(one.getImageData())))
                                         .collect(Collectors.toList());
        Document factoryDocument = new Document("_id", factory.getId()).append("factory",
                                                                               Document.parse(encode(DtoFactory.getInstance().toJson(factory))))
                                                                       .append("images", imageList);
        factories.insertOne(factoryDocument);
        return factory.getId();
    }

    @Override
    public void removeFactory(String factoryId) throws ApiException {
        if (factories.deleteOne(new Document("_id", factoryId)).getDeletedCount() == 0) {
            throw new NotFoundException("Factory with id '" + factoryId + "' was not found");
        }
    }

    @Override
    public Factory getFactory(String factoryId) throws ApiException {
        final FindIterable<Document> findIt = factories.find(new Document("_id", factoryId));
        if (findIt.first() == null) {
            throw new NotFoundException("Factory with id '" + factoryId + "' was not found");
        }
        Document res =  findIt.first();
        String decoded = decode(JSON.serialize(res.get("factory", Document.class)));

        // Processing factory
        Factory factory = DtoFactory.getInstance().createDtoFromJson(decoded, Factory.class);

        factory.setId((String)res.get("_id"));

        return factory;
    }

    @Override
    public List<Factory> findByAttribute(Pair<String, String>... attributes) throws ApiException {
        List<Factory> result = new ArrayList<>();
        Document query = new Document();
        for (Pair<String, String> one : attributes) {
            query.append(format("factory.%s", one.first), one.second);
        }
        final FindIterable<Document> findIt = factories.find(query);
        for (Document one : findIt) {
            Factory factory =
                    DtoFactory.getInstance().createDtoFromJson(decode(one.get("factory", Document.class).toJson()), Factory.class);
            factory.setId((String)one.get("_id"));
            result.add(factory);
        }
        return result;
    }

    @Override
    public Set<FactoryImage> getFactoryImages(String factoryId, String imageId) throws ApiException {
        final FindIterable<Document> findIt = factories.find(new Document("_id", factoryId));
        Document res =  findIt.first();
        if (res == null) {
            throw new NotFoundException("Factory with id '" + factoryId + "' was not found");
        }
        Set<FactoryImage> images = new HashSet<>();

        for (Document obj : (List<Document>)res.get("images")) {
            try {
                if (imageId == null || obj.get("name").equals(imageId)) {
                    FactoryImage image = new FactoryImage();
                    image.setName((String)obj.get("name"));
                    image.setMediaType((String)obj.get("type"));
                    image.setImageData(((Binary)obj.get("data")).getData());
                    images.add(image);
                }
            } catch (IOException e) {
                LOG.error("Wrong image data found for image " + obj.get("name"), e);
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
     * @throws org.eclipse.che.api.core.NotFoundException
     *         if the given factory ID is not found
     * @throws org.eclipse.che.api.core.ServerException
     *         if factory is null
     */
    @Override
    public String updateFactory(String factoryId, Factory factory) throws NotFoundException, ServerException {
        if (factory == null) {
            throw new ServerException("The factory replacement shouldn't be null");
        }

        final Factory clonedFactory = DtoFactory.getInstance().clone(factory);
        clonedFactory.setId(factoryId);

        Document factoryReplacement = new Document("$set",
                                                   new Document("factory", JSON.parse(
                                                           encode(DtoFactory.getInstance()
                                                                            .toJson(clonedFactory)))));

        if (factories.findOneAndUpdate(eq("_id", factoryId), factoryReplacement) == null) {
            throw new NotFoundException("Factory with id '" + factoryId + "' was not found");
        }
        // return the factory ID
        return clonedFactory.getId();
    }

    /**
     * Mongodb is avoiding storage of dot and $ sign. Documentation is suggesting that we encode such characters
     * http://docs.mongodb.org/manual/reference/limits/#Restrictions-on-Field-Names
     * http://docs.mongodb.org/manual/faq/developers/#faq-dollar-sign-escaping
     *
     * @param value
     *         the value to encode
     * @return the encoded value
     */
    protected String encode(String value) {
        return value.replace('.', ESCAPED_DOT).replace('$', ESCAPED_DOLLAR);
    }

    /**
     * Decode the value
     *
     * @param value
     *         value to unescape
     * @return the original value without any encoding
     */
    protected String decode(String value) {
        return value.replace(ESCAPED_DOT, '.').replace(ESCAPED_DOLLAR, '$');
    }
}
