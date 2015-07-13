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
package com.codenvy.ide.clone.client.persist;

import com.codenvy.ide.clone.client.CloneLocalizationConstant;
import com.codenvy.ide.clone.client.CloneResources;
import com.codenvy.ide.permissions.client.part.PermissionsPartPresenter;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.CustomComponentAction;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.ProjectActionEvent;
import org.eclipse.che.ide.api.event.ProjectActionHandler;
import org.eclipse.che.ide.util.Config;
import org.vectomatic.dom.svg.ui.SVGImage;

/**
 * Action to place a Persist button onto toolbar.
 *
 * @author Vitaliy Guliy
 */
@Singleton
public class PersistProjectAction extends Action implements CustomComponentAction {

    private final CloneResources            factoryResources;
    private final CloneLocalizationConstant localizationConstant;
    private final AppContext                appContext;

    private PersistButton persistButton;

    private final PermissionsPartPresenter permissionsPresenter;

    @Inject
    public PersistProjectAction(CloneResources factoryResources,
                                CloneLocalizationConstant localizationConstant,
                                EventBus eventBus,
                                AppContext appContext,
                                final PermissionsPartPresenter permissionsPresenter) {
        this.factoryResources = factoryResources;
        this.localizationConstant = localizationConstant;
        this.permissionsPresenter = permissionsPresenter;
        this.appContext = appContext;

        persistButton = new PersistButton();
        persistButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                PersistProjectAction.this.permissionsPresenter.activateView();
            }
        });

        eventBus.addHandler(ProjectActionEvent.TYPE, new ProjectActionHandler() {
            @Override
            public void onProjectOpened(ProjectActionEvent event) {
                if (!Config.getCurrentWorkspace().isTemporary() && permissionsPresenter.isReadOnlyMode()) {
                    persistButton.setText(PersistProjectAction.this.localizationConstant.copyToolbarButtonText() + " ");
                    persistButton.setTooltip(PersistProjectAction.this.localizationConstant.copyToolbarButtonTitle());
                } else {
                    persistButton.setText(PersistProjectAction.this.localizationConstant.persistToolbarButtonText() + " ");
                    persistButton.setTooltip(PersistProjectAction.this.localizationConstant.persistToolbarButtonTitle());
                }
            }

            @Override
            public void onProjectClosing(ProjectActionEvent event) {
            }

            @Override
            public void onProjectClosed(ProjectActionEvent event) {
            }
        });
    }

    @Override
    public Widget createCustomComponent(Presentation presentation) {
        if (!Config.getCurrentWorkspace().isTemporary() && permissionsPresenter.isReadOnlyMode()) {
            persistButton.setText(localizationConstant.copyToolbarButtonText());
            persistButton.setTooltip(localizationConstant.copyToolbarButtonTitle());
        } else {
            persistButton.setText(localizationConstant.persistToolbarButtonText());
            persistButton.setTooltip(localizationConstant.persistToolbarButtonTitle());
        }

        return persistButton;
    }

    public void update(ActionEvent e) {
        if (appContext.getCurrentProject() == null) {
            e.getPresentation().setVisible(false);
            return;
        }

        if (Config.getCurrentWorkspace().isTemporary()) {
            e.getPresentation().setVisible(true);
        } else if (permissionsPresenter.isReadOnlyMode()) {
            e.getPresentation().setVisible(true);
        } else {
            e.getPresentation().setVisible(false);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        permissionsPresenter.activateView();
    }


    public class PersistButton extends Button {

        private final Element tooltip;

        private final SVGImage image;

        public PersistButton() {
            ensureDebugId("persist-toolbar-button");

            image = new SVGImage(factoryResources.persistButton());
            image.setWidth("12px");
            image.setHeight("12px");
            image.getStyle().setProperty("fill", "#e7e7e7");
            image.getStyle().setProperty("verticalAlign", "middle");
            image.getStyle().setProperty("marginRight", "5px");
            image.getStyle().setProperty("paddingBottom", "2px");

            getElement().insertFirst(image.getElement());

            addStyleName(factoryResources.cloneCSS().mainMenuBarButton());
            addStyleName(factoryResources.cloneCSS().buttonTooltip());
            addStyleName(factoryResources.cloneCSS().tooltip());

            getElement().setAttribute("onmousedown",
                                      "this.style.background='#202020';this.style.color='#e7e7e7';this.style.box-shadow='none';");

            tooltip = DOM.createSpan();

            addMouseOverHandler(new MouseOverHandler() {
                @Override
                public void onMouseOver(MouseOverEvent event) {
                    final Element button = event.getRelativeElement();
                    if (!button.isOrHasChild(tooltip)) {
                        tooltip.getStyle().setProperty("top", (button.getOffsetHeight() + 10) + "px");
                        tooltip.getStyle()
                               .setProperty("right", ((Document.get().getClientWidth() - button.getAbsoluteRight()) - 30) + "px");
                        button.appendChild(tooltip);
                    }
                }
            });
        }

        @Override
        public void setText(String text) {
            super.setText(text);
            getElement().insertFirst(image.getElement());
        }

        public void setTooltip(String tooltip) {
            this.tooltip.setInnerHTML(tooltip);
        }

    }

}
