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

import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.IdeActions;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.workspace.WorkspacePresenter;

import java.util.List;

import static com.codenvy.ide.subscriptions.client.QueueType.DEDICATED;
import static com.codenvy.ide.subscriptions.client.QueueType.SHARED;

/**
 * @author Vitaliy Guliy
 * @author Sergii Leschenko
 * @author Alexander Garagatyi
 */
@Singleton
@Extension(title = "Subscription", version = "1.0.0")
public class SubscriptionsExtension {
    private final SubscriptionServiceClient subscriptionServiceClient;

    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;

    private final SubscriptionIndicatorAction subscriptionIndicatorAction;
    private final QueueTypeIndicatorAction    queueTypeIndicatorAction;
    private final AppContext                  appContext;

    @Inject
    public SubscriptionsExtension(SubscriptionsResources resources,
                                  DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                  WorkspacePresenter workspacePresenter,
                                  AppContext appContext,
                                  ActionManager actionManager,
                                  RedirectLinkAction redirectLinkAction,
                                  SubscriptionIndicatorAction subscriptionIndicatorAction,
                                  QueueTypeIndicatorAction queueTypeIndicatorAction,
                                  SubscriptionServiceClient subscriptionServiceClient) {

        resources.subscriptionsCSS().ensureInjected();

        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.subscriptionIndicatorAction = subscriptionIndicatorAction;
        this.queueTypeIndicatorAction = queueTypeIndicatorAction;
        this.subscriptionServiceClient = subscriptionServiceClient;
        this.appContext = appContext;

        workspacePresenter.setStatusPanelVisible(true);

        actionManager.registerAction("centerContent", redirectLinkAction);
        actionManager.registerAction("subscriptionTitle", subscriptionIndicatorAction);
        actionManager.registerAction("queueTypeIndicator", queueTypeIndicatorAction);

        DefaultActionGroup rightBottomToolbarGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_RIGHT_STATUS_PANEL);
        rightBottomToolbarGroup.addSeparator();
        rightBottomToolbarGroup.add(queueTypeIndicatorAction, Constraints.LAST);
        rightBottomToolbarGroup.addSeparator();

        DefaultActionGroup centerBottomToolbarGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_CENTER_STATUS_PANEL);
        centerBottomToolbarGroup.add(redirectLinkAction);

        DefaultActionGroup leftBottomToolbarGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_LEFT_STATUS_PANEL);
        leftBottomToolbarGroup.add(subscriptionIndicatorAction, Constraints.LAST);

        if (appContext.getWorkspace().isTemporary()) {
            queueTypeIndicatorAction.setQueueType(SHARED);
        } else {
            checkSaasSubscription();
        }


    }

    private void checkSaasSubscription() {
        String accountId = appContext.getWorkspace().getAccountId();
        subscriptionServiceClient.getSubscriptionByServiceId(accountId, "Saas", new AsyncRequestCallback<List<SubscriptionDescriptor>>(
                dtoUnmarshallerFactory.newListUnmarshaller(SubscriptionDescriptor.class)) {
            @Override
            protected void onSuccess(List<SubscriptionDescriptor> result) {
                if (result.isEmpty()) {
                    //User did not have subscription. It means that his account is community
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
        queueTypeIndicatorAction.setQueueType(queueType);
    }
}
