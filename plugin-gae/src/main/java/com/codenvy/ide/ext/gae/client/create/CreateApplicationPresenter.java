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
package com.codenvy.ide.ext.gae.client.create;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.rest.RestContext;

import com.codenvy.ide.ext.gae.client.GAELocalizationConstant;
import com.codenvy.ide.ext.gae.client.GAEResources;
import com.codenvy.ide.ext.gae.client.confirm.ConfirmView;
import com.google.gwt.http.client.UrlBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import static com.google.gwt.user.client.Window.Location.getHost;
import static com.google.gwt.user.client.Window.Location.getProtocol;

/**
 * The presenter that provides a business logic of Google aap engine project.
 * It provides an ability to create new project.
 *
 * @author Evgen Vidolob
 * @author Valeriy Svydenko
 * @author Dmitry Shnurenko
 */
@Singleton
public class CreateApplicationPresenter implements ConfirmView.ActionDelegate {

    public static final String GOOGLE_APP_ENGINE_URL = "https://appengine.google.com/start/createapp";

    private static final String APP_ENGINE         = "appengine";
    private static final String CHANGE_APP_ID      = "change-appid";
    private static final String PROJECT_PATH_PARAM = "projectpath";

    private final String      restContext;
    private final String      wsId;
    private final ConfirmView view;
    private final AppContext  context;

    @Inject
    public CreateApplicationPresenter(@RestContext String restContext,
                                      @Named("workspaceId") String wsId,
                                      final ConfirmView view,
                                      AppContext context,
                                      GAELocalizationConstant locale,
                                      GAEResources resources) {
        this.view = view;
        this.view.setDelegate(this);

        this.context = context;
        this.restContext = restContext;
        this.wsId = wsId;

        this.view.setActionButtonTitle(locale.createApplicationButtonTitle());
        this.view.setSubtitle(locale.createApplicationSubtitle());
        this.view.setUserInstructions(locale.createApplicationInstruction());
        this.view.addSubtitleStyleName(resources.gaeCSS().bigSubTitleLabel());
    }

    /** Shows the create application window. */
    public void showDialog() {
        view.show();
    }

    /** {@inheritDoc} */
    @Override
    public void onActionButtonClicked() {
        CurrentProject currentProject = context.getCurrentProject();
        if (currentProject == null) {
            return;
        }

        UrlBuilder builder = new UrlBuilder();
        String redirectUrl = builder.setProtocol(getProtocol())
                                    .setParameter(PROJECT_PATH_PARAM, currentProject.getProjectDescription().getPath())
                                    .setHost(getHost())
                                    .setPath(restContext + '/' + APP_ENGINE + '/' + wsId + '/' + CHANGE_APP_ID)
                                    .buildString();

        String url = GOOGLE_APP_ENGINE_URL + "?redirect_url=" + redirectUrl;

        view.close();
        view.windowOpen(url);
    }

    /** {@inheritDoc} */
    @Override
    public void onCancelButtonClicked() {
        view.close();
    }

}