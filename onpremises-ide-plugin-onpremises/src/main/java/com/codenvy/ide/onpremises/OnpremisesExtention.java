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
package com.codenvy.ide.onpremises;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.runner.dto.ResourcesDescriptor;
import org.eclipse.che.api.runner.gwt.client.RunnerServiceClient;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.IdeActions;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.util.Config;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.rest.SubscriptionHandler;
import org.eclipse.che.ide.workspace.WorkspacePresenter;

/**
 * @author Igor Vinokur
 */
@Singleton
@Extension(title = "Onpremises", version = "1.0.0")
public class OnpremisesExtention {

    @Inject
    public OnpremisesExtention(PanelResources resources,
                               LocalizationConstants locale,
                               WorkspacePresenter workspacePresenter,
                               ActionManager actionManager,
                               QueueTypeIndicatorAction queueTypeIndicatorAction,
                               OnpremisesIndicatorAction onpremisesIndicatorAction,
                               TrademarkLinkAction trademarkLinkAction) {

        resources.subscriptionsCSS().ensureInjected();

        workspacePresenter.setStatusPanelVisible(true);

        actionManager.registerAction("queueTypeIndicator", queueTypeIndicatorAction);
        actionManager.registerAction("subscriptionTitle", onpremisesIndicatorAction);
        actionManager.registerAction("trademarkLink", trademarkLinkAction);

        DefaultActionGroup rightBottomToolbarGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_RIGHT_STATUS_PANEL);
        rightBottomToolbarGroup.addSeparator();
        rightBottomToolbarGroup.add(queueTypeIndicatorAction, Constraints.LAST);
        rightBottomToolbarGroup.addSeparator();
        queueTypeIndicatorAction.updateCustomComponent();

        DefaultActionGroup leftBottomToolbarGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_LEFT_STATUS_PANEL);
        leftBottomToolbarGroup.add(onpremisesIndicatorAction, Constraints.LAST);
        onpremisesIndicatorAction.setDescription(locale.hostedTypeDescription());

        DefaultActionGroup centerBottomToolbarGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_CENTER_STATUS_PANEL);
        centerBottomToolbarGroup.add(trademarkLinkAction);
        trademarkLinkAction.updateLinkElement(locale.trademarkTitle(), locale.trademarkUrl());
    }
}
