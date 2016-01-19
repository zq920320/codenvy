/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2016] Codenvy, S.A.
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
package com.codenvy.api.dao.mongo;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.mongodb.BasicDBObject;

import com.mongodb.MongoException;
import com.mongodb.MongoWriteException;
import com.mongodb.WriteConcernException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.conversions.Bson;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.workspace.server.dao.StackDao;
import org.eclipse.che.api.workspace.server.model.impl.StackImpl;
import org.eclipse.che.api.workspace.server.stack.image.StackIcon;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.Filters.or;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * //
 *
 * @author Alexander Andrienko
 */
@Singleton
public class StackDaoImpl implements StackDao {

    private final MongoCollection<StackImpl> collection;

    @Inject
    public StackDaoImpl(@Named("mongo.db.organization") MongoDatabase mongoDatabase,
                        @Named("organization.storage.db.stacks.collection") String collectionName) {
        collection = mongoDatabase.getCollection(collectionName, StackImpl.class);
        collection.createIndex(new BasicDBObject("creator", 1));
    }

    @Override
    public void create(StackImpl stack) throws ConflictException, ServerException {
        requireNonNull(stack, "Stack must not be null");
        try{
            collection.insertOne(stack);
        } catch (MongoWriteException | WriteConcernException wcEx) {
            throw new ConflictException(format("stack with id '%s' already exists", stack.getId()));
        } catch (MongoException mongoEx) {
            throw new ServerException(mongoEx.getMessage(), mongoEx);
        }
    }

    @Override
    public StackImpl getById(String id) throws NotFoundException, ServerException {
        requireNonNull(id, "Stack id must not be null");
        final FindIterable<StackImpl> findIt = collection.find(eq("_id", id));
        if (findIt.first() == null) {
            throw new NotFoundException("Stack with id '"+ id + "' was not find");
        }
        return findIt.first();
    }

    @Override
    public void remove(String id) throws ServerException {
        requireNonNull(id, "Stack id must not be null");
        collection.findOneAndDelete(eq("_id", id));
    }

    @Override
    public void update(StackImpl update) throws NotFoundException, ServerException, ConflictException {
        requireNonNull(update, "Stack update must not be null");
        try {
            if (collection.findOneAndReplace(eq("_id", update.getId()), update) == null) {
                throw new NotFoundException("Stack with id '" + update.getId() + "' was not found");
            }
        } catch (MongoWriteException | WriteConcernException wcEx) {
            throw new ConflictException(format("stack with id '%s' already exists", update.getId()));
        } catch (MongoException mongoEx) {
            throw new ServerException(mongoEx.getMessage(), mongoEx);
        }
    }

    @Override
    public List<StackImpl> getByCreator(String creator, int skipCount, int maxItems) throws ServerException {
        requireNonNull(creator, "Creator must not be null");
        try {
            return collection.find(eq("creator", creator))
                             .skip(skipCount)
                             .limit(maxItems)
                             .into(new ArrayList<>());
        } catch (MongoException mongoEx) {
            throw new ServerException("Impossible to retrieve stacks. ", mongoEx);
        }
    }

    @Override
    public List<StackImpl> searchStacks(String creator, List<String> tags, int skipCount, int maxItems) throws NotFoundException, ServerException {
        try {
            Bson bson;
            if (tags != null && !tags.isEmpty()) {
                bson = or(and(in("tags", tags), eq("creator", creator)), and(in("tags", tags), eq("scope", "general")));
            } else  {
                bson = or(eq("creator", creator), eq("scope", "general"));
            }

            return collection.find(bson)
                             .skip(skipCount)
                             .limit(maxItems)
                             .into(new ArrayList<>());
        } catch (MongoException mongoEx) {
            throw new ServerException("Impossible to retrieve stacks. ", mongoEx);
        }
    }

    @Override
    public StackIcon getIcon(String stackId) throws ServerException, NotFoundException {
        requireNonNull(stackId, "Stack id must not be null");
        final FindIterable<StackImpl> findIt = collection.find(eq("_id", stackId));
        if (findIt.first() == null) {
            throw new NotFoundException("Stack with id '"+ stackId + "' was not find");
        }
        StackIcon stackIcon = findIt.first().getIcon();
        if (stackIcon == null) {
            throw new NotFoundException("Icon for stack '" + stackId + "' was not find");
        }
        return stackIcon;
    }

    @Override
    public void uploadIcon(String stackId, StackIcon stackIcon) throws ServerException, NotFoundException, ConflictException {
        requireNonNull(stackId, "Stack id must not be null");
        requireNonNull(stackIcon, "Stack icon must not be null");
        final FindIterable<StackImpl> findIt = collection.find(eq("_id", stackId));
        if (findIt.first() == null) {
            throw new NotFoundException("Stack with id '"+ stackId + "' was not find");
        }
        StackImpl stack = findIt.first();
        stack.setIcon(stackIcon);
        update(stack);
    }
}
