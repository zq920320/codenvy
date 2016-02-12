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
package com.codenvy.ide.factory.client.accept;

import com.codenvy.ide.factory.client.FactoryLocalizationConstant;
import com.google.gwt.user.client.Window;

import org.eclipse.che.api.user.gwt.client.UserServiceClient;
import org.eclipse.che.api.user.shared.dto.UserDescriptor;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.RestContext;
import org.eclipse.che.ide.ui.dialogs.CancelCallback;
import org.eclipse.che.ide.ui.dialogs.ConfirmCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.security.oauth.JsOAuthWindow;
import org.eclipse.che.security.oauth.OAuthCallback;
import org.eclipse.che.security.oauth.OAuthStatus;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Segii Leschenko
 */
@Singleton
public class Authenticator implements OAuthCallback {
    public interface AuthCallback {
        void onAuthenticated();

        void onError(String message);
    }

    private final FactoryLocalizationConstant localizationConstant;
    private final UserServiceClient           userServiceClient;
    private final DtoUnmarshallerFactory      dtoUnmarshallerFactory;
    private final DialogFactory               dialogFactory;
    private final String                      restContext;
    private final AppContext                  appContext;

    private AuthCallback authCallback;

    @Inject
    public Authenticator(FactoryLocalizationConstant localizationConstant,
                         UserServiceClient userServiceClient,
                         DtoUnmarshallerFactory dtoUnmarshallerFactory,
                         DialogFactory dialogFactory,
                         AppContext appContext,
                         @RestContext String restContext) {
        this.localizationConstant = localizationConstant;
        this.userServiceClient = userServiceClient;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.dialogFactory = dialogFactory;
        this.appContext = appContext;
        this.restContext = restContext;
    }

    /**
     * Authorization callback method which called when authorization via oauth finished.
     *
     * @param authStatus
     *         status of authorization, may be NOT_PERFORMED, FAILED, LOGGED_IN, LOGGED_OUT
     */
    @Override
    public void onAuthenticated(OAuthStatus authStatus) {
        if (authStatus == OAuthStatus.LOGGED_IN) {
            authCallback.onAuthenticated();
        }
    }

    public void showOAuthWindow(String gitUrl, AuthCallback authCallback) {
        matchingOAuthInformation(gitUrl, authCallback);
    }

    /**
     * Matching oauth information based on git url. When IDE need to authorize user on repository management system it analyze
     * factory vcs url value and based on it collect oauth provider name
     * and oauth scopes needed to authorize user on repository system management.
     * <p/>
     * TODO: need to improve this method to avoid hardcoded scopes and provider name
     */
    private void matchingOAuthInformation(String location, AuthCallback authCallback) {
        this.authCallback = authCallback;
        String scope;
        String providerId;
        String providerName;

        if (location.contains("github.com")) {
            scope = "user,repo,write:public_key";
            providerId = "github";
            providerName = "GitHub";
        } else if (location.contains("wso2.com")) {
            scope = "openid";
            providerId = "wso2";
            providerName = "WSO2 Cloud";
        } else if (location.contains("projectlocker.com")) {
            Log.info(getClass(), "ProjectLocker");
            scope = "read_git_repository";
            providerId = "projectlocker";
            providerName = "Project Locker";
        } else {
            authCallback.onError(localizationConstant.notSupportedAuthorize());
            return;
        }

        processCurrentLoggedInUser(providerId, providerName, scope);
    }

    /**
     * Fetch current authorized user in IDE and pass user id(e.g. user4r07wjmzu2123qio) to perform oauth authorization to continue cloning
     * private repository via https or ssh.
     *
     * @param providerId
     *         name of oauth provider to instruct auth service select proper provider to pass authorization
     * @param scope
     *         scope that need IDE to access to services after authorization
     */
    private void processCurrentLoggedInUser(final String providerId, final String providerName, final String scope) {
        userServiceClient.getCurrentUser(
                new AsyncRequestCallback<UserDescriptor>(dtoUnmarshallerFactory.newUnmarshaller(UserDescriptor.class)) {
                    @Override
                    protected void onSuccess(UserDescriptor user) {
                        askUserToAuthorize(user.getId(), providerId, providerName, scope);
                    }

                    @Override
                    protected void onFailure(Throwable exception) {
                        authCallback.onError(localizationConstant.oauthFailedToGetCurrentLoggedInUser());
                    }
                });
    }

    /**
     * Ask user to perform authorization to obtain access token to continue cloning private repository via https or uploading public
     * key part to repository management system if cloning perform via ssh.
     *
     * @param userId
     *         ID of current authorized user in IDE, e.g. user4r07wjmzu2123qio
     * @param providerId
     *         id of oauth provider to instruct auth service select proper provider to pass authorization
     * @param providerName
     *         name of oauth provider to instruct auth service select proper provider to pass authorization
     * @param scope
     *         scope that need IDE to access to services after authorization
     */
    private void askUserToAuthorize(final String userId, final String providerId, final String providerName, final String scope) {
        String question = localizationConstant.oAuthLoginPrompt(providerName);
        dialogFactory.createConfirmDialog(localizationConstant.oAuthLoginTitle(), question, new ConfirmCallback() {
            @Override
            public void accepted() {
                showPopUp(userId, providerId, scope);
            }
        }, new CancelCallback() {
            @Override
            public void cancelled() {
                authCallback.onError(localizationConstant.canceledRequiredAuthorize());
            }
        }).show();
    }

    /**
     * Show native javascript popup window to allow user pass authorization on oauth server of git repository management to continue
     * cloning private repository.
     *
     * @param userId
     *         ID of current authorized user in IDE, e.g. user4r07wjmzu2123qio
     * @param providerId
     *         id of oauth provider to instruct auth service select proper provider to pass authorization
     * @param scope
     *         scope that need IDE to access to services after authorization
     */
    private void showPopUp(String userId, String providerId, String scope) {
        String authUrl = restContext + "/oauth/authenticate?oauth_provider=" + providerId + "&scope=" + scope + "&userId=" + userId +
                         "&redirect_after_login=" + Window.Location.getProtocol() + "//" + Window.Location.getHost() + "/ws/" +
                         appContext.getWorkspace().getName();
        JsOAuthWindow authWindow = new JsOAuthWindow(authUrl, "error.url", 500, 980, this);
        authWindow.loginWithOAuth();
    }
}
