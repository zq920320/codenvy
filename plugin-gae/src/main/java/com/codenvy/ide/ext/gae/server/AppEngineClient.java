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
package com.codenvy.ide.ext.gae.server;

import org.eclipse.che.api.auth.oauth.OAuthTokenProvider;
import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.project.server.Project;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.VirtualFileEntry;
import org.eclipse.che.api.vfs.server.ContentStream;
import org.eclipse.che.api.vfs.server.VirtualFileSystemImpl;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.dto.server.DtoFactory;
import com.codenvy.ide.ext.gae.server.applications.yaml.YamlAppInfo;
import com.codenvy.ide.ext.gae.server.applications.yaml.YamlApplication;
import com.codenvy.ide.ext.gae.server.inject.factories.ApplicationFactory;
import com.codenvy.ide.ext.gae.server.inject.factories.IdeAppAdminFactory;
import com.codenvy.ide.ext.gae.shared.ApplicationInfo;
import com.google.appengine.tools.admin.AppAdminFactory.ConnectOptions;
import com.google.appengine.tools.admin.GenericApplication;
import com.google.appengine.tools.admin.IdeAppAdmin;
import com.google.appengine.tools.admin.UpdateFailureEvent;
import com.google.appengine.tools.admin.UpdateListener;
import com.google.appengine.tools.admin.UpdateProgressEvent;
import com.google.appengine.tools.admin.UpdateSuccessEvent;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.eclipse.che.commons.lang.ZipUtils.unzip;


/**
 * This class provides an ability to work with appengine-tool-sdk. This sdk helps us to execute different operation with GAE SDK.
 *
 * @author Andrey Parfonov
 * @author Andrey Plotnikov
 * @author Dmitry Shnurenko
 */
public class AppEngineClient {
    public static final  UpdateListener DUMMY_UPDATE_LISTENER = new DummyUpdateListener();
    private static final Logger         LOG                   = LoggerFactory.getLogger(AppEngineClient.class);

    private final OAuthTokenProvider oauthTokenProvider;
    private final ProjectManager     projectManager;
    private final ApplicationFactory applicationFactory;
    private final IdeAppAdminFactory appAdminFactory;

    @Inject
    public AppEngineClient(IdeAppAdminFactory appAdminFactory,
                           OAuthTokenProvider oauthTokenProvider,
                           ProjectManager projectManager,
                           ApplicationFactory applicationFactory) {

        this.oauthTokenProvider = oauthTokenProvider;
        this.appAdminFactory = appAdminFactory;
        this.projectManager = projectManager;
        this.applicationFactory = applicationFactory;
    }

    /**
     * Deploy a project to GAE with using gae-tools-sdk.
     *
     * @param wsId
     *         workspace id where project is located
     * @param projectPath
     *         path to project that needs to be deployed
     * @param binaries
     *         url where binaries of deployed project is located
     * @param userId
     *         user id that want to deploy project to GAE
     * @return information about deployed project
     * @throws IOException
     *         if some problem happens with reading/writing operation
     * @throws ApiException
     *         if some problem happens with virtual file system
     */
    @Nonnull
    public ApplicationInfo update(@Nonnull String wsId, @Nonnull String projectPath, @Nullable URL binaries, @Nonnull String userId)
            throws IOException, ApiException {
        IdeAppAdmin admin;

        if (binaries != null) {
            // If binaries provided use it. In this case Java project expected.
            admin = createApplicationAdmin(applicationFactory.createJavaApplication(binaries), userId);
        } else {
            admin = createApplicationAdmin(createYamlApplication(wsId, projectPath), userId);
        }

        try {
            admin.update(DUMMY_UPDATE_LISTENER);

            final String id = admin.getApplication().getAppId();

            return DtoFactory.getInstance()
                             .createDto(ApplicationInfo.class)
                             .withApplicationId(id)
                             .withWebURL("http://" + id + ".appspot.com");
        } finally {
            admin.getApplication().cleanStagingDirectory();

            Project project = projectManager.getProject(wsId, projectPath);

            LOG.info("EVENT#application-created# WS#" + EnvironmentContext.getCurrent().getWorkspaceName() +
                     "# USER#" + userId +
                     "# PROJECT#" + project.getName() +
                     "# TYPE#" + project.getConfig().getTypeId() +
                     "# PAAS#GAE#");
        }
    }

    @Nonnull
    private IdeAppAdmin createApplicationAdmin(@Nonnull GenericApplication application, @Nonnull String userId) throws IOException {
        ConnectOptions options = new ConnectOptions();

        OAuthToken token = oauthTokenProvider.getToken("google", userId);
        String oAuthToken = token != null ? token.getToken() : null;

        if (oAuthToken != null) {
            options.setOauthToken(oAuthToken);
        }

        return appAdminFactory.createIdeAppAdmin(options, application);
    }

    @Nonnull
    private YamlApplication createYamlApplication(@Nonnull String wsId, @Nonnull String projectPath) throws IOException,
                                                                                                            ApiException {
        Project project = projectManager.getProject(wsId, projectPath);
        ProjectType type = getApplicationType(project);

        boolean isPythonApp = ProjectType.PYTHON.equals(type);

        Path appDirPath = Files.createTempDirectory("ide-appengine");
        File appDir = appDirPath.toFile();
        unzip(VirtualFileSystemImpl.exportZip(project.getBaseFolder().getVirtualFile()).getStream(), appDir);

        return isPythonApp ? applicationFactory.createPythonApplication(appDir) : applicationFactory.createPHPApplication(appDir);
    }

    @Nonnull
    private ProjectType getApplicationType(@Nonnull Project project) throws IOException, ApiException {
        VirtualFileEntry app = project.getBaseFolder().getChild("app.yaml");
        if (app == null) {
            throw new RuntimeException("Unable determine type of application.");
        }

        ContentStream appYaml = app.getVirtualFile().getContent();
        try (InputStream in = appYaml.getStream();
             BufferedReader r = new BufferedReader(new InputStreamReader(in))) {

            YamlAppInfo appInfo = YamlAppInfo.parse(r);

            switch (appInfo.runtime) {
                case "python":
                case "python27":
                    return ProjectType.PYTHON;

                case "php":
                case "php55":
                    return ProjectType.PHP;

                default:
                    throw new RuntimeException("Unable determine type of application.");
            }
        }
    }

    private enum ProjectType {
        PYTHON, PHP
    }

    public static class DummyUpdateListener implements UpdateListener {
        /** {@inheritDoc } */
        @Override
        public void onSuccess(UpdateSuccessEvent event) {
            // do nothing
        }

        /** {@inheritDoc } */
        @Override
        public void onProgress(UpdateProgressEvent event) {
            // do nothing
        }

        /** {@inheritDoc } */
        @Override
        public void onFailure(UpdateFailureEvent event) {
            // do nothing
        }
    }

}