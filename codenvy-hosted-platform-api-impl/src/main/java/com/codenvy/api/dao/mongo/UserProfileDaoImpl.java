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
package com.codenvy.api.dao.mongo;

import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.user.server.dao.UserDao;
import com.codenvy.api.user.server.dao.UserProfileDao;
import com.codenvy.api.user.server.dao.Profile;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * User Profile DAO implementation based on MongoDB storage.
 */
@Singleton
public class UserProfileDaoImpl implements UserProfileDao {

    protected static final String DB_COLLECTION = "organization.storage.db.profile.collection";

    DBCollection collection;

    UserDao userDao;

    @Inject
    public UserProfileDaoImpl(UserDao userDao, DB db, @Named(DB_COLLECTION) String collectionName) {
        collection = db.getCollection(collectionName);
        collection.ensureIndex(new BasicDBObject("id", 1), new BasicDBObject("unique", true));
        this.userDao = userDao;
    }

    @Override
    public void create(Profile profile) throws ServerException {
        try {
            collection.save(toDBObject(profile));
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
    }

    @Override
    public void update(Profile profile) throws NotFoundException, ServerException {
        DBObject query = new BasicDBObject("id", profile.getId());
        try {
            if (collection.findOne(query) == null) {
                throw new NotFoundException("Profile not found " + profile.getId());
            }
            collection.update(query, toDBObject(profile));
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
    }

    @Override
    public void remove(String id) throws NotFoundException, ServerException {
        try {
            collection.remove(new BasicDBObject("id", id));
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
    }

    @Override
    public Profile getById(String id) throws NotFoundException, ServerException {
        DBObject res;
        try {
            res = collection.findOne(new BasicDBObject("id", id));
        } catch (MongoException me) {
            throw new ServerException(me.getMessage(), me);
        }
        return res != null ? toProfile(res) : null;

    }

    @Override
    public Profile getById(String id, String filter) throws NotFoundException, ServerException {
        Profile profile = getById(id);
        if (profile != null && filter != null && !filter.isEmpty()) {
            Map<String, String> matchedPrefs = new HashMap<>();
            Pattern pattern = Pattern.compile(filter);
            for (Map.Entry<String, String> pref : profile.getPreferences().entrySet()) {
                if (pattern.matcher(pref.getKey()).matches()) {
                    matchedPrefs.put(pref.getKey(), pref.getValue());
                }
            }
            profile.setPreferences(matchedPrefs);
        }
        return profile;
    }

    /**
     * Convert profile to database ready-to-use object
     */
    DBObject toDBObject(Profile profile) {
        return new BasicDBObject().append("id", profile.getId())
                                  .append("userId", profile.getUserId())
                                  .append("attributes", toDBList(profile.getAttributes()))
                                  .append("preferences", toDBList(profile.getPreferences()));
    }

    /**
     * Converts database object to profile ready-to-use object
     */
    Profile toProfile(DBObject profileObj) {
        final BasicDBObject basicProfileObj = (BasicDBObject)profileObj;
        return new Profile().withId(basicProfileObj.getString("id"))
                            .withUserId(basicProfileObj.getString("userId"))
                            .withAttributes(toMap((BasicDBList)basicProfileObj.get("attributes")))
                            .withPreferences(toMap((BasicDBList)basicProfileObj.get("preferences")));
    }

    /**
     * Converts map to database list
     */
    private BasicDBList toDBList(Map<String, String> attributes) {
        final BasicDBList list = new BasicDBList();
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            list.add(new BasicDBObject().append("name", entry.getKey())
                                        .append("value", entry.getValue()));
        }
        return list;
    }

    /**
     * Converts database list to Map
     */
    private Map<String, String> toMap(BasicDBList list) {
        final Map<String, String> attributes = new HashMap<>();
        if (list != null) {
            for (Object obj : list) {
                final BasicDBObject attribute = (BasicDBObject)obj;
                attributes.put(attribute.getString("name"), attribute.getString("value"));
            }
        }
        return attributes;
    }
}
