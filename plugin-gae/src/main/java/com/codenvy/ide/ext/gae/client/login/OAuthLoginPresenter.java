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
package com.codenvy.ide.ext.gae.client.login;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.rest.RestContext;
import org.eclipse.che.security.oauth.JsOAuthWindow;
import org.eclipse.che.security.oauth.OAuthCallback;
import org.eclipse.che.security.oauth.OAuthStatus;

import com.codenvy.ide.ext.gae.client.GAELocalizationConstant;
import com.codenvy.ide.ext.gae.client.GAEResources;
import com.codenvy.ide.ext.gae.client.confirm.ConfirmView;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import static com.google.gwt.user.client.Window.Location.getHost;
import static com.google.gwt.user.client.Window.Location.getProtocol;

/**
 * The presenter that provides a business logic of Google OAuth login.
 *
 * @author Valeriy Svydenko
 */
public class OAuthLoginPresenter implements ConfirmView.ActionDelegate {

    private final GAELocalizationConstant locale;
    private final ConfirmView             view;
    private final String                  baseUrl;
    private final AppContext              appContext;
    private final GAEResources            resources;

    private LoginActionListener loginActionListener;

    @Inject
    public OAuthLoginPresenter(GAELocalizationConstant locale,
                               ConfirmView view,
                               GAEResources resources,
                               AppContext appContext,
                               @RestContext String baseUrl) {

        this.locale = locale;
        this.baseUrl = baseUrl;
        this.appContext = appContext;
        this.view = view;
        this.resources = resources;
        this.view.setDelegate(this);

        prepareView();
    }

    private void prepareView() {
        view.setActionButtonTitle(locale.loginButton());
        view.setSubtitle(locale.loginOauthSubtitle());
        view.setUserInstructions(locale.loginOauthLabel());
        view.addSubtitleStyleName(resources.gaeCSS().bigSubTitleLabel());
    }

    /** Shows the create application window if user is logged in. */
    public void showDialog() {
        view.show();
    }

    /** {@inheritDoc} */
    @Override
    public void onActionButtonClicked() {
        String authUrl = baseUrl +
                         "/oauth/authenticate?oauth_provider=google&scope=https://www.googleapis.com/auth/appengine.admin&userId=" +
                         appContext.getCurrentUser().getProfile().getUserId() +
                         "&redirect_after_login=" + getProtocol() + "//" + getHost() + "/ws/" + appContext.getWorkspace().getName();
        new JsOAuthWindow(authUrl, "err.url", 500, 450, new OAuthCallback() {
            @Override
            public void onAuthenticated(OAuthStatus authStatus) {
                view.close();
                loginActionListener.onLoginWindowHide();
            }
        }).loginWithOAuth();
    }

    /** {@inheritDoc} */
    @Override
    public void onCancelButtonClicked() {
        view.close();
    }

    /**
     * Sets OAuth login actions.
     *
     * @param loginActionListener
     *         OAuth login action
     */
    public void setLoginActionListener(LoginActionListener loginActionListener) {
        this.loginActionListener = loginActionListener;
    }

    /** Interface defines methods which calls from OAuthLogin view. These methods defines some actions when a user is log in. */
    public interface LoginActionListener {
        /** Performs any actions appropriate in response to the login window is hiding. */
        void onLoginWindowHide();
    }
}