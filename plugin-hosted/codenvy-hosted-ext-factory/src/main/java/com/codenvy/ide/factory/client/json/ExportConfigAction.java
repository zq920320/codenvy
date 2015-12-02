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
package com.codenvy.ide.factory.client.json;

import com.codenvy.ide.factory.client.FactoryLocalizationConstant;
import com.codenvy.ide.factory.client.FactoryResources;
import com.codenvy.ide.factory.client.utils.SyncGitServiceClient;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;

import org.eclipse.che.api.analytics.client.logger.AnalyticsEventLogger;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.ext.git.client.GitRepositoryInitializer;
import org.eclipse.che.ide.ui.dialogs.CancelCallback;
import org.eclipse.che.ide.ui.dialogs.ConfirmCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.util.Config;
import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Singleton;

/**
 * @author Sergii Leschenko
 */
@Singleton
public class ExportConfigAction extends Action {
    private final FactoryLocalizationConstant locale;
    private final AppContext                  appContext;
    private final AnalyticsEventLogger        eventLogger;
    private final SyncGitServiceClient        gitService;
    private final DialogFactory               dialogFactory;

    @Inject
    public ExportConfigAction(FactoryLocalizationConstant locale,
                              AppContext appContext,
                              AnalyticsEventLogger eventLogger,
                              FactoryResources resources,
                              SyncGitServiceClient gitService,
                              DialogFactory dialogFactory) {
        super(locale.exportConfigName(), locale.exportConfigDescription(), null, resources.exportConfig());
        this.locale = locale;
        this.appContext = appContext;
        this.eventLogger = eventLogger;
        this.gitService = gitService;
        this.dialogFactory = dialogFactory;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);

        //Ensures that project is open
        if (appContext.getCurrentProject() == null || appContext.getCurrentProject().getRootProject() == null) {
            Log.error(getClass(), locale.exportConfigErrorMessage());
            return;
        }

        final ProjectConfigDto currentProject = appContext.getCurrentProject().getRootProject();

        if (!GitRepositoryInitializer.isGitRepository(currentProject) &&
            !currentProject.getAttributes().containsKey("svn.repository.url")) {
            dialogFactory.createConfirmDialog(locale.exportConfigDialogNotUnderVcsTitle(),
                                              locale.exportConfigDialogNotUnderVcsText(),
                                              new ConfirmCallback() {
                                                  @Override
                                                  public void accepted() {
                                                      gitService.init(currentProject);
                                                      exportConfig(currentProject.getName());
                                                  }
                                              },
                                              new CancelCallback() {
                                                  @Override
                                                  public void cancelled() {
                                                      //Do nothing
                                                  }
                                              }).show();

        } else {
            exportConfig(currentProject.getName());
        }
    }

    private void exportConfig(String projectName) {
        final String currentWorkspaceId = Config.getWorkspaceId();
        String downloadConfigLink = "/api/factory/" + currentWorkspaceId + "/" + projectName;
        Window.open(downloadConfigLink, "Download Config", "");
    }

    /** {@inheritDoc} */
    @Override
    public void update(ActionEvent event) {
        CurrentProject activeProject = appContext.getCurrentProject();
        event.getPresentation().setEnabled(activeProject != null);
    }
}
