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
package com.codenvy.api.dao.mongo;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;

import com.codenvy.api.dao.ldap.UserDaoImpl;

import org.eclipse.che.api.user.server.dao.User;
import org.eclipse.che.api.workspace.server.dao.Member;
import org.eclipse.che.api.workspace.server.dao.Workspace;

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
import static org.mockito.Matchers.anyString;
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

    @Test
    public void shouldBeAbleToCreateMember() throws Exception {
        final Member testMember = createMember();

        memberDao.create(testMember);

        final DBObject memberObj = collection.findOne(memberDao.query(testMember.getUserId(), testMember.getWorkspaceId()));
        assertNotNull(memberObj);
        assertEquals(memberDao.fromDBObject(memberObj), testMember);
    }

    @Test
    public void shouldBeAbleToUpdateMember() throws Exception {
        final Member testMember = createMember();
        memberDao.create(testMember);
        //prepare update
        testMember.setRoles(singletonList("new_role"));

        memberDao.update(testMember);

        final DBObject memberObj = collection.findOne(memberDao.query(testMember.getUserId(), testMember.getWorkspaceId()));
        assertNotNull(memberObj);
        assertEquals(memberDao.fromDBObject(memberObj), testMember);
    }

    @Test
    public void shouldBeAbleToGetWorkspaceMembers() throws Exception {
        when(userDao.getById(anyString())).thenReturn(mock(User.class));
        when(workspaceDao.getById(anyString())).thenReturn(mock(Workspace.class));
        //create first member
        final Member first = new Member().withUserId("test_user_id")
                                         .withWorkspaceId("test_workspace_id")
                                         .withRoles(singletonList("workspace/admin"));
        final Member second = new Member().withUserId("test_user_id2")
                                          .withWorkspaceId(first.getWorkspaceId())
                                          .withRoles(singletonList("workspace/developer"));
        final Member third = new Member().withUserId("test_user_id2")
                                         .withWorkspaceId("another-workspace-id")
                                         .withRoles(singletonList("workspace/developer"));
        memberDao.create(first);
        memberDao.create(second);
        memberDao.create(third);

        final List<Member> result = memberDao.getWorkspaceMembers(first.getWorkspaceId());

        assertEquals(new HashSet<>(result), new HashSet<>(asList(first, second)));
    }

    @Test
    public void shouldBeAbleToGetWorkspaceMember() throws NotFoundException, ServerException, ConflictException {
        final Member testMember = createMember();
        memberDao.create(testMember);

        final Member result = memberDao.getWorkspaceMember(testMember.getWorkspaceId(), testMember.getUserId());

        assertEquals(result, testMember);
    }

    @Test
    public void shouldBeAbleToRemoveMember() throws Exception {
        final Member testMember = createMember();
        memberDao.create(testMember);

        memberDao.remove(testMember);

        assertNull(collection.findOne(memberDao.query(testMember.getUserId(), testMember.getWorkspaceId())));
    }

    @Test
    public void shouldBeAbleToGetUserRelationships() throws Exception {
        when(userDao.getById(anyString())).thenReturn(mock(User.class));
        when(workspaceDao.getById(anyString())).thenReturn(mock(Workspace.class));
        //create first member
        final Member first = new Member().withUserId("test_user_id")
                                         .withWorkspaceId("test_workspace_id")
                                         .withRoles(singletonList("workspace/admin"));
        final Member second = new Member().withUserId("test_user_id")
                                          .withWorkspaceId(first.getWorkspaceId() + "2")
                                          .withRoles(singletonList("workspace/developer"));
        final Member third = new Member().withUserId("test_user_id")
                                         .withWorkspaceId(first.getWorkspaceId() + "3")
                                         .withRoles(singletonList("workspace/developer"));
        memberDao.create(first);
        memberDao.create(second);
        memberDao.create(third);

        final List<Member> result = memberDao.getUserRelationships(first.getUserId());

        assertEquals(new HashSet<>(result), new HashSet<>(asList(first, second, third)));
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
