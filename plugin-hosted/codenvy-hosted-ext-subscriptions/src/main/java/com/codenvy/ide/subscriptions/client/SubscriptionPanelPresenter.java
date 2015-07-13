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

import com.codenvy.api.subscription.gwt.client.SubscriptionServiceClient;
import com.codenvy.api.subscription.shared.dto.SubscriptionDescriptor;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.account.server.Constants;
import org.eclipse.che.api.runner.dto.ResourcesDescriptor;
import org.eclipse.che.api.runner.gwt.client.RunnerServiceClient;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.IdeActions;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.collections.Array;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.util.Config;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.rest.SubscriptionHandler;
import org.eclipse.che.ide.workspace.WorkspacePresenter;

import static com.codenvy.ide.subscriptions.client.QueueType.DEDICATED;
import static com.codenvy.ide.subscriptions.client.QueueType.SHARED;

/**
 * Controls what data will be shown on subscription panel.
 *
 * @author Alexander Garagatyi
 * @author Sergii Leschenko
 * @author Vitaliy Guliy
 * @author Kevin Pollet
 * @author Oleksii Orel
 */
@Singleton
public class SubscriptionPanelPresenter {
    public static final String USED_RESOURCES_CHANGE_CHANEL = "workspace:resources:";

    private final SubscriptionPanelLocalizationConstant locale;

    private final SubscriptionServiceClient subscriptionServiceClient;
    private final RunnerServiceClient       runnerServiceClient;

    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
    private final MessageBus             messageBus;
    private final AppContext             appContext;

    private final ActionManager               actionManager;
    private final MemoryIndicatorAction       memoryIndicatorAction;
    private final QueueTypeIndicatorAction    queueTypeIndicatorAction;
    private final SubscriptionIndicatorAction subscriptionIndicatorAction;
    private final RedirectLinkAction          redirectLinkAction;


    @Inject
    public SubscriptionPanelPresenter(RunnerServiceClient runnerServiceClient,
                                      DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                      WorkspacePresenter workspacePresenter,
                                      MessageBus messageBus,
                                      AppContext appContext,
                                      ActionManager actionManager,
                                      RedirectLinkAction redirectLinkAction,
                                      SubscriptionIndicatorAction subscriptionIndicatorAction,
                                      QueueTypeIndicatorAction queueTypeIndicatorAction,
                                      MemoryIndicatorAction memoryIndicatorAction,
                                      SubscriptionPanelLocalizationConstant locale,
                                      SubscriptionServiceClient subscriptionServiceClient) {

        this.runnerServiceClient = runnerServiceClient;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.appContext = appContext;
        this.messageBus = messageBus;
        this.actionManager = actionManager;
        this.redirectLinkAction = redirectLinkAction;
        this.subscriptionIndicatorAction = subscriptionIndicatorAction;
        this.queueTypeIndicatorAction = queueTypeIndicatorAction;
        this.memoryIndicatorAction = memoryIndicatorAction;
        this.locale = locale;
        this.subscriptionServiceClient = subscriptionServiceClient;

        workspacePresenter.setStatusPanelVisible(true);
    }

    public void process() {
        actionManager.registerAction("memoryIndicator", memoryIndicatorAction);
        actionManager.registerAction("queueTypeIndicator", queueTypeIndicatorAction);
        actionManager.registerAction("centerContent", redirectLinkAction);
        actionManager.registerAction("subscriptionTitle", subscriptionIndicatorAction);

        DefaultActionGroup rightBottomToolbarGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_RIGHT_STATUS_PANEL);
        rightBottomToolbarGroup.add(memoryIndicatorAction, Constraints.FIRST);
        rightBottomToolbarGroup.addSeparator();
        rightBottomToolbarGroup.add(queueTypeIndicatorAction, Constraints.LAST);
        rightBottomToolbarGroup.addSeparator();

        DefaultActionGroup centerBottomToolbarGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_CENTER_STATUS_PANEL);
        centerBottomToolbarGroup.add(redirectLinkAction);

        DefaultActionGroup leftBottomToolbarGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_LEFT_STATUS_PANEL);
        leftBottomToolbarGroup.add(subscriptionIndicatorAction, Constraints.LAST);


        if (Config.getCurrentWorkspace().isTemporary()) {
            updateResourcesInformation(SHARED);

            if (!appContext.getCurrentUser().isUserPermanent()) {
                redirectLinkAction.updateLinkElement(locale.createAccountActionTitle(),
                                                     Window.Location.getHref() + "?login", true);
            }
        } else {
            checkSaasSubscription();
        }
    }

    private void checkSaasSubscription() {
        String accountId = Config.getCurrentWorkspace().getAccountId();
        subscriptionServiceClient.getSubscriptionByServiceId(accountId, "Saas", new AsyncRequestCallback<Array<SubscriptionDescriptor>>(
                dtoUnmarshallerFactory.newArrayUnmarshaller(SubscriptionDescriptor.class)) {
            @Override
            protected void onSuccess(Array<SubscriptionDescriptor> result) {
                if (result.isEmpty()) {
                    Log.error(getClass(), "Required Saas subscription is absent");
                    updateSaasInformation("Community", SHARED);
                    return;
                }

                if (result.size() > 1) {
                    Log.error(getClass(), "User has more than 1 Saas subscriptions");
                    updateSaasInformation("Community", SHARED);
                    return;
                }

                SubscriptionDescriptor subscription = result.get(0);

                final String subscriptionPackage = subscription.getProperties().get("Package");

                updateSaasInformation(subscriptionPackage, DEDICATED);
            }

            @Override
            protected void onFailure(Throwable exception) {
                //User hasn't permission to account
                updateSaasInformation("Community", SHARED);
            }
        });
    }

    private void updateSaasInformation(String subscriptionPackage, QueueType queueType) {
        final String formattedSubscriptionPackage = subscriptionPackage.substring(0, 1).toUpperCase() +
                                                    subscriptionPackage.substring(1).toLowerCase();
        subscriptionIndicatorAction.setDescription("Subscription: SAAS " + formattedSubscriptionPackage);
        updateResourcesInformation(queueType);
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
        if (Config.getCurrentWorkspace().getAttributes().containsKey(Constants.RESOURCES_LOCKED_PROPERTY)) {
            redirectLinkAction.updateLinkElement(locale.lockDownModeTitle(), locale.lockDownModeUrl(), true);
        }
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
