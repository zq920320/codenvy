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

import com.google.gwt.i18n.client.Messages;

/**
 * The localization constants for the permissions extension.
 *
 * @author Kevin Pollet
 */
public interface PermissionsLocalizationConstant extends Messages {
    /*
     * Indicator
     */
    @Key("permissions.indicator.tooltip.title")
    String permissionsIndicatorTooltipTitle(String permissions);

    @Key("permissions.indicator.tooltip.message")
    String permissionsIndicatorTooltipMessage();

    @Key("permissions.indicator.tooltip.message.readonly")
    String permissionsIndicatorTooltipMessageReadOnly();

    @Key("permissions.indicator.tooltip.link.text")
    String permissionsIndicatorTooltipLinkText();

    /*
     * Part
     */
    @Key("permissions.action.title")
    String permissionActionTitle();

    @Key("permissions.view.title")
    String permissionsViewTitle();

    @Key("permissions.view.text.permissions_all")
    String permissionsViewTextPermissionsAll();

    @Key("permissions.view.text.permissions_read_only")
    String permissionsViewTextPermissionsReadonly();

}
