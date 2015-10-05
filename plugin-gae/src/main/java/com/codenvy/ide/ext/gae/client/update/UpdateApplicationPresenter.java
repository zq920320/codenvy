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

import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.api.project.shared.dto.BuildersDescriptor;
import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.notification.Notification;
import org.eclipse.che.ide.api.notification.NotificationManager;
import com.codenvy.ide.ext.gae.client.GAELocalizationConstant;
import com.codenvy.ide.ext.gae.client.GAEResources;
import com.codenvy.ide.ext.gae.client.confirm.ConfirmView;
import com.codenvy.ide.ext.gae.client.login.OAuthLoginPresenter;
import com.codenvy.ide.ext.gae.client.service.GAEServiceClient;
import com.codenvy.ide.ext.gae.client.service.callbacks.FailureCallback;
import com.codenvy.ide.ext.gae.client.service.callbacks.GAEAsyncCallbackFactory;
import com.codenvy.ide.ext.gae.client.service.callbacks.GAEAsyncRequestCallback;
import com.codenvy.ide.ext.gae.client.service.callbacks.SuccessCallback;
import com.codenvy.ide.ext.gae.client.utils.GAEUtil;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.validation.constraints.NotNull;

import static org.eclipse.che.ide.api.notification.Notification.Status;
import static org.eclipse.che.ide.api.notification.Notification.Status.FINISHED;
import static org.eclipse.che.ide.api.notification.Notification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.Notification.Type;
import static org.eclipse.che.ide.api.notification.Notification.Type.ERROR;
import static org.eclipse.che.ide.api.notification.Notification.Type.INFO;
import static com.codenvy.ide.ext.gae.shared.GAEConstants.ERROR_WEB_ENGINE_VALIDATE;
import static com.codenvy.ide.ext.gae.shared.GAEConstants.ERROR_YAML_VALIDATE;

/**
 * The class contains business logic which allows us build and deploy GAE project.
 *
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
@Singleton
public class UpdateApplicationPresenter implements ConfirmView.ActionDelegate, OAuthLoginPresenter.LoginActionListener {

    public static final String ERROR_YAML       = "{\"message\":\"" + ERROR_YAML_VALIDATE + "\"}";
    public static final String ERROR_WEB_ENGINE = "{\"message\":\"" + ERROR_WEB_ENGINE_VALIDATE + "\"}";

    private final GAEServiceClient        service;
    private final NotificationManager     notificationManager;
    private final GAELocalizationConstant locale;
    private final GAEUtil                 gaeUtil;
    private final ConfirmView             view;
    private final OAuthLoginPresenter     loginPresenter;
    private final AppContext              context;
    private final GAEAsyncCallbackFactory callbackFactory;
    private final BuildAction             buildAction;
    private final DeployAction            deployAction;

    private final UpdateGAECallback buildCallBack;
    private final UpdateGAECallback deployCallBack;

    private Notification      notification;
    private ProjectDescriptor activeProject;

    @Inject
    public UpdateApplicationPresenter(final DeployAction deployAction,
                                      BuildAction buildAction,
                                      NotificationManager notificationManager,
                                      final GAELocalizationConstant locale,
                                      GAEUtil gaeUtil,
                                      AppContext context,
                                      GAEAsyncCallbackFactory callbackFactory,
                                      GAEServiceClient service,
                                      ConfirmView view,
                                      GAEResources resources,
                                      OAuthLoginPresenter loginPresenter) {

        this.view = view;
        this.view.setDelegate(this);

        this.gaeUtil = gaeUtil;
        this.notificationManager = notificationManager;
        this.buildAction = buildAction;
        this.deployAction = deployAction;
        this.locale = locale;
        this.loginPresenter = loginPresenter;
        this.context = context;
        this.service = service;
        this.callbackFactory = callbackFactory;

        this.loginPresenter.setLoginActionListener(this);

        this.buildCallBack = new UpdateGAECallback() {
            @Override
            public void onSuccess(@NotNull String message) {
                showNotification(PROGRESS, INFO, locale.deployStarted(activeProject.getName()));

                deployAction.perform(activeProject, message, deployCallBack);
            }

            @Override
            public void onFailure(@NotNull String errorMessage) {
                showNotification(FINISHED, ERROR, errorMessage);
            }
        };

        this.deployCallBack = new UpdateGAECallback() {
            @Override
            public void onSuccess(@NotNull String message) {
                showNotification(FINISHED, INFO, locale.deploySuccess(message));
            }

            @Override
            public void onFailure(@NotNull String errorMessage) {
                showNotification(FINISHED, ERROR, locale.deployError(errorMessage));
            }
        };

        this.view.setActionButtonTitle(locale.updateButton());
        this.view.setSubtitle(locale.deployApplicationSubtitle());
        this.view.setUserInstructions(locale.deployApplicationInstruction());
        this.view.addSubtitleStyleName(resources.gaeCSS().smallSubTitleLabel());
    }

    /** Shows the create application window if user is logged in. */
    public void showDialog() {
        CurrentProject currentProject = context.getCurrentProject();
        if (currentProject == null) {
            return;
        }

        activeProject = currentProject.getProjectDescription();

        notification = null;

        GAEAsyncRequestCallback<Void> validateCallBack = callbackFactory.build(new SuccessCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                checkUserLogged();
            }
        }, new FailureCallback() {
            @Override
            public void onFailure(@NotNull Throwable reason) {
                String message = reason.getMessage();

                switch (message) {
                    case ERROR_WEB_ENGINE:
                        notificationManager.showError(locale.errorValidateWebEngine());
                        break;

                    case ERROR_YAML:
                        notificationManager.showError(locale.errorValidateYaml());
                        break;

                    default:
                        notificationManager.showError(message);
                }
            }
        });

        service.validateProject(activeProject.getPath(), validateCallBack);
    }

    private void checkUserLogged() {
        GAEAsyncRequestCallback<OAuthToken> oAuthCallback = callbackFactory.build(OAuthToken.class, new SuccessCallback<OAuthToken>() {
            @Override
            public void onSuccess(OAuthToken result) {
                if (!gaeUtil.isAuthenticatedInAppEngine(result)) {
                    loginPresenter.showDialog();
                } else {
                    view.show();
                }
            }
        });
        service.getLoggedUser(oAuthCallback);
    }

    /** {@inheritDoc} */
    @Override
    public void onLoginWindowHide() {
        view.show();
    }

    /** {@inheritDoc} */
    @Override
    public void onActionButtonClicked() {
        BuildersDescriptor builders = activeProject.getBuilders();
        if (builders == null || builders.getDefault() == null) {
            showNotification(PROGRESS, INFO, locale.deployStarted(activeProject.getName()));

            deployAction.perform(activeProject, null, deployCallBack);
        } else {
            showNotification(PROGRESS, INFO, locale.buildStarted(activeProject.getName()));

            buildAction.perform(activeProject, buildCallBack);
        }

        view.close();
    }

    /** {@inheritDoc} */
    @Override
    public void onCancelButtonClicked() {
        view.close();
    }

    private void showNotification(@NotNull Status status, @NotNull Type type, @NotNull String message) {
        if (notification == null) {
            notification = new Notification(message, type, status);
            notificationManager.showNotification(notification);
        } else {
            notification.setStatus(status);
            notification.setType(type);
            notification.setMessage(message);
        }
    }
}