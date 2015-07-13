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
package com.codenvy.api.resources.client.preferences;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

import org.eclipse.che.ide.ext.runner.client.tabs.properties.panel.common.Shutdown;

import javax.annotation.Nonnull;

/**
 * @author Ann Shumilova
 */
public class RunnerPreferencesViewImpl implements RunnerPreferencesView {

    private static RunnerPreferencesViewImplUiBinder uiBinder = GWT.create(RunnerPreferencesViewImplUiBinder.class);
    private final FlowPanel rootElement;
    @UiField
    ListBox shutdownField;
    @UiField
    Button  setButton;
    private ActionDelegate delegate;

    public RunnerPreferencesViewImpl() {
        rootElement = uiBinder.createAndBindUi(this);

        for (Enum item : Shutdown.values()) {
            shutdownField.addItem(item.toString());
        }
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public Widget asWidget() {
        return rootElement;
    }

    @UiHandler("shutdownField")
    void handleSelectionChanged(ChangeEvent event) {
        delegate.onValueChanged();
    }

    @UiHandler("setButton")
    void onSetClicked(ClickEvent event) {
        delegate.onSetShutdownClicked();
    }

    @Override
    public void selectShutdown(@Nonnull Shutdown shutdown) {
        this.shutdownField.setItemSelected(shutdown.ordinal(), true);
    }

    /** {@inheritDoc} */
    @Nonnull
    @Override
    public Shutdown getShutdown() {
        String value = shutdownField.getValue(shutdownField.getSelectedIndex());
        return Shutdown.detect(value);
    }

    @Override
    public void enableSetButton(boolean isEnabled) {
        setButton.setEnabled(isEnabled);
    }

    interface RunnerPreferencesViewImplUiBinder
            extends UiBinder<FlowPanel, RunnerPreferencesViewImpl> {
    }
}
