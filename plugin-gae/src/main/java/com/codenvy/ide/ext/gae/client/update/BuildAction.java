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
package com.codenvy.ide.ext.gae.client.update;

import org.eclipse.che.api.builder.BuildStatus;
import org.eclipse.che.api.builder.dto.BuildTaskDescriptor;
import org.eclipse.che.api.builder.gwt.client.BuilderServiceClient;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.ide.api.build.BuildContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import com.codenvy.ide.ext.gae.client.GAELocalizationConstant;
import com.codenvy.ide.ext.gae.client.service.callbacks.FailureCallback;
import com.codenvy.ide.ext.gae.client.service.callbacks.GAEAsyncCallbackFactory;
import com.codenvy.ide.ext.gae.client.service.callbacks.GAEAsyncRequestCallback;
import com.codenvy.ide.ext.gae.client.service.callbacks.SuccessCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.events.MessageHandler;
import org.eclipse.che.ide.websocket.rest.SubscriptionHandler;
import com.google.inject.Inject;

import javax.annotation.Nonnull;

import static org.eclipse.che.api.builder.BuildStatus.SUCCESSFUL;
import static org.eclipse.che.api.builder.internal.Constants.LINK_REL_DOWNLOAD_RESULT;
import static org.eclipse.che.ide.extension.builder.client.BuilderExtension.BUILD_STATUS_CHANNEL;

/**
 * Class contains business logic related to building of project, updating and checking project's status.
 *
 * @author Dmitry Shnurenko
 */
public class BuildAction {

    private final GAELocalizationConstant locale;
    private final GAEAsyncCallbackFactory callbackFactory;
    private final BuilderServiceClient    builderService;
    private final DtoUnmarshallerFactory  dtoUnmarshallerFactory;
    private final MessageBus              messageBus;
    private final BuildContext            buildContext;
    private final NotificationManager     notificationManager;

    private BuildTaskDescriptor                      lastBuildTaskDescriptor;
    private SubscriptionHandler<BuildTaskDescriptor> buildStatusHandler;
    private UpdateGAECallback                        updateGAECallback;
    private boolean                                  isBuildInProgress;

    @Inject
    public BuildAction(GAELocalizationConstant locale,
                       GAEAsyncCallbackFactory callbackFactory,
                       NotificationManager notificationManager,
                       BuilderServiceClient builderService,
                       DtoUnmarshallerFactory dtoUnmarshallerFactory,
                       MessageBus messageBus,
                       BuildContext buildContext) {

        this.locale = locale;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.notificationManager = notificationManager;
        this.messageBus = messageBus;
        this.builderService = builderService;
        this.callbackFactory = callbackFactory;
        this.buildContext = buildContext;

        isBuildInProgress = false;
    }


    /**
     * Method checks build is in progress or not. If build is progress will show error notification, else called
     * method which run build.
     *
     * @param activeProject
     *         project which we use
     * @param updateGAECallback
     *         callback which need to return info about build
     */
    public void perform(@Nonnull ProjectDescriptor activeProject, @Nonnull UpdateGAECallback updateGAECallback) {
        this.updateGAECallback = updateGAECallback;

        if (isBuildInProgress) {
            notificationManager.showError(locale.buildInProgress(activeProject.getName()));
        } else {
            buildProject(activeProject);
        }
    }

    private void buildProject(@Nonnull final ProjectDescriptor activeProject) {
        lastBuildTaskDescriptor = null;
        buildContext.setBuilding(true);

        isBuildInProgress = true;

        GAEAsyncRequestCallback<BuildTaskDescriptor> buildTaskDescriptorCallback =
                callbackFactory.build(BuildTaskDescriptor.class, new SuccessCallback<BuildTaskDescriptor>() {
                    @Override
                    public void onSuccess(BuildTaskDescriptor result) {
                        lastBuildTaskDescriptor = result;
                        BuildStatus status = result.getStatus();

                        if (status == SUCCESSFUL) {
                            buildContext.setBuilding(false);
                            updateGAECallback.onSuccess(getLink(lastBuildTaskDescriptor, LINK_REL_DOWNLOAD_RESULT));
                        } else {
                            startCheckingStatus(result, activeProject);
                        }
                    }
                }, new FailureCallback() {
                    @Override
                    public void onFailure(@Nonnull Throwable reason) {
                        updateGAECallback.onFailure(reason.getMessage());
                    }
                });

        builderService.build(activeProject.getPath(), buildTaskDescriptorCallback);
    }

    @Nonnull
    private String getLink(@Nonnull BuildTaskDescriptor descriptor, @Nonnull String rel) {
        for (Link link : descriptor.getLinks()) {
            if (link.getRel().equalsIgnoreCase(rel)) {
                return link.getHref();
            }
        }

        return "";
    }

    private void startCheckingStatus(@Nonnull final BuildTaskDescriptor buildTaskDescriptor,
                                     @Nonnull final ProjectDescriptor activeProject) {
        buildStatusHandler =
                new SubscriptionHandler<BuildTaskDescriptor>(dtoUnmarshallerFactory.newWSUnmarshaller(BuildTaskDescriptor.class)) {
                    @Override
                    protected void onMessageReceived(BuildTaskDescriptor result) {
                        lastBuildTaskDescriptor = result;
                        onBuildStatusUpdated(result.getStatus(), activeProject);
                    }

                    @Override
                    protected void onErrorReceived(Throwable exception) {
                        isBuildInProgress = false;
                        unsubscribeBuildStatusHandler(buildTaskDescriptor, this);
                        buildContext.setBuilding(false);

                        updateGAECallback.onFailure(locale.deployError(exception.getMessage()));
                    }
                };

        try {
            messageBus.subscribe(BUILD_STATUS_CHANNEL + buildTaskDescriptor.getTaskId(), buildStatusHandler);
        } catch (WebSocketException e) {
            updateGAECallback.onFailure(e.getMessage());
        }
    }

    private void unsubscribeBuildStatusHandler(@Nonnull BuildTaskDescriptor buildTaskDescriptor, @Nonnull MessageHandler handler) {
        try {
            messageBus.unsubscribe(BUILD_STATUS_CHANNEL + buildTaskDescriptor.getTaskId(), handler);
        } catch (WebSocketException e) {
            updateGAECallback.onFailure(e.getMessage());
        }
    }

    /** Process changing build status. */
    private void onBuildStatusUpdated(@Nonnull BuildStatus status, @Nonnull ProjectDescriptor activeProject) {
        switch (status) {
            case SUCCESSFUL:
                updateGAECallback.onSuccess(getLink(lastBuildTaskDescriptor, LINK_REL_DOWNLOAD_RESULT));
                isBuildInProgress = false;
                buildContext.setBuilding(false);
                break;

            case FAILED:
                buildFinished();
                updateGAECallback.onFailure(locale.messagesBuildFailed());
                break;

            case CANCELLED:
                buildFinished();
                updateGAECallback.onFailure(locale.messagesBuildCanceled(activeProject.getName()));
                break;

            default:
        }
    }

    private void buildFinished() {
        isBuildInProgress = false;
        buildContext.setBuilding(false);

        try {
            messageBus.unsubscribe(BUILD_STATUS_CHANNEL + lastBuildTaskDescriptor.getTaskId(), buildStatusHandler);
        } catch (WebSocketException e) {
            updateGAECallback.onFailure(e.getMessage());
        }
    }

}