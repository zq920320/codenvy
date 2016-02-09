/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2016] Codenvy, S.A.
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
package com.codenvy.plugin.contribution.client.steps;

import com.codenvy.plugin.contribution.client.ContributeMessages;
import com.codenvy.plugin.contribution.client.utils.NotificationHelper;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingService;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingServiceProvider;
import com.codenvy.plugin.contribution.vcs.client.hosting.dto.HostUser;
import com.google.gwt.user.client.rpc.AsyncCallback;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.commons.exception.UnauthorizedException;

import javax.validation.constraints.NotNull;
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
    public AuthorizeCodenvyOnVCSHostStep(@NotNull final InitializeWorkflowContextStep initializeWorkflowContextStep,
                                         @NotNull final NotificationHelper notificationHelper,
                                         @NotNull final VcsHostingServiceProvider vcsHostingServiceProvider,
                                         @NotNull final AppContext appContext,
                                         @NotNull final ContributeMessages messages) {
        this.initializeWorkflowContextStep = initializeWorkflowContextStep;
        this.notificationHelper = notificationHelper;
        this.vcsHostingServiceProvider = vcsHostingServiceProvider;
        this.appContext = appContext;
        this.messages = messages;
    }

    @Override
    public void execute(@NotNull final ContributorWorkflow workflow) {
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
