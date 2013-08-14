/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */


package com.codenvy.analytics.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class AnalyticsApplication {

    public void onModuleLoad() {
        TimeLineServiceAsync viewService = GWT.create(TimeLineService.class);
        AnalysisServiceAsync analysisService = GWT.create(AnalysisService.class);
        UserServiceAsync userService = GWT.create(UserService.class);

        HandlerManager eventBus = new HandlerManager(null);
        AppController appViewer = new AppController(viewService, analysisService, userService, eventBus);
        appViewer.go(RootPanel.get());
    }
}
