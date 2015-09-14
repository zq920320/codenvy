/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.ext.gae.client.utils;

import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.ide.api.app.CurrentProject;
import com.google.inject.Singleton;

import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;

import static com.codenvy.ide.ext.gae.shared.GAEConstants.GAE_JAVA_ID;
import static com.codenvy.ide.ext.gae.shared.GAEConstants.GAE_PHP_ID;
import static com.codenvy.ide.ext.gae.shared.GAEConstants.GAE_PYTHON_ID;

/**
 * @author Evgen Vidolob
 * @author Andrey Plotnikov
 */
@Singleton
public class GAEUtilImpl implements GAEUtil {
    public static final  String APP_ENGINE_ADMIN_SCOPE    = "https://www.googleapis.com/auth/appengine.admin";
    private static final String APP_ENGINE_APP_ID_MATCHER = "^[a-z]{1}[a-z0-9\\-]*[a-z0-9]{1}$";

    /** {@inheritDoc} */
    @Override
    public boolean isAppEngineProject(@NotNull CurrentProject project) {
        String type = project.getProjectDescription().getType();
        return GAE_JAVA_ID.equals(type) || GAE_PYTHON_ID.equals(type) || GAE_PHP_ID.equals(type);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isAuthenticatedInAppEngine(@Nullable OAuthToken token) {
        if (token == null) {
            return false;
        }

        String stringToken = token.getToken();
        String tokenScope = token.getScope();

        boolean isEmptyToken = stringToken == null || stringToken.isEmpty() || tokenScope == null || tokenScope.isEmpty();

        return !isEmptyToken && tokenScope.contains(APP_ENGINE_ADMIN_SCOPE);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCorrectAppId(@NotNull String appId) {
        return appId.matches(APP_ENGINE_APP_ID_MATCHER);
    }

}