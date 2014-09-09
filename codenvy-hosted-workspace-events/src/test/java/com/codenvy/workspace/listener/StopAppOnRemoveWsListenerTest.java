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
import com.codenvy.api.runner.RunQueue;
import com.codenvy.api.runner.internal.RunnerEvent;
import com.codenvy.workspace.event.DeleteWorkspaceEvent;

import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.testng.Assert.assertEquals;

@Listeners(MockitoTestNGListener.class)
public class StopAppOnRemoveWsListenerTest {
    private static final long   PROCESS_ID     = 1234567891L;
    private static final long   PROCESS_2_ID   = 1234567892L;
    private static final long   PROCESS_3_ID   = 1234567893L;
    private static final String WORKSPACE_ID   = "ws1_2345678";
    private static final String WORKSPACE_2_ID = "ws2_2345678";
    private static final String PROJECT        = "project";

    @Mock
    private RunQueue runQueue;

    private EventService eventService;

    private StopAppOnRemoveWsListener listener;

    @BeforeMethod
    public void setUp() throws Exception {
        eventService = new EventService();
        listener = new StopAppOnRemoveWsListener(eventService, runQueue);
        final Method subscribe = listener.getClass().getDeclaredMethod("subscribe");
        subscribe.setAccessible(true);
        subscribe.invoke(listener);
    }

    @Test
    public void shouldAddProcessIdOnAddTaskInQueueEvent() throws Exception {
        Map<String, Set<Long>> expected = new HashMap<>();
        expected.put(WORKSPACE_ID, new HashSet<Long>() {{
            add(PROCESS_ID);
            add(PROCESS_2_ID);
        }});
        expected.put(WORKSPACE_2_ID, new HashSet<Long>() {{
            add(PROCESS_3_ID);
        }});

        eventService.publish(RunnerEvent.queueStartedEvent(PROCESS_ID, WORKSPACE_ID, PROJECT));
        eventService.publish(RunnerEvent.queueStartedEvent(PROCESS_2_ID, WORKSPACE_ID, PROJECT));
        eventService.publish(RunnerEvent.queueStartedEvent(PROCESS_3_ID, WORKSPACE_2_ID, PROJECT));

        assertEquals(getProcessesMap(), expected);
    }

    @Test(dataProvider = "removeProcessEventProvider")
    public void shouldBeAbleToRemoveProcess(RunnerEvent event) throws Exception {
        eventService.publish(RunnerEvent.queueStartedEvent(PROCESS_ID, WORKSPACE_ID, PROJECT));
        eventService.publish(RunnerEvent.queueStartedEvent(PROCESS_2_ID, WORKSPACE_ID, PROJECT));

        eventService.publish(event);

        assertEquals(getProcessesMap(), new HashMap<String, Set<Long>>() {{
            put(WORKSPACE_ID, new HashSet<Long>() {{
                add(PROCESS_2_ID);
            }});
        }});
    }

    @DataProvider(name = "removeProcessEventProvider")
    private Object[][] removeProcessEventProvider() {
        return new Object[][]{
                {RunnerEvent.errorEvent(PROCESS_ID, WORKSPACE_ID, PROJECT, "message")},
                {RunnerEvent.queueTerminatedEvent(PROCESS_ID, WORKSPACE_ID, PROJECT)},
                {RunnerEvent.stoppedEvent(PROCESS_ID, WORKSPACE_ID, PROJECT)}
        };
    }

    @Test
    public void shouldStopAppsOnRemoveWs() throws Exception {
        eventService.publish(RunnerEvent.queueStartedEvent(PROCESS_ID, WORKSPACE_ID, PROJECT));
        eventService.publish(RunnerEvent.queueStartedEvent(PROCESS_2_ID, WORKSPACE_ID, PROJECT));
        eventService.publish(RunnerEvent.queueStartedEvent(PROCESS_3_ID, WORKSPACE_2_ID, PROJECT));

        eventService.publish(new DeleteWorkspaceEvent(WORKSPACE_ID, false, "name"));

        assertEquals(getProcessesMap(), new HashMap() {{
            put(WORKSPACE_2_ID, new HashSet<Long>() {{
                add(PROCESS_3_ID);
            }});
        }});
    }

    private Map getProcessesMap() throws NoSuchFieldException, IllegalAccessException {
        Field f = listener.getClass().getDeclaredField("processes");
        f.setAccessible(true);
        return (Map)f.get(listener);
    }
}