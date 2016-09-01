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
package com.codenvy.plugin.gitlab.ide;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.AsyncRequestLoader;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

/**
 * Client for accessing GitLab API
 *
 * @author Michail Kuznyetsov
 */
public class GitLabClientService {
    private static final String SSH_GEN = "/ssh/generate";
    private AsyncRequestFactory asyncRequestFactory;
    private AsyncRequestLoader     loader;
    private DtoUnmarshallerFactory dtoUnmarshallerFactory;
    private AppContext             appContext;

    @Inject
    public GitLabClientService(AsyncRequestFactory asyncRequestFactory,
                               LoaderFactory loaderFactory,
                               DtoUnmarshallerFactory dtoUnmarshallerFactory,
                               AppContext appContext) {
        this.asyncRequestFactory = asyncRequestFactory;
        this.loader = loaderFactory.newLoader();
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.appContext = appContext;
    }

    public void updatePublicKey(@NotNull AsyncRequestCallback<Void> callback) {
        String url = baseUrl() + SSH_GEN;
        asyncRequestFactory.createPostRequest(url, null).loader(loader).send(callback);
    }

    private String baseUrl() {
        return appContext.getDevMachine().getWsAgentBaseUrl() + "/gitlab";
    }

}
