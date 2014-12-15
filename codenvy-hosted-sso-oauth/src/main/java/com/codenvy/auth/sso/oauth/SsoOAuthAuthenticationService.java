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

/**
 *  RESTful wrapper for OAuthAuthenticator.
 * @author Sergii Kabashniuk
 */

import com.codenvy.auth.sso.server.handler.BearerTokenAuthenticationHandler;
import com.codenvy.security.oauth.OAuthAuthenticationException;
import com.codenvy.security.oauth.OAuthAuthenticationService;
import com.codenvy.security.oauth.OAuthAuthenticator;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Path("oauth")
public class SsoOAuthAuthenticationService extends OAuthAuthenticationService {
    @Inject
    BearerTokenAuthenticationHandler authenticationHandler;

    @GET
    @Path("callback")
    public Response callback(@Context UriInfo uriInfo) throws OAuthAuthenticationException {
        URL requestUrl = getRequestUrl(uriInfo);
        Map<String, List<String>> params = getRequestParameters(getState(requestUrl));
        List<String> errorValues = uriInfo.getQueryParameters().get("error");
        if (errorValues != null && errorValues.contains("access_denied")) {
            return Response.temporaryRedirect(
                    uriInfo.getRequestUriBuilder().replacePath(errorPage).replaceQuery(null).build()).build();
        }
        final String providerName = getParameter(params, "oauth_provider");
        OAuthAuthenticator oauth = getAuthenticator(providerName);
        final List<String> scopes = params.get("scope");
        final String userId = oauth.callback(requestUrl, scopes == null ? Collections.<String>emptyList() : scopes);
        final String redirectAfterLogin = getParameter(params, "redirect_after_login");
        final String mode = getParameter(params, "mode");
        //federated_login left for backward compatibility
        if ("sso".equals(mode) || "federated_login".equals(mode)) {


            try {

                Map<String, String> payload = new HashMap<>(2);
                payload.put("provider", providerName);
                payload.put("username", userId);

                return Response.temporaryRedirect(
                        UriBuilder.fromUri(URLDecoder.decode(redirectAfterLogin, "UTF-8"))
                                  .queryParam("username", userId)
                                  .queryParam("oauthbearertoken", authenticationHandler.generateBearerToken(userId, payload))
                                  .build())
                               .build();
            } catch (UnsupportedEncodingException e) {
                throw new OAuthAuthenticationException(e.getLocalizedMessage(), e);
            }

        }
        return Response.temporaryRedirect(URI.create(redirectAfterLogin)).build();
    }

}
