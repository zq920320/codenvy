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
package com.codenvy.plugin.contribution.client.parts.contribute;

import com.codenvy.plugin.contribution.client.ContributeMessages;
import com.codenvy.plugin.contribution.client.steps.CommitWorkingTreeStep;
import com.codenvy.plugin.contribution.client.steps.Context;
import com.codenvy.plugin.contribution.client.steps.ContributorWorkflow;
import com.codenvy.plugin.contribution.client.steps.Step;
import com.codenvy.plugin.contribution.client.steps.events.ContextPropertyChangeEvent;
import com.codenvy.plugin.contribution.client.steps.events.ContextPropertyChangeHandler;
import com.codenvy.plugin.contribution.client.steps.events.StepEvent;
import com.codenvy.plugin.contribution.client.steps.events.StepHandler;
import com.codenvy.plugin.contribution.client.utils.FactoryHelper;
import com.codenvy.plugin.contribution.client.utils.NotificationHelper;
import com.codenvy.plugin.contribution.vcs.client.Branch;
import com.codenvy.plugin.contribution.vcs.client.VcsServiceProvider;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingService;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingServiceProvider;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.factory.dto.Factory;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.ui.dialogs.CancelCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.InputCallback;
import org.eclipse.che.ide.ui.dialogs.input.InputValidator;
import org.eclipse.che.ide.util.loging.Log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.eclipse.che.ide.api.constraints.Constraints.LAST;
import static org.eclipse.che.ide.api.parts.PartStackType.TOOLING;

/**
 * Part for the contribution configuration.
 *
 * @author Kevin Pollet
 */
