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
package com.codenvy.ide.ext.gae.client.wizard.java;

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
 * @author Dmitry Shnurenko
 */
public class GAEJavaPageViewImpl extends Composite implements GAEJavaPageView {

    interface GAEMavenProjectImplUiBinder extends UiBinder<Widget, GAEJavaPageViewImpl> {
    }

    private static final GAEMavenProjectImplUiBinder UI_BINDER = GWT.create(GAEMavenProjectImplUiBinder.class);

    @UiField(provided = true)
    final GAELocalizationConstant locale;
    @UiField(provided = true)
    final GAEResources            resource;
    @UiField
    TextBox groupId;
    @UiField
    TextBox artifactId;
    @UiField
    TextBox version;
    @UiField
    TextBox gaeAppId;

    private ActionDelegate delegate;

    @Inject
    public GAEJavaPageViewImpl(GAELocalizationConstant locale,
                               GAEResources resource) {
        this.locale = locale;
        this.resource = resource;
        initWidget(UI_BINDER.createAndBindUi(this));
    }

    /** {@inheritDoc} */
    @Nonnull
    @Override
    public String getGroupIdValue() {
        return groupId.getText();
    }

    /** {@inheritDoc} */
    @Override
    public void setGroupIdValue(@Nonnull String groupIdValue) {
        groupId.setText(groupIdValue);
    }

    /** {@inheritDoc} */
    @Nonnull
    @Override
    public String getArtifactIdValue() {
        return artifactId.getText();
    }

    /** {@inheritDoc} */
    @Override
    public void setArtifactIdValue(@Nonnull String artifactIdValue) {
        artifactId.setText(artifactIdValue);
    }

    /** {@inheritDoc} */
    @Nonnull
    @Override
    public String getVersionValue() {
        return version.getText();
    }

    /** {@inheritDoc} */
    @Override
    public void setVersion(@Nonnull String versionValue) {
        version.setText(versionValue);
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
    public void showGroupIdInCorrectIndicator(boolean isCorrect) {
        applyStyle(groupId, isCorrect);
    }

    private void applyStyle(@Nonnull TextBox textBox, boolean isCorrect) {
        if (isCorrect) {
            textBox.addStyleName(resource.gaeCSS().wizardIncorrectValueBorder());
        } else {
            textBox.removeStyleName(resource.gaeCSS().wizardIncorrectValueBorder());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void showArtifactIdInCorrectIndicator(boolean isCorrect) {
        applyStyle(artifactId, isCorrect);
    }

    /** {@inheritDoc} */
    @Override
    public void showVersionInCorrectIndicator(boolean isCorrect) {
        applyStyle(version, isCorrect);
    }

    /** {@inheritDoc} */
    @Override
    public void showApplicationIdInCorrectIndicator(boolean isCorrect) {
        applyStyle(gaeAppId, isCorrect);
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

    @UiHandler("groupId")
    public void onGroupIdParameterChanged(@SuppressWarnings("UnusedParameters") KeyUpEvent event) {
        delegate.onValueChanged();
    }

    @UiHandler("artifactId")
    public void onArtifactIdValueChanged(@SuppressWarnings("UnusedParameters") KeyUpEvent event) {
        delegate.onValueChanged();
    }

    @UiHandler("version")
    public void onVersionChanged(@SuppressWarnings("UnusedParameters") KeyUpEvent event) {
        delegate.onValueChanged();
    }

    @UiHandler("gaeAppId")
    public void onApplicationIdChanged(@SuppressWarnings("UnusedParameters") KeyUpEvent event) {
        delegate.onValueChanged();
    }

}