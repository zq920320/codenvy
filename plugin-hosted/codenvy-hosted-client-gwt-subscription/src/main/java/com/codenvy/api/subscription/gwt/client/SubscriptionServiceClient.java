/*
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
package com.codenvy.api.subscription.gwt.client;

import com.codenvy.api.subscription.shared.dto.SubscriptionDescriptor;

import org.eclipse.che.ide.rest.AsyncRequestCallback;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author Sergii Leschenko
 */
public interface SubscriptionServiceClient {
    /**
     * Get subscriptions of specified account.
     *
     * @param accountId
     *         id of account
     * @param callback
     *         the callback to use for the response
     */
    void getSubscriptions(@NotNull String accountId, AsyncRequestCallback<List<SubscriptionDescriptor>> callback);

    /**
     * Get subscription with specified id of specified account.
     *
     * @param accountId
     *         id of account
     * @param serviceId
     *         id of service
     * @param callback
     *         the callback to use for the response
     */
    void getSubscriptionByServiceId(@NotNull String accountId,
                                    @NotNull String serviceId,
                                    AsyncRequestCallback<List<SubscriptionDescriptor>> callback);

}
