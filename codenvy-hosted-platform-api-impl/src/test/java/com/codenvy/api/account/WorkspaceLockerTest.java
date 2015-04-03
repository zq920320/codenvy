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
package com.codenvy.api.account;

import org.eclipse.che.api.account.server.Constants;
import org.eclipse.che.api.workspace.server.dao.Workspace;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link WorkspaceLocker}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class WorkspaceLockerTest {
    @Mock
    WorkspaceDao workspaceDao;

    @InjectMocks
    WorkspaceLocker workspaceLocker;

    @Test
    public void shouldLockWorkspace() throws Exception {
        Workspace workspace = new Workspace().withId("WS_ID");

        when(workspaceDao.getById(eq("WS_ID"))).thenReturn(workspace);

        workspaceLocker.lockResources("WS_ID");

        verify(workspaceDao).update(argThat(new ArgumentMatcher<Workspace>() {
            @Override
            public boolean matches(Object o) {
                final Workspace o1 = (Workspace)o;
                return o1.getId().equals("WS_ID") &&
                       "true".equals(o1.getAttributes().get(Constants.RESOURCES_LOCKED_PROPERTY));
            }
        }));
    }

    @Test
    public void shouldUnlockWorkspace() throws Exception {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(Constants.RESOURCES_LOCKED_PROPERTY, "true");
        Workspace workspace = new Workspace().withId("WS_ID")
                                             .withAttributes(attributes);
        when(workspaceDao.getById(eq("WS_ID"))).thenReturn(workspace);

        workspaceLocker.unlockResources("WS_ID");

        verify(workspaceDao).update(argThat(new ArgumentMatcher<Workspace>() {
            @Override
            public boolean matches(Object o) {
                final Workspace o1 = (Workspace)o;
                return o1.getId().equals("WS_ID") &&
                       !o1.getAttributes().containsKey(Constants.RESOURCES_LOCKED_PROPERTY);
            }
        }));
    }
}
