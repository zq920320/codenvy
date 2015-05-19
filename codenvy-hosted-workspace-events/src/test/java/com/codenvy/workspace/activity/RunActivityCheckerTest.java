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
package com.codenvy.workspace.activity;

import com.codenvy.service.http.WorkspaceInfoCache;

import org.eclipse.che.api.runner.RunQueue;
import org.eclipse.che.api.runner.RunQueueTask;
import org.eclipse.che.api.runner.dto.RunRequest;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDescriptor;
import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link RunActivityChecker}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class RunActivityCheckerTest {
    @Mock
    RunQueue              runQueue;
    @Mock
    WsActivityEventSender wsActivityEventSender;
    @Mock
    WorkspaceInfoCache    workspaceInfoCache;

    @Mock
    RunQueueTask runQueueTask;

    @InjectMocks
    RunActivityChecker runActivityChecker;

    @Test
    public void shouldNotifyWsActivityEventSenderAboutWorkspaceActivityIfExistAnyRunInWorkspace() throws Exception {
        when(runQueueTask.getRequest()).thenReturn(DtoFactory.getInstance().createDto(RunRequest.class)
                                                             .withWorkspace("workspace"));

        when(runQueue.getTasks()).thenReturn(new ArrayList(Arrays.asList(runQueueTask)));
        when(workspaceInfoCache.getById("workspace")).thenReturn(DtoFactory.getInstance().createDto(WorkspaceDescriptor.class)
                                                                           .withTemporary(false));

        runActivityChecker.check();

        verify(wsActivityEventSender).onActivity("workspace", false);
    }
}
