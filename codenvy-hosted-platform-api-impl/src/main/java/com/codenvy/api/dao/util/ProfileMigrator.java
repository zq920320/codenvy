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
package com.codenvy.api.dao.util;

import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.dao.mongo.MongoDatabaseProvider;
import com.codenvy.api.user.server.dao.PreferenceDao;
import com.codenvy.api.user.server.dao.Profile;
import com.codenvy.api.user.server.dao.UserProfileDao;
import com.codenvy.commons.lang.Pair;
import com.mongodb.BasicDBList;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.codenvy.api.dao.mongo.MongoUtil.asMap;
import static java.lang.System.currentTimeMillis;

/**
 * Migrates profile attributes to preferences and ldap storage and then drops profile collection.
 *
 * @author Eugene Voevodin
 */
public final class ProfileMigrator {

    private static final Logger LOG = LoggerFactory.getLogger(ProfileMigrator.class);

    private final DBCollection   profiles;
    private final Set<String>    attributeNames;
    private final UserProfileDao profileDao;
    private final PreferenceDao  preferenceDao;

    @Inject
    public ProfileMigrator(@Named("organization.storage.db.profile.collection") String profilesCollection,
                           @Named("profile.ldap.allowed_attributes") Pair<String, String>[] allowedAttributes,
                           UserProfileDao profileDao,
                           PreferenceDao preferenceDao,
                           @Named("mongo.db.organization") DB db) {
        this.profileDao = profileDao;
        this.preferenceDao = preferenceDao;

        attributeNames = new HashSet<>();
        for (Pair<String, String> allowedAttribute : allowedAttributes) {
            attributeNames.add(allowedAttribute.second);
        }

        profiles = db.collectionExists(profilesCollection) ? db.getCollection(profilesCollection) : null;
    }

    @PostConstruct
    public void migrateProfile() {
        //when profiles collection doesn't exist migration already has been completed
        if (profiles == null) {
            return;
        }

        LOG.info("Migration has been started");

        final long start = currentTimeMillis();

        try (DBCursor cursor = profiles.find()) {
            for (DBObject profile : cursor) {
                final String id = profile.get("id").toString();
                final Object attrsObj = profile.get("attributes");

                if (!(attrsObj instanceof BasicDBList)) {
                    continue;
                }

                final Map<String, String> attributes = asMap(attrsObj);
                final Map<String, String> prefs = removePrefs(attributes);

                if (!prefs.isEmpty()) {
                    tryUpdatePreferences(id, prefs);
                }

                if (!attributes.isEmpty()) {
                    tryUpdateProfile(id, attributes);
                }
            }
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        } finally {
            profiles.drop();
        }

        LOG.info(currentTimeMillis() - start + "ms - migration time");
        LOG.info("Migration has been completed");
    }

    private void tryUpdatePreferences(String id, Map<String, String> update) throws ServerException {
        try {
            final Map<String, String> existing = preferenceDao.getPreferences(id);
            existing.putAll(update);
            preferenceDao.setPreferences(id, existing);
        } catch (NotFoundException ignored) {
            //its okay
        }
    }

    private void tryUpdateProfile(String id, Map<String, String> attributes) {
        try {
            final Profile profile = profileDao.getById(id);
            profile.getAttributes().putAll(attributes);
            profileDao.update(profile);
        } catch (ApiException ignored) {
            //its okay
        }
    }

    private Map<String, String> removePrefs(Map<String, String> attributes) {
        final Map<String, String> preferences = new HashMap<>();

        for (Iterator<Map.Entry<String, String>> it = attributes.entrySet().iterator(); it.hasNext(); ) {
            final Map.Entry<String, String> attribute = it.next();
            if (!attributeNames.contains(attribute.getKey())) {
                preferences.put(attribute.getKey(), attribute.getValue());
                it.remove();
            }
        }
        return preferences;
    }
}
