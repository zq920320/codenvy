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
import com.codenvy.api.factory.dto.Author;
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
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@Listeners(value = {MockitoTestNGListener.class})
public class FactoryWorkspaceResourceProviderTest {
    private static final String WS_ID      = "wsid";
    private static final String ACCOUNT_ID = "accId";

    private String runnerLifetime              = "runnerLifetime";
    private String runnerRam                   = "runnerRam";
    private String builderExecutionTime        = "builderExecutionTime";
    private String trackedRunnerLifetime       = "trackedRunnerLifetime";
    private String trackedBuilderExecutionTime = "trackedBuilderExecutionTime";
    private String trackedRunnerRam            = "trackedRunnerRam";
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
    private Author                            author;
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
        event = new CreateWorkspaceEvent(new Workspace().withId(WS_ID)
                                                        .withTemporary(true));
        provider = new FactoryWorkspaceResourceProvider(trackedRunnerLifetime,
                                                        trackedBuilderExecutionTime,
                                                        trackedRunnerRam,
                                                        runnerLifetime,
                                                        runnerRam,
                                                        builderExecutionTime,
                                                        apiEndpoint,
                                                        false,
                                                        workspaceDao,
                                                        accountDao,
                                                        new EventService(),
                                                        factoryBuilder);
        Field field = HttpJsonHelper.class.getDeclaredField("httpJsonHelperImpl");
        field.setAccessible(true);
        field.set(null, jsonHelper);

