/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2016] Codenvy, S.A.
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
package com.codenvy.api.subscription.server;


import com.codenvy.api.subscription.server.dao.Subscription;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;

/**
 * Base class for any service which may communicate with account via subscriptions
 *
 * @author Eugene Voevodin
 * @author Alexander Garagatyi
 */
public abstract class AbstractSubscriptionService {
    public static final String SUBSCRIPTION_LIMIT_EXHAUSTED_MESSAGE =
            "Impossible to add the subscription as current subscription list already exhaustive for this account";

    private final String serviceId;
    private final String displayName;

    /**
     * @param serviceId
     *         service identifier
     * @param displayName
     *         service name
     */
    public AbstractSubscriptionService(String serviceId, String displayName) {
        this.serviceId = serviceId;
        this.displayName = displayName;
    }

    /**
     * Should be invoked before subscription creation. It can change subscription attributes or fields
     * to prepare subscription for creating
     *
     * @param subscription
     *         subscription to prepare
     * @throws ConflictException
     *         when subscription is incompatible with system
     * @throws ServerException
     *         if internal error occurs
     */
    public abstract void beforeCreateSubscription(Subscription subscription) throws ConflictException, ServerException, ForbiddenException;

    /**
     * Should be invoked after subscription creation
     *
     * @param subscription
     *         created subscription
     * @throws ApiException
     *         when some error occurs while processing {@code subscription}
     */
    public abstract void afterCreateSubscription(Subscription subscription) throws ApiException;

    /**
     * Should be invoked after subscription removing
     *
     * @param subscription
     *         subscription that was removed
     * @throws ApiException
     *         when some error occurs while processing {@code subscription}
     */
    public abstract void onRemoveSubscription(Subscription subscription) throws ApiException;

    /**
     * Should be invoked after subscription update
     *
     * @param oldSubscription
     *         subscription before update
     * @param newSubscription
     *         subscription after update
     * @throws ApiException
     *         when some error occurs while processing {@code subscription}
     */
    public abstract void onUpdateSubscription(Subscription oldSubscription, Subscription newSubscription) throws ApiException;

    /**
     * Should be invoked to check subscriptions.
     * The one of use cases is use this method to check subscription expiration etc
     */
    public abstract void onCheckSubscriptions() throws ApiException;

    /**
     * @return service identifier
     */
    public String getServiceId() {
        return serviceId;
    }

    /**
     * @return service name
     */
    public String getDisplayName() {
        return displayName;
    }
}
