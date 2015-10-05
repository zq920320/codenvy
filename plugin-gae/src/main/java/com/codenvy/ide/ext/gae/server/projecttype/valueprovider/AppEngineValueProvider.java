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
package com.codenvy.ide.ext.gae.server.projecttype.valueprovider;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.Project;
import org.eclipse.che.api.project.server.ValueProvider;
import org.eclipse.che.api.project.server.ValueStorageException;
import org.eclipse.che.api.vfs.server.VirtualFile;
import com.codenvy.ide.ext.gae.server.utils.GAEServerUtil;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

import static com.codenvy.ide.ext.gae.shared.GAEConstants.APP_ENGINE_WEB_XML_PATH;

/**
 * Class contains business logic which allows get and set values of application id needed to deploy application on server.
 *
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
public class AppEngineValueProvider implements ValueProvider {
    private static final String PATH_TO_APP_YAML = "app.yaml";

    private final FolderEntry   project;
    private final GAEServerUtil gaeUtil;

    @Inject
    public AppEngineValueProvider(GAEServerUtil gaeUtil, @Assisted FolderEntry project) {
        this.project = project;
        this.gaeUtil = gaeUtil;
    }

    /** {@inheritDoc} */
    @Override
    @NotNull
    public List<String> getValues(String attributeName) throws ValueStorageException {
        try {
            VirtualFile appEngineWebXml = getConfigurationFile(APP_ENGINE_WEB_XML_PATH);

            if (appEngineWebXml != null) {
                return Arrays.asList(gaeUtil.getApplicationIdFromWebAppEngine(appEngineWebXml));
            }

            VirtualFile yaml = getConfigurationFile(PATH_TO_APP_YAML);
            String appId = "";

            if (yaml != null) {
                appId = gaeUtil.getApplicationIdFromAppYaml(yaml);
            }
            return Arrays.asList(appId);

        } catch (ApiException e) {
            throw new ValueStorageException("Can't read configuration file: " + e.getMessage());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setValues(@NotNull String attributeName, @NotNull List<String> value) throws ValueStorageException {
        if (value.isEmpty()) {
            return;
        }

        if (value.size() > 1) {
            throw new ValueStorageException("Application id must be only one value.");
        }

        String appId = value.get(0);

        if (appId == null) {
            return;
        }

        applyApplicationId(appId);
    }

    private void applyApplicationId(@NotNull String appId) throws ValueStorageException {
        try {
            VirtualFile appEngineWeb = getConfigurationFile(APP_ENGINE_WEB_XML_PATH);
            if (appEngineWeb != null) {
                gaeUtil.setApplicationIdToWebAppEngine(appEngineWeb, appId);
                return;
            }

            VirtualFile appYaml = getConfigurationFile(PATH_TO_APP_YAML);
            if (appYaml != null) {
                gaeUtil.setApplicationIdToAppYaml(appYaml, appId);
                //TODO need this check, because provider calls earlier then generator, and appengine-web.xml can be null.
                //TODO there is special issue which describes this bug {@link https://jira.codenvycorp.com/browse/IDEX-1733}
            }

        } catch (ApiException e) {
            throw new ValueStorageException("Can't read configuration file: " + e.getMessage());
        }
    }

    @Nullable
    private VirtualFile getConfigurationFile(@NotNull String path) throws ApiException {
        VirtualFile projectFolder = project.getVirtualFile();
        return projectFolder.getChild(path);
    }
}