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
package com.codenvy.ide.permissions.client.part;

import org.eclipse.che.api.analytics.client.logger.AnalyticsEventLogger;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import com.codenvy.ide.permissions.client.PermissionsLocalizationConstant;
import com.codenvy.ide.permissions.client.PermissionsResources;
import com.google.inject.Inject;

/**
 * Action for open permissions panel.
 *
 * @author Sergii Leschenko
 * @author Kevin Pollet
 */
public class PermissionsAction extends Action {

    public static final String PERMISSIONS_ACTION_ID = "permissionsProject";

    private final AppContext               appContext;
    private final PermissionsPartPresenter presenter;
    private final AnalyticsEventLogger     eventLogger;

    @Inject
    public PermissionsAction(PermissionsPartPresenter presenter,
                             AppContext appContext,
                             PermissionsLocalizationConstant locale,
                             AnalyticsEventLogger eventLogger,
                             PermissionsResources resources) {
        super(locale.permissionActionTitle(), locale.permissionActionTitle(), null, resources.key());
        this.appContext = appContext;
        this.presenter = presenter;
        this.eventLogger = eventLogger;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);
        presenter.showDialog();
    }

    /** {@inheritDoc} */
    @Override
    public void update(ActionEvent e) {
        CurrentProject activeProject = appContext.getCurrentProject();
        e.getPresentation().setVisible(activeProject != null);
    }

}
