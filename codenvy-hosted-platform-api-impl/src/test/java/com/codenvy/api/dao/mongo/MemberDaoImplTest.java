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
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.dao.ldap.UserDaoImpl;
import com.codenvy.api.user.server.dao.User;
import com.codenvy.api.workspace.server.dao.Member;
import com.codenvy.api.workspace.server.dao.Workspace;
import com.mongodb.BasicDBList;
import com.mongodb.DBObject;

import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;


/**
 * Tests for {@link com.codenvy.api.dao.mongo.MemberDaoImpl}
 *
 * @author Max Shaposhnik
 * @author Eugene Voevodin
 */
@Listeners(value = {MockitoTestNGListener.class})
public class MemberDaoImplTest extends BaseDaoTest {

    private static final String COLL_NAME = "members";

    @Mock
    private UserDaoImpl      userDao;
    @Mock
    private WorkspaceDaoImpl workspaceDao;
    private MemberDaoImpl    memberDao;

    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp(COLL_NAME);
        memberDao = new MemberDaoImpl(userDao, workspaceDao, db, COLL_NAME);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void shouldBeAbleToCreateMember() throws Exception {
        final Member testMember = createMember();

        memberDao.create(testMember);

        final DBObject membersDocument = collection.findOne(testMember.getUserId());
        assertNotNull(membersDocument);
        final BasicDBList members = (BasicDBList)membersDocument.get("members");
        assertEquals(members.size(), 1);
        assertEquals(memberDao.fromDBObject((DBObject)members.get(0)), testMember);
    }

    @Test
    public void shouldBeAbleToUpdateMember() throws Exception {
        final Member testMember = createMember();
        memberDao.create(testMember);
        //prepare update
        testMember.setRoles(singletonList("new_role"));

        memberDao.update(testMember);

        final DBObject membersDocument = collection.findOne(testMember.getUserId());
        assertNotNull(membersDocument);
        final BasicDBList members = (BasicDBList)membersDocument.get("members");
        assertEquals(members.size(), 1);
        assertEquals(memberDao.fromDBObject((DBObject)members.get(0)), testMember);
    }

    @Test
    public void shouldBeAbleToGetWorkspaceMembers() throws Exception {
        //create first member
        final Member first = new Member().withUserId("test_user_id")
                                         .withWorkspaceId("test_workspace_id")
                                         .withRoles(singletonList("workspace/admin"));
        when(userDao.getById(first.getUserId())).thenReturn(mock(User.class));
        when(workspaceDao.getById(first.getWorkspaceId())).thenReturn(mock(Workspace.class));
        memberDao.create(first);
        //create second member
        final Member second = new Member().withUserId("test_user_id2")
                                          .withWorkspaceId(first.getWorkspaceId())
                                          .withRoles(singletonList("workspace/admin"));
        when(userDao.getById(second.getUserId())).thenReturn(mock(User.class));
        when(workspaceDao.getById(second.getWorkspaceId())).thenReturn(mock(Workspace.class));
        memberDao.create(second);

        final List<Member> actualMembers = memberDao.getWorkspaceMembers(first.getWorkspaceId());

        assertEquals(new HashSet<>(actualMembers), new HashSet<>(asList(first, second)));
    }

    @Test
    public void shouldBeAbleToGetWorkspaceMember() throws NotFoundException, ServerException, ConflictException {
        final Member testMember = createMember();
        memberDao.create(testMember);

        final Member actual = memberDao.getWorkspaceMember(testMember.getWorkspaceId(), testMember.getUserId());

        assertEquals(actual, testMember);
    }

    @Test
    public void shouldBeAbleToRemoveMember() throws Exception {
        final Member testMember = createMember();
        memberDao.create(testMember);

        memberDao.remove(testMember);

        assertNull(collection.findOne(testMember.getUserId()));
    }

    @Test
    public void shouldBeAbleToGetUserRelationships() throws Exception {
        //create first member
        final Member first = new Member().withUserId("test_user_id")
                                         .withWorkspaceId("test_workspace_id")
                                         .withRoles(singletonList("workspace/admin"));
        when(userDao.getById(first.getUserId())).thenReturn(mock(User.class));
        when(workspaceDao.getById(first.getWorkspaceId())).thenReturn(mock(Workspace.class));
        memberDao.create(first);
        //create second member
        final Member second = new Member().withUserId(first.getUserId())
                                          .withWorkspaceId("test_workspace_id2")
                                          .withRoles(singletonList("workspace/admin"));
        when(userDao.getById(second.getUserId())).thenReturn(mock(User.class));
        when(workspaceDao.getById(second.getWorkspaceId())).thenReturn(mock(Workspace.class));
        memberDao.create(second);

        final List<Member> actualRelationships = memberDao.getUserRelationships(first.getUserId());

        assertEquals(new HashSet<>(actualRelationships), new HashSet<>(asList(first, second)));
    }

    private Member createMember() throws NotFoundException, ServerException {
        final Member testMember = new Member().withUserId("user12837asjhda823981h")
                                              .withWorkspaceId("workspace123abc456def")
                                              .withRoles(asList("workspace/admin", "workspace/developer"));
        when(userDao.getById(testMember.getUserId())).thenReturn(mock(User.class));
        when(workspaceDao.getById(testMember.getWorkspaceId())).thenReturn(mock(Workspace.class));
        return testMember;
    }
}
