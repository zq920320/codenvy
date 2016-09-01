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

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

import org.eclipse.che.ide.api.ProductInfoDataProvider;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.dialogs.CancelCallback;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.commons.exception.UnauthorizedException;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.RestContext;
import org.eclipse.che.plugin.ssh.key.client.SshKeyUploader;
import org.eclipse.che.security.oauth.JsOAuthWindow;
import org.eclipse.che.security.oauth.OAuthCallback;
import org.eclipse.che.security.oauth.OAuthStatus;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;

/**
 * @author Michail Kuznyetsov
 */
public class GitLabSshKeyUploader implements SshKeyUploader, OAuthCallback {

    private final GitLabClientService        gitLabService;
    private final     String                     baseUrl;
    private final     GitLabLocalizationConstant constant;
    private final     NotificationManager        notificationManager;
    private final     ProductInfoDataProvider    productInfoDataProvider;
    private final     DialogFactory              dialogFactory;
    private final     AppContext                 appContext;

    private AsyncCallback<Void> callback;
    private String              userId;

    @Inject
    public GitLabSshKeyUploader(GitLabClientService gitLabService,
                                @RestContext String baseUrl,
                                GitLabLocalizationConstant constant,
                                NotificationManager notificationManager,
                                ProductInfoDataProvider productInfoDataProvider,
                                DialogFactory dialogFactory,
                                AppContext appContext) {
        this.gitLabService = gitLabService;
        this.baseUrl = baseUrl;
        this.constant = constant;
        this.notificationManager = notificationManager;
        this.productInfoDataProvider = productInfoDataProvider;
        this.dialogFactory = dialogFactory;
        this.appContext = appContext;
    }

    @Override
    public void uploadKey(final String userId, final AsyncCallback<Void> callback) {
        this.callback = callback;
        this.userId = userId;

        gitLabService.updatePublicKey(new AsyncRequestCallback<Void>() {
            @Override
            protected void onSuccess(Void o) {
                callback.onSuccess(o);
            }

            @Override
            protected void onFailure(Throwable e) {
                if (e instanceof UnauthorizedException) {
                    oAuthLoginStart();
                    return;
                }

                callback.onFailure(e);
            }
        });
    }

    /** Log in gitlab */
    private void oAuthLoginStart() {
        dialogFactory.createConfirmDialog(constant.authorizationDialogTitle(),
                                          constant.authorizationDialogText(productInfoDataProvider.getName()),
                                          new ConfirmCallback() {
                                              @Override
                                              public void accepted() {
                                                  showPopUp();
                                              }
                                          },
                                          new CancelCallback() {
                                              @Override
                                              public void cancelled() {
                                                  callback.onFailure(new Exception(constant.authorizationRequestRejected()));
                                              }
                                          }).show();
    }

    private void showPopUp() {
        String authUrl = baseUrl + "/oauth/authenticate?oauth_provider=gitlab.codenvy-stg.com"
                         + "&scope=api&userId=" + userId + "&redirect_after_login=" +
                         Window.Location.getProtocol() + "//" + Window.Location.getHost() + "/ws/" + appContext.getWorkspace()
                                                                                                               .getConfig()
                                                                                                               .getName();
        JsOAuthWindow authWindow = new JsOAuthWindow(authUrl, "error.url", 500, 980, this);
        authWindow.loginWithOAuth();
    }

    /** {@inheritDoc} */
    @Override
    public void onAuthenticated(OAuthStatus authStatus) {
        if (OAuthStatus.LOGGED_IN.equals(authStatus)) {
            uploadKey(userId, callback);
        } else {
            notificationManager.notify(constant.authorizationFailed(), StatusNotification.Status.FAIL, FLOAT_MODE);
            callback.onFailure(new Exception(constant.authorizationFailed()));
        }
    }
}
