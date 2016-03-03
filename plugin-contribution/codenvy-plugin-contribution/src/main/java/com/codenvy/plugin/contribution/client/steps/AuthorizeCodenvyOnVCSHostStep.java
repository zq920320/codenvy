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
import com.codenvy.plugin.contribution.vcs.client.hosting.dto.HostUser;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.commons.exception.UnauthorizedException;

import javax.inject.Inject;

import static com.codenvy.plugin.contribution.client.steps.events.StepEvent.Step.AUTHORIZE_CODENVY_ON_VCS_HOST;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * This step authorizes Codenvy on the VCS Host.
 *
 * @author Kevin Pollet
 * @author Yevhenii Voevodin
 */
public class AuthorizeCodenvyOnVCSHostStep implements Step {
    private final NotificationManager             notificationManager;
    private final AppContext                      appContext;
    private final ContributeMessages              messages;
    private final DetermineUpstreamRepositoryStep determineUpstreamRepositoryStep;

    @Inject
    public AuthorizeCodenvyOnVCSHostStep(final DetermineUpstreamRepositoryStep determineUpstreamRepositoryStep,
                                         final NotificationManager notificationManager,
                                         final AppContext appContext,
                                         final ContributeMessages messages) {
        this.determineUpstreamRepositoryStep = determineUpstreamRepositoryStep;
        this.notificationManager = notificationManager;
        this.appContext = appContext;
        this.messages = messages;
    }

    @Override
    public void execute(final ContributorWorkflow workflow) {
        workflow.getContext()
                .getVcsHostingService()
                .getUserInfo()
                .then(authSuccessOp(workflow))
                .catchError(getUserErrorOp(workflow));
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

    private Operation<PromiseError> getUserErrorOp(final ContributorWorkflow workflow) {
        return new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                try {
                    throw error.getCause();
                } catch (UnauthorizedException unEx) {
                    authenticate(workflow);
                } catch (Throwable thr) {
                    handleThrowable(thr, workflow);
                }
            }
        };
    }

    private void authenticate(final ContributorWorkflow workflow) {
        workflow.getContext()
                .getVcsHostingService()
                .authenticate(appContext.getCurrentUser())
                .then(authSuccessOp(workflow))
                .catchError(new Operation<PromiseError>() {
                    @Override
                    public void apply(PromiseError err) throws OperationException {
                        try {
                            throw err.getCause();
                        } catch (UnauthorizedException unEx) {
                            notificationManager.notify(messages.stepAuthorizeCodenvyOnVCSHostErrorCannotAccessVCSHostTitle(),
                                                       messages.stepAuthorizeCodenvyOnVCSHostErrorCannotAccessVCSHostContent(),
                                                       FAIL,
                                                       true);
                            workflow.fireStepErrorEvent(AUTHORIZE_CODENVY_ON_VCS_HOST);
                        } catch (Throwable thr) {
                            handleThrowable(thr, workflow);
                        }
                    }
                });
    }

    private void handleThrowable(final Throwable thr, final ContributorWorkflow workflow) {
        notificationManager.notify(thr.getLocalizedMessage(), FAIL, true);
        workflow.fireStepErrorEvent(AUTHORIZE_CODENVY_ON_VCS_HOST);
    }
}
