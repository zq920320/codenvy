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

import org.eclipse.che.api.analytics.client.logger.AnalyticsEventLogger;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.CustomComponentAction;
import org.eclipse.che.ide.api.action.Presentation;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Show Onpremises indicator.
 *
 * @author Igor Vinokur
 */
public class OnpremisesIndicatorAction extends Action implements CustomComponentAction {
    private final FlowPanel            panel;
    private final AnalyticsEventLogger eventLogger;

    private String displayedDescription;
    private String description;


    @Inject
    public OnpremisesIndicatorAction(PanelResources resources, AnalyticsEventLogger eventLogger) {
        panel = new FlowPanel();
        panel.addStyleName(resources.subscriptionsCSS().panel());
        panel.addStyleName(resources.subscriptionsCSS().subscriptionTitle());

        this.eventLogger = eventLogger;
    }

    public void setDescription(String description) {
        this.description = description;
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
        if (description == null) {
            panel.clear();
            displayedDescription = null;
        } else {
            if (!description.equals(displayedDescription)) {
                panel.clear();
                panel.add(new HTML(description));
                displayedDescription = description;
            }
        }
        e.getPresentation().setVisible(panel.getElement().getChildCount() > 0);
    }
}
