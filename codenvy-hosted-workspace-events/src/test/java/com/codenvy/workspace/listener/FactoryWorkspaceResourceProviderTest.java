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
package com.codenvy.workspace.listener;

import com.codenvy.workspace.event.CreateWorkspaceEvent;

import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.rest.HttpJsonHelper;
import org.eclipse.che.api.factory.FactoryBuilder;
import org.eclipse.che.api.factory.dto.Author;
import org.eclipse.che.api.factory.dto.Factory;
import org.eclipse.che.api.workspace.server.dao.Workspace;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;
import org.eclipse.che.commons.lang.Pair;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.Map;

import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@Listeners(value = {MockitoTestNGListener.class})
public class FactoryWorkspaceResourceProviderTest {
    private static final String WS_ID      = "wsid";
    private static final String ACCOUNT_ID = "accId";

    private String runnerLifetime       = "runnerLifetime";
    private String runnerRam            = "runnerRam";
    private String builderExecutionTime = "builderExecutionTime";

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
    private CreateWorkspaceEvent event;

    private FactoryWorkspaceResourceProvider provider;

    @BeforeMethod
    public void setUp() throws Exception {
        encodedFactoryUrl = URLEncoder.encode("http://dev.box.com/factory?id=factory123456", "UTF-8");
        event = new CreateWorkspaceEvent(new Workspace().withId(WS_ID)
                                                        .withTemporary(true));
        provider = new FactoryWorkspaceResourceProvider(runnerLifetime,
                                                        runnerRam,
                                                        builderExecutionTime,
                                                        workspaceDao,
                                                        new EventService());
        Field field = HttpJsonHelper.class.getDeclaredField("httpJsonHelperImpl");
        field.setAccessible(true);
        field.set(null, jsonHelper);

        when(factory.getV()).thenReturn("2.0");
        when(factory.getCreator()).thenReturn(author);
        when(author.getAccountId()).thenReturn(ACCOUNT_ID);
        when(workspaceDao.getById(WS_ID)).thenReturn(workspace);
        when(workspace.getAttributes()).thenReturn(attributes);
        when(workspace.withAttributes(anyMapOf(String.class, String.class))).thenReturn(workspace);
    }

    @Test
    public void shouldDoNothingOnCreateNonTemporaryWs() {
        provider.onEvent(new CreateWorkspaceEvent(workspace));

        verifyZeroInteractions(workspaceDao);
    }

    @Test
    public void shouldSetCommonAttributesOnCreateTemporaryWs()
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

    @Test(dataProvider = "attributesProvider")
    public void shouldNotSetCommonValueIfValueIsNullOrEmpty(String runnerLifetime, String runnerRam, String builderExecutionTime)
            throws NotFoundException, ServerException, ConflictException {
        provider = new FactoryWorkspaceResourceProvider(runnerLifetime,
                                                        runnerRam,
                                                        builderExecutionTime,
                                                        workspaceDao,
                                                        new EventService());


        provider.onEvent(event);

        verify(workspaceDao).update(workspace);
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