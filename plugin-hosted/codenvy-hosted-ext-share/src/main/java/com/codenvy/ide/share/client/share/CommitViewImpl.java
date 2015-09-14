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
package com.codenvy.ide.share.client.share;

import com.codenvy.ide.share.client.ShareLocalizationConstant;
import org.eclipse.che.ide.ui.window.Window;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import javax.validation.constraints.NotNull;

/**
 * UI for {@link CommitView}.
 *
 * @author Kevin Pollet
 */
public class CommitViewImpl extends Window implements CommitView {

    @UiField(provided = true)
    ShareLocalizationConstant locale;

    @UiField
    TextArea commitDescription;

    private final Button ok;

    private ActionDelegate delegate;

    @Inject
    public CommitViewImpl(CommitViewImplUiBinder uiBinder, ShareLocalizationConstant locale) {
        this.locale = locale;

        setWidget(uiBinder.createAndBindUi(this));
        setTitle(locale.commitDialogTitle());

        ok = createButton(locale.commitDialogButtonOkText(), "commit-dialog-ok", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onOk();
            }
        });
        ok.addStyleName(resources.centerPanelCss().blueButton());

        final Button continueWithoutCommitting =
                createButton(locale.commitDialogButtonContinueText(), "commit-dialog-continue-without-committing",
                             new ClickHandler() {
                                 @Override
                                 public void onClick(ClickEvent event) {
                                     delegate.onContinue();
                                 }
                             });

        getFooter().add(ok);
        getFooter().add(continueWithoutCommitting);
    }

    @Override
    public void show(String commitDescription) {
        this.commitDescription.setText(commitDescription == null ? "" : commitDescription);
        new Timer() {
            @Override
            public void run() {
                ok.setFocus(true);
            }
        }.schedule(300);
        show();
    }

    @Override
    public void close() {
        hide();
    }

    @NotNull
    @Override
    public String getCommitDescription() {
        return commitDescription.getText();
    }

    @Override
    public void setOkButtonEnabled(boolean enabled) {
        ok.setEnabled(enabled);
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    protected void onClose() {
    }

    @SuppressWarnings("UnusedParameters")
    @UiHandler("commitDescription")
    void onCommitDescriptionChanged(KeyUpEvent event) {
        delegate.onCommitDescriptionChanged();
    }

    interface CommitViewImplUiBinder extends UiBinder<Widget, CommitViewImpl> {
    }
}
