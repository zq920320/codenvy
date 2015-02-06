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
package com.codenvy.api.account.subscribtion.saas;

import com.codenvy.api.account.billing.BillingPeriod;
import com.codenvy.api.account.metrics.MeterBasedStorage;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.account.shared.dto.AccountResources;
import com.codenvy.api.account.shared.dto.WorkspaceResources;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.workspace.server.dao.Workspace;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link com.codenvy.api.account.subscribtion.saas.SaasSubscriptionService}
 *
 * @author Sergii Leschenko
 */
@Listeners(value = {MockitoTestNGListener.class})
public class SaasSubscriptionServiceTest {
    @Mock
    WorkspaceDao      workspaceDao;
    @Mock
    MeterBasedStorage meterBasedStorage;
    @Mock
    BillingPeriod     billingPeriod;

    @InjectMocks
    private SaasSubscriptionService service;

    @BeforeMethod
    void setUp() {

    }

    @Test
    void shouldReturnAccountResources() throws ServerException {
        Workspace workspace = new Workspace().withId("workspaceID");
        when(workspaceDao.getByAccount(anyString())).thenReturn(Arrays.asList(workspace));

        Map<String, Double> usedReport = new HashMap<>();
        usedReport.put("workspaceID", 1024.0);
        when(meterBasedStorage.getMemoryUsedReport(anyString(), anyLong(), anyLong())).thenReturn(usedReport);

        final AccountResources accountResources = service.getAccountResources(new Subscription());

        final List<WorkspaceResources> used = accountResources.getUsed();
        assertEquals(used.size(), 1);
        assertEquals(used.get(0).getWorkspaceId(), "workspaceID");
        assertEquals(used.get(0).getMemory(), new Long(1024));
    }

    @Test
    void shouldReturnInformationAboutUsedResourcesForWorkspaceThatHasNotUsedResources() throws ServerException {
        Workspace workspace = new Workspace().withId("workspaceID");
        when(workspaceDao.getByAccount(anyString())).thenReturn(Arrays.asList(workspace));

        when(meterBasedStorage.getMemoryUsedReport(anyString(), anyLong(), anyLong())).thenReturn(new HashMap<String, Double>());

        final AccountResources accountResources = service.getAccountResources(new Subscription());

        final List<WorkspaceResources> used = accountResources.getUsed();
        assertEquals(used.size(), 1);
        assertEquals(used.get(0).getWorkspaceId(), "workspaceID");
        assertEquals(used.get(0).getMemory(), new Long(0));
    }
}
