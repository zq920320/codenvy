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
package com.codenvy.api.metrics.server.limit.subscriber;

import com.codenvy.api.metrics.server.limit.ActiveTasksHolder;
import com.codenvy.api.metrics.server.limit.ResourcesWatchdog;
import com.codenvy.api.metrics.server.limit.WorkspaceResourcesUsageLimitChangedEvent;

import org.eclipse.che.api.core.notification.EventService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Tests for {@link ChangeResourceUsageLimitSubscriber}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class ChangeResourceUsageLimitSubscriberTest {
    @Mock
    ActiveTasksHolder activeTasksHolder;
    @Mock
    EventService      eventService;
    @Mock
    ResourcesWatchdog resourcesWatchdog;

    @InjectMocks
    ChangeResourceUsageLimitSubscriber changeResourceUsageLimitSubscriber;

    @Test
    public void shouldStartCheckingInResourcesWatchdogIfItPresentInActiveTaskHolderOnEvent() {
        when(activeTasksHolder.getWatchdog("ws_id")).thenReturn(resourcesWatchdog);

        changeResourceUsageLimitSubscriber.onEvent(new WorkspaceResourcesUsageLimitChangedEvent("ws_id"));

        verify(resourcesWatchdog).checkLimit();
    }

    @Test
    public void shouldNotStartCheckingInResourcesWatchdogIfItAbsentInActiveTaskHolderOnEvent() {
        when(activeTasksHolder.getWatchdog("ws_id")).thenReturn(null);

        changeResourceUsageLimitSubscriber.onEvent(new WorkspaceResourcesUsageLimitChangedEvent("ws_id"));

        verify(resourcesWatchdog, never()).checkLimit();
    }
}