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
package com.codenvy.ide.ext.gae.server.generators;


import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.handlers.CreateProjectHandler;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.api.vfs.server.VirtualFile;
import com.codenvy.ide.ext.gae.server.utils.GAEServerUtil;

import org.apache.commons.io.IOUtils;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Map;

import static com.codenvy.ide.ext.gae.shared.GAEConstants.APPLICATION_ID;

/**
 * Class contains general business logic of generators projects which use app.yaml file in order to deploy application to GAE.
 *
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
public abstract class GeneralYamlProjectGenerator implements CreateProjectHandler {
    protected static final String YAML_NAME = "app.yaml";

    private final String        projectId;
    private final GAEServerUtil gaeUtil;

    public GeneralYamlProjectGenerator(@NotNull String projectId, @NotNull GAEServerUtil gaeUtil) {
        this.projectId = projectId;
        this.gaeUtil = gaeUtil;
    }

    /** {@inheritDoc} */
    @Override
    public String getProjectType() {
        return projectId;
    }

    /**
     * Method contains business logic which allows set application id to app.yaml file.Method can throw
     * ServerException and ForbiddenException.
     *
     * @param baseFolder
     *         folder which contains app.yaml file
     * @param attributes
     *         project attributes
     * @throws ServerException
     * @throws ForbiddenException
     */
    protected void applyYamlApplicationId(@NotNull FolderEntry baseFolder,
                                          @NotNull Map<String, AttributeValue> attributes) throws ApiException {
        AttributeValue applicationId = attributes.get(APPLICATION_ID);

        if (applicationId == null) {
            return;
        }

        VirtualFile yaml = baseFolder.getChild(YAML_NAME).getVirtualFile();
        gaeUtil.setApplicationIdToAppYaml(yaml, applicationId.getList().get(0));
    }

    /**
     * Method allows create file in special base folder with special name,content and mime type.Method can throw ServerException.
     *
     * @param baseFolder
     *         folder in which need create file
     * @param fileName
     *         name of file which are creating
     * @param pathToContent
     *         path to content which needs add in file
     * @param type
     *         type of created file
     * @throws ServerException
     */
    protected void createFile(@NotNull FolderEntry baseFolder,
                              @NotNull String fileName,
                              @NotNull String pathToContent,
                              @NotNull String type) throws ServerException {
        try {
            baseFolder.createFile(fileName, IOUtils.toByteArray(getClass().getResourceAsStream(pathToContent)), type);
        } catch (ApiException | IOException e) {
            throw new ServerException("Can't write a file: " + e.getMessage(), e);
        }
    }

}