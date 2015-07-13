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
package com.codenvy.ide.support.help.client.action;

import org.eclipse.che.api.analytics.client.logger.AnalyticsEventLogger;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import com.codenvy.ide.support.help.client.HelpLocalizationConstant;
import com.codenvy.ide.support.help.client.HelpResources;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;

/**
 * Open a new window with the IRC channel URL
 *
 * @author Oleksii Orel
 */
public class RedirectToEngineerChatChannelAction extends Action {

    private final AnalyticsEventLogger     eventLogger;
    private final HelpLocalizationConstant locale;

    @Inject
    public RedirectToEngineerChatChannelAction(HelpLocalizationConstant locale,
                                               AnalyticsEventLogger eventLogger,
                                               HelpResources resources) {
        super(locale.actionRedirectToEngineerChatChannelTitle(), locale.actionRedirectToEngineerChatChannelDescription(), null,
              resources.ircChannel());
        this.eventLogger = eventLogger;
        this.locale = locale;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);
        Window.open(locale.actionRedirectToEngineerChatChannelUrl(), "_blank", "");
    }
}