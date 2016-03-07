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
import com.google.inject.Singleton;

import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.JsPromiseError;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Provider for the {@link com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingService}.
 *
 * @author Kevin Pollet
 * @author Yevhenii Voevodin
 */
@Singleton
public class VcsHostingServiceProvider {
    private static final String ORIGIN_REMOTE_NAME = "origin";

    private final AppContext             appContext;
    private final VcsServiceProvider     vcsServiceProvider;
    private final Set<VcsHostingService> vcsHostingServices;

    @Inject
    public VcsHostingServiceProvider(final AppContext appContext,
                                     final VcsServiceProvider vcsServiceProvider,
                                     final Set<VcsHostingService> vcsHostingServices) {
        this.appContext = appContext;
        this.vcsServiceProvider = vcsServiceProvider;
        this.vcsHostingServices = vcsHostingServices;
    }

    /**
     * Returns the dedicated {@link VcsHostingService} implementation for the {@link #ORIGIN_REMOTE_NAME origin} remote.
     */
    public Promise<VcsHostingService> getVcsHostingService() {
        final CurrentProject currentProject = appContext.getCurrentProject();
        final VcsService vcsService = vcsServiceProvider.getVcsService();
        if (currentProject == null || vcsService == null) {
            return Promises.reject(JsPromiseError.create(new NoVcsHostingServiceImplementationException()));
        }
        return vcsService.listRemotes(currentProject.getRootProject())
                         .then(new Function<List<Remote>, VcsHostingService>() {
                             @Override
                             public VcsHostingService apply(List<Remote> remotes) throws FunctionException {
                                 for (Remote remote : remotes) {
                                     if (ORIGIN_REMOTE_NAME.equals(remote.getName())) {
                                         for (final VcsHostingService hostingService : vcsHostingServices) {
                                             if (hostingService.isHostRemoteUrl(remote.getUrl())) {
                                                 return hostingService.init(remote.getUrl());
                                             }
                                         }
                                     }
                                 }
                                 throw new FunctionException(new NoVcsHostingServiceImplementationException());
                             }
                         });
    }

    /**
     * Returns the {@link com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingService} implementation corresponding to the current
     * project origin url.
     *
     * @param callback
     *         the callback called when the {@link com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingService} implementation is
     *         retrieved.
     * @deprecated use {@link #getVcsHostingService()} instead
     */
    @Deprecated
    public void getVcsHostingService(final AsyncCallback<VcsHostingService> callback) {
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
                                    callback.onSuccess(oneVcsHostingService.init(oneRemote.getUrl()));
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
