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
import com.codenvy.plugin.contribution.vcs.client.VcsService;
import com.codenvy.plugin.contribution.vcs.client.VcsServiceProvider;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingService;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingServiceProvider;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.inject.Singleton;

import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;

import javax.validation.constraints.NotNull;
import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static com.codenvy.plugin.contribution.projecttype.shared.ContributionProjectTypeConstants.CONTRIBUTE_BRANCH_VARIABLE_NAME;

/**
 * This step initialize the contribution workflow context.
 *
 * @author Kevin Pollet
 */
@Singleton
public class InitializeWorkflowContextStep implements Step {

    private static final Predicate<Remote> ORIGIN_REMOTE_FILTER = new Predicate<Remote>() {
        @Override
        public boolean apply(Remote remote) {
            return remote.getName().equals("origin");
        }
    };

    private final VcsServiceProvider        vcsServiceProvider;
    private final VcsHostingServiceProvider vcsHostingServiceProvider;
    private final AppContext                appContext;
    private final NotificationHelper        notificationHelper;
    private final ContributeMessages        messages;
    private final Step                      defineWorkBranchStep;

    @Inject
    public InitializeWorkflowContextStep(final VcsServiceProvider vcsServiceProvider,
                                         final VcsHostingServiceProvider vcsHostingServiceProvider,
                                         final AppContext appContext,
                                         final NotificationHelper notificationHelper,
                                         final ContributeMessages messages,
                                         final DefineWorkBranchStep defineWorkBranchStep) {
        this.vcsServiceProvider = vcsServiceProvider;
        this.vcsHostingServiceProvider = vcsHostingServiceProvider;
        this.appContext = appContext;
        this.notificationHelper = notificationHelper;
        this.messages = messages;
        this.defineWorkBranchStep = defineWorkBranchStep;
    }

    @Override
    public void execute(@NotNull final ContributorWorkflow workflow) {
        final CurrentProject currentProject = appContext.getCurrentProject();
        final VcsService vcsService = vcsServiceProvider.getVcsService();

        if (currentProject != null && vcsService != null) {
            final ProjectConfigDto project = currentProject.getRootProject();
            workflow.getContext().setProject(project);

            vcsHostingServiceProvider.getVcsHostingService()
                                     .then(new Operation<VcsHostingService>() {
                                         @Override
                                         public void apply(final VcsHostingService service) throws OperationException {
                                             vcsService.listRemotes(project)
                                                       .then(setUpOriginRepoOp(workflow, service))
                                                       .catchError(errorSettingUpOriginRepoOp());
                                         }
                                     })
                                     .catchError(notificationHelper.showErrorOp(getClass()));
        }
    }

    private Operation<List<Remote>> setUpOriginRepoOp(final ContributorWorkflow workflow, final VcsHostingService vcsHostingService) {
        return new Operation<List<Remote>>() {
            @Override
            public void apply(final List<Remote> remotes) throws OperationException {
                final Optional<Remote> remoteOpt = FluentIterable.from(remotes)
                                                                 .filter(ORIGIN_REMOTE_FILTER)
                                                                 .first();
                if (remoteOpt.isPresent()) {
                    final Context context = workflow.getContext();
                    final Map<String, List<String>> attributes = context.getProject().getAttributes();
                    final Remote remote = remoteOpt.get();
                    final String originUrl = remote.getUrl();

                    context.setOriginRepositoryOwner(vcsHostingService.getRepositoryOwnerFromUrl(originUrl));
                    context.setOriginRepositoryName(vcsHostingService.getRepositoryNameFromUrl(originUrl));

                    // set project information
                    if (attributes.containsKey(CONTRIBUTE_BRANCH_VARIABLE_NAME) &&
                        !attributes.get(CONTRIBUTE_BRANCH_VARIABLE_NAME).isEmpty()) {

                        final String clonedBranch = attributes.get(CONTRIBUTE_BRANCH_VARIABLE_NAME).get(0);

                        context.setClonedBranchName(clonedBranch);
                    }

                    context.setVcsHostingService(vcsHostingService);

                    workflow.executeStep(defineWorkBranchStep);
                }
            }
        };
    }

    private Operation<PromiseError> errorSettingUpOriginRepoOp() {
        return new Operation<PromiseError>() {
            @Override
            public void apply(final PromiseError error) throws OperationException {
                notificationHelper.showError(InitializeWorkflowContextStep.class,
                                             messages.contributorExtensionErrorSetupOriginRepository(error.getMessage()),
                                             error.getCause());
            }
        };
    }
}
