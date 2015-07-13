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
package com.codenvy.api.subscription.gwt.client;

import com.codenvy.api.subscription.shared.dto.SubscriptionDescriptor;
import com.google.inject.Inject;

import org.eclipse.che.ide.MimeType;
import org.eclipse.che.ide.collections.Array;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.HTTPHeader;

import javax.annotation.Nonnull;

/**
 * @author Sergii Leschenko
 */
public class SubscriptionServiceClientImpl implements SubscriptionServiceClient {
    private final AsyncRequestFactory asyncRequestFactory;

    @Inject
    public SubscriptionServiceClientImpl(AsyncRequestFactory asyncRequestFactory) {
        this.asyncRequestFactory = asyncRequestFactory;
    }


    /** {@inheritDoc} */
    @Override
    public void getSubscriptions(@Nonnull String accountId, AsyncRequestCallback<Array<SubscriptionDescriptor>> callback) {
        final String requestUrl = "/api/subscription/find/account?id=" + accountId;
        asyncRequestFactory.createGetRequest(requestUrl)
                           .header(HTTPHeader.ACCEPT, MimeType.APPLICATION_JSON)
                           .send(callback);
    }

    @Override
    public void getSubscriptionByServiceId(@Nonnull String accountId,
                                           @Nonnull String serviceId,
                                           AsyncRequestCallback<Array<SubscriptionDescriptor>> callback) {
        final String requestUrl = "/api/subscription/find/account?id=" + accountId + "&service=" + serviceId;
        asyncRequestFactory.createGetRequest(requestUrl)
                           .header(HTTPHeader.ACCEPT, MimeType.APPLICATION_JSON)
                           .send(callback);
    }
}
