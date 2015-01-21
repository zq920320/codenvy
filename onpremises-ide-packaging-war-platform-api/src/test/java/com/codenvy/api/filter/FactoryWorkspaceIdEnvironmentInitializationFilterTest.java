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
package com.codenvy.api.filter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.stubVoid;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.workspace.shared.dto.WorkspaceDescriptor;
import com.codenvy.auth.sso.client.filter.RequestFilter;
import com.codenvy.commons.env.EnvironmentContext;
import com.codenvy.service.http.WorkspaceInfoCache;

import org.everrest.test.mock.MockHttpServletRequest;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Listeners(value = MockitoTestNGListener.class)
public class FactoryWorkspaceIdEnvironmentInitializationFilterTest {
    @Mock
    WorkspaceInfoCache  cache;
    @Mock
    WorkspaceDescriptor descriptor;

    @Mock
    FilterChain     chain;
    @Mock
    ServletResponse response;


    @InjectMocks
    FactoryWorkspaceIdEnvironmentInitializationFilter filter;

    @AfterMethod
    public void clearContext() {
        EnvironmentContext.reset();
    }


    @Test
    public void testEnv() throws ServerException, NotFoundException, IOException, ServletException {
        //given
        when(cache.getById(eq("id-23423"))).thenReturn(descriptor);
        when(descriptor.getId()).thenReturn("wsid-123123");

       Mockito.doAnswer(new Answer() {
           @Override
           public Object answer(InvocationOnMock invocation) throws Throwable {
               Assert.assertEquals(EnvironmentContext.getCurrent().getWorkspaceId(), "wsid-123123");
               return null;
           }
       }).when(chain).doFilter(any(ServletRequest.class), any(ServletResponse.class));

        ServletRequest request =
                new MockHttpServletRequest("http://localhost:8080/api/factory/id-23423/spring", null, 0, "GET", null);
        //when

        filter.doFilter(request, response, chain);
        verify(chain).doFilter(eq(request), eq(response));


    }


    @Test(dataProvider = "skip")
    public void testShouldSkip(String requestUri, String method) throws Exception {
        //given
        HttpServletRequest request =
                new MockHttpServletRequest("http://localhost:8080" + requestUri, null, 0, method, null);

        RequestFilter filter = FactoryWorkspaceIdEnvironmentInitializationFilter.REQUEST_FILTER;
        //when
        boolean result = filter.shouldSkip(request);
        //then
        Assert.assertTrue(result);

    }


    @DataProvider(name = "skip")
    public Object[][] skip() {
        return new Object[][]{{"/api/factory", "POST"},
                              {"/api/factory/ojo2934kpoak/image", "GET"},
                              {"/api/factory/ojo2934kpoa/snippet", "GET"},
                              {"/api/factory/asdflas", "GET"},
                              {"/api/factory/find", "GET"}
        };
    }
}