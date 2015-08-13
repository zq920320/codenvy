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
package com.codenvy.ide.ext.gae.server.utils;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.vfs.server.VirtualFile;
import com.google.inject.ImplementedBy;

import javax.annotation.Nonnull;

/**
 * The utility class. It contains different methods which are needed in a few places in the extension.
 *
 * @author Valeriy Svydenko
 */
@ImplementedBy(GAEServerUtilImpl.class)
public interface GAEServerUtil {
    /**
     * Sets application id into appengine-web.xml.
     *
     * @param webAppEngine
     *         GAE configuration file
     * @param applicationId
     *         GAE application id
     * @throws ApiException
     *         if some problem happens with reading/writing operation
     */
    void setApplicationIdToWebAppEngine(@Nonnull VirtualFile webAppEngine, @Nonnull String applicationId) throws ApiException;

    /**
     * Sets application id into app.yaml.
     *
     * @param appYaml
     *         GAE configuration file for Python or PHP projects
     * @param applicationId
     *         GAE application id
     * @throws ApiException
     *         if some problem happens with reading/writing operation
     */
    void setApplicationIdToAppYaml(@Nonnull VirtualFile appYaml, @Nonnull String applicationId) throws ApiException;

    /**
     * Gets application id from app.yaml.
     *
     * @param appYaml
     *         GAE configuration file for Python or PHP projects
     * @return application id of project
     * @throws ApiException
     *         if some problem happens with reading/writing operation
     */
    @Nonnull
    String getApplicationIdFromAppYaml(@Nonnull VirtualFile appYaml) throws ApiException;

    /**
     * Gets application id from appengine-web.xml.
     *
     * @param webAppEngine
     *         GAE configuration file for Java projects
     * @return application id of project
     * @throws ApiException
     *         if some problem happens with reading/writing operation
     */
    @Nonnull
    String getApplicationIdFromWebAppEngine(@Nonnull VirtualFile webAppEngine) throws ApiException;
}
