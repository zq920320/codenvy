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

import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.api.parts.base.BaseView;
import com.codenvy.ide.permissions.client.PermissionsLocalizationConstant;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Vitaliy Guliy
 * @author Sergii Leschenko
 * @author Kevin Pollet
 */
@Singleton
public class PermissionsPartViewImpl extends BaseView<PermissionsPartView.ActionDelegate> implements PermissionsPartView {

    interface PermissionsPartViewImplUiBinder extends UiBinder<Widget, PermissionsPartViewImpl> {
    }

    private final PermissionsLocalizationConstant locale;

    @UiField
    HTMLPanel permissionsInfo;

    @UiField
    FlowPanel widgets;

    @Inject
    public PermissionsPartViewImpl(PermissionsPartViewImplUiBinder uiBinder,
                                   PartStackUIResources partStackUIResources,
                                   PermissionsLocalizationConstant locale) {
        super(partStackUIResources);
        this.locale = locale;

        setTitle(locale.permissionsViewTitle());
        setContentWidget(uiBinder.createAndBindUi(this));
    }

    /** {@inheritDoc} */
    @Override
    public void updatePermissions(String permissions) {
        if ("write".equalsIgnoreCase(permissions)) {
            permissionsInfo.getElement().setInnerHTML(locale.permissionsViewTextPermissionsAll());

        } else if ("read".equalsIgnoreCase(permissions)) {
            permissionsInfo.getElement().setInnerHTML(locale.permissionsViewTextPermissionsReadonly());

        } else {
            // don't display any permission
            permissionsInfo.getElement().setInnerHTML("");
        }
    }

    @Override
    public void displayWidget(Widget widget) {
        widgets.clear();

        if (widget != null) {
            widgets.add(widget);
        }
    }

}
