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

import com.google.gwt.i18n.client.Messages;

/**
 * @author vzhukovskii@codenvy.com
 */
public interface DashboardLocalizationConstant extends Messages {
    /* ************************************************************************************************************
     *
     * Dashboard
     *
     * ************************************************************************************************************/

    @Key("open.dashboard.dialog.title")
    String openDashboardTitle();

    @Key("open.dashboard.dialog.text")
    String openDashboardText();

    @Key("open.dashboard.toolbar-button.title")
    String openDashboardToolbarButtonTitle();
}
