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
package com.codenvy.ide.subscriptions.client;

import com.google.gwt.i18n.client.Messages;

/**
 * The localization constants for the subscription panel presenter.
 *
 * @author Oleksii Orel
 */
public interface SubscriptionPanelLocalizationConstant extends Messages {
    /*
     * Create account action
     */
    @Key("redirectLinkAction.createAccount.title")
    String createAccountActionTitle();

    /*
    * Lock down mode
    */
    @Key("redirectLinkAction.lockDownMode.title")
    String lockDownModeTitle();

    @Key("redirectLinkAction.lockDownMode.url")
    String lockDownModeUrl();

    /*
     * Subscription descriptors
     */
    @Key("subscriptionIndicatorAction.onPremises.description")
    String onPremisesDescription();

    @Key("trademarkLinkAction.onPremises.title")
    String trademarkTitle();

    @Key("trademarkLinkAction.onPremises.url")
    String trademarkUrl();

}
