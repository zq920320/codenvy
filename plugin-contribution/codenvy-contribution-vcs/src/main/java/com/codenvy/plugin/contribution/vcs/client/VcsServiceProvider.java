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
package com.codenvy.plugin.contribution.vcs.client;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;

import javax.validation.constraints.NotNull;
import javax.inject.Inject;

import static org.eclipse.che.ide.ext.git.client.GitRepositoryInitializer.isGitRepository;

/**
 * Provider for the {@link com.codenvy.plugin.contribution.vcs.client.VcsService}.
 *
 * @author Kevin Pollet
 */
public class VcsServiceProvider {
    private final AppContext    appContext;
    private final GitVcsService gitVcsService;

    @Inject
    public VcsServiceProvider(@NotNull final AppContext appContext, @NotNull final GitVcsService gitVcsService) {
        this.appContext = appContext;
        this.gitVcsService = gitVcsService;
    }

    /**
     * Returns the {@link com.codenvy.plugin.contribution.vcs.client.VcsService} implementation corresponding to the current project VCS.
     *
     * @return the {@link com.codenvy.plugin.contribution.vcs.client.VcsService} implementation or {@code null} if not supported or not
     * initialized.
     */
    public VcsService getVcsService() {
        final CurrentProject currentProject = appContext.getCurrentProject();
        if (currentProject != null) {
            if (isGitRepository(currentProject.getRootProject())) {
                return gitVcsService;
            }
        }
        return null;
    }
}
