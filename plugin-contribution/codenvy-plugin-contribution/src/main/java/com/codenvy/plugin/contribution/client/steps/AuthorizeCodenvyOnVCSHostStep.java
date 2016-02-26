/*
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

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.commons.exception.UnauthorizedException;

import javax.inject.Inject;

import static com.codenvy.plugin.contribution.client.steps.events.StepEvent.Step.AUTHORIZE_CODENVY_ON_VCS_HOST;

/**
 * This step authorizes Codenvy on the VCS Host.
 *
 * @author Kevin Pollet
 */
public class AuthorizeCodenvyOnVCSHostStep implements Step {
    private final NotificationHelper              notificationHelper;
    private final VcsHostingServiceProvider       hostingServiceProvider;
    private final AppContext                      appContext;
    private final ContributeMessages              messages;
    private final DetermineUpstreamRepositoryStep determineUpstreamRepositoryStep;

    @Inject
    public AuthorizeCodenvyOnVCSHostStep(final DetermineUpstreamRepositoryStep determineUpstreamRepositoryStep,
                                         final NotificationHelper notificationHelper,
                                         final VcsHostingServiceProvider vcsHostingServiceProvider,
                                         final AppContext appContext,
                                         final ContributeMessages messages) {
        this.determineUpstreamRepositoryStep = determineUpstreamRepositoryStep;
        this.notificationHelper = notificationHelper;
        this.hostingServiceProvider = vcsHostingServiceProvider;
        this.appContext = appContext;
        this.messages = messages;
    }

    @Override
    public void execute(final ContributorWorkflow workflow) {
        hostingServiceProvider.getVcsHostingService()
                              .then(new Operation<VcsHostingService>() {
                                  @Override
                                  public void apply(VcsHostingService service) throws OperationException {
                                      service.getUserInfo()
                                             .then(authSuccessOp(workflow))
                                             .catchError(authErrorOp(workflow, service));
                                  }
                              })
                              .catchError(new Operation<PromiseError>() {
                                  @Override
                                  public void apply(PromiseError error) throws OperationException {
                                      workflow.fireStepErrorEvent(AUTHORIZE_CODENVY_ON_VCS_HOST, error.getMessage());
                                  }
                              });
    }

    private Operation<HostUser> authSuccessOp(final ContributorWorkflow workflow) {
        return new Operation<HostUser>() {
            @Override
            public void apply(HostUser user) throws OperationException {
                workflow.getContext().setHostUserLogin(user.getLogin());
                workflow.fireStepDoneEvent(AUTHORIZE_CODENVY_ON_VCS_HOST);
                workflow.setStep(determineUpstreamRepositoryStep);
                workflow.executeStep();
            }
        };
    }

    private Operation<PromiseError> authErrorOp(final ContributorWorkflow workflow, final VcsHostingService service) {
        return new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                if (isUnauthorized(error.getCause())) {
                    service.authenticate(appContext.getCurrentUser())
                           .then(authSuccessOp(workflow))
                           .catchError(new Operation<PromiseError>() {
                               @Override
                               public void apply(PromiseError error) throws OperationException {
                                   workflow.fireStepErrorEvent(AUTHORIZE_CODENVY_ON_VCS_HOST);
                                   if (isUnauthorized(error.getCause())) {
                                       notificationHelper.showError(AuthorizeCodenvyOnVCSHostStep.class,
                                                                    messages.stepAuthorizeCodenvyOnVCSHostErrorCannotAccessVCSHost());

                                   } else {
                                       notificationHelper.showError(AuthorizeCodenvyOnVCSHostStep.class, error.getCause());
                                   }
                               }
                           });
                } else {
                    workflow.fireStepErrorEvent(AUTHORIZE_CODENVY_ON_VCS_HOST);
                    notificationHelper.showError(AuthorizeCodenvyOnVCSHostStep.class, error.getCause());
                }
            }
        };
    }

    private boolean isUnauthorized(Throwable cause) {
        return cause instanceof UnauthorizedException;
    }
}
