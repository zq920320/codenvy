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
package com.codenvy.ide.factory.client.utils;

import org.eclipse.che.api.git.shared.AddRequest;
import org.eclipse.che.api.git.shared.CommitRequest;
import org.eclipse.che.api.git.shared.InitRequest;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.notification.Notification;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.rest.RestContext;

import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.eclipse.che.ide.api.notification.Notification.Type.ERROR;

/**
 * Service contains methods for working with Git repository from client side in a synchronous way.
 *
 * @author Kevin Pollet
 * @author Sergii Leschenko
 */
public class SyncGitServiceClient {
    private final DtoFactory              dtoFactory;
    private final GitLocalizationConstant gitLocale;
    private final NotificationManager     notificationManager;
    private final AppContext              appContext;
    @SuppressWarnings({"unused", "FieldCanBeLocal"})//used in native method
    private final Class<Status>           statusClass;
    @SuppressWarnings({"unused", "FieldCanBeLocal"})//used in native method
    private final Class<Revision>         revisionClass;
    @SuppressWarnings({"unused", "FieldCanBeLocal"})//used in native method
    private final String                  restContext;
    @SuppressWarnings({"unused", "FieldCanBeLocal"})//used in native method
    private final String                  workspaceId;

    @Inject
    public SyncGitServiceClient(@RestContext String restContext,
                                @Named("workspaceId") String workspaceId,
                                DtoFactory dtoFactory,
                                GitLocalizationConstant gitLocale,
                                NotificationManager notificationManager,
                                AppContext appContext) {
        this.dtoFactory = dtoFactory;
        this.workspaceId = workspaceId;
        this.restContext = restContext;
        this.gitLocale = gitLocale;
        this.notificationManager = notificationManager;
        this.appContext = appContext;
        this.statusClass = Status.class;
        this.revisionClass = Revision.class;
    }

    /**
     * Init GIT repository by synchronous request.
     *
     * @param project
     *         project (root of GIT repository)
     */
    public native void init(@NotNull ProjectDescriptor project) /*-{
        var instance = this;
        try {
            var url = instance.@com.codenvy.ide.factory.client.utils.SyncGitServiceClient::restContext
                + "/git/"
                + instance.@com.codenvy.ide.factory.client.utils.SyncGitServiceClient::workspaceId
                + "/init?projectPath=" + project.@org.eclipse.che.api.project.shared.dto.ProjectDescriptor::getPath()();

            var request = new XMLHttpRequest();
            request.open("POST", url, false);
            request.setRequestHeader("Content-type", "application/json");
            var data = instance.@com.codenvy.ide.factory.client.utils.SyncGitServiceClient::createInitRequest(Lorg/eclipse/che/api/project/shared/dto/ProjectDescriptor;)(project);
            request.send(data);

            if (request.status != 204) {
                instance.@com.codenvy.ide.factory.client.utils.SyncGitServiceClient::processError(Ljava/lang/String;)("Can't init repository. "
                    + request.responseText);
            }

            instance.@com.codenvy.ide.factory.client.utils.SyncGitServiceClient::updateGitProvider()();
            instance.@com.codenvy.ide.factory.client.utils.SyncGitServiceClient::showNotificationInitRepository()();
        } catch (exc) {
            console.log(exc.message);
            instance.@com.codenvy.ide.factory.client.utils.SyncGitServiceClient::processError(Ljava/lang/String;)(exc.message);
        }
    }-*/;

    /**
     * Gets the working tree status : list of untracked, changed not commited and changed not updated.
     *
     * @param project
     *         project (root of GIT repository)
     */
    public native Status status(@NotNull ProjectDescriptor project) /*-{
        var instance = this;
        try {
            var statusClass = instance.@com.codenvy.ide.factory.client.utils.SyncGitServiceClient::statusClass;
            var dtoFactory = instance.@com.codenvy.ide.factory.client.utils.SyncGitServiceClient::dtoFactory;
            var url = instance.@com.codenvy.ide.factory.client.utils.SyncGitServiceClient::restContext
                + "/git/"
                + instance.@com.codenvy.ide.factory.client.utils.SyncGitServiceClient::workspaceId
                + "/status?projectPath=" + project.@org.eclipse.che.api.project.shared.dto.ProjectDescriptor::getPath()() + "&short=false";

            var request = new XMLHttpRequest();
            request.open("POST", url, false);
            request.setRequestHeader("Content-type", "application/json");
            request.send();

            if (request.status == 200) {
                return dtoFactory.@org.eclipse.che.ide.dto.DtoFactory::createDtoFromJson(Ljava/lang/String;Ljava/lang/Class;)(request.responseText, statusClass);
            }

            instance.@com.codenvy.ide.factory.client.utils.SyncGitServiceClient::processError(Ljava/lang/String;)("Can't get git status. " +
                request.responseText);
        } catch (exc) {
            console.log(exc.message);
            instance.@com.codenvy.ide.factory.client.utils.SyncGitServiceClient::processError(Ljava/lang/String;)(exc.message);
        }
    }-*/;

