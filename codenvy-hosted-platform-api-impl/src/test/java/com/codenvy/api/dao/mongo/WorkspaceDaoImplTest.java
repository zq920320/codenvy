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
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.codenvy.api.workspace.server.dao.Workspace;
import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Tests for {@link com.codenvy.api.dao.mongo.WorkspaceDaoImpl}
 *
 * @author Max Shaposhnik
 * @author Eugene Voevodin
 */
public class WorkspaceDaoImplTest extends BaseDaoTest {

    private static final String WORKSPACE_ID   = "workspace123abc456def";
    private static final String COLL_NAME      = "workspaces";
    private static final String WORKSPACE_NAME = "ws1";

    private WorkspaceDaoImpl workspaceDao;

    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp(COLL_NAME);
        workspaceDao = new WorkspaceDaoImpl(new Gson(), db, new EventService(), COLL_NAME);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void mustSaveWorkspace() throws Exception {
        Workspace workspace = new Workspace().withId(WORKSPACE_ID)
                                             .withName(WORKSPACE_NAME)
                                             .withTemporary(true)
                                             .withAttributes(getAttributes());
        // main invoke
        workspaceDao.create(workspace);

        DBObject res = collection.findOne(new BasicDBObject("id", WORKSPACE_ID));
        assertNotNull(res, "Specified user profile does not exists.");
        assertEquals(workspace, workspaceDao.fromDBObject(res));
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Workspace name required")
    public void mustNotCreateWorkspaceWithNullName() throws ConflictException, ServerException {
        workspaceDao.create(new Workspace().withName(null));
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Incorrect workspace name")
    public void mustNotCreateWorkspaceThatNameLengthMoreThan20Characters() throws ConflictException, ServerException {
        workspaceDao.create(new Workspace().withName("12345678901234567890x"));
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Incorrect workspace name")
    public void mustNotCreateWorkspaceThatNameLengthLessThan3Characters() throws ConflictException, ServerException {
        workspaceDao.create(new Workspace().withName("ws"));
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Incorrect workspace name")
    public void mustNotCreateWorkspaceThatNameStartsNotWithLetterOrDigit() throws ConflictException, ServerException {
        workspaceDao.create(new Workspace().withName(".ws"));
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Incorrect workspace name")
    public void mustNotCreateWorkspaceThatNameEndsNotWithLetterOrDigit() throws ConflictException, ServerException {
        workspaceDao.create(new Workspace().withName("ws-"));
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Incorrect workspace name")
    public void mustNotCreateWorkspaceThatNameContainsIllegalCharacters() throws ConflictException, ServerException {
        workspaceDao.create(new Workspace().withName("worksp@ce"));
    }

    @Test
    public void mustUpdateWorkspace() throws Exception {
        Workspace workspace = new Workspace().withId(WORKSPACE_ID)
                                             .withName(WORKSPACE_NAME)
                                             .withAttributes(getAttributes())
                                             .withTemporary(true);
        // Put first object
        collection.insert(new BasicDBObject("id", WORKSPACE_ID).append("name", WORKSPACE_NAME));
        // main invoke
        workspaceDao.update(workspace);

        DBObject res = collection.findOne(new BasicDBObject("id", WORKSPACE_ID));
        assertNotNull(res, "Specified workspace does not exists.");
        assertEquals(workspaceDao.fromDBObject(res), workspace);
    }

    @Test
    public void mustNotSaveWorkspaceIfSameNameExist() throws Exception {
        // Put first object
        collection.insert(new BasicDBObject("id", WORKSPACE_ID).append("name", WORKSPACE_NAME));

        Workspace workspace = new Workspace().withId(WORKSPACE_ID)
                                             .withName(WORKSPACE_NAME)
                                             .withTemporary(true);
        try {
            workspaceDao.create(workspace);
            fail("Workspace with same name exists, but another is created.");
        } catch (ConflictException e) {
            // OK
        }
    }

    @Test
    public void mustFindWorkspaceById() throws Exception {
        collection.insert(new BasicDBObject("id", WORKSPACE_ID).append("name", WORKSPACE_NAME)
                                                               .append("temporary", true));
        Workspace result = workspaceDao.getById(WORKSPACE_ID);
        assertNotNull(result);
        assertEquals(result.getName(), WORKSPACE_NAME);
        assertTrue(result.isTemporary());
    }

    @Test
    public void mustFindWorkspaceByName() throws Exception {
        collection.insert(new BasicDBObject("id", WORKSPACE_ID).append("name", WORKSPACE_NAME)
                                                               .append("temporary", true));
        Workspace result = workspaceDao.getByName(WORKSPACE_NAME);
        assertNotNull(result);
        assertEquals(result.getId(), WORKSPACE_ID);
        assertTrue(result.isTemporary());
    }

    @Test
    public void mustFindWorkspacesByAccount() throws Exception {
        String accId = "acc123456";
        collection.insert(new BasicDBObject("id", WORKSPACE_ID).append("name", WORKSPACE_NAME)
                                                               .append("temporary", true)
                                                               .append("accountId", accId));
        collection.insert(new BasicDBObject("id", WORKSPACE_ID + "2").append("name", WORKSPACE_NAME + "2")
                                                                     .append("temporary", false)
                                                                     .append("accountId", accId));

        List<Workspace> result = workspaceDao.getByAccount(accId);
        assertNotNull(result);
        assertEquals(result.size(), 2);
    }

    @Test
    public void mustRemoveWorkspace() throws Exception {
        Workspace workspace = new Workspace().withId(WORKSPACE_ID)
                                             .withName(WORKSPACE_NAME)
                                             .withAttributes(getAttributes())
                                             .withTemporary(true);
        workspaceDao.create(workspace);

        workspaceDao.remove(WORKSPACE_ID);
        assertNull(collection.findOne(new BasicDBObject("id", WORKSPACE_ID)));
    }

    private Map<String, String> getAttributes() {
        final Map<String, String> attributes = new HashMap<>(3);
        attributes.put("attr1", "value1");
        attributes.put("attr2", "value2");
        attributes.put("attr3", "value3");
        return attributes;
    }

}
