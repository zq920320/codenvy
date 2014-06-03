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
import com.codenvy.api.user.server.dao.MemberDao;
import com.codenvy.api.user.server.dao.UserDao;
import com.codenvy.api.user.shared.dto.Member;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.codenvy.api.workspace.shared.dto.Attribute;
import com.codenvy.api.workspace.shared.dto.Workspace;
import com.codenvy.dto.server.DtoFactory;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.fail;

/**
 *
 */

@Listeners(value = {MockitoTestNGListener.class})
public class WorkspaceDaoImplTest extends BaseDaoTest {

    @Mock
    private UserDao userDao;

    @Mock
    private MemberDao memberDao;

    private EventService eventService;

    private static final String COLL_NAME = "workspaces";
    WorkspaceDao workspaceDao;

    private static final String WORKSPACE_ID   = "workspace123abc456def";
    private static final String WORKSPACE_NAME = "ws1";

    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp(COLL_NAME);
        eventService = new EventService();
        workspaceDao = new WorkspaceDaoImpl(userDao, memberDao, db, COLL_NAME, eventService);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        super.tearDown();
    }


    @Test
    public void mustSaveWorkspace() throws Exception {

        Workspace workspace = DtoFactory.getInstance().createDto(Workspace.class).withId(WORKSPACE_ID).withName(WORKSPACE_NAME)
                                        .withAttributes(getAttributes()).withTemporary(true);
        // main invoke
        workspaceDao.create(workspace);

        DBObject res = collection.findOne(new BasicDBObject("id", WORKSPACE_ID));
        assertNotNull(res, "Specified user profile does not exists.");

        Workspace result =
                DtoFactory.getInstance().createDtoFromJson(res.toString(), Workspace.class);
        assertEquals(workspace.getLinks(), result.getLinks());
        assertEquals(workspace, result);
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Workspace name required")
    public void mustNotCreateWorkspaceWithNullName() throws ConflictException, ServerException {
        workspaceDao.create(DtoFactory.getInstance().createDto(Workspace.class).withName(null));
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Incorrect workspace name")
    public void mustNotCreateWorkspaceThatNameLengthMoreThan20Characters() throws ConflictException, ServerException {
        workspaceDao.create(DtoFactory.getInstance().createDto(Workspace.class).withName("12345678901234567890x"));
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Incorrect workspace name")
    public void mustNotCreateWorkspaceThatNameLengthLessThan3Characters() throws ConflictException, ServerException {
        workspaceDao.create(DtoFactory.getInstance().createDto(Workspace.class).withName("ws"));
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Incorrect workspace name")
    public void mustNotCreateWorkspaceThatNameStartsNotWithLetterOrDigit() throws ConflictException, ServerException {
        workspaceDao.create(DtoFactory.getInstance().createDto(Workspace.class).withName(".ws"));
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Incorrect workspace name")
    public void mustNotCreateWorkspaceThatNameEndsNotWithLetterOrDigit() throws ConflictException, ServerException {
        workspaceDao.create(DtoFactory.getInstance().createDto(Workspace.class).withName("ws-"));
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Incorrect workspace name")
    public void mustNotCreateWorkspaceThatNameContainsIllegalCharacters() throws ConflictException, ServerException {
        workspaceDao.create(DtoFactory.getInstance().createDto(Workspace.class).withName("worksp@ce"));
    }

    @Test
    public void mustUpdateWorkspace() throws Exception {

        Workspace workspace = DtoFactory.getInstance().createDto(Workspace.class).withId(WORKSPACE_ID).withName(WORKSPACE_NAME)
                                        .withAttributes(getAttributes()).withTemporary(true);

        // Put first object
        collection.insert(new BasicDBObject("id", WORKSPACE_ID).append("name", WORKSPACE_NAME));

        // main invoke
        workspaceDao.update(workspace);

        DBObject res = collection.findOne(new BasicDBObject("id", WORKSPACE_ID));
        assertNotNull(res, "Specified workspace does not exists.");

        Workspace result =
                DtoFactory.getInstance().createDtoFromJson(res.toString(), Workspace.class);

        assertEquals(workspace.getLinks(), result.getLinks());
        assertEquals(result, workspace);
    }


    @Test
    public void mustNotSaveWorkspaceIfSameNameExist() throws Exception {
        // Put first object
        collection.insert(new BasicDBObject("id", WORKSPACE_ID).append("name", WORKSPACE_NAME));

        Workspace workspace = DtoFactory.getInstance().createDto(Workspace.class).withId(WORKSPACE_ID).withName(
                WORKSPACE_NAME).withTemporary(true);
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
        when(memberDao.getWorkspaceMembers(WORKSPACE_ID)).thenReturn(Collections.<Member>emptyList());

        Workspace workspace = DtoFactory.getInstance().createDto(Workspace.class).withId(WORKSPACE_ID).withName(WORKSPACE_NAME)
                                        .withAttributes(getAttributes()).withTemporary(true);
        workspaceDao.create(workspace);

        workspaceDao.remove(WORKSPACE_ID);
        assertNull(collection.findOne(new BasicDBObject("id", WORKSPACE_ID)));
    }

    private List<Attribute> getAttributes() {
        List<Attribute> attributes = new ArrayList<>();
        attributes.add(DtoFactory.getInstance().createDto(Attribute.class).withName("attr1").withValue("value1")
                                 .withDescription("description1"));
        attributes.add(DtoFactory.getInstance().createDto(Attribute.class).withName("attr2").withValue("value2")
                                 .withDescription("description2"));
        attributes.add(DtoFactory.getInstance().createDto(Attribute.class).withName("attr3").withValue("value3")
                                 .withDescription("description3"));
        return attributes;
    }

}
