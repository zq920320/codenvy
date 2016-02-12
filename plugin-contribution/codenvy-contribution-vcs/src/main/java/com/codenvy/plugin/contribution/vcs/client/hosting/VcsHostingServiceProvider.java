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
package com.codenvy.plugin.contribution.vcs.client.hosting;

import com.codenvy.plugin.contribution.vcs.client.VcsService;
import com.codenvy.plugin.contribution.vcs.client.VcsServiceProvider;
import com.google.gwt.user.client.rpc.AsyncCallback;

import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;

import javax.validation.constraints.NotNull;
import javax.inject.Inject;
import java.util.List;
import java.util.Set;

/**
 * Provider for the {@link com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingService}.
 *
 * @author Kevin Pollet
 */
public class VcsHostingServiceProvider {
    private static final String ORIGIN_REMOTE_NAME = "origin";

    private final AppContext             appContext;
    private final VcsServiceProvider     vcsServiceProvider;
    private final Set<VcsHostingService> vcsHostingServices;


    @Inject
    public VcsHostingServiceProvider(@NotNull final AppContext appContext,
                                     @NotNull final VcsServiceProvider vcsServiceProvider,
                                     @NotNull final Set<VcsHostingService> vcsHostingServices) {
        this.appContext = appContext;
        this.vcsServiceProvider = vcsServiceProvider;
        this.vcsHostingServices = vcsHostingServices;
    }

    /**
     * Returns the {@link com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingService} implementation corresponding to the current
     * project origin url.
     *
     * @param callback
     *         the callback called when the {@link com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingService} implementation is
     *         retrieved.
     */
    public void getVcsHostingService(@NotNull final AsyncCallback<VcsHostingService> callback) {
        final CurrentProject currentProject = appContext.getCurrentProject();
        final VcsService vcsService = vcsServiceProvider.getVcsService();

        if (currentProject != null && vcsService != null) {
            vcsService.listRemotes(currentProject.getRootProject(), new AsyncCallback<List<Remote>>() {
                @Override
                public void onFailure(final Throwable exception) {
                    callback.onFailure(exception);
                }

                @Override
                public void onSuccess(final List<Remote> remotes) {
                    for (final Remote oneRemote : remotes) {
                        if (ORIGIN_REMOTE_NAME.equals(oneRemote.getName())) {

                            for (final VcsHostingService oneVcsHostingService : vcsHostingServices) {
                                if (oneVcsHostingService.isHostRemoteUrl(oneRemote.getUrl())) {
                                    callback.onSuccess(oneVcsHostingService);
                                    return;
                                }
                            }

                            break;
                        }
                    }

                    callback.onFailure(new NoVcsHostingServiceImplementationException());
                }
            });

        } else {
            callback.onFailure(new NoVcsHostingServiceImplementationException());
        }
    }
}
