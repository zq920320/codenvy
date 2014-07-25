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

import static org.testng.Assert.*;

/**
 * Tests for {@link com.codenvy.api.dao.mongo.UserProfileDaoImpl}
 *
 * @author Max
 */
@Listeners(value = {MockitoTestNGListener.class})
public class UserProfileDaoImplTest extends BaseDaoTest {

    private static final String USER_ID    = "user123abc456def";
    private static final String PROFILE_ID = "profile123abc456def";
    private static final String COLL_NAME  = "profile";

    @Mock
    private UserDao            userDao;
    private UserProfileDaoImpl profileDaoImpl;

    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp(COLL_NAME);
        profileDaoImpl = new UserProfileDaoImpl(userDao, db, COLL_NAME);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void mustSaveProfile() throws Exception {
        Map<String, String> prefs = new HashMap<>();
        prefs.put("first", "first_value");
        prefs.put("____firstASD", "other_first_value");
        prefs.put("second", "second_value");
        prefs.put("other", "other_value");
        Profile profile = new Profile().withId(PROFILE_ID)
                                       .withUserId(USER_ID)
                                       .withAttributes(getAttributes())
                                       .withPreferences(prefs);
        // main invoke
        profileDaoImpl.create(profile);

        DBObject res = collection.findOne(new BasicDBObject("id", PROFILE_ID));
        assertNotNull(res, "Specified user profile does not exists.");
        Profile result = profileDaoImpl.toProfile(res);

        assertEquals(profile, result);
    }

    @Test
    public void mustGetProfileByIdWithPreferencesFilter() throws ServerException, NotFoundException {
        Map<String, String> prefs = new HashMap<>();
        prefs.put("first", "first_value");
        prefs.put("____firstASD", "other_first_value");
        prefs.put("second", "second_value");
        prefs.put("other", "other_value");

        Profile tmp = new Profile().withId(PROFILE_ID)
                                   .withUserId(USER_ID)
                                   .withAttributes(Collections.<String, String>emptyMap())
                                   .withPreferences(prefs);

        collection.save(profileDaoImpl.toDBObject(tmp));

        Profile profile = profileDaoImpl.getById(PROFILE_ID, ".*first.*");
        assertNotNull(profile);
        Map<String, String> expectedPrefs = new HashMap<>();
        expectedPrefs.put("first", "first_value");
        expectedPrefs.put("____firstASD", "other_first_value");
        assertEquals(expectedPrefs, profile.getPreferences());

        profile = profileDaoImpl.getById(PROFILE_ID, "other");
        assertNotNull(profile);
        expectedPrefs.clear();
        expectedPrefs.put("other", "other_value");
        assertEquals(expectedPrefs, profile.getPreferences());
    }

    @Test
    public void mustNotUpdateProfileIfNotExist() throws Exception {
        final Profile profile = new Profile().withId(PROFILE_ID)
                                             .withUserId(USER_ID)
                                             .withAttributes(getAttributes());
        try {
            profileDaoImpl.update(profile);
            fail("Update of non-existing profile prohibited.");
        } catch (NotFoundException e) {
            // OK
        }
    }

    @Test
    public void mustRemoveProfile() throws Exception {
        final DBObject obj = new BasicDBObject().append("id", PROFILE_ID)
                                                .append("userId", USER_ID);
        collection.insert(obj);

        profileDaoImpl.remove(PROFILE_ID);

        assertNull(collection.findOne(new BasicDBObject("id", PROFILE_ID)));
    }

    private Map<String, String> getAttributes() {
        final Map<String, String> attributes = new HashMap<>(3);
        attributes.put("attr1", "value1");
        attributes.put("attr2", "value2");
        attributes.put("attr3", "value3");
        return attributes;
    }
}
