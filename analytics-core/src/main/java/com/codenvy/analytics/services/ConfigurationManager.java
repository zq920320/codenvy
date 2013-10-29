/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.analytics.services;

import java.io.IOException;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public interface ConfigurationManager<T> {

    /**
     * Loads a configuration.
     *
     * @param resource
     *         the resource name or the file name
     * @throws IOException
     *         if an error occurred during reading
     */
    T loadConfiguration(String resource) throws IOException;

    /**
     * Stores a configuration.
     *
     * @throws IOException
     *         if an error occurred during storing
     */
    void storeConfiguration(T configuration) throws IOException;
}
