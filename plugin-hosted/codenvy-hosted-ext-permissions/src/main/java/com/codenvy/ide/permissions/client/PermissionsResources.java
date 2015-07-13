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
package com.codenvy.ide.permissions.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * The resources for the permissions extension.
 *
 * @author Kevin Pollet
 */
public interface PermissionsResources extends ClientBundle {
    interface PermissionsCSS extends CssResource {
        String permissionsIndicator();

        String permissionsIndicatorReadOnly();

        String permissionsIndicatorTooltip();

        String permissionsIndicatorTooltipHeader();

        String permissionsIndicatorTooltipBody();

        String permissionsIndicatorTooltipBodyMessageReadOnly();
    }

    @Source({"Permissions.css", "org/eclipse/che/ide/api/ui/style.css"})
    PermissionsCSS permissionsCSS();

    @Source("permissions/key.svg")
    SVGResource key();
}
