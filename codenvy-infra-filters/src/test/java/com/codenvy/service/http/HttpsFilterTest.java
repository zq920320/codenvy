/*
 * CODENVY CONFIDENTIAL
 * __________________
 * 
 *  [2012] - [2013] Codenvy, S.A. 
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
package com.codenvy.service.http;

import org.everrest.core.impl.RuntimeDelegateImpl;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ext.RuntimeDelegate;
import java.io.IOException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Listeners(value = {MockitoTestNGListener.class})
public class HttpsFilterTest {
    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private HttpsFilter filter;

    @BeforeMethod
    public void setUp() throws Exception {
        RuntimeDelegate.setInstance(new RuntimeDelegateImpl());

        System.setProperty("tenant.masterhost.protocol", "https");
        filter = new HttpsFilter();
    }

    @Test
    public void shouldMovePermanentlyToHttps() throws IOException, ServletException {
        String url = "http://server.com:8080/ide/myworkspace";
        String query = "param1=value&param2=value1&param2=value2";

        when(request.isSecure()).thenReturn(false);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURL()).thenReturn(new StringBuffer(url));
        when(request.getQueryString()).thenReturn(query);

        filter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        verify(response).setHeader("Location", new StringBuilder("https").append(url.substring(4)).append("?").append(query).toString());
    }
}
