/*******************************************************************************
 * Copyright (c) 2014-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.plugin.contribution.client.steps;

import com.codenvy.plugin.contribution.client.ContributeMessages;
import com.codenvy.plugin.contribution.client.utils.NotificationHelper;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingService;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingServiceProvider;
import com.codenvy.plugin.contribution.vcs.client.hosting.dto.HostUser;
import com.google.gwt.user.client.rpc.AsyncCallback;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.commons.exception.UnauthorizedException;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static com.codenvy.plugin.contribution.client.steps.events.StepEvent.Step.AUTHORIZE_CODENVY_ON_VCS_HOST;

/**
 * This step authorizes Codenvy on the VCS Host.
 *
 * @author Kevin Pollet
 */
public class AuthorizeCodenvyOnVCSHostStep implements Step {
    private final Step                      initializeWorkflowContextStep;
    private final NotificationHelper        notificationHelper;
    private final VcsHostingServiceProvider vcsHostingServiceProvider;
    private final AppContext                appContext;
    private final ContributeMessages        messages;

    @Inject
    public AuthorizeCodenvyOnVCSHostStep(@Nonnull final InitializeWorkflowContextStep initializeWorkflowContextStep,
                                         @Nonnull final NotificationHelper notificationHelper,
                                         @Nonnull final VcsHostingServiceProvider vcsHostingServiceProvider,
                                         @Nonnull final AppContext appContext,
                                         @Nonnull final ContributeMessages messages) {
        this.initializeWorkflowContextStep = initializeWorkflowContextStep;
        this.notificationHelper = notificationHelper;
        this.vcsHostingServiceProvider = vcsHostingServiceProvider;
        this.appContext = appContext;
        this.messages = messages;
    }

    @Override
    public void execute(@Nonnull final ContributorWorkflow workflow) {
        vcsHostingServiceProvider.getVcsHostingService(new AsyncCallback<VcsHostingService>() {
            @Override
            public void onFailure(final Throwable exception) {
                workflow.fireStepErrorEvent(AUTHORIZE_CODENVY_ON_VCS_HOST, exception.getMessage());
            }

            @Override
            public void onSuccess(final VcsHostingService vcsHostingService) {
                vcsHostingService.getUserInfo(new AsyncCallback<HostUser>() {
                    @Override
                    public void onFailure(final Throwable exception) {
                        if (exception instanceof UnauthorizedException) {

                            vcsHostingService.authenticate(appContext.getCurrentUser(), new AsyncCallback<HostUser>() {
                                @Override
                                public void onFailure(final Throwable exception) {
                                    workflow.fireStepErrorEvent(AUTHORIZE_CODENVY_ON_VCS_HOST);

                                    if (exception instanceof UnauthorizedException) {
                                        notificationHelper
                                                .showError(AuthorizeCodenvyOnVCSHostStep.class,
                                                           messages.stepAuthorizeCodenvyOnVCSHostErrorCannotAccessVCSHost());

                                    } else {
                                        notificationHelper.showError(AuthorizeCodenvyOnVCSHostStep.class, exception);
                                    }
                                }

                                @Override
                                public void onSuccess(final HostUser user) {
                                    onVCSHostUserAuthenticated(workflow, user);
                                }
                            });

                        } else {
                            workflow.fireStepErrorEvent(AUTHORIZE_CODENVY_ON_VCS_HOST);
                            notificationHelper.showError(AuthorizeCodenvyOnVCSHostStep.class, exception);
                        }
                    }

                    @Override
                    public void onSuccess(final HostUser user) {
                        onVCSHostUserAuthenticated(workflow, user);
                    }
                });
            }
        });
    }

    private void onVCSHostUserAuthenticated(final ContributorWorkflow workflow, final HostUser user) {
        workflow.getContext().setHostUserLogin(user.getLogin());
        workflow.fireStepDoneEvent(AUTHORIZE_CODENVY_ON_VCS_HOST);
        workflow.setStep(initializeWorkflowContextStep);
        workflow.executeStep();
    }
}
