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
package com.codenvy.ide.onpremises;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import org.eclipse.che.api.analytics.client.logger.AnalyticsEventLogger;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.CustomComponentAction;
import org.eclipse.che.ide.api.action.Presentation;

/**
 * Show trademark link or trademark name.
 *
 * @author Igor Vinokur
 */
public class TrademarkLinkAction extends Action implements CustomComponentAction {
    private final AnalyticsEventLogger eventLogger;
    private final FlowPanel            panel;
    private       Element              htmlElement;

    @Inject
    public TrademarkLinkAction(PanelResources resources, AnalyticsEventLogger eventLogger) {
        panel = new FlowPanel();
        panel.addStyleName(resources.subscriptionsCSS().panel());
        panel.addStyleName(resources.subscriptionsCSS().centerContent());

        this.eventLogger = eventLogger;
    }

    /**
     * Update element that redirect on new address after click on it
     * or a simple label, if the url is null
     *
     * @param content
     *         text that will be displayed for user
     * @param url
     *         url for redirect, if exist
     */
    public void updateLinkElement(String content, String url) {
        if (htmlElement != null) {
            panel.getElement().removeChild(htmlElement);
        }

        if (content != null && !content.isEmpty()) {
            if (url == null || url.isEmpty()) {
                htmlElement = DOM.createDiv();
                htmlElement.setInnerHTML(content);
            } else {
                Element linkElement = DOM.createElement("a");
                linkElement.setInnerHTML(content);
                linkElement.setPropertyString("text", content);
                linkElement.setPropertyString("href", url);
                linkElement.setPropertyString("target", "_blank");
                htmlElement = linkElement;
            }
            panel.getElement().appendChild(htmlElement);
        } else {
            htmlElement = null;
        }
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
    }
}
