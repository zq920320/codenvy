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
package com.codenvy.api.account.subscription.limit;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;

import com.codenvy.api.account.subscribtion.subscription.limit.ActiveRunHolder;
import com.codenvy.api.runner.internal.RunnerEvent;
import com.codenvy.api.workspace.server.dao.Workspace;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;

import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for ActiveRunHolder.
 * @author Max Shaposhnik (mshaposhnik@codenvy.com) on 1/19/15.
 *
 */
@Listeners(MockitoTestNGListener.class)
public class ActiveRunHolderTest {

    @Mock
    WorkspaceDao workspaceDao;

    ActiveRunHolder activeRunHolder;

    private static final String ACC_ID = "accountId";
    private static final String WS_ID  = "workspaceId";

    @BeforeMethod
    public void setUp() throws Exception {
        when(workspaceDao.getById(anyString())).thenReturn(new Workspace().withId(WS_ID).withAccountId(ACC_ID));
        activeRunHolder = new ActiveRunHolder(workspaceDao);
    }

    @Test
    public void shouldAddAndRemoveTask() throws Exception {
        activeRunHolder.addRun(RunnerEvent.startedEvent(1L, WS_ID, "project1"));
        activeRunHolder.addRun(RunnerEvent.startedEvent(2L, WS_ID, "project2"));
        activeRunHolder.addRun(RunnerEvent.startedEvent(3L, WS_ID, "project4"));

        activeRunHolder.removeRun(RunnerEvent.stoppedEvent(1L, WS_ID, "project1"));

        Assert.assertEquals(activeRunHolder.getActiveRuns().get(ACC_ID).size(), 2);
    }

    @Test
    public void shouldRemoveAccountIfNoMoreTasks() throws Exception {
        activeRunHolder.addRun(RunnerEvent.startedEvent(1L, WS_ID, "project1"));
        activeRunHolder.addRun(RunnerEvent.startedEvent(2L, WS_ID, "project2"));

        activeRunHolder.removeRun(RunnerEvent.stoppedEvent(1L, WS_ID, "project1"));
        activeRunHolder.removeRun(RunnerEvent.startedEvent(2L, WS_ID, "project2"));

        assertFalse(activeRunHolder.getActiveRuns().containsKey(ACC_ID));
    }


}
