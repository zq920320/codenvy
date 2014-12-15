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
package com.codenvy.auth.sso.oauth;


import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.auth.shared.dto.OAuthToken;
import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.user.server.dao.UserDao;
import com.codenvy.api.workspace.server.dao.MemberDao;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.codenvy.auth.sso.server.InputDataValidator;
import com.codenvy.auth.sso.server.handler.BearerTokenAuthenticationHandler;
import com.codenvy.dto.server.DtoFactory;
import com.codenvy.security.oauth.OAuthAuthenticationException;
import com.codenvy.security.oauth.OAuthAuthenticator;
import com.codenvy.security.oauth.OAuthAuthenticatorProvider;

import org.codenvy.mail.MailSenderClient;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@Listeners(MockitoTestNGListener.class)
public class CreateUserWithCapturedProfileInfoTest {
    private static final String     USERNAME = "user@gmail.com";
    private static final OAuthToken TOKEN    =
            DtoFactory.getInstance().createDto(OAuthToken.class).withToken("1231243");
    @Mock
    private ServletContext                         servletContext;
    @Mock
    private OAuthAuthenticator                     authenticator;
    @Mock
    private UserDao                                userDao;
    @Mock
    private ServletConfig                          servletConfig;
    @Mock
    private MemberDao                              memberDao;
    @Mock
    private WorkspaceDao                           workspaceDao;
    @Mock
    private AccountDao                             accountDao;
    @Mock
    private MailSenderClient                       mailSenderClient;
    @Mock
    private OAuthAuthenticatorProvider             authenticatorProvider;
    @Mock
    private BearerTokenAuthenticationHandler       handler;
    @Mock
    private WorkspaceNameProposer                  wsNameProposer;
    @Mock
    private com.codenvy.security.oauth.shared.User googleUser;
    @Mock
    private InputDataValidator                     inputDataValidator;

    @InjectMocks
    private OAuthLoginServlet oAuthLoginServlet;

    @BeforeMethod
    public void setUp() throws Exception {
        // oAuthLoginServlet = new OAuthLoginServlet();
        System.setProperty("mailsender.application.server.url", "");
        when(servletConfig.getServletContext()).thenReturn(servletContext);

        //oAuthLoginServlet.init(servletConfig);
    }


    @Test
    public void shouldParseFirstAndLastNames() throws OAuthAuthenticationException, ApiException {
        when(googleUser.getName()).thenReturn("Mark Downey");
        when(userDao.getByAlias(USERNAME)).thenThrow(NotFoundException.class);
        when(authenticator.getUser(TOKEN)).thenReturn(googleUser);

        Map res = oAuthLoginServlet.createProfileInfo(USERNAME, authenticator, TOKEN);

        assertEquals(res.get("firstName"), "Mark");
        assertEquals(res.get("lastName"), "Downey");
    }


    @Test
    public void shouldParseFirstNames() throws OAuthAuthenticationException, ServletException, ApiException {

        when(googleUser.getName()).thenReturn("Mark");
        when(userDao.getByAlias(USERNAME)).thenThrow(NotFoundException.class);
        when(authenticator.getUser(TOKEN)).thenReturn(googleUser);

        Map res = oAuthLoginServlet.createProfileInfo(USERNAME, authenticator, TOKEN);

        assertEquals(res.get("firstName"), "Mark");
        assertNull(res.get("lastName"));
    }

    @Test
    public void shouldParseFirstNamesAndTrim() throws OAuthAuthenticationException, ServletException, ApiException {
        when(googleUser.getName()).thenReturn("  Mark   ");

        when(userDao.getByAlias(USERNAME)).thenThrow(NotFoundException.class);
        when(authenticator.getUser(TOKEN)).thenReturn(googleUser);

        Map res = oAuthLoginServlet.createProfileInfo(USERNAME, authenticator, TOKEN);

        assertEquals(res.get("firstName"), "Mark");
        assertNull(res.get("lastName"));
    }

    @Test
    public void shouldIgnoreOneSpaceName() throws OAuthAuthenticationException, ServletException, ApiException {
        when(googleUser.getName()).thenReturn(" ");

        when(userDao.getByAlias(USERNAME)).thenThrow(NotFoundException.class);
        when(authenticator.getUser(TOKEN)).thenReturn(googleUser);

        Map res = oAuthLoginServlet.createProfileInfo(USERNAME, authenticator, TOKEN);

        assertEquals(res.size(), 0);

    }

    @Test
    public void shouldTrimSpaces() throws OAuthAuthenticationException, ServletException, ApiException {
        when(googleUser.getName()).thenReturn("   Mark    Downey    ");

        when(userDao.getByAlias(USERNAME)).thenThrow(NotFoundException.class);
        when(authenticator.getUser(TOKEN)).thenReturn(googleUser);

        Map res = oAuthLoginServlet.createProfileInfo(USERNAME, authenticator, TOKEN);

        assertEquals(res.get("firstName"), "Mark");
        assertEquals(res.get("lastName"), "Downey");
    }


    @Test
    public void shouldParseFirstAndLastNamesWithFewWords() throws OAuthAuthenticationException,
                                                                  ServletException, ApiException {

        when(googleUser.getName()).thenReturn("Mark Tyler Downey Jewel");


        when(userDao.getByAlias(USERNAME)).thenThrow(NotFoundException.class);
        when(authenticator.getUser(TOKEN)).thenReturn(googleUser);

        Map res = oAuthLoginServlet.createProfileInfo(USERNAME, authenticator, TOKEN);

        assertEquals(res.get("firstName"), "Mark");
        assertEquals(res.get("lastName"), "Tyler Downey Jewel");
    }

}