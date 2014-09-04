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
package com.codenvy.factory.workspace;

import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.core.notification.EventService;
import com.codenvy.api.factory.FactoryBuilder;
import com.codenvy.api.factory.dto.Factory;
import com.codenvy.api.workspace.server.dao.Workspace;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.codenvy.workspace.event.CreateWorkspaceEvent;

import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.net.URI;
import java.net.URLEncoder;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@Listeners(value = {MockitoTestNGListener.class})
public class FactoryWorkspaceResourceProviderTest {
    private static final String WS_ID     = "wsid";
    private static final String factoryId = "factoryId";

    private String runnerLifetime              = "runnerLifetime";
    private String runnerRam                   = "runnerRam";
    private String builderExecutionTime        = "builderExecutionTime";
    private String trackedRunnerRam            = "trackedRunnerRam";
    private String trackedRunnerLifetime       = "trackedRunnerLifetime";
    private String trackedBuilderExecutionTime = "trackedBuilderExecutionTime";
    private String apiEndpoint                 = "http://dev.box.com/api";

    @Mock
    private FactoryBuilder      factoryBuilder;
    @Mock
    private WorkspaceDao        workspaceDao;
    @Mock
    private Workspace           workspace;
    @Mock
    private Factory             factory;
    @Mock
    private Map<String, String> attributes;

    private String               factoryUrl;
    private String               nonfactoryUrl;
    private CreateWorkspaceEvent event;

    private FactoryWorkspaceResourceProvider provider;

    @BeforeMethod
    public void setUp() throws Exception {
        factoryUrl = URLEncoder.encode("http://dev.box.com/factory?id=" + factoryId, "UTF-8");
        nonfactoryUrl =
                URLEncoder.encode("http://dev.box.com/factory?v=1.1&vcsUrl=http://github.com/codenvy/platform-api.git", "UTF-8");
        event = new CreateWorkspaceEvent(WS_ID, true);
        provider =
                new FactoryWorkspaceResourceProvider(trackedRunnerRam, trackedRunnerLifetime, trackedBuilderExecutionTime, runnerLifetime,
                                                     runnerRam, builderExecutionTime, apiEndpoint, workspaceDao, new EventService(),
                                                     factoryBuilder);
    }

    @Test
    public void shouldDoNothingOnCreateNonTemporaryWs() {
        provider.onEvent(new CreateWorkspaceEvent(WS_ID, false));

        verifyZeroInteractions(workspaceDao);
    }

    @Test
    public void shouldSetCommonAttributesIfWsDoesNotContainFactoryUrl() throws NotFoundException, ServerException, ConflictException {
        when(workspaceDao.getById(WS_ID)).thenReturn(workspace);
        when(workspace.getAttributes()).thenReturn(attributes);
        when(workspace.withAttributes(anyMap())).thenReturn(workspace);

        provider.onEvent(event);

        verify(workspaceDao).update(workspace);
        verify(attributes).put("codenvy:runner_lifetime", runnerLifetime);
        verify(attributes).put("codenvy:runner_ram", runnerRam);
        verify(attributes).put("codenvy:builder_execution_time", builderExecutionTime);
    }

    // TODO fix it when we will be able to mock HttpJsonHelper
    @Test(enabled = false)
    public void shouldBeAbleToSetTrackedValuesForEncodedFactoryWithTrackedSubscription()
            throws NotFoundException, ServerException, ConflictException {
        when(workspaceDao.getById(WS_ID)).thenReturn(workspace);
        when(workspace.getAttributes()).thenReturn(attributes);
        when(attributes.get("factoryUrl")).thenReturn(factoryUrl);
        when(workspace.withAttributes(anyMap())).thenReturn(workspace);

        provider.onEvent(event);

        verify(workspaceDao).update(workspace);
        verify(attributes).put("codenvy:runner_lifetime", trackedRunnerLifetime);
        verify(attributes).put("codenvy:runner_ram", trackedRunnerRam);
        verify(attributes).put("codenvy:builder_execution_time", trackedBuilderExecutionTime);
    }

