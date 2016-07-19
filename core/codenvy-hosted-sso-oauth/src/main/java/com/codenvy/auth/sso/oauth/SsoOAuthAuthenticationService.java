/*
 *  [2012] - [2016] Codenvy, S.A.
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

import com.codenvy.auth.sso.server.BearerTokenManager;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.security.oauth.OAuthAuthenticationException;
import org.eclipse.che.security.oauth.OAuthAuthenticationService;
import org.eclipse.che.security.oauth.OAuthAuthenticator;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * RESTful wrapper for OAuthAuthenticator.
 *
 * @author Sergii Kabashniuk
 */
@Path("oauth")
public class SsoOAuthAuthenticationService extends OAuthAuthenticationService {
    @Inject
    BearerTokenManager authenticationHandler;

    @Inject
    private UserManager userManager;

    @Inject
    @Named("auth.sso.create_workspace_page_url")
    private String createWorkspacePage;

    @Inject
    @Named("auth.sso.login_page_url")
    private String loginPage;

    @Inject
    @Named("auth.no.account.found.page")
    private String noAccountFoundErrorPage;


    @Inject
    @Named("user.self.creation.allowed")
    private boolean userSelfCreationAllowed;

    @GET
    @Path("callback")
    @Override
    public Response callback(@QueryParam("errorValues") List<String> errorValues) throws OAuthAuthenticationException, BadRequestException {
        URL requestUrl = getRequestUrl(uriInfo);
        Map<String, List<String>> params = getRequestParameters(getState(requestUrl));
        if (errorValues != null && errorValues.contains("access_denied")) {
            return Response.temporaryRedirect(
                    uriInfo.getRequestUriBuilder().replacePath(errorPage).replaceQuery(null).build()).build();
        }
        final String providerName = getParameter(params, "oauth_provider");
        OAuthAuthenticator oauth = getAuthenticator(providerName);
        final List<String> scopes = params.get("scope");
        final String oauthUserId = oauth.callback(requestUrl, scopes == null ? Collections.<String>emptyList() : scopes);
//        String redirectUrl = getParameter(params, "redirect_url");
//        final String mode = getParameter(params, "mode");
        //federated_login left for backward compatibility
//        if ("sso".equals(mode) || "federated_login".equals(mode)) {
        Map<String, String> payload = new HashMap<>();
        payload.put("provider", providerName);
        payload.put("email", oauthUserId);

        try {
            userManager.getByEmail(oauthUserId);
            return Response.temporaryRedirect(UriBuilder.fromUri(loginPage).replaceQuery(requestUrl.getQuery())
                                                        .replaceQueryParam("signature")
                                                        .replaceQueryParam("oauth_provider")
                                                        .replaceQueryParam("bearertoken",
                                                                           authenticationHandler.generateBearerToken(payload))
                                                        .build())
                           .build();
        } catch (NotFoundException e) {
            if (!userSelfCreationAllowed) {
                return Response.temporaryRedirect(UriBuilder.fromUri(noAccountFoundErrorPage).build()).build();
            }


            return Response.temporaryRedirect(UriBuilder.fromUri(createWorkspacePage).replaceQuery(requestUrl.getQuery())
                                                        .replaceQueryParam("signature")
                                                        .replaceQueryParam("oauth_provider")
                                                        .replaceQueryParam("bearertoken",
                                                                           authenticationHandler.generateBearerToken(payload))
                                                        .build())
                           .build();
        } catch (ServerException se) {
            return Response.serverError().entity(se.getLocalizedMessage()).build();
        }
    }

    private String findAvailableUsername(String email) throws ServerException {
        String candidate = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        int count = 1;
        while (getUserByName(candidate).isPresent()) {
            candidate = candidate.concat(String.valueOf(count++));
        }
        return candidate;
    }

    private Optional<User> getUserByName(String name) throws ServerException {
        try {
            User user = userManager.getByName(name);
            return Optional.of(user);
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }
}
