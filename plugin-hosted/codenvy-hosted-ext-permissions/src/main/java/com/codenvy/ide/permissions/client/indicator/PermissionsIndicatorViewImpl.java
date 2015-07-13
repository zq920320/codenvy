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
package com.codenvy.ide.permissions.client.indicator;

import com.codenvy.ide.permissions.client.PermissionsResources;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import static com.google.gwt.dom.client.Style.Unit.PX;

/**
 * The {@link com.codenvy.ide.permissions.client.indicator.PermissionsIndicatorView} implementation.
 *
 * @author Kevin Pollet
 */
@Singleton
public class PermissionsIndicatorViewImpl implements PermissionsIndicatorView {

    @UiField
    FlowPanel panel;

    @UiField
    InlineLabel rights;

    @UiField
    FlowPanel tooltip;

    @UiField
    InlineLabel tooltipTitle;

    @UiField
    HTML tooltipMessage;

    @UiField
    Anchor tooltipLink;

    @UiField(provided = true)
    PermissionsResources resources;

    private ActionDelegate delegate;

    @Inject
    public PermissionsIndicatorViewImpl(PermissionsResources resources, PermissionsIndicatorViewImplUiBinder uiBinder) {
        this.resources = resources;

        uiBinder.createAndBindUi(this);

        this.tooltip.setVisible(false);
        this.panel.addDomHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                delegate.onMouseOver();
            }
        }, MouseOverEvent.getType());
        this.panel.addDomHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                delegate.onMouseOut();
            }
        }, MouseOutEvent.getType());
        this.panel.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onClick();
            }
        }, ClickEvent.getType());
        this.tooltip.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                event.stopPropagation();
            }
        }, ClickEvent.getType());
        this.tooltipLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onTooltipLinkClick();
            }
        });
    }

    @Override
    public Widget asWidget() {
        return panel;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        if (readOnly) {
            panel.addStyleName(resources.permissionsCSS().permissionsIndicatorReadOnly());
            tooltipMessage.addStyleName(resources.permissionsCSS().permissionsIndicatorTooltipBodyMessageReadOnly());
        } else {
            panel.removeStyleName(resources.permissionsCSS().permissionsIndicatorReadOnly());
            tooltipMessage.removeStyleName(resources.permissionsCSS().permissionsIndicatorTooltipBodyMessageReadOnly());
        }
    }

    @Override
    public void setTooltipTitle(String title) {
        tooltipTitle.setText(title);
    }

    @Override
    public void setTooltipMessage(String message) {
        tooltipMessage.setHTML(message);
    }

    @Override
    public void setTooltipLinkText(String text) {
        tooltipLink.setText(text);
    }

    @Override
    public void setPermissions(String permissions) {
        rights.setText(permissions);
    }

    @Override
    public void showTooltip() {
        final Element tooltipElement = tooltip.getElement();

        tooltipElement.getStyle().setTop(panel.getElement().getOffsetHeight() + 13, PX);
        tooltipElement.getStyle().setRight(Document.get().getClientWidth() - panel.getElement().getAbsoluteRight() - 30, PX);
        tooltip.setVisible(true);
    }

    @Override
    public void hideTooltip() {
        tooltip.setVisible(false);
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    interface PermissionsIndicatorViewImplUiBinder extends UiBinder<Widget, PermissionsIndicatorViewImpl> {
    }
}
