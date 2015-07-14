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
package com.codenvy.ide.subscriptions.client;

import org.eclipse.che.api.analytics.client.logger.AnalyticsEventLogger;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.CustomComponentAction;
import org.eclipse.che.ide.api.action.Presentation;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Show additional content for redirect.
 *
 * @author Oleksii Orel
 */
public class RedirectLinkAction extends Action implements CustomComponentAction {
    private final FlowPanel            panel;
    private final AnalyticsEventLogger eventLogger;

    private Element link;
    private Element displayedLink;


    @Inject
    public RedirectLinkAction(SubscriptionsResources resources, AnalyticsEventLogger eventLogger) {
        panel = new FlowPanel();
        panel.addStyleName(resources.subscriptionsCSS().panel());
        panel.addStyleName(resources.subscriptionsCSS().centerContent());

        this.eventLogger = eventLogger;
    }

    /**
     * Update additional content element
     *
     * @param content
     *         text that should be shown
     * @param url
     *         url that should be opened if user clicks on the content
     * @param mustBeOpenedInCurrentWindow
     *         indicates if link must be opened in current window only
     */
    public void updateLinkElement(String content, String url, boolean mustBeOpenedInCurrentWindow) {
        if (mustBeOpenedInCurrentWindow) {
            link = getJavaScriptLink(content, url);
        } else {
            link = getHtmlLink(content, url);
        }
    }

    /**
     * Create element that redirect on new address after click on it in current window
     *
     * @param prompt
     *         text that will be displayed for user
     * @param url
     *         url for redirect
     */
    private Element getJavaScriptLink(String prompt, String url) {
        Element span = DOM.createElement("span");
        span.setInnerHTML(prompt);
        span.setAttribute("onclick", "window.location = '" + url + "';");
        return span;
    }

    /**
     * Create element that redirect on new address after click on it
     *
     * @param prompt
     *         text that will be displayed for user
     * @param url
     *         url for redirect
     */
    private Element getHtmlLink(String prompt, String url) {
        Element a = DOM.createElement("a");
        a.setInnerHTML(prompt);
        a.setPropertyString("text", prompt);
        a.setPropertyString("href", url);
        return a;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);
    }

    @Override
    public Widget createCustomComponent(Presentation presentation) {
        return panel;
    }

    @Override
    public void update(ActionEvent e) {
        if(link == null) {
            panel.clear();
            displayedLink = null;
        } else {
            if (!link.equals(displayedLink)) {
                panel.clear();
                panel.getElement().appendChild(link);
                displayedLink = link;
            }
        }
        e.getPresentation().setVisible(panel.getElement().getChildCount()>0);
    }
}
