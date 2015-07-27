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

import com.codenvy.docker.client.manage.input.InputDialogView.ActionDelegate;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import static org.eclipse.che.ide.ui.window.Window.Resources;

/**
 * The footer for input dialog {@link InputDialogViewImpl}
 *
 * @author Sergii Leschenko
 */
public class InputDialogFooter extends Composite {
    interface ConfirmWindowFooterUiBinder extends UiBinder<Widget, InputDialogFooter> {}

    @UiField
    Button saveButton;

    @UiField
    Button cancelButton;

    private ActionDelegate actionDelegate;

    @Inject
    public InputDialogFooter(Resources resources, ConfirmWindowFooterUiBinder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));

        saveButton.addStyleName(resources.centerPanelCss().blueButton());
        saveButton.getElement().setId("inputCredentials-dialog-save");
        cancelButton.addStyleName(resources.centerPanelCss().button());
        cancelButton.getElement().setId("inputCredentials-dialog-cancel");
    }

    public void setDelegate(final ActionDelegate delegate) {
        this.actionDelegate = delegate;
    }

    @UiHandler("saveButton")
    public void handleOkClick(final ClickEvent event) {
        this.actionDelegate.accepted();
    }

    @UiHandler("cancelButton")
    public void handleCancelClick(final ClickEvent event) {
        this.actionDelegate.cancelled();
    }

    Button getSaveButton() {
        return saveButton;
    }
}
