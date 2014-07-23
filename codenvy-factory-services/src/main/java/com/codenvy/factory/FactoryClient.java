/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
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
package com.codenvy.factory;

import com.codenvy.api.core.ApiException;
import com.codenvy.api.factory.dto.Factory;

/** Allows to get factory from factories storage. */
public interface FactoryClient {
    /**
     * Get factory from storage by id.
     *
     * @param factoryId
     *         - factory id
     * @return - stored factory if id is correct, null otherwise
     * @throws com.codenvy.api.core.ApiException
     */
    public Factory getFactory(String factoryId) throws ApiException;
}
