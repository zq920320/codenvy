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
package com.codenvy.ide.ext.gae.client.wizard.yaml;

import com.codenvy.ide.ext.gae.client.GAELocalizationConstant;
import com.codenvy.ide.ext.gae.client.GAEResources;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import javax.annotation.Nonnull;

/**
 * View representation of GAE wizard.
 *
 * @author Valeriy Svydenko
 */
public class GAEYamlPageViewImpl extends Composite implements GAEYamlPageView {

    interface GAEPythonProjectImplUiBinder extends UiBinder<Widget, GAEYamlPageViewImpl> {
    }

    private static final GAEPythonProjectImplUiBinder UI_BINDER = GWT.create(GAEPythonProjectImplUiBinder.class);

    @UiField(provided = true)
    final GAELocalizationConstant locale;
    @UiField(provided = true)
    final GAEResources            resource;
    @UiField
    TextBox gaeAppId;

    private ActionDelegate delegate;

    @Inject
    public GAEYamlPageViewImpl(GAELocalizationConstant locale, GAEResources resource) {
        this.locale = locale;
        this.resource = resource;
        initWidget(UI_BINDER.createAndBindUi(this));
    }

    /** {@inheritDoc} */
    @Nonnull
    @Override
    public String getGaeAppIdValue() {
        return gaeAppId.getText();
    }

    /** {@inheritDoc} */
    @Override
    public void setGaeApplicationId(@Nonnull String applicationId) {
        gaeAppId.setText(applicationId);
    }

    /** {@inheritDoc} */
    @Override
    public void showApplicationIdInCorrectIndicator(boolean isCorrect) {
        if (isCorrect) {
            gaeAppId.addStyleName(resource.gaeCSS().wizardIncorrectValueBorder());
        } else {
            gaeAppId.removeStyleName(resource.gaeCSS().wizardIncorrectValueBorder());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setFocusToApplicationIdField() {
        gaeAppId.setFocus(true);
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(@Nonnull ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @UiHandler("gaeAppId")
    public void onApplicationIdChanged(@SuppressWarnings("UnusedParameters") KeyUpEvent event) {
        delegate.onValueChanged();
    }

}