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
package com.codenvy.workspace.listener;

import com.codenvy.api.core.ServerException;
import com.codenvy.api.core.notification.EventService;
import com.codenvy.api.core.notification.EventSubscriber;
import com.codenvy.api.user.server.dao.Profile;
import com.codenvy.api.user.server.dao.UserDao;
import com.codenvy.api.user.server.dao.UserProfileDao;
import com.codenvy.api.workspace.server.dao.Member;
import com.codenvy.api.workspace.server.dao.MemberDao;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.codenvy.workspace.event.StopWsEvent;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalNotification;

import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link com.codenvy.workspace.listener.WorkspaceRemovalListener}
 *
 * @author Alexander Garagatyi
 */
@Listeners(value = {MockitoTestNGListener.class})
public class WorkspaceRemovalListenerTest {
    private static final String WS_ID = "wsId";
    @Mock
    private WorkspaceDao   workspaceDao;
    @Mock
    private MemberDao      memberDao;
    @Mock
    private UserDao        userDao;
    @Mock
    private UserProfileDao userProfileDao;

    private RemovalNotification<String, Boolean> notification;
    private TestEventSubscriber                  testEventSubscriber;
    private WorkspaceRemovalListener             listener;

    @BeforeMethod
    public void setUp() throws Exception {
        EventService eventService = new EventService();
        listener = new WorkspaceRemovalListener(eventService, workspaceDao, memberDao, userDao, userProfileDao);
        testEventSubscriber = new TestEventSubscriber();

        final Constructor<RemovalNotification> constructor =
                RemovalNotification.class.getDeclaredConstructor(Object.class, Object.class, RemovalCause.class);
        constructor.setAccessible(true);
        notification = constructor.newInstance(WS_ID, true, RemovalCause.EXPIRED);

        eventService.subscribe(testEventSubscriber);
    }

    @Test
    public void shouldRemoveTempWsOnlyIfWorkspaceHasNoMemberships() throws Exception {
        when(memberDao.getWorkspaceMembers(WS_ID)).thenReturn(Collections.<Member>emptyList());

        listener.onRemoval(notification);

        verify(workspaceDao).remove(WS_ID);
        assertTrue(testEventSubscriber.isEventPublished());
    }

    @Test
    public void shouldRemoveTempWsWithTempUsersThatAreMembers() throws Exception {
        List<Member> members = new ArrayList<>();
        members.add(new Member().withWorkspaceId(WS_ID).withUserId("PERSISTENT_USER_1"));
        members.add(new Member().withWorkspaceId(WS_ID).withUserId("TEMP_USER_2"));
        when(memberDao.getWorkspaceMembers(WS_ID)).thenReturn(members);
        when(userProfileDao.getById("PERSISTENT_USER_1"))
                .thenReturn(new Profile().withAttributes(Collections.singletonMap("temporary", "false")));
        when(userProfileDao.getById("TEMP_USER_2")).thenReturn(new Profile().withAttributes(Collections.singletonMap("temporary", "true")));

        listener.onRemoval(notification);

        verify(workspaceDao).remove(WS_ID);
        verify(userDao).remove("TEMP_USER_2");
        verify(userDao, never()).remove("PERSISTENT_USER_1");
        assertTrue(testEventSubscriber.isEventPublished());
    }

    @Test
    public void shouldSendEventEvenIfRemovingFailed() throws Exception {
        doThrow(new ServerException("")).when(workspaceDao).remove(WS_ID);

        listener.onRemoval(notification);

        verify(workspaceDao).remove(WS_ID);
        assertTrue(testEventSubscriber.isEventPublished());
    }

    @Test
    public void shouldDoNothingIfWsIsNotTemp() throws Exception {
        final Constructor<RemovalNotification> constructor =
                RemovalNotification.class.getDeclaredConstructor(Object.class, Object.class, RemovalCause.class);
        constructor.setAccessible(true);
        notification = constructor.newInstance(WS_ID, false, RemovalCause.EXPIRED);

        listener.onRemoval(notification);

        verifyZeroInteractions(workspaceDao, memberDao, userDao, userProfileDao);
        assertTrue(testEventSubscriber.isEventPublished());
    }

    private class TestEventSubscriber implements EventSubscriber<StopWsEvent> {
        private boolean isEventPublished;

        public TestEventSubscriber() {
            isEventPublished = false;
        }

        @Override
        public void onEvent(StopWsEvent event) {
            isEventPublished = true;
        }

        public boolean isEventPublished() {
            return isEventPublished;
        }
    }
}