    /**
     * Add changes to Git index (temporary storage). Sends request over WebSocket.
     *
     * @param project
     *         project (root of GIT repository)
     * @param update
     *         if <code>true</code> then never stage new files, but stage modified new contents of tracked files and remove files from
     *         the index if the corresponding files in the working tree have been removed
     * @param filePattern
     *         pattern of the files to be added, default is "." (all files are added)
     */
    public native void add(@NotNull ProjectDescriptor project, boolean update, @Nullable List<String> filePattern) /*-{
        var instance = this;
        try {
            var url = instance.@com.codenvy.ide.factory.client.utils.SyncGitServiceClient::restContext
                + "/git/"
                + instance.@com.codenvy.ide.factory.client.utils.SyncGitServiceClient::workspaceId
                + "/add?projectPath=" + project.@org.eclipse.che.api.project.shared.dto.ProjectDescriptor::getPath()();

            var request = new XMLHttpRequest();
            request.open("POST", url, false);
            request.setRequestHeader("Content-type", "application/json");
            request.send(instance.@com.codenvy.ide.factory.client.utils.SyncGitServiceClient::createAddRequest(ZLjava/util/List;)(update, filePattern));

            if (request.status != 204) {
                instance.@com.codenvy.ide.factory.client.utils.SyncGitServiceClient::processError(Ljava/lang/String;)("Can't add changes to git. " +
                    request.responseText);
            }
        } catch (exc) {
            console.log(exc.message);
            instance.@com.codenvy.ide.factory.client.utils.SyncGitServiceClient::processError(Ljava/lang/String;)(exc.message);
        }
    }-*/;

    /**
     * Performs commit changes from index to repository. The result of the commit is represented by {@link Revision}, which is returned by
     * callback in <code>onSuccess(Revision result)</code>. Sends request over WebSocket.
     *
     * @param project
     *         project (root of GIT repository)
     * @param message
     *         commit log message
     * @param all
     *         automatically stage files that have been modified and deleted
     * @param amend
     *         indicates that previous commit must be overwritten
     */
    public native Revision commit(@NotNull ProjectDescriptor project, @NotNull String message, boolean all, boolean amend) /*-{
        var instance = this;
        try {
            var dtoFactory = instance.@com.codenvy.ide.factory.client.utils.SyncGitServiceClient::dtoFactory;
            var revisionClass = instance.@com.codenvy.ide.factory.client.utils.SyncGitServiceClient::revisionClass;
            var url = instance.@com.codenvy.ide.factory.client.utils.SyncGitServiceClient::restContext
                + "/git/"
                + instance.@com.codenvy.ide.factory.client.utils.SyncGitServiceClient::workspaceId
                + "/commit?projectPath=" + project.@org.eclipse.che.api.project.shared.dto.ProjectDescriptor::getPath()();

            var request = new XMLHttpRequest();
            request.open("POST", url, false);
            request.setRequestHeader("Content-type", "application/json");
            request.send(instance.@com.codenvy.ide.factory.client.utils.SyncGitServiceClient::createCommitRequest(Ljava/lang/String;ZZ)(message, all, amend));

            if (request.status == 200) {
                return dtoFactory.@org.eclipse.che.ide.dto.DtoFactory::createDtoFromJson(Ljava/lang/String;Ljava/lang/Class;)(request.responseText, revisionClass);
            }

            instance.@com.codenvy.ide.factory.client.utils.SyncGitServiceClient::processError(Ljava/lang/String;)("Can't commit changes. " +
                request.responseText);
        } catch (exc) {
            console.log(exc.message);
            instance.@com.codenvy.ide.factory.client.utils.SyncGitServiceClient::processError(Ljava/lang/String;)(exc.message);
        }
    }-*/;

    private void updateGitProvider() {
        CurrentProject currentProject = appContext.getCurrentProject();
        if (currentProject != null) {
            ProjectDescriptor rootProjectDescriptor = currentProject.getRootProject();
            if (rootProjectDescriptor.getAttributes() == null) {
                rootProjectDescriptor.setAttributes(new HashMap<String, List<String>>());

                rootProjectDescriptor.getAttributes().put("vcs.provider.name", new ArrayList<>(Collections.singletonList("git")));
            } else if (rootProjectDescriptor.getAttributes().get("vcs.provider.name") == null
                       || rootProjectDescriptor.getAttributes().get("vcs.provider.name").isEmpty()) {
                rootProjectDescriptor.getAttributes().put("vcs.provider.name", new ArrayList<>(Collections.singletonList("git")));
            } else {
                rootProjectDescriptor.getAttributes().get("vcs.provider.name").add("git");
            }
        }
    }

    private String createCommitRequest(@NotNull String message, boolean all, boolean amend) {
        final CommitRequest commitRequest = dtoFactory.createDto(CommitRequest.class).withMessage(message).withAmend(amend).withAll(all);
        return dtoFactory.toJson(commitRequest);
    }

    private String createAddRequest(boolean update, @Nullable List<String> filePattern) {
        final AddRequest addRequest = dtoFactory.createDto(AddRequest.class).withUpdate(update)
                                                .withFilepattern(filePattern == null ? AddRequest.DEFAULT_PATTERN : filePattern);
        return dtoFactory.toJson(addRequest);
    }

    private String createInitRequest(ProjectDescriptor project) {
        InitRequest initRequest = dtoFactory.createDto(InitRequest.class);
        initRequest.setWorkingDir(project.getName());
        initRequest.setInitCommit(true);
        initRequest.setBare(false);
        return dtoFactory.toJson(initRequest);
    }

    private void showNotificationInitRepository() {
        Notification notification = new Notification(gitLocale.initSuccess(), Notification.Type.INFO);
        notificationManager.showNotification(notification);
    }

    /**
     * Show error message
     *
     * @param message
     *         error message
     */
    private void processError(String message) {
        final Notification notification = new Notification(message, ERROR);
        notificationManager.showNotification(notification);
    }
}
