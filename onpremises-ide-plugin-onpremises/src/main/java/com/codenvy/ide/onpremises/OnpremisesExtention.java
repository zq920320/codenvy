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

import static com.codenvy.ide.onpremises.QueueType.DEDICATED;

/**
 * @author Igor Vinokur
 */
@Singleton
@Extension(title = "Subscription", version = "1.0.0")
public class OnpremisesExtention {

    public static final String USED_RESOURCES_CHANGE_CHANEL = "workspace:resources:";

    private final RunnerServiceClient runnerServiceClient;

    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
    private final MessageBus             messageBus;

    private final MemoryIndicatorAction    memoryIndicatorAction;
    private final QueueTypeIndicatorAction queueTypeIndicatorAction;

    @Inject
    public OnpremisesExtention(PanelResources resources,
                               LocalizationConstants locale,
                               RunnerServiceClient runnerServiceClient,
                               DtoUnmarshallerFactory dtoUnmarshallerFactory,
                               WorkspacePresenter workspacePresenter,
                               MessageBus messageBus,
                               ActionManager actionManager,
                               QueueTypeIndicatorAction queueTypeIndicatorAction,
                               MemoryIndicatorAction memoryIndicatorAction,
                               OnpremisesIndicatorAction onpremisesIndicatorAction,
                               TrademarkLinkAction trademarkLinkAction) {

        this.queueTypeIndicatorAction = queueTypeIndicatorAction;
        this.runnerServiceClient = runnerServiceClient;
        this.memoryIndicatorAction = memoryIndicatorAction;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.messageBus = messageBus;

        resources.subscriptionsCSS().ensureInjected();

        workspacePresenter.setStatusPanelVisible(true);

        actionManager.registerAction("memoryIndicator", memoryIndicatorAction);
        actionManager.registerAction("queueTypeIndicator", queueTypeIndicatorAction);
        actionManager.registerAction("subscriptionTitle", onpremisesIndicatorAction);
        actionManager.registerAction("trademarkLink", trademarkLinkAction);

        DefaultActionGroup rightBottomToolbarGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_RIGHT_STATUS_PANEL);
        rightBottomToolbarGroup.add(memoryIndicatorAction, Constraints.FIRST);
        rightBottomToolbarGroup.addSeparator();
        rightBottomToolbarGroup.add(queueTypeIndicatorAction, Constraints.LAST);
        rightBottomToolbarGroup.addSeparator();
        updateResourcesInformation(DEDICATED);

        DefaultActionGroup leftBottomToolbarGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_LEFT_STATUS_PANEL);
        leftBottomToolbarGroup.add(onpremisesIndicatorAction, Constraints.LAST);
        onpremisesIndicatorAction.setDescription(locale.hostedTypeDescription());

        DefaultActionGroup centerBottomToolbarGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_CENTER_STATUS_PANEL);
        centerBottomToolbarGroup.add(trademarkLinkAction);
        trademarkLinkAction.updateLinkElement(locale.trademarkTitle(), locale.trademarkUrl());
    }

    private void updateResourcesInformation(QueueType queueType) {
        queueTypeIndicatorAction.setQueueType(queueType);
        runnerServiceClient.getResources(
                new AsyncRequestCallback<ResourcesDescriptor>(dtoUnmarshallerFactory.newUnmarshaller(ResourcesDescriptor.class)) {
                    @Override
                    protected void onSuccess(ResourcesDescriptor result) {
                        memoryIndicatorAction.setUsedMemorySize(result.getUsedMemory());
                        memoryIndicatorAction.setTotalMemorySize(result.getTotalMemory());
                        try {
                            messageBus.subscribe(USED_RESOURCES_CHANGE_CHANEL + Config.getWorkspaceId(),
                                                 new UsedResourcesUpdater(Config.getWorkspaceId()));
                        } catch (WebSocketException e) {
                            Log.error(getClass(), "Can't open websocket connection");
                        }
                    }

                    @Override
                    protected void onFailure(Throwable exception) {
                        Log.error(getClass(), exception.getMessage());
                    }
                });
    }

    private class UsedResourcesUpdater extends SubscriptionHandler<ResourcesDescriptor> {
        private final String workspaceId;

        UsedResourcesUpdater(String workspaceId) {
            super(dtoUnmarshallerFactory.newWSUnmarshaller(ResourcesDescriptor.class));
            this.workspaceId = workspaceId;
        }

        @Override
        protected void onMessageReceived(ResourcesDescriptor result) {
            if (result.getUsedMemory() != null) {
                memoryIndicatorAction.setUsedMemorySize(result.getUsedMemory());
            }

            if (result.getTotalMemory() != null) {
                memoryIndicatorAction.setTotalMemorySize(result.getTotalMemory());
            }
        }

        @Override
        protected void onErrorReceived(Throwable throwable) {
            try {
                messageBus.unsubscribe(USED_RESOURCES_CHANGE_CHANEL + workspaceId, this);
                Log.error(UsedResourcesUpdater.class, throwable);
            } catch (WebSocketException e) {
                Log.error(UsedResourcesUpdater.class, e);
            }
        }
    }
}