        when(factory.getV()).thenReturn("2.0");
        when(factory.getCreator()).thenReturn(author);
        when(author.getAccountId()).thenReturn(ACCOUNT_ID);
        when(workspaceDao.getById(WS_ID)).thenReturn(workspace);
        when(workspace.getAttributes()).thenReturn(attributes);
        when(factoryBuilder.buildEncoded(any(URI.class))).thenReturn(factory);
        when(workspace.withAttributes(anyMapOf(String.class, String.class))).thenReturn(workspace);
        Subscription subscription = new Subscription().withProperties(Collections.singletonMap("RAM", "8GB"));
        when(accountDao.getActiveSubscriptions(ACCOUNT_ID, "Factory")).thenReturn(Collections.singletonList(subscription));
    }

    @Test
    public void shouldDoNothingOnCreateNonTemporaryWs() {
        provider.onEvent(new CreateWorkspaceEvent(workspace));

        verifyZeroInteractions(workspaceDao);
    }

    @Test
    public void shouldSetCommonAttributesIfWsDoesNotContainFactoryUrl() throws NotFoundException, ServerException, ConflictException {
        when(attributes.get("factoryUrl")).thenReturn(null);
        provider.onEvent(event);

        verify(workspaceDao).update(workspace);
        verifySettingOfAttributes(runnerLifetime, runnerRam, builderExecutionTime, false);
    }

    @Test
    public void shouldSetCommonAttributesIfNonEncodedFactoryIsNotTracked()
            throws ApiException, IOException {
        when(attributes.get("factoryUrl")).thenReturn(nonEncodedFactoryUrl);
        when(factory.getCreator()).thenReturn(null);

        provider.onEvent(event);

        verify(workspaceDao).update(workspace);
        verifySettingOfAttributes(runnerLifetime, runnerRam, builderExecutionTime, false);
    }

    @Test
    public void shouldSetCommonAttributesIfEncodedFactoryIsNotTracked()
            throws NotFoundException, ServerException, ConflictException, UnauthorizedException, IOException, ForbiddenException {
        when(attributes.get("factoryUrl")).thenReturn(encodedFactoryUrl);
        when(jsonHelper.request(eq(Factory.class),
                                anyString(),
                                anyString(),
                                isNull(),
                                eq(Pair.of("validate", false))))
                .thenReturn(factory);
        when(factory.getCreator()).thenReturn(null);

        provider.onEvent(event);

        verify(workspaceDao).update(workspace);
        verifySettingOfAttributes(runnerLifetime, runnerRam, builderExecutionTime, false);
    }

    @Test
    public void shouldSetCommonValuesIfEncodedTrackedFactoryHasNoSubscriptions() throws ApiException, IOException {
        when(attributes.get("factoryUrl")).thenReturn(encodedFactoryUrl);
        when(jsonHelper.request(eq(Factory.class),
                                anyString(),
                                anyString(),
                                isNull(),
                                eq(Pair.of("validate", false))))
                .thenReturn(factory);
        when(accountDao.getActiveSubscriptions(ACCOUNT_ID, "Factory")).thenReturn(Collections.<Subscription>emptyList());

        provider.onEvent(event);

        verify(workspaceDao).update(workspace);
        verifySettingOfAttributes(runnerLifetime, runnerRam, builderExecutionTime, false);
        verify(factory, atLeastOnce()).getCreator();
        verify(author).getAccountId();
    }

    @Test
    public void shouldSetCommonValuesIfNonEncodedTrackedFactoryHasNoSubscriptions() throws ApiException, IOException {
        when(attributes.get("factoryUrl")).thenReturn(nonEncodedFactoryUrl);
        when(accountDao.getActiveSubscriptions(ACCOUNT_ID, "Factory")).thenReturn(Collections.<Subscription>emptyList());

        provider.onEvent(event);

        verify(workspaceDao).update(workspace);
        verifySettingOfAttributes(runnerLifetime, runnerRam, builderExecutionTime, false);
    }

    @Test
    public void shouldSetTrackedValuesIfEncodedFactoryIsTracked() throws ApiException, IOException {
        when(attributes.get("factoryUrl")).thenReturn(encodedFactoryUrl);
        when(jsonHelper.request(eq(Factory.class),
                                anyString(),
                                anyString(),
                                isNull(),
                                eq(Pair.of("validate", false))))
                .thenReturn(factory);

        provider.onEvent(event);

        verify(workspaceDao).update(workspace);
        verifySettingOfAttributes(trackedRunnerLifetime, "8192", trackedBuilderExecutionTime, true);
    }

    @Test
    public void shouldSetTrackedValuesIfNonEncodedFactoryIsTracked() throws ApiException, IOException {
        when(attributes.get("factoryUrl")).thenReturn(nonEncodedFactoryUrl);

        provider.onEvent(event);

        verify(workspaceDao).update(workspace);
        verifySettingOfAttributes(trackedRunnerLifetime, "8192", trackedBuilderExecutionTime, true);
    }

    @Test(dataProvider = "attributesProvider")
    public void shouldNotSetCommonValueIfValueIsNullOrEmpty(String runnerLifetime,
                                                            String runnerRam, String builderExecutionTime)
            throws NotFoundException, ServerException, ConflictException {
        provider = new FactoryWorkspaceResourceProvider(trackedRunnerLifetime,
                                                        trackedBuilderExecutionTime,
                                                        trackedRunnerRam,
                                                        runnerLifetime,
                                                        runnerRam,
                                                        builderExecutionTime,
                                                        apiEndpoint,
                                                        false,
                                                        workspaceDao,
                                                        accountDao,
                                                        new EventService(),
                                                        factoryBuilder);


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
        when(attributes.get("factoryUrl")).thenReturn(encodedFactoryUrl);
        when(jsonHelper.request(eq(Factory.class),
                                anyString(),
                                anyString(),
                                isNull(),
                                eq(Pair.of("validate", false))))
                .thenThrow(new ServerException(""));

        provider.onEvent(event);

        verify(workspaceDao).update(workspace);
        verifySettingOfAttributes(runnerLifetime, runnerRam, builderExecutionTime, false);
    }

    @Test
    public void shouldSetCommonValuesIfExeptionIsThrownOnBuildNonEncodedFactory() throws ApiException, IOException {
        when(attributes.get("factoryUrl")).thenReturn(nonEncodedFactoryUrl);
        when(factoryBuilder.buildEncoded(any(URI.class))).thenThrow(new ApiException(""));

        provider.onEvent(event);

        verify(workspaceDao).update(workspace);
        verifySettingOfAttributes(runnerLifetime, runnerRam, builderExecutionTime, false);
    }

    @Test
    public void shouldSetTrackedValuesIfOnPremisesTrue() throws ApiException, IOException {
        provider = new FactoryWorkspaceResourceProvider(trackedRunnerLifetime,
                                                        trackedBuilderExecutionTime,
                                                        trackedRunnerRam,
                                                        runnerLifetime,
                                                        runnerRam,
                                                        builderExecutionTime,
                                                        apiEndpoint,
                                                        true,
                                                        workspaceDao,
                                                        accountDao,
                                                        new EventService(),
                                                        factoryBuilder);

        provider.onEvent(event);

        verify(workspaceDao).update(workspace);
        verifySettingOfAttributes(trackedRunnerLifetime, trackedRunnerRam, trackedBuilderExecutionTime, false);
        verifyZeroInteractions(factory, factoryBuilder, accountDao, author, jsonHelper);
    }

    private void verifySettingOfAttributes(String runnerLifetime, String runnerRam, String builderExecutionTime, boolean runnerInfra) {
        verify(attributes).put("codenvy:runner_lifetime", runnerLifetime);
        verify(attributes).put("codenvy:runner_ram", runnerRam);
        verify(attributes).put("codenvy:builder_execution_time", builderExecutionTime);
        if (runnerInfra) {
            verify(attributes).put("codenvy:runner_infra", "paid");
        } else {
            verify(attributes, never()).put(eq("codenvy:runner_infra"), anyString());
        }
    }
}