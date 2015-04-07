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
package com.codenvy.api.dao.ldap;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.user.server.dao.Profile;
import org.eclipse.che.commons.lang.Pair;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link UserProfileDaoImpl}
 *
 * @author Eugene Voevodin
 */
public class UserProfileDaoTest extends BaseTest {

    UserProfileDaoImpl profileDao;

    @BeforeMethod
    public void setUp() throws NamingException {
        final InitialLdapContextFactory contextFactory = new InitialLdapContextFactory(embeddedLdapServer.getUrl(),
                                                                                       null,
                                                                                       null,
                                                                                       null,
                                                                                       null,
                                                                                       null,
                                                                                       null,
                                                                                       null,
                                                                                       null);
        @SuppressWarnings("unchecked")
        final Pair<String, String>[] allowedAttributes = new Pair[3];
        allowedAttributes[0] = Pair.of("sn", "name");
        allowedAttributes[1] = Pair.of("telephoneNumber", "phone");
        allowedAttributes[2] = Pair.of("description", "about");

        final ProfileAttributesMapper mapper = new ProfileAttributesMapper("cn", "uid", allowedAttributes);

        profileDao = new UserProfileDaoImpl(contextFactory, mapper, "dc=codenvy,dc=com");

        final Attributes attributes = new BasicAttributes();
        attributes.put(new BasicAttribute("objectClass", "inetOrgPerson"));
        attributes.put(new BasicAttribute("cn", "profile-id"));
        attributes.put(new BasicAttribute("uid", "profile-id"));
        attributes.put(new BasicAttribute("sn", "Test User"));
        attributes.put(new BasicAttribute("password", "test password"));
        attributes.put(new BasicAttribute("telephoneNumber", "+380000000000"));
        attributes.put(new BasicAttribute("description", "test description"));
        contextFactory.createContext().createSubcontext(mapper.formatDn("profile-id", "dc=codenvy,dc=com"), attributes);
    }

    @Test
    public void shouldBeAbleToGetProfileById() throws Exception {
        final Profile profile = profileDao.getById("profile-id");

        assertEquals(profile.getId(), "profile-id");
        assertEquals(profile.getUserId(), "profile-id");

        final Map<String, String> expectedAttributes = new HashMap<>();
        expectedAttributes.put("name", "Test User");
        expectedAttributes.put("about", "test description");
        expectedAttributes.put("phone", "+380000000000");

        assertEquals(profile.getAttributes(), expectedAttributes);
    }

    @Test
    public void shouldBeAbleToUpdateProfileAttributes() throws Exception {
        final Profile profile = profileDao.getById("profile-id");
        profile.getAttributes().put("name", "new name");
        profile.getAttributes().put("fake", "should not be added");
        profile.getAttributes().put("about", null);

        profileDao.update(profile);

        final Map<String, String> expectedAttributes = new HashMap<>();
        expectedAttributes.put("name", "new name");
        expectedAttributes.put("phone", "+380000000000");

        assertEquals(profileDao.getById("profile-id").getAttributes(), expectedAttributes);
    }

    @Test(expectedExceptions = NotFoundException.class,
          expectedExceptionsMessageRegExp = "Profile with id 'fake' was not found")
    public void shouldThrowNotFoundExceptionWhenGettingProfileWhichDoesNotExist() throws Exception {
        profileDao.getById("fake");
    }
}
