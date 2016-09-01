/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.ext.git.server.github;

import org.eclipse.che.api.auth.oauth.OAuthTokenProvider;
import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.api.git.exception.GitException;
import org.eclipse.che.api.git.shared.ProviderInfo;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.api.git.CredentialsProvider;
import org.eclipse.che.api.git.UserCredential;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author Sergii Kabashniuk
 */
@Singleton
public class GitLabOAuthCredentialProvider implements CredentialsProvider {

    private static final Logger LOG = LoggerFactory.getLogger(GitLabOAuthCredentialProvider.class);

    private static String OAUTH_PROVIDER_NAME = "gitlab.codenvy-stg.com";
    private final OAuthTokenProvider oAuthTokenProvider;
    private final String             authorizationServicePath;
    private final String[]             gitLabAvailableHosts;

    @Inject
    public GitLabOAuthCredentialProvider(OAuthTokenProvider oAuthTokenProvider) {
        this.oAuthTokenProvider = oAuthTokenProvider;
        this.authorizationServicePath = "/oauth/authenticate";
        this.gitLabAvailableHosts = new String[]{"gitlab.codenvy-stg.com"};
    }

    @Override
    public UserCredential getUserCredential() throws GitException {
        try {
            OAuthToken token = oAuthTokenProvider.getToken(gitLabAvailableHosts[0], EnvironmentContext.getCurrent().getSubject().getUserId());
            if (token != null) {
                return new UserCredential("oauth2", token.getToken(), OAUTH_PROVIDER_NAME);
            }
        } catch (IOException e) {
            LOG.warn(e.getLocalizedMessage());
        }
        return null;
    }

    @Override
    public String getId() {
        return OAUTH_PROVIDER_NAME;
    }

    @Override
    public boolean canProvideCredentials(String url) {
        for (String host : gitLabAvailableHosts) {
            if (url.contains(host)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ProviderInfo getProviderInfo() {
        return new ProviderInfo(OAUTH_PROVIDER_NAME, UriBuilder.fromPath(authorizationServicePath)
                                                               .queryParam("oauth_provider", OAUTH_PROVIDER_NAME)
                                                               .queryParam("userId", EnvironmentContext.getCurrent().getSubject().getUserId())
                                                               .queryParam("scope", "api")
                                                               .build()
                                                               .toString());
    }

}
