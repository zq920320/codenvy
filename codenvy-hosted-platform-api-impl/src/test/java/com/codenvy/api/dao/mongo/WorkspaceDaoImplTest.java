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


import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.core.notification.EventService;
import com.codenvy.api.workspace.server.dao.Workspace;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Tests for {@link WorkspaceDaoImpl}
 *
 * @author Max Shaposhnik
 * @author Eugene Voevodin
 */
public class WorkspaceDaoImplTest extends BaseDaoTest {

    private static final String COLL_NAME = "workspaces";

    private WorkspaceDaoImpl workspaceDao;

    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp(COLL_NAME);
        workspaceDao = new WorkspaceDaoImpl(db, new EventService(), COLL_NAME);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void shouldBeAbleToCreateWorkspace() throws Exception {
        final Workspace testWorkspace = createWorkspace();

        workspaceDao.create(testWorkspace);

        final DBObject workspaceDocument = collection.findOne(new BasicDBObject("id", testWorkspace.getId()));
        assertNotNull(workspaceDocument);
        assertEquals(workspaceDao.toWorkspace(workspaceDocument), testWorkspace);
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Workspace name required")
    public void shouldNotBeAbleToCreateWorkspaceWithNullName() throws ConflictException, ServerException {
        workspaceDao.create(new Workspace().withName(null));
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Incorrect workspace name")
    public void shouldNotBeAbleToCreateWorkspaceWithNameLengthMoreThan20Characters() throws ConflictException, ServerException {
        workspaceDao.create(new Workspace().withName("12345678901234567890x"));
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Incorrect workspace name")
    public void shouldNotBeAbleToCreateWorkspaceWithNameLengthLessThan3Characters() throws ConflictException, ServerException {
        workspaceDao.create(new Workspace().withName("ws"));
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Incorrect workspace name")
    public void shouldNotBeAbleToCreateWorkspaceWithNameStartsNotWithLetterOrDigit() throws ConflictException, ServerException {
        workspaceDao.create(new Workspace().withName(".ws"));
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Incorrect workspace name")
    public void shouldNotBeAbleToCreateWorkspaceWithNameEndsNotWithLetterOrDigit() throws ConflictException, ServerException {
        workspaceDao.create(new Workspace().withName("ws-"));
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Incorrect workspace name")
    public void shouldNotBeAbleToCreateWorkspaceWithNameWhichContainsIllegalCharacters() throws ConflictException, ServerException {
        workspaceDao.create(new Workspace().withName("worksp@ce"));
    }

    @Test
    public void shouldBeAbleToUpdate() throws Exception {
        final Workspace testWorkspace = createWorkspace();
        //persist workspace
        collection.insert(workspaceDao.toDBObject(testWorkspace));
        //prepare update
        final Workspace update = new Workspace().withId(testWorkspace.getId())
                                                .withName("new_name")
                                                .withAttributes(singletonMap("new_attribute", "value"));

        workspaceDao.update(update);

        final DBObject workspaceDocument = collection.findOne(new BasicDBObject("id", testWorkspace.getId()));
        assertNotNull(workspaceDocument);
        final Workspace actual = workspaceDao.toWorkspace(workspaceDocument);
        assertEquals(actual.getName(), update.getName());
        assertEquals(actual.getAttributes(), update.getAttributes());
    }

    @Test
    public void shouldThrowConflictExceptionIfWorkspaceWithUpdateNameAlreadyExists() throws Exception {
        final Workspace testWorkspace = createWorkspace();
        //persist workspace
        collection.insert(workspaceDao.toDBObject(testWorkspace));
        //prepare update
        final Workspace update = new Workspace().withId(testWorkspace.getId())
                                                .withName(testWorkspace.getName());
        //persist workspace with same name as update name
        collection.insert(new BasicDBObject("id", "test_id2").append("name", update.getName()));
        try {
            workspaceDao.create(update);
            fail();
        } catch (ConflictException ex) {
            assertEquals(ex.getMessage(), "Workspace with name '" + testWorkspace.getName() + "' already exists");
        }
    }

    @Test
    public void shouldBeAbleToGetWorkspaceById() throws Exception {
        final Workspace testWorkspace = createWorkspace();
        collection.insert(workspaceDao.toDBObject(testWorkspace));

        final Workspace actual = workspaceDao.getById(testWorkspace.getId());

        assertEquals(actual, testWorkspace);
    }

    @Test
    public void shouldBeAbleToGetWorkspaceByName() throws Exception {
        final Workspace testWorkspace = createWorkspace();
        collection.insert(workspaceDao.toDBObject(testWorkspace));

        final Workspace actual = workspaceDao.getByName(testWorkspace.getName());

        assertEquals(actual, testWorkspace);
    }

    @Test
    public void shouldBeAbleToGetWorkspacesByAccount() throws Exception {
        final Workspace testWorkspace1 = createWorkspace();
        final Workspace testWorkspace2 = createWorkspace().withId("test_id2");
        collection.insert(workspaceDao.toDBObject(testWorkspace1));
        collection.insert(workspaceDao.toDBObject(testWorkspace2));

        final List<Workspace> workspaces = workspaceDao.getByAccount(testWorkspace1.getAccountId());

        assertEquals(workspaces.size(), 2);
        assertTrue(workspaces.contains(testWorkspace1));
        assertTrue(workspaces.contains(testWorkspace2));
    }

    @Test
    public void shouldBeAbleToRemoveWorkspace() throws Exception {
        final Workspace testWorkspace = createWorkspace();
        collection.insert(workspaceDao.toDBObject(testWorkspace));

        workspaceDao.remove(testWorkspace.getId());

        assertNull(collection.findOne(new BasicDBObject("id", testWorkspace.getId())));
    }

    private Workspace createWorkspace() {
        final Map<String, String> attributes = new HashMap<>(8);
        attributes.put("attr.with.dots", "value1");
        attributes.put("attr2", "value2");
        attributes.put("attr3", "value3");
        return new Workspace().withId("test_id")
                              .withAccountId("test_account_id")
                              .withName("test_name")
                              .withAttributes(attributes);
    }
}
