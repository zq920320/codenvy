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

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Factory extension resources (css styles, images).
 *
 * @author Ann Shumilova
 */
public interface DashboardResources extends ClientBundle {
    interface DashboardCSS extends CssResource {
        String tooltip();

        String buttonTooltip();

        String dashboardButton();

        String dashboardButtonTooltip();
    }

    @Source({"Dashboard.css", "org/eclipse/che/ide/api/ui/style.css"})
    DashboardCSS dashboardCSS();

    @Source("dashboard/dashboard.svg")
    SVGResource dashboard();
}