public class ContributePartPresenter extends BasePresenter
        implements ContributePartView.ActionDelegate, StepHandler, ContextPropertyChangeHandler {
    private final ContributePartView        view;
    private final WorkspaceAgent            workspaceAgent;
    private final ContributeMessages        messages;
    private final ContributorWorkflow       workflow;
    private final VcsHostingServiceProvider vcsHostingServiceProvider;
    private final Step                      commitWorkingTreeStep;
    private final AppContext                appContext;
    private final VcsServiceProvider        vcsServiceProvider;
    private final NotificationHelper        notificationHelper;
    private final DialogFactory             dialogFactory;
    private       boolean                   updateMode;

    @Inject
    public ContributePartPresenter(@Nonnull final ContributePartView view,
                                   @Nonnull final ContributeMessages messages,
                                   @Nonnull final WorkspaceAgent workspaceAgent,
                                   @Nonnull final EventBus eventBus,
                                   @Nonnull final ContributorWorkflow workflow,
                                   @Nonnull final VcsHostingServiceProvider vcsHostingServiceProvider,
                                   @Nonnull final CommitWorkingTreeStep commitWorkingTreeStep,
                                   @Nonnull final AppContext appContext,
                                   @Nonnull final VcsServiceProvider vcsServiceProvider,
                                   @Nonnull final NotificationHelper notificationHelper,
                                   @Nonnull final DialogFactory dialogFactory) {
        this.view = view;
        this.workspaceAgent = workspaceAgent;
        this.workflow = workflow;
        this.vcsHostingServiceProvider = vcsHostingServiceProvider;
        this.messages = messages;
        this.commitWorkingTreeStep = commitWorkingTreeStep;
        this.appContext = appContext;
        this.vcsServiceProvider = vcsServiceProvider;
        this.notificationHelper = notificationHelper;
        this.dialogFactory = dialogFactory;
        this.updateMode = false;

        this.view.setDelegate(this);
        eventBus.addHandler(StepEvent.TYPE, this);
        eventBus.addHandler(ContextPropertyChangeEvent.TYPE, this);
    }

    public void open() {
        view.setRepositoryUrl("");
        view.setClonedBranch("");
        view.setContributionBranchName("");
        view.setContributionBranchNameEnabled(true);
        view.setContributionBranchNameList(Collections.<String>emptyList());
        view.setContributionTitle("");
        view.setContributionTitleEnabled(true);
        view.setContributionComment("");
        view.setContributionCommentEnabled(true);
        view.setContributeButtonText(messages.contributePartConfigureContributionSectionButtonContributeText());
        view.hideStatusSection();
        view.hideNewContributionSection();

        updateMode = false;
        updateControls();

        workspaceAgent.openPart(ContributePartPresenter.this, TOOLING, LAST);
    }

    public void remove() {
        workspaceAgent.removePart(ContributePartPresenter.this);
    }

    @Override
    public void onContribute() {
        if (updateMode) {
            view.showStatusSection(messages.contributePartStatusSectionNewCommitsPushedStepLabel(),
                                   messages.contributePartStatusSectionPullRequestUpdatedStepLabel());

        } else {
            view.showStatusSection(messages.contributePartStatusSectionForkCreatedStepLabel(),
                                   messages.contributePartStatusSectionBranchPushedStepLabel(),
                                   messages.contributePartStatusSectionPullRequestIssuedStepLabel());
        }

        view.hideStatusSectionMessage();
        view.setContributeButtonEnabled(false);
        view.setContributionProgressState(true);

        // resume the contribution workflow and execute the commit tree step
        workflow.getConfiguration()
                .withContributionBranchName(view.getContributionBranchName())
                .withContributionComment(view.getContributionComment())
                .withContributionTitle(view.getContributionTitle());

        workflow.setStep(commitWorkingTreeStep);
        workflow.executeStep();
    }


    @Override
    public void onOpenPullRequestOnVcsHost() {
        final Context context = workflow.getContext();

        vcsHostingServiceProvider.getVcsHostingService(new AsyncCallback<VcsHostingService>() {
            @Override
            public void onFailure(final Throwable exception) {
                notificationHelper.showError(ContributePartPresenter.class, exception);
            }

            @Override
            public void onSuccess(final VcsHostingService vcsHostingService) {
                Window.open(vcsHostingService.makePullRequestUrl(context.getUpstreamRepositoryOwner(), context.getUpstreamRepositoryName(),
                                                                 context.getPullRequestIssueNumber()), "", "");
            }
        });
    }

    @Override
    public void onNewContribution() {
        final Factory factory = appContext.getFactory();
        if (factory != null) {
            final String createProjectUrl = FactoryHelper.getCreateProjectRelUrl(factory);
            if (createProjectUrl != null) {
                Window.open(createProjectUrl, "", "");
            }

        } else {
            final Context context = workflow.getContext();
            vcsServiceProvider.getVcsService().checkoutBranch(context.getProject(), context.getClonedBranchName(),
                                                              false, new AsyncCallback<String>() {
                        @Override
                        public void onFailure(final Throwable exception) {
                            notificationHelper.showError(ContributePartPresenter.class, exception);
                        }

                        @Override
                        public void onSuccess(final String branchName) {
                            view.setContributionBranchName(context.getClonedBranchName());
                            view.setContributionBranchNameEnabled(true);
                            view.setContributionTitle("");
                            view.setContributionTitleEnabled(true);
                            view.setContributionComment("");
                            view.setContributionCommentEnabled(true);
                            view.setContributeButtonText(messages.contributePartConfigureContributionSectionButtonContributeText());
                            view.hideStatusSection();
                            view.hideStatusSectionMessage();
                            view.hideNewContributionSection();

                            updateMode = false;
                            updateControls();

                            notificationHelper
                                    .showInfo(messages.contributePartNewContributionBranchClonedCheckedOut(context.getClonedBranchName()));
                        }
                    });
        }
    }

    @Override
    public void onRefreshContributionBranchNameList() {
        refreshContributionBranchNameList();
    }

    @Override
    public void onCreateNewBranch() {
        dialogFactory.createInputDialog(messages.contributePartConfigureContributionDialogNewBranchTitle(),
                                        messages.contributePartConfigureContributionDialogNewBranchLabel(),
                                        new CreateNewBranchCallback(),
                                        new CancelNewBranchCallback())
                     .withValidator(new BranchNameValidator())
                     .show();
    }

    @Override
    public void updateControls() {
        final String contributionTitle = view.getContributionTitle();

        boolean isValid = true;
        view.showContributionTitleError(false);

        if (contributionTitle == null || contributionTitle.trim().isEmpty()) {
            view.showContributionTitleError(true);
            isValid = false;
        }

        view.setContributeButtonEnabled(isValid);
    }

    @Override
    public void go(final AcceptsOneWidget container) {
        container.setWidget(view.asWidget());
    }

    @Nonnull
    @Override
    public String getTitle() {
        return messages.contributePartTitle();
    }

    @Nullable
    @Override
    public ImageResource getTitleImage() {
        return null;
    }

    @Nullable
    @Override
    public String getTitleToolTip() {
        return null;
    }

    @Override
    public int getSize() {
        return 350;
    }

    @Override
    public void onStepDone(@Nonnull final StepEvent event) {
        switch (event.getStep()) {
            case CREATE_FORK: {
                if (!updateMode) {
                    view.setCurrentStatusStepStatus(true);
                }
            }
            break;

            case PUSH_BRANCH_ON_FORK: {
                view.setCurrentStatusStepStatus(true);
            }
            break;

            case ISSUE_PULL_REQUEST: {
                view.setCurrentStatusStepStatus(true);
                view.setContributeButtonEnabled(true);
                view.setContributionProgressState(false);
                view.showStatusSectionMessage(updateMode ? messages.contributePartStatusSectionContributionUpdatedMessage()
                                                         : messages.contributePartStatusSectionContributionCreatedMessage(), false);
                view.setContributionBranchNameEnabled(false);
                view.setContributionTitleEnabled(false);
                view.setContributionCommentEnabled(false);
                view.setContributeButtonText(messages.contributePartConfigureContributionSectionButtonContributeUpdateText());
                updateMode = true;

                vcsHostingServiceProvider.getVcsHostingService(new AsyncCallback<VcsHostingService>() {
                    @Override
                    public void onFailure(final Throwable exception) {
                        notificationHelper.showError(ContributePartPresenter.class, exception);
                    }

                    @Override
                    public void onSuccess(final VcsHostingService vcsHostingService) {
                        view.showNewContributionSection(vcsHostingService.getName());
                    }
                });
            }
            break;

            default:
                break;
        }
    }

    @Override
    public void onStepError(@Nonnull final StepEvent event) {
        switch (event.getStep()) {
            case COMMIT_WORKING_TREE: {
                view.hideStatusSection();
            }
            break;

            case CREATE_FORK: {
                if (!updateMode) {
                    view.setCurrentStatusStepStatus(false);
                    view.showStatusSectionMessage(event.getMessage(), true);
                }
            }
            break;

            case PUSH_BRANCH_ON_FORK:
            case ISSUE_PULL_REQUEST: {
                view.setCurrentStatusStepStatus(false);
                view.showStatusSectionMessage(event.getMessage(), true);
            }
            break;

            default:
                Log.error(ContributePartPresenter.class, "Step error:", event.getMessage());
                break;
        }

        view.setContributeButtonEnabled(true);
        view.setContributionProgressState(false);
    }

    @Override
    public void onContextPropertyChange(final ContextPropertyChangeEvent event) {
        final Context context = event.getContext();

        switch (event.getContextProperty()) {
            case CLONED_BRANCH_NAME: {
                view.setClonedBranch(context.getClonedBranchName());
            }
            break;

            case WORK_BRANCH_NAME: {
                refreshContributionBranchNameList(new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(final Throwable exception) {
                        notificationHelper.showError(ContributePartPresenter.class, exception);
                    }

                    @Override
                    public void onSuccess(final Void notUsed) {
                        view.setContributionBranchName(context.getWorkBranchName());
                    }
                });
            }
            break;

            case ORIGIN_REPOSITORY_NAME:
            case ORIGIN_REPOSITORY_OWNER: {
                final String originRepositoryName = context.getOriginRepositoryName();
                final String originRepositoryOwner = context.getOriginRepositoryOwner();

                if (originRepositoryName != null && originRepositoryOwner != null) {
                    vcsHostingServiceProvider.getVcsHostingService(new AsyncCallback<VcsHostingService>() {
                        @Override
                        public void onFailure(final Throwable exception) {
                            notificationHelper.showError(ContributePartPresenter.class, exception);
                        }

                        @Override
                        public void onSuccess(final VcsHostingService vcsHostingService) {
                            view.setRepositoryUrl(vcsHostingService.makeHttpRemoteUrl(originRepositoryOwner, originRepositoryName));
                        }
                    });
                }
            }
            break;

            case PROJECT: {
                refreshContributionBranchNameList();
            }
            break;

            default:
                break;
        }
    }

    private void refreshContributionBranchNameList() {
        refreshContributionBranchNameList(new AsyncCallback<Void>() {
            @Override
            public void onFailure(final Throwable exception) {
                notificationHelper.showError(ContributePartPresenter.class, exception);
            }

            @Override
            public void onSuccess(final Void notUsed) {
                // nothing to do branch name list is refreshed
            }
        });
    }

    private void refreshContributionBranchNameList(final AsyncCallback<Void> callback) {
        vcsServiceProvider.getVcsService().listLocalBranches(workflow.getContext().getProject(), new AsyncCallback<List<Branch>>() {
            @Override
            public void onFailure(final Throwable exception) {
                callback.onFailure(exception);
            }

            @Override
            public void onSuccess(final List<Branch> branches) {
                final List<String> branchNames = new ArrayList<>();
                for (final Branch oneBranch : branches) {
                    branchNames.add(oneBranch.getDisplayName());
                }

                view.setContributionBranchNameList(branchNames);
                callback.onSuccess(null);
            }
        });
    }

    private static class BranchNameValidator implements InputValidator {
        private static final Violation ERROR_WITH_NO_MESSAGE = new Violation() {
            @Nullable
            @Override
            public String getMessage() {
                return "";
            }

            @Nullable
            @Override
            public String getCorrectedValue() {
                return null;
            }
        };

        @Nullable
        @Override
        public Violation validate(final String branchName) {
            return branchName.matches("[0-9A-Za-z-]+") ? null : ERROR_WITH_NO_MESSAGE;
        }
    }

    private class CreateNewBranchCallback implements InputCallback {
        @Override
        public void accepted(final String branchName) {
            final Context context = workflow.getContext();

            vcsServiceProvider.getVcsService().isLocalBranchWithName(context.getProject(), branchName, new AsyncCallback<Boolean>() {
                @Override
                public void onFailure(final Throwable exception) {
                    notificationHelper.showError(ContributePartPresenter.class, exception);
                }

                @Override
                public void onSuccess(final Boolean branchExists) {
                    if (branchExists) {
                        view.setContributionBranchName(branchName);
                        notificationHelper.showError(ContributePartPresenter.class,
                                                     messages.contributePartConfigureContributionDialogNewBranchErrorBranchExists(
                                                             branchName));

                    } else {
                        vcsServiceProvider.getVcsService().checkoutBranch(context.getProject(), branchName, true,
                                                                          new AsyncCallback<String>() {
                                                                              @Override
                                                                              public void onFailure(final Throwable exception) {

                                                                              }

                                                                              @Override
                                                                              public void onSuccess(final String notUsed) {
                                                                                  refreshContributionBranchNameList(
                                                                                          new AsyncCallback<Void>() {
                                                                                              @Override
                                                                                              public void onFailure(
                                                                                                      final Throwable exception) {
                                                                                                  notificationHelper.showError(
                                                                                                          ContributePartPresenter.class,
                                                                                                          exception);
                                                                                              }

                                                                                              @Override
                                                                                              public void onSuccess(final Void notUsed) {
                                                                                                  view.setContributionBranchName(
                                                                                                          branchName);
                                                                                              }
                                                                                          });
                                                                              }
                                                                          });
                    }
                }
            });
        }
    }

    private class CancelNewBranchCallback implements CancelCallback {
        @Override
        public void cancelled() {
            view.setContributionBranchName(workflow.getContext().getWorkBranchName());
        }
    }
}
