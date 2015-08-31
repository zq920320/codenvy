/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.ext.gae.client.update;

import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.ext.gae.client.create.CreateApplicationPresenter;
import com.codenvy.ide.ext.gae.client.service.GAEServiceClient;
import com.codenvy.ide.ext.gae.client.service.callbacks.FailureCallback;
import com.codenvy.ide.ext.gae.client.service.callbacks.GAERequestCallBackFactory;
import com.codenvy.ide.ext.gae.client.service.callbacks.GAERequestCallback;
import com.codenvy.ide.ext.gae.client.service.callbacks.SuccessCallback;
import com.codenvy.ide.ext.gae.shared.ApplicationInfo;
import com.google.inject.Inject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Class contains business logic related to deploying of project.
 *
 * @author Dmitry Shnurenko
 */
public class DeployAction {
    public static final String APP_NOT_EXIST          = "This application does not exist";
    public static final String DO_NOT_HAVE_PERMISSION = "You do not have permission to modify this app";

    private final GAERequestCallBackFactory  requestCallBackFactory;
    private final CreateApplicationPresenter createApplicationPresenter;
    private final GAEServiceClient           service;

    @Inject
    public DeployAction(GAERequestCallBackFactory requestCallBackFactory,
                        CreateApplicationPresenter createApplicationPresenter,
                        GAEServiceClient service) {

        this.createApplicationPresenter = createApplicationPresenter;
        this.requestCallBackFactory = requestCallBackFactory;
        this.service = service;

    }

    /**
     * Method calls special method on {@link GAEServiceClient} which deploys application to GAE.
     *
     * @param activeProject
     *         project which need deploy
     * @param binariesUrl
     *         url to binaries file. Binaries may be null, if application haven't builders
     * @param updateGAECallback
     *         callback which need to return deploy info
     */
    public void perform(@Nonnull ProjectDescriptor activeProject,
                        @Nullable String binariesUrl,
                        @Nonnull final UpdateGAECallback updateGAECallback) {

        GAERequestCallback<ApplicationInfo> callback =
                requestCallBackFactory.build(ApplicationInfo.class, new SuccessCallback<ApplicationInfo>() {
                    @Override
                    public void onSuccess(ApplicationInfo result) {
                        String webUrl = result.getWebURL();
                        String link = "<a href='" + webUrl + "' target='_blank'>" + webUrl + "</a>";

                        updateGAECallback.onSuccess(link);
                    }
                }, new FailureCallback() {
                    @Override
                    public void onFailure(@Nonnull Throwable reason) {
                        String message = reason.getMessage();

                        if (message.contains(APP_NOT_EXIST) || message.contains(DO_NOT_HAVE_PERMISSION)) {
                            createApplicationPresenter.showDialog();
                        }

                        updateGAECallback.onFailure(message);
                    }
                });

        service.update(activeProject.getPath(), binariesUrl, callback);
    }
}