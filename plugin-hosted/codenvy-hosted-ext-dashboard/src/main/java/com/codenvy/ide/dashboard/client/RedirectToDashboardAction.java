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
package com.codenvy.ide.dashboard.client;

import com.codenvy.ide.factory.client.login.PromptToLoginView;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import org.eclipse.che.api.analytics.client.logger.AnalyticsEventLogger;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.CustomComponentAction;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.util.Config;
import org.vectomatic.dom.svg.ui.SVGImage;

import static com.google.gwt.dom.client.Style.Unit.PX;

/**
 * Action to provide Dashboard button onto toolbar.
 *
 * @author Oleksii Orel
 */
public class RedirectToDashboardAction extends Action implements CustomComponentAction {
    private final PromptToLoginView             promptToLoginView;
    private final AnalyticsEventLogger          eventLogger;
    private final DashboardLocalizationConstant constant;
    private final DashboardResources            resources;
    private final AppContext                    appContext;

    @Inject
    public RedirectToDashboardAction(DashboardLocalizationConstant constant,
                                     DashboardResources resources,
                                     PromptToLoginView promptToLoginView,
                                     AnalyticsEventLogger eventLogger,
                                     AppContext appContext) {
        this.constant = constant;
        this.promptToLoginView = promptToLoginView;
        this.resources = resources;
        this.eventLogger = eventLogger;
        this.appContext = appContext;

        promptToLoginView.setDelegate(new PromptToLoginView.ActionDelegate() {
            @Override
            public void onLogin() {
                String separator = Window.Location.getQueryString().contains("?") ? "&" : "?";
                Window.Location.replace(Window.Location.getPath() + Window.Location.getQueryString() + separator + "login");
            }

            @Override
            public void onCreateAccount() {
                String url = "/site/create-account" +
                             "?action=clone-projects" +
                             "&src-workspace-id=" + Config.getCurrentWorkspace().getId();
                Window.Location.replace(url);
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);
    }

    @Override
    public Widget createCustomComponent(Presentation presentation) {
        final Anchor dashboardButton = new Anchor();
        final SVGImage buttonImage = new SVGImage(resources.dashboard());
        final Element tooltip = DOM.createSpan();

        dashboardButton.ensureDebugId("dashboard-toolbar-button");
        dashboardButton.addStyleName(resources.dashboardCSS().dashboardButton());
        dashboardButton.addStyleName(resources.dashboardCSS().tooltip());
        dashboardButton.addStyleName(resources.dashboardCSS().buttonTooltip());
        dashboardButton.addStyleName(resources.dashboardCSS().dashboardButtonTooltip());
        dashboardButton.getElement().insertFirst(buttonImage.getElement());
        tooltip.setInnerText(constant.openDashboardToolbarButtonTitle());

        if (appContext.getCurrentUser().isUserPermanent()) {
            dashboardButton.setHref("/dashboard");
            dashboardButton.getElement().setAttribute("target", "_blank");
        } else {
            // handle clicking and show prompt
            dashboardButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    promptToLoginView.showDialog(constant.openDashboardTitle(), constant.openDashboardText());
                }
            });
        }

        dashboardButton.addMouseOverHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                final Element link = event.getRelativeElement();
                if (!link.isOrHasChild(tooltip)) {
                    tooltip.getStyle().setTop(link.getAbsoluteTop() + link.getOffsetHeight() + 7, PX);
                    tooltip.getStyle().setLeft(link.getAbsoluteLeft() + link.getOffsetWidth() / 2 - 9, PX);
                    link.appendChild(tooltip);
                }
            }
        });

        return dashboardButton;
    }

}
