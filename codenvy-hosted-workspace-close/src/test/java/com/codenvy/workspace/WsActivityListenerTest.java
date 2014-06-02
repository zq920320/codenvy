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
package com.codenvy.workspace;

import com.codenvy.api.core.notification.EventService;
import com.codenvy.workspace.event.WsActivityEvent;
import com.google.common.cache.RemovalNotification;

import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.*;

import static org.mockito.Mockito.*;

@Listeners(value = {MockitoTestNGListener.class})
public class WsActivityListenerTest {

    @Mock
    private WorkspaceRemovalListener listener;

    private EventService eventService;

    WsActivityListener activityListener;
    final Long   temporaryTimeout  = 200L;
    final Long   persistentTimeout = 500L;
    final String WSID              = "workspace1231234";


    @BeforeMethod
    public void setUp() throws Exception {
        eventService = new EventService();
        activityListener = spy(new WsActivityListener(listener, temporaryTimeout, persistentTimeout, eventService));
    }

    @Test
    public void shouldRemoveTempWSOnTimeout() throws Exception {
        activityListener.onMessage(WSID, true);
        Thread.sleep(500);
        activityListener.onMessage("anotherWS", true);
        verify(listener, timeout(1000)).onRemoval(Matchers.<RemovalNotification<String, Boolean>>any());
    }

    @Test
    public void shouldRemovePersistentWSOnTimeout() throws Exception {
        activityListener.onMessage(WSID, false);
        Thread.sleep(1000);
        activityListener.onMessage("anotherWS", false);
        verify(listener, timeout(1000)).onRemoval(Matchers.<RemovalNotification<String, Boolean>>any());
    }

    @Test
    public void shouldNotRemoveWSOnTimeoutNotReached() throws Exception {
        activityListener.onMessage(WSID, true);
        Thread.sleep(100);
        activityListener.onMessage(WSID, true);
        Thread.sleep(100);
        activityListener.onMessage(WSID, true);
        Thread.sleep(100);
        activityListener.onMessage(WSID, true);
        verifyZeroInteractions(listener);
    }

    @Test
    public void shouldNotRemovePersistentWSOnTimeoutNotReached() throws Exception {
        activityListener.onMessage(WSID, false);
        Thread.sleep(400);
        activityListener.onMessage(WSID, false);
        Thread.sleep(400);
        activityListener.onMessage(WSID, false);
        verifyZeroInteractions(listener);
    }

    @Test
    public void shouldCallOnMessageOnActivityEvent() {
        // given
        activityListener.subscribe();

        // when
        eventService.publish(new WsActivityEvent(WSID, true));

        // then
        verify(activityListener).onMessage(WSID, true);
    }
}
