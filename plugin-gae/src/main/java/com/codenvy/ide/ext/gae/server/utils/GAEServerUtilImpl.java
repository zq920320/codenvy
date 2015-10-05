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
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.InvalidValueException;
import org.eclipse.che.api.project.server.ValueStorageException;
import org.eclipse.che.api.vfs.server.VirtualFile;
import org.eclipse.che.commons.xml.XMLTree;
import com.codenvy.ide.ext.gae.server.applications.yaml.YamlAppInfo;
import com.google.apphosting.utils.config.AppEngineConfigException;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.io.IOUtils;

import javax.validation.constraints.NotNull;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

/**
 * @author Valeriy Svydenko
 */
@Singleton
public class GAEServerUtilImpl implements GAEServerUtil {
    private static final Pattern PATTERN_YAML         = Pattern.compile("application:.*");
    private static final String  APPLICATION_ID_XPATH = "appengine-web-app/application";

    @Inject
    public GAEServerUtilImpl() {
    }

    /** {@inheritDoc} */
    @Override
    public void setApplicationIdToWebAppEngine(@NotNull VirtualFile file, @NotNull String applicationId) throws ApiException {
        try {
            XMLTree webXmlTree = XMLTree.from(file.getContent().getStream());
            webXmlTree.updateText(APPLICATION_ID_XPATH, applicationId);
            file.updateContent(new ByteArrayInputStream(webXmlTree.getBytes()), null);
        } catch (IOException e) {
            throw new ServerException(e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setApplicationIdToAppYaml(@NotNull VirtualFile file, @NotNull String applicationId) throws ApiException {
        try {
            String content = IOUtils.toString(file.getContent().getStream());
            String newContent = PATTERN_YAML.matcher(content).replaceFirst("application: " + applicationId);
            file.updateContent(new ByteArrayInputStream(newContent.getBytes()), null);
        } catch (IOException e) {
            throw new ServerException("Can't write a file: " + e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public String getApplicationIdFromAppYaml(@NotNull VirtualFile appYaml) throws ApiException {

        try (InputStream inputStream = appYaml.getContent().getStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            YamlAppInfo appInfo = YamlAppInfo.parse(reader);
            if (appInfo == null) {
                throw new InvalidValueException("app.yaml not valid or empty, check it");
            }
            String appId = appInfo.application;
            return appId == null ? "" : appId;
        } catch (IOException | AppEngineConfigException e) {
            throw new ServerException(e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public String getApplicationIdFromWebAppEngine(@NotNull VirtualFile webAppEngine) throws ApiException {
        try {
            String appId = XMLTree.from(webAppEngine.getContent().getStream()).getSingleText(APPLICATION_ID_XPATH);
            return appId == null ? "" : appId;
        } catch (IOException e) {
            throw new ServerException(e.getMessage(), e);
        }
    }
}