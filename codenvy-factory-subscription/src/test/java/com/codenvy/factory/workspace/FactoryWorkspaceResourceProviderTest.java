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

import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.core.UnauthorizedException;
import com.codenvy.api.core.notification.EventService;
import com.codenvy.api.core.rest.HttpJsonHelper;
import com.codenvy.api.factory.FactoryBuilder;
import com.codenvy.api.factory.dto.Factory;
import com.codenvy.api.workspace.server.dao.Workspace;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.codenvy.commons.lang.Pair;
import com.codenvy.workspace.event.CreateWorkspaceEvent;

import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@Listeners(value = {MockitoTestNGListener.class})
public class FactoryWorkspaceResourceProviderTest {
    private static final String WS_ID  = "wsid";
    private static final String ORG_ID = "orgid";

    private String runnerLifetime              = "runnerLifetime";
    private String runnerRam                   = "runnerRam";
    private String builderExecutionTime        = "builderExecutionTime";
    private String trackedRunnerLifetime       = "trackedRunnerLifetime";
    private String trackedBuilderExecutionTime = "trackedBuilderExecutionTime";
    private String apiEndpoint                 = "http://dev.box.com/api";

    @Mock
    private FactoryBuilder                    factoryBuilder;
    @Mock
    private WorkspaceDao                      workspaceDao;
    @Mock
    private AccountDao                        accountDao;
    @Mock
    private Workspace                         workspace;
    @Mock
    private Factory                           factory;
    @Mock
    private Map<String, String>               attributes;
    @Mock
    private HttpJsonHelper.HttpJsonHelperImpl jsonHelper;


    private String               encodedFactoryUrl;
    private String               nonEncodedFactoryUrl;
    private CreateWorkspaceEvent event;

    private FactoryWorkspaceResourceProvider provider;

    @BeforeMethod
    public void setUp() throws Exception {
        encodedFactoryUrl = URLEncoder.encode("http://dev.box.com/factory?id=factory123456", "UTF-8");
        nonEncodedFactoryUrl =
                URLEncoder.encode("http://dev.box.com/factory?v=1.1&vcsUrl=http://github.com/codenvy/platform-api.git", "UTF-8");
        event = new CreateWorkspaceEvent(WS_ID, true);
        provider = new FactoryWorkspaceResourceProvider(trackedRunnerLifetime,
                                                        trackedBuilderExecutionTime,
                                                        runnerLifetime,
                                                        runnerRam,
                                                        builderExecutionTime,
                                                        apiEndpoint,
                                                        workspaceDao,
                                                        accountDao,
                                                        new EventService(),
                                                        factoryBuilder);
        Field field = HttpJsonHelper.class.getDeclaredField("httpJsonHelperImpl");
        field.setAccessible(true);
        field.set(null, jsonHelper);

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
        when(workspace.withAttributes(anyMapOf(String.class, String.class))).thenReturn(workspace);

        provider.onEvent(event);

        verify(workspaceDao).update(workspace);
        verify(attributes).put("codenvy:runner_lifetime", runnerLifetime);
        verify(attributes).put("codenvy:runner_ram", runnerRam);
        verify(attributes).put("codenvy:builder_execution_time", builderExecutionTime);
    }

    @Test
    public void shouldSetCommonAttributesIfNonEncodedFactoryIsNotTracked()
            throws ApiException, IOException {
        when(workspaceDao.getById(WS_ID)).thenReturn(workspace);
        when(workspace.getAttributes()).thenReturn(attributes);
        when(attributes.get("factoryUrl")).thenReturn(nonEncodedFactoryUrl);
        when(factoryBuilder.buildNonEncoded(any(URI.class))).thenReturn(factory);
        when(workspace.withAttributes(anyMapOf(String.class, String.class))).thenReturn(workspace);

        provider.onEvent(event);

        verify(workspaceDao).update(workspace);
        verifySettingOfAttributes(runnerLifetime, runnerRam, builderExecutionTime);
    }

