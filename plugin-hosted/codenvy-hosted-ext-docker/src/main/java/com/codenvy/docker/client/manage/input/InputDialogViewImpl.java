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

import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import org.eclipse.che.ide.ui.window.Window;

import javax.annotation.Nonnull;

/**
 * Implementation of {@link InputDialogView}
 *
 * @author Sergii Leschenko
 */
public class InputDialogViewImpl extends Window implements InputDialogView {
    interface ConfirmWindowUiBinder extends UiBinder<Widget, InputDialogViewImpl> {}

    private final InputDialogFooter footer;

    @UiField
    TextBox serverAddress;

    @UiField
    HorizontalPanel serverPanel;

    @UiField
    TextBox username;

    @UiField
    TextBox email;

    @UiField
    PasswordTextBox password;

    @UiField
    Label errorHint;

    private ActionDelegate delegate;

    @Inject
    public InputDialogViewImpl(ConfirmWindowUiBinder uiBinder, @Nonnull InputDialogFooter footer) {
        Widget widget = uiBinder.createAndBindUi(this);
        setWidget(widget);

        this.footer = footer;
        getFooter().add(this.footer);

        this.ensureDebugId("askValueDialog-window");
    }

    @Override
    public void show() {
        super.show();
        new Timer() {
            @Override
            public void run() {
                if (isVisibleServer()) {
                    serverAddress.setFocus(true);
                } else {
                    username.setFocus(true);
                }
            }
        }.schedule(300);
    }

    @Override
    public void setDelegate(final ActionDelegate delegate) {
        this.delegate = delegate;
        this.footer.setDelegate(this.delegate);
    }

    @Override
    protected void onClose() {
    }

    @Override
    protected void onEnterClicked() {
        delegate.onEnterClicked();
    }

    @Override
    public void showDialog() {
        this.show();
    }

    @Override
    public void closeDialog() {
        this.hide();
    }

    @Override
    public void showErrorHint(String text) {
        errorHint.setText(text);
        footer.getSaveButton().setEnabled(false);
    }

    @Override
    public void hideErrorHint() {
        errorHint.setText("");
        footer.getSaveButton().setEnabled(true);
    }

    @Override
    public String getEmail() {
        return email.getText();
    }

    @Override
    public String getPassword() {
        return password.getText();
    }

    @Override
    public String getServerAddress() {
        return serverAddress.getText();
    }

    @Override
    public String getUsername() {
        return username.getText();
    }

    @Override
    public void setUsername(String username) {
        this.username.setText(username);
    }

    @Override
    public void setServerAddress(String serverAddress) {
        this.serverAddress.setText(serverAddress);
    }

    @Override
    public void setEmail(String email) {
        this.email.setText(email);
    }

    @Override
    public void setPassword(String password) {
        this.password.setText(password);
    }

    @Override
    public void setReadOnlyServer() {
        this.serverAddress.setReadOnly(true);
    }

    @Override
    public void setHideServer() {
        this.serverPanel.setVisible(false);
    }

    @Override
    public boolean isVisibleServer() {
        return serverPanel.isVisible();
    }

    @UiHandler({"serverAddress", "username", "password", "email"})
    void onKeyUp(KeyUpEvent event) {
        delegate.dataChanged();
    }
}
