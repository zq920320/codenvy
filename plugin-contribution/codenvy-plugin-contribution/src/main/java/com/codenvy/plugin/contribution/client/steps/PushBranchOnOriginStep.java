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
import com.codenvy.plugin.contribution.vcs.client.BranchUpToDateException;
import com.codenvy.plugin.contribution.vcs.client.VcsService;
import com.codenvy.plugin.contribution.vcs.client.VcsServiceProvider;
import com.codenvy.plugin.contribution.vcs.client.hosting.NoPullRequestException;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingService;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingServiceProvider;
import com.codenvy.plugin.contribution.vcs.client.hosting.dto.PullRequest;
import com.google.gwt.user.client.rpc.AsyncCallback;

import org.eclipse.che.api.git.shared.PushResponse;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.ssh.gwt.client.SshServiceClient;
import org.eclipse.che.api.ssh.shared.dto.SshPairDto;
import org.eclipse.che.ide.ui.dialogs.CancelCallback;
import org.eclipse.che.ide.ui.dialogs.ConfirmCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import static com.codenvy.plugin.contribution.client.steps.events.StepEvent.Step.PUSH_BRANCH_ON_ORIGIN;

/**
 * Push the local contribution branch to origin repository
 *
 * @author Mihail Kuznyetsov
 */
public class PushBranchOnOriginStep implements Step {
    private final Step                      generateReviewFactoryStep;
    private final VcsServiceProvider        vcsServiceProvider;
    private final VcsHostingServiceProvider vcsHostingServiceProvider;
    private final ContributeMessages        messages;
    private final DialogFactory             dialogFactory;
    private final SshServiceClient          sshService;

    @Inject
    public PushBranchOnOriginStep(@NotNull final GenerateReviewFactoryStep generateReviewFactoryStep,
                                  @NotNull final VcsServiceProvider vcsServiceProvider,
                                  @NotNull final VcsHostingServiceProvider vcsHostingServiceProvider,
                                  @NotNull final ContributeMessages messages,
                                  @NotNull final DialogFactory dialogFactory,
                                  @NotNull final SshServiceClient sshService) {
        this.generateReviewFactoryStep = generateReviewFactoryStep;
        this.vcsServiceProvider = vcsServiceProvider;
        this.vcsHostingServiceProvider = vcsHostingServiceProvider;
        this.messages = messages;
        this.dialogFactory = dialogFactory;
        this.sshService = sshService;
    }

    @Override
    public void execute(@NotNull final ContributorWorkflow workflow) {
        final Context context = workflow.getContext();
        final String originRepositoryOwner = context.getOriginRepositoryOwner();
        final String originRepositoryName = context.getOriginRepositoryName();

        context.getVcsHostingService()
               .getPullRequest(originRepositoryOwner, originRepositoryName, context.getHostUserLogin(), context.getWorkBranchName())
               .then(new Operation<PullRequest>() {
                   @Override
                   public void apply(PullRequest pullRequest) throws OperationException {
                       final ConfirmCallback okCallback = new ConfirmCallback() {
                           @Override
                           public void accepted() {
                               pushBranch(workflow, context);
                           }
                       };
                       final CancelCallback cancelCallback = new CancelCallback() {
                           @Override
                           public void cancelled() {
                               workflow.fireStepErrorEvent(PUSH_BRANCH_ON_ORIGIN, messages.stepPushBranchCanceling());
                           }
                       };

                       dialogFactory.createConfirmDialog(messages.contributePartConfigureContributionDialogUpdateTitle(),
                                                         messages.contributePartConfigureContributionDialogUpdateText(
                                                                 pullRequest.getHeadRef()),
                                                         okCallback,
                                                         cancelCallback)
                                    .show();
                   }
               })
               .catchError(new Operation<PromiseError>() {
                   @Override
                   public void apply(PromiseError error) throws OperationException {
                       if (error.getCause() instanceof NoPullRequestException) {
                           pushBranch(workflow, context);
                           return;
                       }
                       workflow.fireStepErrorEvent(PUSH_BRANCH_ON_ORIGIN, error.getMessage());
                   }
               });
    }

    protected void pushBranch(final ContributorWorkflow workflow, final Context context) {
        final VcsService vcsService = vcsServiceProvider.getVcsService();
        vcsService.pushBranch(context.getProject(),
                              context.getOriginRemoteName(),
                              context.getWorkBranchName(),
                              new AsyncCallback<PushResponse>() {
                                  @Override
                                  public void onSuccess(final PushResponse result) {
                                      workflow.fireStepDoneEvent(PUSH_BRANCH_ON_ORIGIN);

                                      workflow.setStep(generateReviewFactoryStep);
                                      workflow.executeStep();
                                  }

                                  @Override
                                  public void onFailure(final Throwable exception) {
                                      if (exception instanceof BranchUpToDateException) {
                                          workflow.fireStepErrorEvent(PUSH_BRANCH_ON_ORIGIN, messages.stepPushBranchErrorBranchUpToDate());
                                      } else if (exception.getMessage().contains("Unable get private ssh key")) {
                                          askGenerateSSH(workflow, context);
                                      } else {
                                          workflow.fireStepErrorEvent(PUSH_BRANCH_ON_ORIGIN,
                                                                      messages.stepPushBranchErrorPushingBranch(exception.getMessage()));
                                      }
                                  }
                              });
    }

    private void askGenerateSSH(final ContributorWorkflow workflow, final Context context) {
        final ConfirmCallback okCallback = new ConfirmCallback() {
            @Override
            public void accepted() {
                vcsHostingServiceProvider.getVcsHostingService(new AsyncCallback<VcsHostingService>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                        workflow.fireStepErrorEvent(PUSH_BRANCH_ON_ORIGIN, throwable.getMessage());
                    }

                    @Override
                    public void onSuccess(VcsHostingService vcsHostingService) {
                        generateSSHAndPushBranch(workflow, context, vcsHostingService.getHost());
                    }
                });
            }
        };

        final CancelCallback cancelCallback = new CancelCallback() {
            @Override
            public void cancelled() {
                workflow.fireStepErrorEvent(PUSH_BRANCH_ON_ORIGIN, messages.stepPushBranchCanceling());
            }
        };

        dialogFactory.createConfirmDialog(messages.contributePartConfigureContributionDialogSshNotFoundTitle(),
                                          messages.contributePartConfigureContributionDialogSshNotFoundText(),
                                          okCallback,
                                          cancelCallback)
                     .show();
    }

    private void generateSSHAndPushBranch(final ContributorWorkflow workflow, final Context context, String host) {
        sshService.generatePair("git", host)
                  .then(new Operation<SshPairDto>() {
                      @Override
                      public void apply(SshPairDto arg) throws OperationException {
                          pushBranch(workflow, context);
                      }
                  })
                  .catchError(new Operation<PromiseError>() {
                      @Override
                      public void apply(PromiseError err) throws OperationException {
                          workflow.fireStepErrorEvent(PUSH_BRANCH_ON_ORIGIN, err.getMessage());
                      }
                  });
    }
}
