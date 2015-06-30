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
import com.codenvy.api.metrics.server.limit.MeteredTask;

import org.eclipse.che.api.builder.BuildQueue;
import org.eclipse.che.api.builder.BuildQueueTask;
import org.eclipse.che.api.builder.internal.BuilderEvent;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.notification.EventService;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link BuildEventSubscriber}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class BuildEventSubscriberTest {
    ArgumentCaptor<MeteredTask> meteredTaskCaptor;

    @Mock
    BuildQueue        buildQueue;
    @Mock
    ActiveTasksHolder activeTasksHolder;
    @Mock
    EventService      eventService;
    @Mock
    BuildQueueTask buildQueueTask;

    @InjectMocks
    BuildEventSubscriber buildEventSubscriber;


    @BeforeMethod
    public void setUp() throws Exception {
        meteredTaskCaptor = ArgumentCaptor.forClass(MeteredTask.class);

        when(buildQueue.getTask(anyLong())).thenReturn(buildQueueTask);
    }

    @Test
    public void shouldAddMeteredTaskToActiveTaskHolderWhenBuildIsAddedInQueue() {
        buildEventSubscriber.onEvent(BuilderEvent.queueStartedEvent(1L, "ws", "project"));

        verify(activeTasksHolder).addMeteredTask(Matchers.<MeteredTask>anyObject());
    }

    @Test
    public void shouldRemoveMeteredTaskFromActiveTaskHolderWhenBuildIsDone() {
        buildEventSubscriber.onEvent(BuilderEvent.doneEvent(1L, "ws", "project"));

        verify(activeTasksHolder).removeMeteredTask(Matchers.<MeteredTask>anyObject());
    }

    @Test
    public void shouldRemoveMeteredTaskFromActiveTaskHolderWhenBuildIsCanceled() {
        buildEventSubscriber.onEvent(BuilderEvent.canceledEvent(1L, "ws", "project"));

        verify(activeTasksHolder).removeMeteredTask(Matchers.<MeteredTask>anyObject());
    }

    @Test
    public void shouldAddMeteredTaskWithIdThatEqualsToBuildPlusProcessId() {
        buildEventSubscriber.onEvent(BuilderEvent.queueStartedEvent(1L, "ws", "project"));
        verify(activeTasksHolder).addMeteredTask(meteredTaskCaptor.capture());
        MeteredTask meteredTask = meteredTaskCaptor.getValue();

        assertEquals(meteredTask.getId(), "build-1");
    }

    @Test
    public void shouldCancelBuildTaskOnInterruptOfMeterdTaskAddedInActiveTaskHolder() throws Exception {
        buildEventSubscriber.onEvent(BuilderEvent.queueStartedEvent(1L, "ws", "project"));
        verify(activeTasksHolder).addMeteredTask(meteredTaskCaptor.capture());
        MeteredTask meteredTask = meteredTaskCaptor.getValue();

        meteredTask.interrupt();

        verify(buildQueue, times(2)).getTask(1L);
        verify(buildQueueTask).cancel();
    }

    @Test
    public void shouldRemoveMeteredTaskFromActiveTaskHolderWhenExceptionOccursOnInterrupt() throws Exception {
        buildEventSubscriber.onEvent(BuilderEvent.queueStartedEvent(1L, "ws", "project"));
        verify(activeTasksHolder).addMeteredTask(meteredTaskCaptor.capture());
        when(buildQueue.getTask(anyLong())).thenReturn(buildQueueTask);
        when(buildQueue.getTask(anyLong())).thenThrow(new NotFoundException(""));
        MeteredTask meteredTask = meteredTaskCaptor.getValue();

        meteredTask.interrupt();

        verify(buildQueue, times(2)).getTask(1L);
        verify(activeTasksHolder).removeMeteredTask(meteredTask);
    }
}
