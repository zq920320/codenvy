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
package com.codenvy.docker.client.manage.input;

import com.codenvy.docker.client.DockerLocalizationConstant;
import com.codenvy.docker.client.manage.input.callback.InputCallback;
import com.codenvy.docker.dto.AuthConfig;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import org.eclipse.che.ide.dto.DtoFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * {@link InputDialog} implementation.
 *
 * @author Sergii Leschenko
 */
public class InputDialogPresenter implements InputDialog, InputDialogView.ActionDelegate {
    public enum InputMode {
        CREATE,
        EDIT,
        CREATE_DOCKERHUB,
        EDIT_DOCKERHUB
    }

    private final InputDialogView            view;
    private final InputCallback              inputCallback;
    private final DtoFactory                 dtoFactory;
    private final DockerLocalizationConstant locale;

    @AssistedInject
    public InputDialogPresenter(@Assisted InputMode inputMode,
                                @Nullable @Assisted InputCallback inputCallback,
                                @Nonnull InputDialogView view,
                                DtoFactory dtoFactory,
                                DockerLocalizationConstant locale) {
        this.locale = locale;
        switch (inputMode) {
            case CREATE:
                view.setTitle(locale.addPrivateRegitryTitle());
                break;
            case EDIT:
                view.setTitle(locale.editPrivateRegistryTitle());
                view.setReadOnlyServer();
                break;
            case CREATE_DOCKERHUB:
                view.setTitle(locale.addDockerhubAccountTitle());
                view.setHideServer();
                break;
            case EDIT_DOCKERHUB:
                view.setTitle(locale.editDockerhubAccountTitle());
                view.setHideServer();
                break;
        }
        this.view = view;
        this.inputCallback = inputCallback;
        this.dtoFactory = dtoFactory;
        this.view.setDelegate(this);
    }

    @Override
    public void cancelled() {
        this.view.closeDialog();
    }

    @Override
    public void accepted() {
        if (isInputValid()) {
            view.closeDialog();
            if (inputCallback != null) {
                inputCallback.saved(dtoFactory.createDto(AuthConfig.class)
                                              .withEmail(view.getEmail())
                                              .withPassword(view.getPassword())
                                              .withServeraddress(view.getServerAddress())
                                              .withUsername(view.getUsername()));
            }
        }
    }

    @Override
    public void onEnterClicked() {
        accepted();
    }

    @Override
    public void dataChanged() {
        view.hideErrorHint();
    }

    @Override
    public void show() {
        view.showDialog();
    }

    @Override
    public void setData(AuthConfig authConfig) {
        view.setUsername(authConfig.getUsername());
        view.setServerAddress(authConfig.getServeraddress());
        view.setEmail(authConfig.getEmail());
        view.setPassword(authConfig.getPassword());
    }

    private boolean isInputValid() {
        String invalidField = null;

        if (view.isVisibleServer() && view.getServerAddress().trim().isEmpty()) {
            invalidField = locale.inputCredentialsServerAddressLabel().toLowerCase();
        }

        if (invalidField == null && view.getUsername().trim().isEmpty()) {
            invalidField = locale.inputCredentialsUsernameLabel().toLowerCase();
        }

        if (invalidField == null && view.getEmail().trim().isEmpty()) {
            invalidField = locale.inputCredentialsEmailLabel().toLowerCase();
        }

        if (invalidField == null && view.getPassword().trim().isEmpty()) {
            invalidField = locale.inputCredentialsPasswordLabel().toLowerCase();
        }

        if (invalidField != null) {
            view.showErrorHint(locale.inputMissedValueOfField(invalidField));
            return false;
        } else {
            view.hideErrorHint();
            return true;
        }
    }
}
