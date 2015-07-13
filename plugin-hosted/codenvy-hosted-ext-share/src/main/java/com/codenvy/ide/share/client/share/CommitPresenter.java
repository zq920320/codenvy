/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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
package com.codenvy.ide.share.client.share;

import com.codenvy.ide.share.client.ShareLocalizationConstant;
import com.codenvy.ide.factory.client.utils.SyncGitServiceClient;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.inject.Inject;

import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;

import java.util.Date;

import static com.codenvy.ide.share.client.share.CommitPresenter.CommitActionHandler.CommitAction.CONTINUE;
import static com.codenvy.ide.share.client.share.CommitPresenter.CommitActionHandler.CommitAction.OK;
import static com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat.ISO_8601;
import static org.eclipse.che.ide.ext.git.client.GitRepositoryInitializer.isGitRepository;

/**
 * This presenter provides base functionality to commit project changes or not before cloning or generating a factory url.
 *
 * @author Kevin Pollet
 */
public class CommitPresenter implements CommitView.ActionDelegate {
    private final CommitView                view;
    private final ShareLocalizationConstant locale;
    private final AppContext                context;
    private final SyncGitServiceClient      syncGitServiceClient;
    private       CommitActionHandler       handler;

    @Inject
    public CommitPresenter(CommitView view,
                           ShareLocalizationConstant locale,
                           AppContext context,
                           SyncGitServiceClient syncGitServiceClient) {
        this.view = view;
        this.locale = locale;
        this.context = context;
        this.syncGitServiceClient = syncGitServiceClient;

        this.view.setDelegate(this);
    }

    /**
     * Opens the {@link CommitView} with a default commit description.
     */
    public void showView() {
        final DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat(ISO_8601);
        final String commitDescription = locale.commitDialogDescriptionDefaultMessage(dateTimeFormat.format(new Date()));

        view.show(commitDescription);
    }

    /**
     * Sets the {@link CommitPresenter.CommitActionHandler} called after the ok or continue action is
     * executed.
     *
     * @param handler
     *         the handler to set.
     */
    public void setCommitActionHandler(CommitActionHandler handler) {
        this.handler = handler;
    }

    /**
     * Returns if the current project has uncommitted changes.
     *
     * @return {@code true} if the current project has uncommitted changes, {@code false} otherwise.
     */
    public boolean hasUncommittedChanges() {
        final CurrentProject project = context.getCurrentProject();
        return project != null
               && isGitRepository(project.getRootProject())
               && !syncGitServiceClient.status(project.getRootProject()).isClean();
    }

    @Override
    public void onOk() {
        final CurrentProject project = context.getCurrentProject();
        if (project != null) {
            final ProjectDescriptor projectDescriptor = project.getRootProject();
            syncGitServiceClient.add(projectDescriptor, false, null);
            syncGitServiceClient.commit(projectDescriptor, view.getCommitDescription(), true, false);
            view.close();

            if (handler != null) {
                handler.onCommitAction(OK);
            }
        }
    }

    @Override
    public void onContinue() {
        view.close();

        if (handler != null) {
            handler.onCommitAction(CONTINUE);
        }
    }

    @Override
    public void onCommitDescriptionChanged() {
        view.setOkButtonEnabled(!view.getCommitDescription().isEmpty());
    }

    public interface CommitActionHandler {
        enum CommitAction {
            OK,
            CONTINUE
        }

        /**
         * Called when a commit actions is done on the commit view.
         *
         * @param action
         *         the action.
         */
        void onCommitAction(CommitAction action);
    }
}
