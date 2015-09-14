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
import com.google.inject.ImplementedBy;

import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * The utility class for GAE extension. It contains different methods which are needed in a few places in the extension.
 *
 * @author Evgen Vidolob
 * @author Andrey Plotnikov
 */
@ImplementedBy(GAEUtilImpl.class)
public interface GAEUtil {

    /**
     * Returns whether a given project is GAE project.
     * It analyzes a project type of a given project and try to find a project type which is specified for GAE.
     *
     * @param project
     *         project that needs to be analyzed
     * @return <code>true</code> if a given project is GAE project, and <code>false</code> otherwise
     */
    boolean isAppEngineProject(@NotNull CurrentProject project);

    /**
     * Returns whether a user is authorized in GAE. It analyzes a given token and try to find a credential for GAE.
     *
     * @param token
     *         token that needs to be analyzed
     * @return <code>true</code> if a user is authorized in GAE, and <code>false</code> otherwise
     */
    boolean isAuthenticatedInAppEngine(@Nullable OAuthToken token);

    /**
     * Returns whether an application ID is valid.
     * It analyzes an application ID which is used for the deploying process.
     *
     * @param appId
     *         application ID that needs to be validated
     * @return <code>true</code> if a given application ID is valid, and <code>false</code> otherwise
     */
    boolean isCorrectAppId(@NotNull String appId);
}