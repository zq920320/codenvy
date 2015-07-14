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
package com.codenvy.ide.profile.client.action;

import com.codenvy.ide.profile.client.ProfileLocalizationConstant;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;

import org.eclipse.che.api.analytics.client.logger.AnalyticsEventLogger;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;

/**
 * Open a new dashboard window with information about the projects
 *
 * @author Oleksii Orel
 */
public class RedirectToDashboardProjectsAction extends Action {

    private final AnalyticsEventLogger     eventLogger;
    private final ProfileLocalizationConstant locale;

    @Inject
    public RedirectToDashboardProjectsAction(ProfileLocalizationConstant locale,
                                             AnalyticsEventLogger eventLogger) {
        super(locale.redirectToDashboardProjectsTitle(), locale.redirectToDashboardProjectsDescription(), null, null);
        this.eventLogger = eventLogger;
        this.locale = locale;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);
        Window.open(locale.redirectToDashboardProjectsUrl(), "_blank", "");
    }
}