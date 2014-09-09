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
package com.codenvy.workspace.activity;

import com.codenvy.api.core.notification.EventService;
import com.codenvy.workspace.event.WsActivityEvent;
import com.codenvy.workspace.listener.WorkspaceRemovalListener;
import com.google.common.cache.RemovalNotification;

import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@Listeners(value = {MockitoTestNGListener.class})
public class WsActivityListenerTest {

    @Mock
    private WorkspaceRemovalListener listener;

    private EventService eventService;

    private WsActivityListener activityListener;
    private final Long   temporaryTimeout  = 20L;
    private final Long   persistentTimeout = 50L;
    private final String WSID              = "workspace1231234";

    @BeforeMethod
    public void setUp() throws Exception {
        eventService = new EventService();
        activityListener = new WsActivityListener(listener, temporaryTimeout, persistentTimeout, eventService);
        final Method subscribe = activityListener.getClass().getDeclaredMethod("subscribe");
        subscribe.setAccessible(true);
        subscribe.invoke(activityListener);
    }

    @Test
    public void shouldRemoveTempWSOnTimeout() throws Exception {
        eventService.publish(new WsActivityEvent(WSID, true));
        Thread.sleep(50);
        eventService.publish(new WsActivityEvent("anotherWS", true));
        verify(listener, timeout(100)).onRemoval(Matchers.<RemovalNotification<String, Boolean>>any());
    }

    @Test
    public void shouldRemovePersistentWSOnTimeout() throws Exception {
        eventService.publish(new WsActivityEvent(WSID, false));
        Thread.sleep(100);
        eventService.publish(new WsActivityEvent("anotherWS", false));
        verify(listener, timeout(100)).onRemoval(Matchers.<RemovalNotification<String, Boolean>>any());
    }

    @Test
    public void shouldNotRemoveWSOnTimeoutNotReached() throws Exception {
        eventService.publish(new WsActivityEvent(WSID, true));
        Thread.sleep(10);
        eventService.publish(new WsActivityEvent(WSID, true));
        Thread.sleep(10);
        eventService.publish(new WsActivityEvent(WSID, true));
        Thread.sleep(10);
        eventService.publish(new WsActivityEvent(WSID, true));
        verifyZeroInteractions(listener);
    }

    @Test
    public void shouldNotRemovePersistentWSOnTimeoutNotReached() throws Exception {
        eventService.publish(new WsActivityEvent(WSID, false));
        Thread.sleep(40);
        eventService.publish(new WsActivityEvent(WSID, false));
        Thread.sleep(40);
        eventService.publish(new WsActivityEvent(WSID, false));
        verifyZeroInteractions(listener);
    }
}
