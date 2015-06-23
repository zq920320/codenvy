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
package com.codenvy.api.subscription.saas.server;

import com.codenvy.api.subscription.saas.shared.dto.Invoice;

import com.codenvy.api.subscription.server.dao.Subscription;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;

/**
 * Process payments.
 *
 * @author Alexander Garagatyi
 */
public interface InvoicePaymentService {
    /**
     * Charge subscription.
     *
     * @param subscription
     *         subscription for which the user pays
     * @throws ServerException
     *         if internal server error occurs
     */
    void charge(Subscription subscription) throws ApiException;

    void charge(Invoice invoice) throws ForbiddenException, ServerException;
}
