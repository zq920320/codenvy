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
package com.codenvy.api.dao.mongo;

import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.user.server.dao.UserProfileDao;
import com.codenvy.api.user.server.dao.Profile;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import static com.codenvy.api.dao.mongo.MongoUtil.asDBList;
import static com.codenvy.api.dao.mongo.MongoUtil.asMap;
import static java.lang.String.format;

/**
 * Implementation of {@link UserProfileDao} based on MongoDB storage.
 * <pre>
 * Profile collection document scheme:
 *
 * {
 *      "id" : "profileId...",
 *      "userId" : "userId...",
 *      "attributes": [
 *          ...
 *          {
 *              "name" : "key...",
 *              "value" : "value..."
 *          }
 *          ...
 *      ],
 * }
 * </pre>
 *
 * @author Eugene Voevodin
 * @author Max Shaposhnik
 */
@Singleton
public class UserProfileDaoImpl implements UserProfileDao {
    private static final Logger LOG           = LoggerFactory.getLogger(UserProfileDaoImpl.class);
    private static final String DB_COLLECTION = "organization.storage.db.profile.collection";

    private final DBCollection collection;

    @Inject
    public UserProfileDaoImpl(@Named("mongo.db.organization") DB db, @Named(DB_COLLECTION) String collectionName) {
        collection = db.getCollection(collectionName);
        collection.ensureIndex(new BasicDBObject("id", 1), new BasicDBObject("unique", true));
    }

    @Override
    public void create(Profile newProfile) throws ServerException {
        try {
            collection.save(toDBObject(newProfile));
        } catch (MongoException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new ServerException("It is not possible to create profile");
        }
    }

    @Override
    public void update(Profile update) throws NotFoundException, ServerException {
        final DBObject query = new BasicDBObject("id", update.getId());
        try {
            if (collection.findOne(query) == null) {
                throw new NotFoundException(format("Profile with id %s was not found", update.getId()));
            }
            collection.update(query, toDBObject(update));
        } catch (MongoException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new ServerException("It is not possible to update profile");
        }
    }

    @Override
    public Profile getById(String id) throws NotFoundException, ServerException {
        final DBObject profileDocument;
        try {
            profileDocument = collection.findOne(new BasicDBObject("id", id));
        } catch (MongoException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new ServerException("It is not possible to get profile");
        }
        if (profileDocument == null) {
            throw new NotFoundException(format("Profile with id %s was not found", id));
        }
        return toProfile(profileDocument);
    }

    @Override
    public void remove(String id) throws NotFoundException, ServerException {
        try {
            collection.remove(new BasicDBObject("id", id));
        } catch (MongoException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new ServerException("It is not possible to remove profile");
        }
    }

    /**
     * Convert profile to database ready-to-use object
     */
    /* used in tests */DBObject toDBObject(Profile profile) {
        return new BasicDBObject().append("id", profile.getId())
                                  .append("userId", profile.getUserId())
                                  .append("attributes", asDBList(profile.getAttributes()));
    }

    /**
     * Converts database object to profile ready-to-use object
     */
    /* used in tests */Profile toProfile(DBObject profileObj) {
        final BasicDBObject basicProfileObj = (BasicDBObject)profileObj;
        return new Profile().withId(basicProfileObj.getString("id"))
                            .withUserId(basicProfileObj.getString("userId"))
                            .withAttributes(asMap(basicProfileObj.get("attributes")));
    }
}
