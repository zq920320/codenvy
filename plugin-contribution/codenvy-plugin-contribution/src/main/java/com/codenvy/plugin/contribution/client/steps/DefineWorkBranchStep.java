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
import com.codenvy.plugin.contribution.vcs.client.VcsService;
import com.codenvy.plugin.contribution.vcs.client.VcsServiceProvider;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;

import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.Notification;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Date;

import static com.codenvy.plugin.contribution.projecttype.shared.ContributionProjectTypeConstants.CONTRIBUTE_VARIABLE_NAME;
import static org.eclipse.che.ide.api.notification.Notification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.Notification.Type.INFO;

/**
 * This step defines the working branch for the user contribution.
 * <ul>
 * <li>If the user comes from a contribution factory the contribution branch has to be created automatically.
 * <li>If the project is cloned from GitHub the contribution branch is the current one.
 * </ul>
 * <p/>
 * The next step is executed when the user click on the contribute/update
 * button. See {@link com.codenvy.plugin.contribution.client.parts.contribute.ContributePartPresenter#onContribute()}
 *
 * @author Kevin Pollet
 */
public class DefineWorkBranchStep implements Step {
    private static final String GENERATED_WORKING_BRANCH_NAME_PREFIX = "contrib-";

    private final ContributeMessages messages;
    private final NotificationHelper notificationHelper;
    private final VcsServiceProvider vcsServiceProvider;
    private final AppContext         appContext;

    @Inject
    public DefineWorkBranchStep(@Nonnull final ContributeMessages messages,
                                @Nonnull final NotificationHelper notificationHelper,
                                @Nonnull final VcsServiceProvider vcsServiceProvider,
                                @Nonnull final AppContext appContext) {
        this.messages = messages;
        this.notificationHelper = notificationHelper;
        this.vcsServiceProvider = vcsServiceProvider;
        this.appContext = appContext;
    }

    @Override
    public void execute(@Nonnull final ContributorWorkflow workflow) {
        final Context context = workflow.getContext();
        final ProjectDescriptor project = appContext.getCurrentProject().getRootProject();
        final VcsService vcsService = vcsServiceProvider.getVcsService();

        // if we come from a contribute factory we have to create the working branch
        if (project.getAttributes().containsKey(CONTRIBUTE_VARIABLE_NAME)
            && project.getAttributes().get(CONTRIBUTE_VARIABLE_NAME).contains("github")) {

            final String workingBranchName = generateWorkBranchName();
            final Notification createWorkingBranchNotification =
                    new Notification(messages.stepDefineWorkBranchCreatingWorkBranch(workingBranchName), INFO, PROGRESS);
            notificationHelper.showNotification(createWorkingBranchNotification);

            // the working branch is only created if it doesn't exist
            vcsService.isLocalBranchWithName(context.getProject(), workingBranchName, new AsyncCallback<Boolean>() {
                @Override
                public void onFailure(final Throwable exception) {
                    notificationHelper.finishNotificationWithError(DefineWorkBranchStep.class, exception, createWorkingBranchNotification);
                }

                @Override
                public void onSuccess(final Boolean branchExists) {
                    // shorthand for create + checkout new temporary working branch -> checkout -b branchName
                    vcsService.checkoutBranch(context.getProject(), workingBranchName, !branchExists, new AsyncCallback<String>() {
                        @Override
                        public void onSuccess(final String result) {
                            context.setWorkBranchName(workingBranchName);
                            notificationHelper.finishNotification(messages.stepDefineWorkBranchWorkBranchCreated(workingBranchName),
                                                                  createWorkingBranchNotification);
                        }

                        @Override
                        public void onFailure(final Throwable exception) {
                            notificationHelper
                                    .finishNotificationWithError(DefineWorkBranchStep.class, exception, createWorkingBranchNotification);
                        }
                    });
                }
            });

            // if it's a github project the working branch is the current one
        } else {
            vcsService.getBranchName(context.getProject(), new AsyncCallback<String>() {
                @Override
                public void onFailure(final Throwable exception) {
                    notificationHelper.showError(DefineWorkBranchStep.class, exception);
                }

                @Override
                public void onSuccess(final String branchName) {
                    context.setWorkBranchName(branchName);
                }
            });
        }
    }

    /**
     * Generates the work branch name used for the contribution.
     *
     * @return the work branch name, never {@code null}.
     */
    private String generateWorkBranchName() {
        final DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat("MMddyyyy");
        return GENERATED_WORKING_BRANCH_NAME_PREFIX + dateTimeFormat.format(new Date());
    }
}
