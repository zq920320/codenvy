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
package com.codenvy.workspace;

import com.codenvy.commons.env.EnvironmentContext;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.*;

import javax.servlet.*;

import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@Listeners(value = {MockitoTestNGListener.class})
public class LastAccessTimeFilterTest {
    @Mock
    private FilterChain filterChain;

    @Mock
    private WsActivitySender activitySender;

    @Mock
    private ServletRequest request;

    @Mock
    private ServletResponse response;

    @InjectMocks
    private LastAccessTimeFilter filter;

    @BeforeMethod
    public void setUp() throws Exception {
        EnvironmentContext.setCurrent(new EnvironmentContext());
    }

    @Test
    public void shouldCallActivitySenderIfWorkspaceIdIsNotNull() throws Exception {
        EnvironmentContext.getCurrent().setWorkspaceId("id");

        filter.doFilter(request, response, filterChain);

        verify(activitySender).onMessage(anyString(), anyBoolean());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void shouldNotCallActivitySenderIfNoWorkspaceIdIsSet() throws IOException, ServletException {
        filter.doFilter(request, response, filterChain);

        verifyZeroInteractions(activitySender);
        verify(filterChain).doFilter(request, response);
    }
}
