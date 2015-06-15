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
package com.codenvy.workspace.listener;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.user.server.dao.PreferenceDao;
import org.eclipse.che.api.user.server.dao.UserDao;
import org.eclipse.che.api.workspace.server.dao.Member;
import org.eclipse.che.api.workspace.server.dao.MemberDao;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;
import com.codenvy.workspace.event.StopWsEvent;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalNotification;

import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.eq;
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
    private static final String PERSISTENT_USER_ID = "PERSISTENT_USER_1";
    private static final String TEMP_USER_ID = "TEMP_USER_2";
    @Mock
    private WorkspaceDao  workspaceDao;
    @Mock
    private MemberDao     memberDao;
    @Mock
    private UserDao       userDao;
    @Mock
    private PreferenceDao preferenceDao;

    private RemovalNotification<String, Boolean> notification;
    private TestEventSubscriber                  testEventSubscriber;
    private WorkspaceRemovalListener             listener;

    @BeforeMethod
    public void setUp() throws Exception {
        EventService eventService = new EventService();
        listener = new WorkspaceRemovalListener(eventService, workspaceDao, memberDao, userDao, preferenceDao);
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
        members.add(new Member().withWorkspaceId(WS_ID).withUserId(PERSISTENT_USER_ID));
        members.add(new Member().withWorkspaceId(WS_ID).withUserId(TEMP_USER_ID));
        when(memberDao.getWorkspaceMembers(WS_ID)).thenReturn(members);
        when(preferenceDao.getPreferences(PERSISTENT_USER_ID)).thenReturn(Collections.singletonMap("temporary", "false"));
        when(preferenceDao.getPreferences(TEMP_USER_ID)).thenReturn(Collections.singletonMap("temporary", "true"));

        listener.onRemoval(notification);

        verify(workspaceDao).remove(WS_ID);
        verify(userDao).remove(TEMP_USER_ID);
        verify(userDao, never()).remove(PERSISTENT_USER_ID);
        assertTrue(testEventSubscriber.isEventPublished());
    }

    @Test
    public void shouldNotRemoveUsersThatAreMembersOfAnotherWorkspaces() throws Exception {
        List<Member> members = new ArrayList<>();
        members.add(new Member().withWorkspaceId(WS_ID).withUserId(TEMP_USER_ID));
        when(memberDao.getWorkspaceMembers(WS_ID)).thenReturn(members);
        when(memberDao.getUserRelationships(TEMP_USER_ID))
                .thenReturn(Arrays.asList(new Member().withUserId(TEMP_USER_ID).withWorkspaceId(WS_ID)));
        when(preferenceDao.getPreferences(PERSISTENT_USER_ID)).thenReturn(Collections.singletonMap("temporary", "false"));
        when(preferenceDao.getPreferences(TEMP_USER_ID)).thenReturn(Collections.singletonMap("temporary", "true"));

        listener.onRemoval(notification);

        verify(workspaceDao).remove(WS_ID);
        verify(userDao, never()).remove(TEMP_USER_ID);
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

        verifyZeroInteractions(workspaceDao, memberDao, userDao, preferenceDao);
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
