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
package com.codenvy.factory.commons;

import com.codenvy.organization.client.AccountManager;
import com.codenvy.organization.client.UserManager;
import com.codenvy.organization.client.WorkspaceManager;
import com.codenvy.organization.model.Account;
import com.codenvy.organization.model.ItemReference;
import com.codenvy.organization.model.User;
import com.codenvy.organization.model.Workspace;

import org.everrest.assured.EverrestJetty;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/** Test of FactoryServlet functionality. */

@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class FactoryServletTest {

    @Mock
    private UserManager userManager;

    @Mock
    private AccountManager accountManager;

    @Mock
    private WorkspaceManager workspaceManager;

    @Mock
    private HttpServletRequest req;

    @Mock
    private HttpServletResponse res;

    @Mock
    private User user;

    @Mock
    private Account acc;

    @Mock
    private Workspace ws;

    @Mock
    private Principal principal;

    private FactoryServlet servlet;

    private static final ItemReference REF = new ItemReference("1234adfDF");

    private String username = "user1@test.com";

    private String f_URL = "http://localhost:8080/factory?v=1&wname=ws1&pname=proj1";

    @BeforeMethod
    public void setup() throws Exception {
        System.setProperty("organization.application.server.url", "http://localhost:8080");
        this.servlet = new FactoryServlet(userManager, accountManager, workspaceManager);
    }

    @Test
    public void shouldRedirectToExistingWorkspace() throws Exception {

        Set<ItemReference> set = Collections.singleton(REF);
        Map map = Collections.singletonMap("factoryURL", f_URL);

        when(user.getAccounts()).thenReturn(set);
        when(acc.getWorkspaces()).thenReturn(set);

        when(userManager.getUserByAlias(anyString())).thenReturn(user);
        when(accountManager.getAccountById(anyString())).thenReturn(acc);
        when(workspaceManager.getWorkspaceById(anyString())).thenReturn(ws);

        when(ws.getAttributes()).thenReturn(map);
        when(ws.getName()).thenReturn("ws1");
        when(req.getRequestURL()).thenReturn(new StringBuffer(f_URL.split("\\?")[0]));
        when(req.getQueryString()).thenReturn(f_URL.split("\\?")[1]);
        when(req.getUserPrincipal()).thenReturn(principal);
        when(req.getParameter("pname")).thenReturn("proj1");


        when(principal.getName()).thenReturn(username);

        //main call
        servlet.doGet(req, res);

        verify(res, times(1)).sendRedirect("/ide/ws1/proj1");
    }

    @Test
    public void shouldFailIfNoUserPrincipal() throws Exception {
        when(req.getRequestURL()).thenReturn(new StringBuffer(f_URL));
        when(req.getUserPrincipal()).thenReturn(null);

        //main call
        servlet.doGet(req, res);

        verify(res, times(1)).sendRedirect("/error" + anyString());
    }

}
