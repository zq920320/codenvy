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
package com.codenvy.api.dao.mongo;

import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.user.server.dao.UserDao;
import com.codenvy.api.user.server.dao.UserProfileDao;
import com.codenvy.api.user.shared.dto.Attribute;
import com.codenvy.api.user.shared.dto.Profile;
import com.codenvy.dto.server.DtoFactory;
import com.mongodb.*;
import com.mongodb.util.JSON;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
     * Convert UserProfile object to Database ready-to-use object,
     * Due to mongo restriction of keys (they cannot contain "." symbols),
     * we cannot store preferences as plain map, so we store them as a list of name-value pairs.
     *
     * @param obj
     *         object to convert
     * @return DBObject
     */
    private DBObject toDBObject(Profile obj) {
        BasicDBList attributes = new BasicDBList();
        if (obj.getAttributes() != null) {
            for (Attribute one : obj.getAttributes()) {
                Attribute attribute = DtoFactory.getInstance().createDto(Attribute.class)
                                                .withName(one.getName())
                                                .withValue(one.getValue())
                                                .withDescription(one.getDescription());
                attributes.add(JSON.parse(attribute.toString()));
            }
        }

        BasicDBList preferences = new BasicDBList();
        for (Map.Entry<String, String> entry : obj.getPreferences().entrySet()) {
            BasicDBObjectBuilder pref = new BasicDBObjectBuilder();
            pref.add("name", entry.getKey()).add("value", entry.getValue());
            preferences.add(pref.get());
        }
        BasicDBObjectBuilder profileBUilder = new BasicDBObjectBuilder();
        profileBUilder.add("id", obj.getId())
                      .add("userId", obj.getUserId())
                      .add("attributes", attributes)
                      .add("preferences", preferences);

        return profileBUilder.get();

    }

    private Profile toProfile(DBObject res) {
        List<Attribute> attributes = new ArrayList<>();
        BasicDBList atts = (BasicDBList)res.get("attributes");
        for (Object obj : atts) {
            attributes.add(DtoFactory.getInstance().createDtoFromJson(obj.toString(), Attribute.class));
        }

        Map<String, String> preferences = new HashMap<>();
        BasicDBList prefs = (BasicDBList)res.get("preferences");
        for (Object obj : prefs) {
            BasicDBObject dbObject = (BasicDBObject)obj;
            preferences.put(dbObject.getString("name"), dbObject.getString("value"));
        }

        return DtoFactory.getInstance().createDto(Profile.class)
                         .withId((String)res.get("id"))
                         .withUserId((String)res.get("userId"))
                         .withAttributes(attributes)
                         .withPreferences(preferences);
    }
}
