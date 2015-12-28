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
package com.codenvy.ide.factory.client.json;

import com.codenvy.ide.factory.client.FactoryLocalizationConstant;
import com.codenvy.ide.factory.client.FactoryResources;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;

import org.eclipse.che.api.analytics.client.logger.AnalyticsEventLogger;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;

import javax.inject.Singleton;

/**
 * @author Sergii Leschenko
 */
@Singleton
public class ExportConfigAction extends Action {
    private final AppContext                  appContext;
    private final AnalyticsEventLogger        eventLogger;
    private final String                      exportConfigURL;

    @Inject
    public ExportConfigAction(FactoryLocalizationConstant locale,
                              AppContext appContext,
                              AnalyticsEventLogger eventLogger,
                              FactoryResources resources) {
        super(locale.exportConfigName(), locale.exportConfigDescription(), null, resources.exportConfig());
        this.appContext = appContext;
        this.eventLogger = eventLogger;
        this.exportConfigURL = "/api/factory/workspace/" + appContext.getWorkspace().getId();
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);
        exportConfig();
    }

    private void exportConfig() {
        Window.open(exportConfigURL, "Download Config", "");
    }

    /** {@inheritDoc} */
    @Override
    public void update(ActionEvent event) {
        CurrentProject activeProject = appContext.getCurrentProject();
        event.getPresentation().setEnabled(activeProject != null);
    }
}
