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
package com.codenvy.auth.sso.client;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

import org.everrest.test.mock.MockHttpServletRequest;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Listeners(value = MockitoTestNGListener.class)
public class ResetRolesServletTest {

    @Mock
    HttpServletResponse response;
    @Mock
    SessionStore        sessionStore;

    @Mock
    SsoClientPrincipal principal;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    HttpSession session;

    @InjectMocks
    ResetRolesServlet servlet;

    @Test
    public void shouldFailIfTokenIsNotSet() throws ServletException, IOException {
        //given
        HttpServletRequest request =
                new MockHttpServletRequest("http://localhost:8080/sso/logout", null, 0, "POST", null);
        //when
        servlet.doPost(request, response);
        //when                                    ,
        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), eq("Token is not set"));

    }

    @Test
    public void shouldInvalidateRoles() throws ServletException, IOException {
        //given
        //given
        MockHttpServletRequest request =
                new MockHttpServletRequest("http://localhost:8080/sso/logout", null, 0, "POST", null);
        request.setParameter("authToken", "t-12312344");
        request.setParameter("workspaceId", "ws-4556767");
        request.setParameter("accountId", "a1-232");
        when(session.getAttribute(eq("principal"))).thenReturn(principal);
        when(sessionStore.removeSessionByToken(eq("t-12312344"))).thenReturn(session);
        //when
        servlet.doPost(request, response);
        //when
        verify(sessionStore).removeSessionByToken("t-12312344");
        ArgumentCaptor<RolesContext> captor = ArgumentCaptor.forClass(RolesContext.class);
        verify(principal).invialidateRoles(captor.capture());
        verifyZeroInteractions(response);
        Assert.assertEquals(captor.getValue().getAccountId(), "a1-232");
        Assert.assertEquals(captor.getValue().getWorkspaceId(), "ws-4556767");
    }

}