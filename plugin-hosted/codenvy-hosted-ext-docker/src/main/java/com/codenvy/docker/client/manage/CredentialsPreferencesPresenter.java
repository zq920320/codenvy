/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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
package com.codenvy.docker.client.manage;

import com.codenvy.docker.client.DockerLocalizationConstant;
import com.codenvy.docker.client.manage.input.InputDialog;
import com.codenvy.docker.client.manage.input.callback.InputCallback;
import com.codenvy.docker.dto.AuthConfig;
import com.codenvy.docker.dto.AuthConfigs;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import org.eclipse.che.api.user.gwt.client.UserProfileServiceClient;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.preferences.AbstractPreferencePagePresenter;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.StringMapUnmarshaller;
import org.eclipse.che.ide.ui.dialogs.ConfirmCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.util.Base64;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static com.codenvy.docker.client.manage.input.InputDialogPresenter.InputMode;

/**
 * Presenter for displaying and work with docker registry credentials
 *
 * @author Sergii Leschenko
 */
public class CredentialsPreferencesPresenter extends AbstractPreferencePagePresenter implements CredentialsPreferencesView.ActionDelegate {
    private static final String AUTH_PREFERENCE_NAME = "codenvy:dockerCredentials";
    private static final String DEFAULT_SERVER = "https://index.docker.io/v1/";

    private final CredentialsPreferencesView view;
    private final UserProfileServiceClient   userProfileServiceClient;
    private final DtoFactory                 dtoFactory;
    private final CredentialsDialogFactory   inputDialogFactory;
    private final AppContext                 appContext;
    private final DialogFactory              dialogFactory;
    private final DockerLocalizationConstant locale;

    @Inject
    public CredentialsPreferencesPresenter(DockerLocalizationConstant locale,
                                           CredentialsPreferencesView view,
                                           UserProfileServiceClient userProfileServiceClient,
                                           DtoFactory dtoFactory,
                                           CredentialsDialogFactory credentialsDialogFactory,
                                           AppContext appContext,
                                           DialogFactory dialogFactory) {
        super(locale.dockerPreferencesTitle(), locale.dockerPreferencesCategory(), null);
        this.view = view;
        this.view.setDelegate(this);
        this.userProfileServiceClient = userProfileServiceClient;
        this.dtoFactory = dtoFactory;
        this.inputDialogFactory = credentialsDialogFactory;
        this.appContext = appContext;
        this.dialogFactory = dialogFactory;
        this.locale = locale;
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public void storeChanges() {
    }

    @Override
    public void revertChanges() {
    }

    @Override
    public void go(AcceptsOneWidget container) {
        refreshCredentials();
        container.setWidget(view);
    }

    @Override
    public void onAddClicked() {
        inputDialogFactory.createInputDialog(InputMode.CREATE, new InputCallback() {
            @Override
            public void saved(AuthConfig authConfig) {
                addAuthConfig(authConfig);
            }
        }).show();
    }

    @Override
    public void onAddAccountClicked() {
        inputDialogFactory.createInputDialog(InputMode.CREATE_DOCKERHUB, new InputCallback() {
            @Override
            public void saved(AuthConfig authConfig) {
                addAuthConfig(authConfig);
            }
        }).show();
    }

    @Override
    public void onEditClicked(AuthConfig authConfig) {
        final InputDialog inputDialog = inputDialogFactory
                .createInputDialog(authConfig.getServeraddress().equals(DEFAULT_SERVER) ? InputMode.EDIT_DOCKERHUB : InputMode.EDIT,
                                   new InputCallback() {
            @Override
            public void saved(AuthConfig authConfig) {
                addAuthConfig(authConfig);
            }
        });
        inputDialog.setData(authConfig);
        inputDialog.show();
    }

    @Override
    public void onDeleteClicked(final AuthConfig authConfig) {
        dialogFactory.createConfirmDialog(locale.removeCredentialsConfirmTitle(),
                                          locale.removeCredentialsConfirmText(authConfig.getServeraddress()), new ConfirmCallback() {
                    @Override
                    public void accepted() {
                        removeAuthConfig(authConfig.getServeraddress());
                    }
                }, null).show();
    }

    private void refreshCredentials() {
        view.setKeys(getUserCredentials().getConfigs().values());
    }

    private void addAuthConfig(AuthConfig authConfig) {
        if (authConfig.getServeraddress().isEmpty()) {
            authConfig.setServeraddress(DEFAULT_SERVER);
        }
        AuthConfigs authConfigs = getUserCredentials();
        authConfigs.getConfigs().put(authConfig.getServeraddress(), authConfig);
        updateAuthConfigs(authConfigs);
    }

    private void removeAuthConfig(String remoteAddress) {
        AuthConfigs authConfigs = getUserCredentials();
        authConfigs.getConfigs().remove(remoteAddress);
        updateAuthConfigs(authConfigs);
    }

    private void updateAuthConfigs(AuthConfigs authConfigs) {
        HashMap<String, String> preferences = new HashMap<>();
        preferences.put(AUTH_PREFERENCE_NAME, Base64.encode(dtoFactory.toJson(authConfigs)));
        userProfileServiceClient.updatePreferences(preferences, new AsyncRequestCallback<Map<String, String>>(new StringMapUnmarshaller()) {
            @Override
            protected void onSuccess(Map<String, String> result) {
                appContext.getCurrentUser().setPreferences(result);
                refreshCredentials();
            }

            @Override
            protected void onFailure(Throwable exception) {

            }
        });
    }

    private AuthConfigs getUserCredentials() {
        AuthConfigs authConfigs;
        String authConfigsJson = appContext.getCurrentUser().getPreferences().get(AUTH_PREFERENCE_NAME);
        if (authConfigsJson != null) {
            final String decrypt = Base64.decode(authConfigsJson);
            authConfigs = dtoFactory.createDtoFromJson(decrypt, AuthConfigs.class);
        } else {
            authConfigs = dtoFactory.createDto(AuthConfigs.class);
        }
        return authConfigs;
    }
}
