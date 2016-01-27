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
package com.codenvy.api.resources.server;

import com.codenvy.api.metrics.server.dao.MeterBasedStorage;
import com.codenvy.api.metrics.server.period.MetricPeriod;
import com.codenvy.api.metrics.server.period.Period;
import com.codenvy.api.resources.shared.dto.UpdateResourcesDescriptor;
import com.codenvy.api.resources.shared.dto.WorkspaceResources;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.jayway.restassured.response.Response;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.api.workspace.server.dao.Workspace;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.user.UserImpl;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link ResourcesService}
 *
 * @author Sergii Leschenko
 */
@Listeners({EverrestJetty.class, MockitoTestNGListener.class})
public class ResourcesServiceTest {
    private final static String ACCOUNT_ID = "account";

    @SuppressWarnings("unused")
    private final ApiExceptionMapper exceptionMapper = new ApiExceptionMapper();
    @SuppressWarnings("unused")
    private final EnvironmentFilter  filter          = new EnvironmentFilter();

    @Mock
    Period period;

    @Mock
    MeterBasedStorage meterBasedStorage;
    @Mock
    WorkspaceDao      workspaceDao;
    @Mock
    MetricPeriod      metricPeriod;
    @Mock
    ResourcesManager  resourcesManager;

    @InjectMocks
    private ResourcesService resourcesService;

    @Filter
    public static class EnvironmentFilter implements RequestFilter {
        public void doFilter(GenericContainerRequest request) {
            EnvironmentContext context = EnvironmentContext.getCurrent();
            context.setUser(new UserImpl(ADMIN_USER_NAME, "id-2314", "token-2323",
                                         Collections.<String>emptyList(), false));
        }
    }

    @BeforeMethod
    public void setUp() {
        when(metricPeriod.getCurrent()).thenReturn(period);
    }

    @Test
    public void shouldBeAbleToRedistributeResources() throws Exception {
        Response response = given().auth()
                                   .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                   .contentType("application/json")
                                   .when()
                                   .body(newDto(UpdateResourcesDescriptor.class).withWorkspaceId("some_workspace"))
                                   .post(SECURE_PATH + "/resources/" + ACCOUNT_ID);

        assertEquals(response.getStatusCode(), 204);
        verify(resourcesManager).redistributeResources(eq("account"), anyListOf(UpdateResourcesDescriptor.class));
    }

    @Test
    public void shouldThrowExceptionWhenResourcesManagerThrowsIt() throws Exception {
        doThrow(new ServerException("Error")).when(resourcesManager)
                                             .redistributeResources(anyString(), anyListOf(UpdateResourcesDescriptor.class));

        Response response = given().auth()
                                   .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                   .contentType("application/json")
                                   .when()
                                   .body(newDto(UpdateResourcesDescriptor.class).withWorkspaceId("some_workspace"))
                                   .post(SECURE_PATH + "/resources/" + ACCOUNT_ID);

        assertEquals(response.getStatusCode(), 500);
        assertEquals(unwrapDto(response, ServiceError.class).getMessage(), "Error");
        verify(resourcesManager).redistributeResources(eq("account"), anyListOf(UpdateResourcesDescriptor.class));
    }

    @Test
    public void shouldBeAbleToGetAccountResources() throws Exception {
        when(period.getStartDate()).thenReturn(new Date());
        when(workspaceDao.getByAccount(ACCOUNT_ID)).thenReturn(Collections.singletonList(new Workspace().withId("ws_id")
                                                                                                        .withAccountId(ACCOUNT_ID)));

        when(meterBasedStorage.getMemoryUsedReport(eq(ACCOUNT_ID), anyLong(), anyLong())).thenReturn(new HashMap<String, Double>());

        Response response = given().auth()
                                   .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                   .when()
                                   .get(SECURE_PATH + "/resources/" + ACCOUNT_ID + "/used");

        assertEquals(response.getStatusCode(), 200);

        List<WorkspaceResources> result = unwrapDtoList(response, WorkspaceResources.class);

        assertEquals(result.size(), 1);
    }

    @Test
    public void shouldReturnUsedResourcesByWorkspaceWhichDoesNotUseResourcesOnGetUsedAccountResources() throws Exception {
        when(period.getStartDate()).thenReturn(new Date());
        when(workspaceDao.getByAccount("account")).thenReturn(Collections.singletonList(new Workspace().withId("workspaceID")
                                                                                                       .withAccountId(ACCOUNT_ID)));

        Response response = given().auth()
                                   .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                   .when()
                                   .get(SECURE_PATH + "/resources/" + ACCOUNT_ID + "/used");

        assertEquals(response.getStatusCode(), 200);

        List<WorkspaceResources> result = unwrapDtoList(response, WorkspaceResources.class);
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getWorkspaceId(), "workspaceID");
        assertEquals(result.get(0).getMemory(), 0D);
    }

    @Test
    public void shouldReturnUsedResourcesByWorkspaceWhichNotReturnsByWorkspaceDaoOnGetUsedAccountResources() throws Exception {
        when(period.getStartDate()).thenReturn(new Date());
        when(meterBasedStorage.getMemoryUsedReport(eq(ACCOUNT_ID), anyLong(), anyLong())).thenReturn(ImmutableMap.of("ws_id", 123D));

        Response response = given().auth()
                                   .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                   .when()
                                   .get(SECURE_PATH + "/resources/" + ACCOUNT_ID + "/used");

        assertEquals(response.getStatusCode(), 200);

        List<WorkspaceResources> result = unwrapDtoList(response, WorkspaceResources.class);
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getWorkspaceId(), "ws_id");
        assertEquals(result.get(0).getMemory(), 123D);
    }

    @Test
    public void shouldReturnUsedResourcesByWorkspacesOnGetUsedAccountResources() throws Exception {
        when(period.getStartDate()).thenReturn(new Date());
        when(workspaceDao.getByAccount(ACCOUNT_ID)).thenReturn(Collections.singletonList(new Workspace().withId("ws_id")
                                                                                                        .withAccountId(ACCOUNT_ID)));
        when(meterBasedStorage.getMemoryUsedReport(eq(ACCOUNT_ID), anyLong(), anyLong())).thenReturn(ImmutableMap.of("ws_id", 123D));

        Response response = given().auth()
                                   .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                   .when()
                                   .get(SECURE_PATH + "/resources/" + ACCOUNT_ID + "/used");

        assertEquals(response.getStatusCode(), 200);

        List<WorkspaceResources> result = unwrapDtoList(response, WorkspaceResources.class);
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getWorkspaceId(), "ws_id");

    }

    private static <T> T newDto(Class<T> clazz) {
        return DtoFactory.getInstance().createDto(clazz);
    }

    private static <T> T unwrapDto(com.jayway.restassured.response.Response response, Class<T> dtoClass) {
        return DtoFactory.getInstance().createDtoFromJson(response.body().print(), dtoClass);
    }

    private static <T> List<T> unwrapDtoList(com.jayway.restassured.response.Response response, Class<T> dtoClass) {
        return FluentIterable.from(DtoFactory.getInstance().createListDtoFromJson(response.body().print(), dtoClass)).toList();
    }
}
