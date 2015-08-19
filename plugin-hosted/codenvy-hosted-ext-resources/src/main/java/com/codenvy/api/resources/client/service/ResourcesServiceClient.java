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

import org.eclipse.che.ide.rest.AsyncRequestCallback;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Client for IDE3 Subscription service.
 *
 * @author Sergii Leschenko
 */
public interface ResourcesServiceClient {
    void redistributeResources(@Nonnull String accountId,
                               @Nonnull List<UpdateResourcesDescriptor> updateResources,
                               AsyncRequestCallback<Void> callback);
}
