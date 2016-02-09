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
import com.codenvy.plugin.contribution.vcs.client.VcsServiceProvider;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingService;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingServiceProvider;
import com.google.gwt.user.client.rpc.AsyncCallback;

import org.eclipse.che.api.git.shared.Remote;

import javax.validation.constraints.NotNull;
import javax.inject.Inject;
import java.util.List;

import static com.codenvy.plugin.contribution.client.steps.events.StepEvent.Step.ADD_FORK_REMOTE;

/**
 * Adds the forked remote repository to the remotes of the project.
 */
public class AddForkRemoteStep implements Step {
    private final static String ORIGIN_REMOTE_NAME = "origin";
    private final static String FORK_REMOTE_NAME   = "fork";

    private final VcsServiceProvider        vcsServiceProvider;
    private final Step                      pushBranchOnForkStep;
    private final ContributeMessages        messages;
    private final VcsHostingServiceProvider vcsHostingServiceProvider;

    @Inject
    public AddForkRemoteStep(@NotNull final VcsServiceProvider vcsServiceProvider,
                             @NotNull final VcsHostingServiceProvider vcsHostingServiceProvider,
                             @NotNull final PushBranchOnForkStep pushBranchOnForkStep,
                             @NotNull final ContributeMessages messages) {
        this.vcsServiceProvider = vcsServiceProvider;
        this.vcsHostingServiceProvider = vcsHostingServiceProvider;
        this.pushBranchOnForkStep = pushBranchOnForkStep;
        this.messages = messages;
    }

    @Override
    public void execute(@NotNull final ContributorWorkflow workflow) {
        final Context context = workflow.getContext();
        final String originRepositoryOwner = context.getOriginRepositoryOwner();
        final String originRepositoryName = context.getOriginRepositoryName();
        final String upstreamRepositoryOwner = context.getUpstreamRepositoryOwner();
        final String upstreamRepositoryName = context.getUpstreamRepositoryName();

        // the fork remote has to be added only if we cloned the upstream else it's origin
        if (originRepositoryOwner.equalsIgnoreCase(upstreamRepositoryOwner) &&
            originRepositoryName.equalsIgnoreCase(upstreamRepositoryName)) {

            vcsHostingServiceProvider.getVcsHostingService(new AsyncCallback<VcsHostingService>() {
                @Override
                public void onFailure(final Throwable exception) {
                    workflow.fireStepErrorEvent(ADD_FORK_REMOTE, exception.getMessage());
                }

                @Override
                public void onSuccess(final VcsHostingService vcsHostingService) {
                    final String remoteUrl =
                            vcsHostingService.makeSSHRemoteUrl(context.getHostUserLogin(), context.getForkedRepositoryName());
                    checkRemotePresent(workflow, remoteUrl);
                }
            });

        } else {
            context.setForkedRemoteName(ORIGIN_REMOTE_NAME);
            proceed(workflow);
        }
    }

    private void checkRemotePresent(final ContributorWorkflow workflow, final String remoteUrl) {
        final Context context = workflow.getContext();

        vcsServiceProvider.getVcsService()
                          .listRemotes(workflow.getContext().getProject(), new AsyncCallback<List<Remote>>() {
                              @Override
                              public void onSuccess(final List<Remote> result) {
                                  for (final Remote remote : result) {
                                      if (FORK_REMOTE_NAME.equals(remote.getName())) {
                                          context.setForkedRemoteName(FORK_REMOTE_NAME);
                                          if (remoteUrl.equals(remote.getUrl())) {
                                              // all is correct, continue
                                              proceed(workflow);

                                          } else {
                                              replaceRemote(workflow, remoteUrl);
                                          }
                                          // leave the method, do not go to addRemote(...)
                                          return;
                                      }
                                  }
                                  addRemote(workflow, remoteUrl);
                              }

                              @Override
                              public void onFailure(final Throwable exception) {
                                  workflow.fireStepErrorEvent(ADD_FORK_REMOTE, messages.stepAddForkRemoteErrorCheckRemote());
                              }
                          });
    }

    /**
     * Add the remote to the project.
     *
     * @param workflow
     *         the {@link com.codenvy.plugin.contribution.client.steps.ContributorWorkflow}.
     * @param remoteUrl
     *         the url of the remote
     */
    private void addRemote(final ContributorWorkflow workflow, final String remoteUrl) {
        final Context context = workflow.getContext();

        vcsServiceProvider.getVcsService()
                          .addRemote(context.getProject(), FORK_REMOTE_NAME, remoteUrl, new AsyncCallback<Void>() {
                              @Override
                              public void onSuccess(final Void notUsed) {
                                  context.setForkedRemoteName(FORK_REMOTE_NAME);

                                  proceed(workflow);
                              }

                              @Override
                              public void onFailure(final Throwable exception) {
                                  workflow.fireStepErrorEvent(ADD_FORK_REMOTE, messages.stepAddForkRemoteErrorAddFork());
                              }
                          });
    }

    /**
     * Removes the fork remote from the project before adding it with the correct URL.
     *
     * @param workflow
     *         the {@link com.codenvy.plugin.contribution.client.steps.ContributorWorkflow}.
     * @param remoteUrl
     *         the url of the remote
     */
    private void replaceRemote(final ContributorWorkflow workflow, final String remoteUrl) {
        final Context context = workflow.getContext();

        vcsServiceProvider.getVcsService()
                          .deleteRemote(context.getProject(), FORK_REMOTE_NAME, new AsyncCallback<Void>() {
                              @Override
                              public void onSuccess(final Void result) {
                                  addRemote(workflow, remoteUrl);
                              }

                              @Override
                              public void onFailure(final Throwable caught) {
                                  workflow.fireStepErrorEvent(ADD_FORK_REMOTE, messages.stepAddForkRemoteErrorSetForkedRepositoryRemote());
                              }
                          });
    }

    private void proceed(final ContributorWorkflow workflow) {
        workflow.fireStepDoneEvent(ADD_FORK_REMOTE);
        workflow.setStep(pushBranchOnForkStep);
        workflow.executeStep();
    }
}
