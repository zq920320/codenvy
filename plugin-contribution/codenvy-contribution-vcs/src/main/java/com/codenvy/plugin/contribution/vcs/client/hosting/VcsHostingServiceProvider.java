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
package com.codenvy.plugin.contribution.vcs.client.hosting;

import com.codenvy.plugin.contribution.vcs.client.Remote;
import com.codenvy.plugin.contribution.vcs.client.VcsService;
import com.codenvy.plugin.contribution.vcs.client.VcsServiceProvider;
import com.google.gwt.user.client.rpc.AsyncCallback;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;

import javax.annotation.Nonnull;
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
    public VcsHostingServiceProvider(@Nonnull final AppContext appContext,
                                     @Nonnull final VcsServiceProvider vcsServiceProvider,
                                     @Nonnull final Set<VcsHostingService> vcsHostingServices) {
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
    public void getVcsHostingService(@Nonnull final AsyncCallback<VcsHostingService> callback) {
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
