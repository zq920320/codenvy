/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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
package com.codenvy.ide.clone.client.persist;

import org.eclipse.che.api.analytics.client.logger.AnalyticsEventLogger;
import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.project.shared.dto.ProjectReference;
import org.eclipse.che.ide.api.event.OpenProjectEvent;
import org.eclipse.che.ide.api.event.RefreshProjectTreeEvent;
import org.eclipse.che.ide.api.notification.Notification;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.collections.Array;
import org.eclipse.che.ide.commons.exception.ServerException;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.util.Config;
import org.eclipse.che.ide.util.loging.Log;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Performs copy to Named Workspace functionality.
 *
 * @author Vitaliy Guliy
 */
@Singleton
public class PersistProjectHandler {

    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
    private final NotificationManager    notificationManager;
    private final EventBus               eventBus;
    private final ProjectServiceClient   projectServiceClient;
    private final AnalyticsEventLogger   eventLogger;

    private String srcWorkspaceId;

    private Array<ProjectReference> srcWorkspaceProjects;

    private boolean openProjectAfterCloning;
    private String  projectNameToOpen;

    /**
     * This is default constructor.
     */
    @Inject
    public PersistProjectHandler(DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                 NotificationManager notificationManager,
                                 EventBus eventBus,
                                 ProjectServiceClient projectServiceClient,
                                 AnalyticsEventLogger eventLogger) {
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.notificationManager = notificationManager;
        this.eventBus = eventBus;
        this.projectServiceClient = projectServiceClient;
        this.eventLogger = eventLogger;
    }

    /**
     * Checks for action and starts copying the projects.
     */
    public void process() {
        final String action = Config.getStartupParam("action");
        if (null == action || !"clone-projects".equals(action)) {
            return;
        }

        srcWorkspaceId = Config.getStartupParam("src-workspace-id");
        String srcProjectName = Config.getStartupParam("src-project-name");
        if (null != srcProjectName) {
            copyProject(srcProjectName, 0);
        } else {
            getSourceWorkspaceProjects();
        }
    }

    /**
     * Copy project to current ws.
     *
     * @param srcProjectName
     *         name of the project that should be copied
     */
    private void copyProject(final String srcProjectName, final int sameNameIndex) {
        final String projectName = srcProjectName + (sameNameIndex > 0 ? "-" + sameNameIndex : "");
        try {
            projectServiceClient.cloneProjectToCurrentWorkspace(srcWorkspaceId, "/" + srcProjectName, projectName,
                                                                new AsyncRequestCallback<String>() {
                                                                    @Override
                                                                    protected void onSuccess(String result) {
                                                                        eventLogger.log(PersistProjectHandler.this, "clone");
                                                                        eventBus.fireEvent(new OpenProjectEvent(projectName));
                                                                    }

                                                                    @Override
                                                                    protected void onFailure(Throwable exception) {
                                                                        if (exception instanceof ServerException &&
                                                                            exception.getMessage().contains("already exists")) {
                                                                            copyProject(srcProjectName, sameNameIndex + 1);
                                                                        } else {
                                                                            showErrorMessage("Unable to clone project. ", exception);
                                                                        }
                                                                    }
                                                                }
                                                               );
        } catch (Exception error) {
            showErrorMessage("Unable to clone project. ", error);
        }
    }

    /**
     * Gets list of projects of source workspace.
     */
    private void getSourceWorkspaceProjects() {
        try {
            projectServiceClient.getProjectsInSpecificWorkspace(srcWorkspaceId, new AsyncRequestCallback<Array<ProjectReference>>(
                    dtoUnmarshallerFactory.newArrayUnmarshaller(ProjectReference.class)) {
                @Override
                protected void onSuccess(Array<ProjectReference> result) {
                    srcWorkspaceProjects = result;
                    openProjectAfterCloning = srcWorkspaceProjects.size() == 1;
                    copyProjects(0);
                }

                @Override
                protected void onFailure(Throwable throwable) {
                    showErrorMessage("Unable to fetch projects from source workspace. ", throwable);
                }
            });
        } catch (Exception exception) {
            showErrorMessage("Unable to fetch projects from source workspace. ", exception);
        }
    }

    /**
     * Starts copying the projects from source workspace to current.
     *
     * @param sameNameIndex
     *         postfix number to the name of project. If it equals zero then postfix is absent
     */
    private void copyProjects(final int sameNameIndex) {
        if (srcWorkspaceProjects.isEmpty()) {
            if (openProjectAfterCloning) {
                eventBus.fireEvent(new OpenProjectEvent(projectNameToOpen));
            } else {
                eventBus.fireEvent(new RefreshProjectTreeEvent());
            }
            return;
        }

        final ProjectReference srcProject = srcWorkspaceProjects.get(0);
        final String projectName = srcProject.getName() + (sameNameIndex > 0 ? "-" + sameNameIndex : "");
        try {
            projectServiceClient.cloneProjectToCurrentWorkspace(srcWorkspaceId, srcProject.getPath(), projectName,
                                                                new AsyncRequestCallback<String>() {
                                                                    @Override
                                                                    protected void onSuccess(String result) {
                                                                        eventLogger.log(PersistProjectHandler.this, "clone");

                                                                        if (openProjectAfterCloning) {
                                                                            projectNameToOpen = projectName;
                                                                        }

                                                                        srcWorkspaceProjects.remove(0);
                                                                        copyProjects(0);
                                                                    }

                                                                    @Override
                                                                    protected void onFailure(Throwable exception) {
                                                                        if (exception instanceof ServerException &&
                                                                            exception.getMessage().contains("already exists")) {
                                                                            copyProjects(sameNameIndex + 1);
                                                                        } else {
                                                                            showErrorMessage("Unable to clone project. ", exception);
                                                                        }
                                                                    }
                                                                }
                                                               );
        } catch (Exception error) {
            showErrorMessage("Unable to clone project. ", error);
        }
    }

    private void showErrorMessage(String messagePrefix, Throwable error) {
        notificationManager.showNotification(new Notification(messagePrefix + error.getMessage(), Notification.Type.ERROR));
        Log.error(PersistProjectHandler.class, messagePrefix + error.getMessage());
    }

}
