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
package com.codenvy.ide.ext.gae.client.confirm;

import com.codenvy.ide.ext.gae.client.GAELocalizationConstant;
import com.codenvy.ide.ext.gae.client.GAEResources;
import org.eclipse.che.ide.ui.window.Window;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.annotation.Nonnull;

/**
 * Provides a graphical representation which allows user to create GAE project.
 *
 * @author Evgen Vidolob
 * @author Valeriy Svydenko
 * @author Dmitry Shnurenko
 */
public class ConfirmViewImpl extends Window implements ConfirmView {
    @Singleton
    interface CreateApplicationViewImplUiBinder extends UiBinder<LayoutPanel, ConfirmViewImpl> {
    }

    private static final CreateApplicationViewImplUiBinder UI_BINDER = GWT.create(CreateApplicationViewImplUiBinder.class);

    @UiField
    Label instructionLabel;
    @UiField
    Label subTitleLabel;
    @UiField(provided = true)
    final GAEResources            resource;
    @UiField(provided = true)
    final GAELocalizationConstant locale;

    private final Button actionButton;

    private ActionDelegate delegate;

    @Inject
    public ConfirmViewImpl(GAELocalizationConstant locale, GAEResources gaeResources) {
        super(true);

        this.resource = gaeResources;
        this.locale = locale;

        setWidget(UI_BINDER.createAndBindUi(this));

        setTitle(locale.applicationActionViewTitle());

        ClickHandler cancelButtonClickHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onCancelButtonClicked();
            }
        };

        Button cancelButton = createButton(resources, cancelButtonClickHandler);
        cancelButton.setText(locale.cancelButton());

        ClickHandler actionButtonClickHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onActionButtonClicked();
            }
        };

        actionButton = createButton(resources, actionButtonClickHandler);
        actionButton.addStyleName(resources.centerPanelCss().blueButton());

        getFooter().add(actionButton);
        getFooter().add(cancelButton);
    }

    private Button createButton(@Nonnull Resources resources, @Nonnull ClickHandler clickHandler) {
        Button button = new Button();

        button.addStyleName(resources.centerPanelCss().alignBtn());
        button.addStyleName(resources.centerPanelCss().button());
        button.addClickHandler(clickHandler);

        return button;
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    @Override
    protected void onClose() {
        delegate.onCancelButtonClicked();
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        hide();
    }

    /** {@inheritDoc} */
    @Override
    public void setActionButtonTitle(@Nonnull String title) {
        actionButton.setText(title);
    }

    /** {@inheritDoc} */
    @Override
    public void addSubtitleStyleName(@Nonnull String styleName) {
        subTitleLabel.addStyleName(styleName);
    }

    /** {@inheritDoc} */
    @Override
    public void windowOpen(@Nonnull String url) {
        com.google.gwt.user.client.Window.open(url, "_blank", "");
    }

    /** {@inheritDoc} */
    @Override
    public void setUserInstructions(@Nonnull String instructions) {
        instructionLabel.setText(instructions);
    }

    /** {@inheritDoc} */
    @Override
    public void setSubtitle(@Nonnull String subtitle) {
        subTitleLabel.setText(subtitle);
    }
}