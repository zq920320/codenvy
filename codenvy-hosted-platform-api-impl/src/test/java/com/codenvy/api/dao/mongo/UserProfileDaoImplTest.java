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
import com.codenvy.api.user.server.dao.Profile;
import com.codenvy.api.user.server.dao.UserDao;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.testng.Assert.*;

/**
 * Tests for {@link UserProfileDaoImpl}
 *
 * @author Max Shaposhnik
 * @author Eugene Voevodin
 */
public class UserProfileDaoImplTest extends BaseDaoTest {

    private static final String COLL_NAME = "profile";

    private UserProfileDaoImpl profileDaoImpl;

    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp(COLL_NAME);
        profileDaoImpl = new UserProfileDaoImpl(db, COLL_NAME);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void shouldBeAbleToCreateProfile() throws Exception {
        final Profile testProfile = createProfile();

        profileDaoImpl.create(testProfile);

        final DBObject profileDocument = collection.findOne(new BasicDBObject("id", testProfile.getId()));
        assertNotNull(profileDocument, "Specified user profile does not exists");
        assertEquals(profileDaoImpl.toProfile(profileDocument), testProfile);
    }

    @Test
    public void shouldBeAbleToGetProfileById() throws NotFoundException, ServerException {
        final Profile testProfile = createProfile();
        collection.save(profileDaoImpl.toDBObject(testProfile));

        final Profile actual = profileDaoImpl.getById(testProfile.getId());

        assertEquals(actual, testProfile);
    }

    @Test
    public void shouldBeAbleToGetProfileByIdWithFilteredPreferences() throws ServerException, NotFoundException {
        final Map<String, String> preferences = new HashMap<>(8);
        preferences.put("first", "first_value");
        preferences.put("____firstASD", "other_first_value");
        preferences.put("second", "second_value");
        preferences.put("other", "other_value");
        final Profile testProfile = createProfile().withPreferences(preferences);
        collection.save(profileDaoImpl.toDBObject(testProfile));

        final Profile actual = profileDaoImpl.getById(testProfile.getId(), ".*first.*");

        final Map<String, String> expectedPreferences = new HashMap<>(4);
        expectedPreferences.put("first", "first_value");
        expectedPreferences.put("____firstASD", "other_first_value");
        testProfile.setPreferences(expectedPreferences);
        assertEquals(actual, testProfile);
    }

    @Test
    public void shouldBeAbleToUpdateProfile() throws NotFoundException, ServerException {
        final Map<String, String> attributes = new HashMap<>(4);
        attributes.put("attribute1", "test");
        attributes.put("attribute2", "test");
        //persist profile
        final Profile testProfile = createProfile().withAttributes(attributes);
        collection.save(profileDaoImpl.toDBObject(testProfile));
        //prepare update
        testProfile.setAttributes(singletonMap("new_attribute", "test"));

        profileDaoImpl.update(testProfile);

        final DBObject profileDocument = collection.findOne(new BasicDBObject("id", testProfile.getId()));
        assertNotNull(profileDocument);
        assertEquals(profileDaoImpl.toProfile(profileDocument), testProfile);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldNotBeAbleToUpdateProfileWhichDoesNotExist() throws Exception {
        profileDaoImpl.update(createProfile());
    }

    @Test
    public void shouldBeAbleToRemoveProfile() throws Exception {
        final Profile testProfile = createProfile();
        collection.insert(profileDaoImpl.toDBObject(testProfile));

        profileDaoImpl.remove(testProfile.getId());

        assertNull(collection.findOne(new BasicDBObject("id", testProfile.getId())));
    }

    private Profile createProfile() {
        final Map<String, String> attributes = new HashMap<>(4);
        attributes.put("attr1", "value1");
        attributes.put("attr2", "value2");
        final Map<String, String> preferences = new HashMap<>(4);
        preferences.put("pref1", "v1");
        preferences.put("pref2", "v2");
        return new Profile().withId("test_id")
                            .withUserId("test_id")
                            .withAttributes(attributes)
                            .withPreferences(preferences);
    }
}
