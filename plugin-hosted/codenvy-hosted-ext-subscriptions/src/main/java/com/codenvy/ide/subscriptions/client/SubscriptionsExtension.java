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

import org.eclipse.che.ide.api.extension.Extension;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Vitaliy Guliy
 * @author Sergii Leschenko
 * @author Alexander Garagatyi
 */
@Singleton
@Extension(title = "Subscription", version = "1.0.0")
public class SubscriptionsExtension {
    @Inject
    public SubscriptionsExtension(SubscriptionPanelPresenter subscriptionPanelPresenter, SubscriptionsResources resources) {
        resources.subscriptionsCSS().ensureInjected();
        subscriptionPanelPresenter.process();
    }
}
