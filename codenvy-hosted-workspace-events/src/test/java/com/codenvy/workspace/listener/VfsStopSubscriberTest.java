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

import com.codenvy.api.core.notification.EventService;
import com.codenvy.workspace.event.DeleteWorkspaceEvent;
import com.codenvy.workspace.event.StopWsEvent;

import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@Listeners(value = {MockitoTestNGListener.class})
public class VfsStopSubscriberTest {
    private VfsStopSubscriber subscriber;

    private EventService eventService;

    @Mock
    private VfsCleanupPerformer vfsCleanupPerformer;

    @BeforeMethod
    public void setUp() throws Exception {
        eventService = new EventService();

        subscriber = new VfsStopSubscriber(eventService, vfsCleanupPerformer);
    }

    @Test
    public void shouldUnregisterProviderOnStopWsEvent() throws IOException {
        // given
        subscriber.subscribe();

        // when
        eventService.publish(new StopWsEvent("id", true));

        // then
        verify(vfsCleanupPerformer, timeout(500)).unregisterProvider("id");
    }

    @Test
    public void shouldUnregisterProviderAndRemoveFolderOnDeleteWsEvent() throws IOException {
        // given
        subscriber.subscribe();

        // when
        eventService.publish(new DeleteWorkspaceEvent("id", true, "name"));

        // then
        verify(vfsCleanupPerformer, timeout(500)).unregisterProvider("id");
        verify(vfsCleanupPerformer).removeFS("id", true);
    }
}
