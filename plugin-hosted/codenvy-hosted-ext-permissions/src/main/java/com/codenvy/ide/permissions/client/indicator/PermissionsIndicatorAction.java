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
package com.codenvy.ide.permissions.client.indicator;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.CustomComponentAction;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import com.codenvy.ide.permissions.client.PermissionsLocalizationConstant;
import com.codenvy.ide.permissions.client.part.PermissionsPartPresenter;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.validation.constraints.NotNull;
import java.util.List;


/**
 * Action displaying the user permissions for the current project.
 *
 * @author Kevin Pollet
 */
@Singleton
public class PermissionsIndicatorAction extends Action implements CustomComponentAction, PermissionsIndicatorView.ActionDelegate {

    private static final String MISSING_PERMISSION_STRING       = "-";
    private static final String READ                            = "read";
    private static final String WRITE                           = "write";
    private static final String RUN                             = "run";
    private static final String BUILD                           = "build";
    private static final String UPDATE                          = "update_acl";
    public static final  String PERMISSIONS_INDICATOR_ACTION_ID = "permissionsIndicator";

    private final AppContext                      context;
    private final PermissionsIndicatorView        view;
    private final PermissionsLocalizationConstant locale;
    private final PermissionsPartPresenter        permissionsPartPresenter;

    @Inject
    public PermissionsIndicatorAction(AppContext context,
                                      PermissionsIndicatorView view,
                                      PermissionsLocalizationConstant locale,
                                      PermissionsPartPresenter permissionsPartPresenter) {
        this.context = context;
        this.view = view;
        this.locale = locale;
        this.permissionsPartPresenter = permissionsPartPresenter;

        this.view.setDelegate(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    @Override
    public void update(ActionEvent e) {
        final CurrentProject currentProject = context.getCurrentProject();

        e.getPresentation().setVisible(currentProject != null);
        if (currentProject != null) {
            view.setReadOnly(isCurrentProjectReadOnly());
            view.setPermissions(getCurrentProjectPermissionsAsString());
        }
    }

    @Override
    public Widget createCustomComponent(Presentation presentation) {
        return view.asWidget();
    }

    @Override
    public void onMouseOver() {
        final CurrentProject currentProject = context.getCurrentProject();
        if (currentProject != null) {
            final String permissions = getCurrentProjectPermissionsAsString();
            view.setTooltipTitle(locale.permissionsIndicatorTooltipTitle(permissions));
            view.setTooltipMessage(isCurrentProjectReadOnly() ? locale.permissionsIndicatorTooltipMessageReadOnly()
                                                              : locale.permissionsIndicatorTooltipMessage());
            view.setTooltipLinkText(locale.permissionsIndicatorTooltipLinkText());
            view.showTooltip();
        }
    }

    @Override
    public void onMouseOut() {
        view.hideTooltip();
    }

    @Override
    public void onClick() {
        permissionsPartPresenter.showDialog();
    }

    @Override
    public void onTooltipLinkClick() {
        permissionsPartPresenter.showDialog();
    }

    /**
     * Return the {@link com.codenvy.ide.api.app.CurrentProject} user permissions as a {@link java.lang.String}. R for read, W for write, X
     * for run, B for build and U for update.
     *
     * @return the {@link com.codenvy.ide.api.app.CurrentProject} user permissions as a {@link java.lang.String}.
     */
    private String getCurrentProjectPermissionsAsString() {
        final CurrentProject currentProject = context.getCurrentProject();
        if (currentProject != null) {
            final List<String> permissions = currentProject.getProjectDescription().getPermissions();
            String permissionsString = containsIgnoreCase(permissions, READ) ? "R" : MISSING_PERMISSION_STRING;
            permissionsString += containsIgnoreCase(permissions, WRITE) ? "W" : MISSING_PERMISSION_STRING;
            permissionsString += containsIgnoreCase(permissions, BUILD) ? "B" : MISSING_PERMISSION_STRING;
            permissionsString += containsIgnoreCase(permissions, RUN) ? "X" : MISSING_PERMISSION_STRING;
            return permissionsString + (containsIgnoreCase(permissions, UPDATE) ? "U" : MISSING_PERMISSION_STRING);

        }
        throw new NullPointerException("Current project cannot be null");
    }

    /**
     * Return if the {@link com.codenvy.ide.api.app.CurrentProject} is read only.
     *
     * @return {@code true} if read only, {@code false} otherwise.
     */
    private boolean isCurrentProjectReadOnly() {
        final CurrentProject currentProject = context.getCurrentProject();
        if (currentProject != null) {
            final List<String> permissions = currentProject.getProjectDescription().getPermissions();
            return permissions.size() == 1 && permissions.contains(READ);
        }
        throw new NullPointerException("Current project cannot be null");
    }

    /**
     * Return if the given {@link java.util.List} contains the given {@link java.lang.String}.
     *
     * @param list
     *         the {@link java.util.List}, must be non {@code null}.
     * @param string
     *         the {@link java.lang.String} to search.
     * @return {@code true} if found, {@code false} otherwise.
     */
    private boolean containsIgnoreCase(@NotNull List<String> list, String string) {
        for (String oneString : list) {
            if (oneString.equalsIgnoreCase(string)) {
                return true;
            }
        }
        return false;
    }

}
