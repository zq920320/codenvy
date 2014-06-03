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
import com.codenvy.api.user.shared.dto.Member;
import com.codenvy.api.user.shared.dto.User;
import com.codenvy.api.workspace.shared.dto.Workspace;
import com.codenvy.dto.server.DtoFactory;
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


@Listeners(value = {MockitoTestNGListener.class})
public class MemberDaoImplTest extends BaseDaoTest {

    @Mock
    private UserDaoImpl userDao;

    @Mock
    private WorkspaceDaoImpl workspaceDao;

    private static final String COLL_NAME = "members";
    MemberDaoImpl memberDao;


    private static final String WORKSPACE_ID = "workspace123abc456def";
    private static final String USER_ID      = "user12837asjhda823981h";
    List<String> roles = Arrays.asList("workspace/admin", "workspace/developer");

    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp(COLL_NAME);
        memberDao = new MemberDaoImpl(userDao, workspaceDao, db, COLL_NAME);

        when(userDao.getById(anyString())).thenReturn(DtoFactory.getInstance().createDto(User.class));
        when(workspaceDao.getById(anyString())).thenReturn(DtoFactory.getInstance().createDto(Workspace.class));
    }

    @Test
    public void shouldCreateMember() throws Exception {

        Member member =
                DtoFactory.getInstance().createDto(Member.class).withUserId(USER_ID).withWorkspaceId(WORKSPACE_ID)
                          .withRoles(roles);

        memberDao.create(member);
        DBObject res = collection.findOne(USER_ID);
        assertNotNull(res, "Specified user membership does not exists.");

        for (Object dbmembers : (BasicDBList)res.get("members")) {
            Member one = DtoFactory.getInstance().createDtoFromJson(dbmembers.toString(), Member.class);
            assertEquals(one.getWorkspaceId(), WORKSPACE_ID);
            assertEquals(roles, one.getRoles());
        }

    }

    @Test
    public void shouldUpdateMember() throws Exception {
        Member member1 =
                DtoFactory.getInstance().createDto(Member.class).withUserId(USER_ID).withWorkspaceId(WORKSPACE_ID)
                          .withRoles(roles.subList(0, 1));

        memberDao.create(member1);
        member1.setRoles(roles);

        memberDao.update(member1);

        DBObject res = collection.findOne(USER_ID);
        assertNotNull(res, "Specified user membership does not exists.");

        for (Object dbmember : (BasicDBList)res.get("members")) {
            Member one = DtoFactory.getInstance().createDtoFromJson(dbmember.toString(), Member.class);
            assertEquals(one.getWorkspaceId(), WORKSPACE_ID);
            assertEquals(roles, one.getRoles());
        }
    }

    @Test
    public void shouldFindWorkspaceMembers() throws Exception {
        Member member1 =
                DtoFactory.getInstance().createDto(Member.class).withUserId(USER_ID).withWorkspaceId(WORKSPACE_ID)
                          .withRoles(roles.subList(0, 1));
        Member member2 =
                DtoFactory.getInstance().createDto(Member.class).withUserId("anotherUser").withWorkspaceId(WORKSPACE_ID)
                          .withRoles(roles);

        memberDao.create(member1);
        memberDao.create(member2);

        List<Member> found = memberDao.getWorkspaceMembers(WORKSPACE_ID);
        assertEquals(found.size(), 2);

    }

    @Test
    public void shouldRemoveMember() throws Exception {
        List<String> roles = Arrays.asList("account/admin", "account/developer");
        Member member1 =
                DtoFactory.getInstance().createDto(Member.class).withUserId(USER_ID).withWorkspaceId(WORKSPACE_ID)
                          .withRoles(roles.subList(0, 1));
        Member member2 =
                DtoFactory.getInstance().createDto(Member.class).withUserId(USER_ID).withWorkspaceId("anotherWS")
                          .withRoles(roles);

        memberDao.create(member1);
        memberDao.create(member2);

        memberDao.remove(member1);

        BasicDBList list = (BasicDBList)collection.findOne(USER_ID).get("members");
        assertEquals(list.size(), 1);
    }

    @Test
    public void shouldFindUserRelationships() throws Exception {
        Member member1 =
                DtoFactory.getInstance().createDto(Member.class).withUserId(USER_ID).withWorkspaceId(WORKSPACE_ID)
                          .withRoles(roles.subList(0, 1));
        Member member2 =
                DtoFactory.getInstance().createDto(Member.class).withUserId(USER_ID).withWorkspaceId("anotherWOrkspace")
                          .withRoles(roles);

        memberDao.create(member1);
        memberDao.create(member2);

        List<Member> found = memberDao.getUserRelationships(USER_ID);
        assertEquals(found.size(), 2);
    }
}