    @Test
    public void shouldSetCommonAttributesIfEncodedFactoryIsNotTracked()
            throws NotFoundException, ServerException, ConflictException, UnauthorizedException, IOException, ForbiddenException {
        when(workspaceDao.getById(WS_ID)).thenReturn(workspace);
        when(workspace.getAttributes()).thenReturn(attributes);
        when(attributes.get("factoryUrl")).thenReturn(encodedFactoryUrl);
        when(jsonHelper.request(eq(Factory.class),
                                anyString(),
                                anyString(),
                                isNull(),
                                eq(Pair.of("validate", false))))
                .thenReturn(factory);
        when(workspace.withAttributes(anyMapOf(String.class, String.class))).thenReturn(workspace);

        provider.onEvent(event);

        verify(workspaceDao).update(workspace);
        verifySettingOfAttributes(runnerLifetime, runnerRam, builderExecutionTime);
    }

    @Test
    public void shouldSetCommonValuesIfEncodedTrackedFactoryHasNoSubscriptions() throws ApiException, IOException {
        when(workspaceDao.getById(WS_ID)).thenReturn(workspace);
        when(workspace.getAttributes()).thenReturn(attributes);
        when(attributes.get("factoryUrl")).thenReturn(encodedFactoryUrl);
        when(jsonHelper.request(eq(Factory.class),
                                anyString(),
                                anyString(),
                                isNull(),
                                eq(Pair.of("validate", false))))
                .thenReturn(factory);
        when(factory.getOrgid()).thenReturn(ORG_ID);
        when(workspace.withAttributes(anyMapOf(String.class, String.class))).thenReturn(workspace);
        when(accountDao.getSubscriptions(ORG_ID, "Factory")).thenReturn(Collections.<Subscription>emptyList());

        provider.onEvent(event);

        verify(workspaceDao).update(workspace);
        verifySettingOfAttributes(runnerLifetime, runnerRam, builderExecutionTime);
    }

    @Test
    public void shouldSetCommonValuesIfNonEncodedTrackedFactoryHasNoSubscriptions() throws ApiException, IOException {
        when(workspaceDao.getById(WS_ID)).thenReturn(workspace);
        when(workspace.getAttributes()).thenReturn(attributes);
        when(attributes.get("factoryUrl")).thenReturn(nonEncodedFactoryUrl);
        when(factoryBuilder.buildNonEncoded(any(URI.class))).thenReturn(factory);
        when(factory.getOrgid()).thenReturn(ORG_ID);
        when(workspace.withAttributes(anyMapOf(String.class, String.class))).thenReturn(workspace);
        when(accountDao.getSubscriptions(ORG_ID, "Factory")).thenReturn(Collections.<Subscription>emptyList());

        provider.onEvent(event);

        verify(workspaceDao).update(workspace);
        verifySettingOfAttributes(runnerLifetime, runnerRam, builderExecutionTime);
    }

    @Test
    public void shouldSetTrackedValuesIfEncodedFactoryIsTracked() throws ApiException, IOException {
        when(workspaceDao.getById(WS_ID)).thenReturn(workspace);
        when(workspace.getAttributes()).thenReturn(attributes);
        when(attributes.get("factoryUrl")).thenReturn(encodedFactoryUrl);
        when(jsonHelper.request(eq(Factory.class),
                                anyString(),
                                anyString(),
                                isNull(),
                                eq(Pair.of("validate", false))))
                .thenReturn(factory);
        when(factory.getOrgid()).thenReturn(ORG_ID);
        when(workspace.withAttributes(anyMapOf(String.class, String.class))).thenReturn(workspace);
        Subscription subscription = new Subscription().withProperties(Collections.singletonMap("RAM", "8GB"));
        when(accountDao.getSubscriptions(ORG_ID, "Factory")).thenReturn(Collections.singletonList(subscription));

        provider.onEvent(event);

        verify(workspaceDao).update(workspace);
        verifySettingOfAttributes(trackedRunnerLifetime, "8192", trackedBuilderExecutionTime);
    }