    // TODO fix it when we will be able to mock HttpJsonHelper
    @Test(enabled = false)
    public void shouldSetCommonAttributesIfEncodedFactoryIsNotTracked()
            throws NotFoundException, ServerException, ConflictException {
        when(workspaceDao.getById(WS_ID)).thenReturn(workspace);
        when(workspace.getAttributes()).thenReturn(attributes);
        when(attributes.get("factoryUrl")).thenReturn(factoryUrl);
        when(workspace.withAttributes(anyMap())).thenReturn(workspace);

        provider.onEvent(event);

        verify(workspaceDao).update(workspace);
        verify(attributes).put("codenvy:runner_lifetime", trackedRunnerLifetime);
        verify(attributes).put("codenvy:runner_ram", trackedRunnerRam);
        verify(attributes).put("codenvy:builder_execution_time", trackedBuilderExecutionTime);
    }

    @Test
    public void shouldBeAbleToSetTrackedValuesForNonEncodedFactoryWithTrackedSubscription() throws ApiException {
        when(workspaceDao.getById(WS_ID)).thenReturn(workspace);
        when(workspace.getAttributes()).thenReturn(attributes);
        when(attributes.get("factoryUrl")).thenReturn(nonfactoryUrl);
        when(factoryBuilder.buildNonEncoded(any(URI.class))).thenReturn(factory);
        when(workspace.withAttributes(anyMap())).thenReturn(workspace);

        provider.onEvent(event);

        verify(workspaceDao).update(workspace);
        verify(attributes).put("codenvy:runner_lifetime", runnerLifetime);
        verify(attributes).put("codenvy:runner_ram", runnerRam);
        verify(attributes).put("codenvy:builder_execution_time", builderExecutionTime);
    }

    @Test
    public void shouldBeAbleToSetTrackedValuesIfNonEncodedFactoryIsTracked() throws ApiException {
        when(workspaceDao.getById(WS_ID)).thenReturn(workspace);
        when(workspace.getAttributes()).thenReturn(attributes);
        when(attributes.get("factoryUrl")).thenReturn(nonfactoryUrl);
        when(factoryBuilder.buildNonEncoded(any(URI.class))).thenReturn(factory);
        when(factory.getOrgid()).thenReturn("orgid");
        when(workspace.withAttributes(anyMap())).thenReturn(workspace);

        provider.onEvent(event);

        verify(workspaceDao).update(workspace);
        verify(attributes).put("codenvy:runner_lifetime", trackedRunnerLifetime);
        verify(attributes).put("codenvy:runner_ram", trackedRunnerRam);
        verify(attributes).put("codenvy:builder_execution_time", trackedBuilderExecutionTime);
    }

    @Test(dataProvider = "attributesProvider")
    public void shouldNotSetCommonValueIfValueIsNullOrEmpty(String runnerLifetime,
                                                            String runnerRam, String builderExecutionTime)
            throws NotFoundException, ServerException, ConflictException {
        provider =
                new FactoryWorkspaceResourceProvider(trackedRunnerRam, trackedRunnerLifetime, trackedBuilderExecutionTime, runnerLifetime,
                                                     runnerRam, builderExecutionTime, apiEndpoint, workspaceDao, new EventService(),
                                                     factoryBuilder);

        when(workspaceDao.getById(WS_ID)).thenReturn(workspace);
        when(workspace.getAttributes()).thenReturn(attributes);
        when(workspace.withAttributes(anyMap())).thenReturn(workspace);

        provider.onEvent(event);

        verify(workspaceDao).update(workspace);
        verify(attributes).get(anyString());
        if (runnerLifetime != null) {
            verify(attributes).put("codenvy:runner_lifetime", runnerLifetime);
        }
        if (runnerRam != null) {
            verify(attributes).put("codenvy:runner_ram", runnerRam);
        }
        if (builderExecutionTime != null) {
            verify(attributes).put("codenvy:builder_execution_time", builderExecutionTime);
        }
        verifyNoMoreInteractions(attributes);
    }

    @DataProvider(name = "attributesProvider")
    public String[][] attributesProvider() {
        return new String[][]{{null, "notNull", "notNull"},
                              {"notNull", null, "notNull"},
                              {"notNull", "notNull", null},
                              {null, null, null}
        };
    }
}