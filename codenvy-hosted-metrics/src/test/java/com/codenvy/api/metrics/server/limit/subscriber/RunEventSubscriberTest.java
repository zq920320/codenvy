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

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.runner.RunQueue;
import org.eclipse.che.api.runner.RunQueueTask;
import org.eclipse.che.api.runner.internal.RunnerEvent;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link RunEventSubscriber}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class RunEventSubscriberTest {
    ArgumentCaptor<MeteredTask> meteredTaskCaptor;

    @Mock
    ActiveTasksHolder activeTasksHolder;
    @Mock
    RunQueue          runQueue;
    @Mock
    EventService      eventService;
    @Mock
    RunQueueTask      runQueueTask;

    @InjectMocks
    RunEventSubscriber runEventSubscriber;

    @BeforeMethod
    public void setUp() throws Exception {
        meteredTaskCaptor = ArgumentCaptor.forClass(MeteredTask.class);

        when(runQueue.getTask(anyLong())).thenReturn(runQueueTask);
    }

    @Test
    public void shouldAddMeteredTaskToActiveTaskHolderWhenBuildIsAddedInQueue() {
        runEventSubscriber.onEvent(RunnerEvent.queueStartedEvent(1L, "ws", "project"));

        verify(activeTasksHolder).addMeteredTask(Matchers.<MeteredTask>anyObject());
    }

    @Test
    public void shouldRemoveMeteredTaskFromActiveTaskHolderOnErrorEvent() {
        runEventSubscriber.onEvent(RunnerEvent.errorEvent(1L, "ws", "project", "error"));

        verify(activeTasksHolder).removeMeteredTask(Matchers.<MeteredTask>anyObject());
    }

    @Test
    public void shouldRemoveMeteredTaskFromActiveTaskHolderOnStoppedEvent() {
        runEventSubscriber.onEvent(RunnerEvent.stoppedEvent(1L, "ws", "project"));

        verify(activeTasksHolder).removeMeteredTask(Matchers.<MeteredTask>anyObject());
    }

    @Test
    public void shouldRemoveMeteredTaskFromActiveTaskHolderOnCanceledEvent() {
        runEventSubscriber.onEvent(RunnerEvent.canceledEvent(1L, "ws", "project"));

        verify(activeTasksHolder).removeMeteredTask(Matchers.<MeteredTask>anyObject());
    }

    @Test
    public void shouldAddMeteredTaskWithIdThatEqualsToRunIdPlusProcessId() {
        runEventSubscriber.onEvent(RunnerEvent.queueStartedEvent(1L, "ws", "project"));
        verify(activeTasksHolder).addMeteredTask(meteredTaskCaptor.capture());
        MeteredTask meteredTask = meteredTaskCaptor.getValue();

        assertEquals(meteredTask.getId(), "run-1");
    }

    @Test
    public void shouldCancelBuildTaskOnInterruptOfMeterdTaskAddedInActiveTaskHolder() throws Exception {
        runEventSubscriber.onEvent(RunnerEvent.queueStartedEvent(1L, "ws", "project"));
        verify(activeTasksHolder).addMeteredTask(meteredTaskCaptor.capture());
        MeteredTask meteredTask = meteredTaskCaptor.getValue();

        meteredTask.interrupt();

        verify(runQueue).getTask(1L);
        verify(runQueueTask).stop();
    }

    @Test
    public void shouldRemoveMeteredTaskFromActiveTaskHolderWhenExceptionOccursOnInterrupt() throws Exception {
        runEventSubscriber.onEvent(RunnerEvent.queueStartedEvent(1L, "ws", "project"));
        verify(activeTasksHolder).addMeteredTask(meteredTaskCaptor.capture());
        when(runQueue.getTask(anyLong())).thenReturn(runQueueTask);
        when(runQueue.getTask(anyLong())).thenThrow(new NotFoundException(""));
        MeteredTask meteredTask = meteredTaskCaptor.getValue();

        meteredTask.interrupt();

        verify(runQueue).getTask(1L);
        verify(activeTasksHolder).removeMeteredTask(meteredTask);
    }
}
