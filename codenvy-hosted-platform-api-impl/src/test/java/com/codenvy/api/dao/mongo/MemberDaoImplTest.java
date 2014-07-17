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

import com.codenvy.api.dao.ldap.UserDaoImpl;
import com.codenvy.api.user.shared.dto.User;
import com.codenvy.api.workspace.server.dao.Member;
import com.codenvy.api.workspace.server.dao.Workspace;
import com.codenvy.dto.server.DtoFactory;
import com.google.gson.Gson;
import com.mongodb.BasicDBList;
import com.mongodb.DBObject;

import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;


/**
 * Tests for {@link com.codenvy.api.dao.mongo.MemberDaoImpl}
 *
 * @author Max Shaposhnik
 * @author Eugene Voevodin
 */
@Listeners(value = {MockitoTestNGListener.class})
public class MemberDaoImplTest extends BaseDaoTest {

    private static final String       COLL_NAME    = "members";
    private static final String       WORKSPACE_ID = "workspace123abc456def";
    private static final String       USER_ID      = "user12837asjhda823981h";
    private static final List<String> roles        = Arrays.asList("workspace/admin", "workspace/developer");

    @Mock
    private UserDaoImpl      userDao;
    @Mock
    private WorkspaceDaoImpl workspaceDao;
    private MemberDaoImpl    memberDao;

    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp(COLL_NAME);
        memberDao = new MemberDaoImpl(new Gson(), userDao, workspaceDao, db, COLL_NAME);
        when(userDao.getById(anyString())).thenReturn(DtoFactory.getInstance().createDto(User.class));
        when(workspaceDao.getById(anyString())).thenReturn(new Workspace());
    }

    @Test
    public void shouldCreateMember() throws Exception {
        final Member member = new Member().withUserId(USER_ID)
                                          .withWorkspaceId(WORKSPACE_ID)
                                          .withRoles(roles);

        memberDao.create(member);

        final DBObject res = collection.findOne(USER_ID);
        assertNotNull(res, "Specified user membership does not exists.");

        for (Object dbmembers : (BasicDBList)res.get("members")) {
            Member one = memberDao.fromDBObject((DBObject)dbmembers);
            assertEquals(one.getWorkspaceId(), WORKSPACE_ID);
            assertEquals(roles, one.getRoles());
        }

    }

    @Test
    public void shouldUpdateMember() throws Exception {
        Member member1 = new Member().withUserId(USER_ID)
                                     .withWorkspaceId(WORKSPACE_ID)
                                     .withRoles(roles.subList(0, 1));
        memberDao.create(member1);
        member1.setRoles(roles);

        memberDao.update(member1);

        DBObject res = collection.findOne(USER_ID);
        assertNotNull(res, "Specified user membership does not exists.");
        for (Object dbMember : (BasicDBList)res.get("members")) {
            Member one = memberDao.fromDBObject((DBObject)dbMember);
            assertEquals(one.getWorkspaceId(), WORKSPACE_ID);
            assertEquals(roles, one.getRoles());
        }
    }

    @Test
    public void shouldFindWorkspaceMembers() throws Exception {
        Member member1 = new Member().withUserId(USER_ID)
                                     .withWorkspaceId(WORKSPACE_ID)
                                     .withRoles(roles.subList(0, 1));
        Member member2 = new Member().withUserId("anotherUser")
                                     .withWorkspaceId(WORKSPACE_ID)
                                     .withRoles(roles);

        memberDao.create(member1);
        memberDao.create(member2);

        List<Member> found = memberDao.getWorkspaceMembers(WORKSPACE_ID);
        assertEquals(found.size(), 2);

    }

    @Test
    public void shouldRemoveMember() throws Exception {
        List<String> roles = Arrays.asList("account/admin", "account/developer");
        Member member1 = new Member().withUserId(USER_ID)
                                     .withWorkspaceId(WORKSPACE_ID)
                                     .withRoles(roles.subList(0, 1));
        Member member2 = new Member().withUserId(USER_ID)
                                     .withWorkspaceId("anotherWS")
                                     .withRoles(roles);

        memberDao.create(member1);
        memberDao.create(member2);

        memberDao.remove(member1);

        BasicDBList list = (BasicDBList)collection.findOne(USER_ID).get("members");
        assertEquals(list.size(), 1);
    }

    @Test
    public void shouldFindUserRelationships() throws Exception {
        Member member1 = new Member().withUserId(USER_ID)
                                     .withWorkspaceId(WORKSPACE_ID)
                                     .withRoles(roles.subList(0, 1));
        Member member2 = new Member().withUserId(USER_ID)
                                     .withWorkspaceId("anotherWOrkspace")
                                     .withRoles(roles);

        memberDao.create(member1);
        memberDao.create(member2);

        List<Member> found = memberDao.getUserRelationships(USER_ID);
        assertEquals(found.size(), 2);
    }
}
