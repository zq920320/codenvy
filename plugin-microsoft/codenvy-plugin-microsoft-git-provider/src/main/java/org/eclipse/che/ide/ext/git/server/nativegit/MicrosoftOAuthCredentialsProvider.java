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
package org.eclipse.che.ide.ext.git.server.nativegit;

import org.eclipse.che.api.auth.oauth.OAuthTokenProvider;
import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.api.git.GitException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.env.EnvironmentContext;

import org.eclipse.che.api.git.CredentialsProvider;
import org.eclipse.che.api.git.UserCredential;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Credentials provider for Microsoft
 *
 * @author Max Shaposhnik
 */
@Singleton
public class MicrosoftOAuthCredentialsProvider implements CredentialsProvider {

    private static final Logger LOG                 = LoggerFactory.getLogger(MicrosoftOAuthCredentialsProvider.class);
    private static final String OAUTH_PROVIDER_NAME = "microsoft";

    private final Pattern            microsoftUrlPattern;
    private final OAuthTokenProvider oAuthTokenProvider;

    @Inject
    public MicrosoftOAuthCredentialsProvider(OAuthTokenProvider oAuthTokenProvider,
                                             @Nullable @Named("oauth.microsoft.git.pattern") String gitPattern) {
        this.oAuthTokenProvider = oAuthTokenProvider;
        this.microsoftUrlPattern = Pattern.compile(gitPattern);
    }

    @Override
    public UserCredential getUserCredential() throws GitException {
        try {
            OAuthToken token = oAuthTokenProvider.getToken(OAUTH_PROVIDER_NAME, EnvironmentContext.getCurrent().getUser().getId());
            return new UserCredential(token.getToken(), token.getToken(), OAUTH_PROVIDER_NAME);
        } catch (IOException ioEx) {
            LOG.warn(ioEx.getLocalizedMessage());
        }
        return null;
    }

    @Override
    public String getId() {
        return OAUTH_PROVIDER_NAME;
    }

    @Override
    public boolean canProvideCredentials(String url) {
        return microsoftUrlPattern.matcher(url).matches();
    }
}