    @Test
    public void shouldSetTrackedValuesIfNonEncodedFactoryIsTracked() throws ApiException, IOException {
        when(workspaceDao.getById(WS_ID)).thenReturn(workspace);
        when(workspace.getAttributes()).thenReturn(attributes);
        when(attributes.get("factoryUrl")).thenReturn(nonEncodedFactoryUrl);
        when(factoryBuilder.buildNonEncoded(any(URI.class))).thenReturn(factory);
        when(factory.getOrgid()).thenReturn(ORG_ID);
        when(workspace.withAttributes(anyMapOf(String.class, String.class))).thenReturn(workspace);
        Subscription subscription = new Subscription().withProperties(Collections.singletonMap("RAM", "8GB"));
        when(accountDao.getSubscriptions(ORG_ID, "Factory")).thenReturn(Collections.singletonList(subscription));

        provider.onEvent(event);

        verify(workspaceDao).update(workspace);
        verifySettingOfAttributes(trackedRunnerLifetime, "8192", trackedBuilderExecutionTime);
    }

    @Test(dataProvider = "attributesProvider")
    public void shouldNotSetCommonValueIfValueIsNullOrEmpty(String runnerLifetime,
                                                            String runnerRam, String builderExecutionTime)
            throws NotFoundException, ServerException, ConflictException {
        provider = new FactoryWorkspaceResourceProvider(trackedRunnerLifetime,
                                                        trackedBuilderExecutionTime,
                                                        runnerLifetime,
                                                        runnerRam,
                                                        builderExecutionTime,
                                                        apiEndpoint,
                                                        workspaceDao,
                                                        accountDao,
                                                        new EventService(),
                                                        factoryBuilder);

        when(workspaceDao.getById(WS_ID)).thenReturn(workspace);
        when(workspace.getAttributes()).thenReturn(attributes);
        when(workspace.withAttributes(anyMapOf(String.class, String.class))).thenReturn(workspace);

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

    @Test
    public void shouldSetCommonValuesIfExceptionOccursOnGetFactory() throws Exception {
        when(workspaceDao.getById(WS_ID)).thenReturn(workspace);
        when(workspace.getAttributes()).thenReturn(attributes);
        when(attributes.get("factoryUrl")).thenReturn(encodedFactoryUrl);
        when(jsonHelper.request(eq(Factory.class),
                                anyString(),
                                anyString(),
                                isNull(),
                                eq(Pair.of("validate", false))))
                .thenThrow(new ServerException(""));
        when(factory.getOrgid()).thenReturn(ORG_ID);
        when(workspace.withAttributes(anyMapOf(String.class, String.class))).thenReturn(workspace);
        Subscription subscription = new Subscription().withProperties(Collections.singletonMap("RAM", "8GB"));
        when(accountDao.getSubscriptions(ORG_ID, "Factory")).thenReturn(Collections.singletonList(subscription));

        provider.onEvent(event);

        verify(workspaceDao).update(workspace);
        verifySettingOfAttributes(runnerLifetime, runnerRam, builderExecutionTime);
    }

    @Test
    public void shouldSetCommonValuesIfExeptionIsThrownOnBuildNonEncodedFactory() throws ApiException, IOException {
        when(workspaceDao.getById(WS_ID)).thenReturn(workspace);
        when(workspace.getAttributes()).thenReturn(attributes);
        when(attributes.get("factoryUrl")).thenReturn(nonEncodedFactoryUrl);
        when(factoryBuilder.buildNonEncoded(any(URI.class))).thenThrow(new ApiException(""));
        when(factory.getOrgid()).thenReturn(ORG_ID);
        when(workspace.withAttributes(anyMapOf(String.class, String.class))).thenReturn(workspace);
        Subscription subscription = new Subscription().withProperties(Collections.singletonMap("RAM", "8GB"));
        when(accountDao.getSubscriptions(ORG_ID, "Factory")).thenReturn(Collections.singletonList(subscription));

        provider.onEvent(event);

        verify(workspaceDao).update(workspace);
        verifySettingOfAttributes(runnerLifetime, runnerRam, builderExecutionTime);
    }

    private void verifySettingOfAttributes(String runnerLifetime, String runnerRam, String builderExecutionTime) {
        verify(attributes).put("codenvy:runner_lifetime", runnerLifetime);
        verify(attributes).put("codenvy:runner_ram", runnerRam);
        verify(attributes).put("codenvy:builder_execution_time", builderExecutionTime);
    }
}