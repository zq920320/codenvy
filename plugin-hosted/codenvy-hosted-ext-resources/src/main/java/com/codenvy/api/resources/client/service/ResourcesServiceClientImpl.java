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
package com.codenvy.api.resources.client.service;

import com.codenvy.api.resources.shared.dto.UpdateResourcesDescriptor;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.account.gwt.client.AccountServiceClient;
import org.eclipse.che.ide.MimeType;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.HTTPHeader;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Implementation of {@link AccountServiceClient} service.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class ResourcesServiceClientImpl implements ResourcesServiceClient {
    private final AsyncRequestFactory asyncRequestFactory;

    @Inject
    public ResourcesServiceClientImpl(AsyncRequestFactory asyncRequestFactory) {
        this.asyncRequestFactory = asyncRequestFactory;
    }

    @Override
    public void redistributeResources(@Nonnull String accountId, @Nonnull List<UpdateResourcesDescriptor> updateResources,
                                      AsyncRequestCallback<Void> callback) {
        final String requestUrl = "/api/resources/" + accountId;
        asyncRequestFactory.createPostRequest(requestUrl, updateResources)
                           .header(HTTPHeader.CONTENT_TYPE, MimeType.APPLICATION_JSON)
                           .send(callback);
    }
}
