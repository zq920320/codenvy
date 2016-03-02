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
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;

import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.Notification;

import javax.validation.constraints.NotNull;
import javax.inject.Inject;
import java.util.Date;

import static com.codenvy.plugin.contribution.projecttype.shared.ContributionProjectTypeConstants.CONTRIBUTE_VARIABLE_NAME;

/**
 * This step defines the working branch for the user contribution.
 * <ul>
 * <li>If the user comes from a contribution factory the contribution branch has to be created automatically.
 * <li>If the project is cloned from GitHub the contribution branch is the current one.
 * </ul>
 * <p>
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
    public DefineWorkBranchStep(@NotNull final ContributeMessages messages,
                                @NotNull final NotificationHelper notificationHelper,
                                @NotNull final VcsServiceProvider vcsServiceProvider,
                                @NotNull final AppContext appContext) {
        this.messages = messages;
        this.notificationHelper = notificationHelper;
        this.vcsServiceProvider = vcsServiceProvider;
        this.appContext = appContext;
    }

    @Override
    public void execute(@NotNull final ContributorWorkflow workflow) {
        final Context context = workflow.getContext();
        final ProjectConfigDto project = appContext.getCurrentProject().getRootProject();
        final VcsService vcsService = vcsServiceProvider.getVcsService();

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
