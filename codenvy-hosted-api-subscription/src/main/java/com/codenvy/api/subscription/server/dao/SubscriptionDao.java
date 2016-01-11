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
package com.codenvy.api.subscription.server.dao;

import com.google.common.annotations.Beta;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;

import java.util.List;

/**
 * @author Sergii Leschenko
 */
public interface SubscriptionDao {
    /**
     * Adds new subscription to account that already exists in persistent layer
     *
     * @param subscription
     *         subscription POJO
     */
    @Beta
    void create(Subscription subscription) throws NotFoundException, ConflictException, ServerException;

    /**
     * Get subscription from persistent layer
     *
     * @param subscriptionId
     *         subscription identifier
     * @return Subscription POJO
     * @throws org.eclipse.che.api.core.NotFoundException
     *         when subscription doesn't exist
     */
    @Beta
    Subscription getById(String subscriptionId) throws NotFoundException, ServerException;

    /**
     * Gets list of active subscriptions related to given account.
     *
     * @param accountId
     *         account id
     * @return list of subscriptions, or empty list if no subscriptions found
     */
    @Beta
    List<Subscription> getActive(String accountId) throws NotFoundException, ServerException;

    /**
     * Gets list of subscriptions related to given account.
     *
     * @param accountId
     *         account id
     * @return list of subscriptions, or empty list if no subscriptions found or account does not exist
     */
    @Beta
    List<Subscription> getByAccountId(String accountId) throws ServerException;

    /**
     * Gets active subscription with given service related to given account.
     *
     * @param accountId
     *         account id
     * @param serviceId
     *         service id
     * @return subscription or {@code null} if no subscription found
     */
    @Beta
    Subscription getActiveByServiceId(String accountId, String serviceId) throws ServerException, NotFoundException;

    /**
     * Update existing subscription.
     *
     * @param subscription
     *         new subscription
     */
    @Beta
    void update(Subscription subscription) throws NotFoundException, ServerException;

    /**
     * Remove subscription related to existing account
     *
     * @param subscriptionId
     *         subscription identifier for removal
     */
    @Beta
    void remove(String subscriptionId) throws NotFoundException, ServerException;

    /**
     * Mark subscription as deactivated
     *
     * @param subscriptionId
     *         subscription identifier for deactivation
     */
    @Beta
    void deactivate(String subscriptionId) throws NotFoundException, ServerException;

    SubscriptionQueryBuilder getSubscriptionQueryBuilder();
}
