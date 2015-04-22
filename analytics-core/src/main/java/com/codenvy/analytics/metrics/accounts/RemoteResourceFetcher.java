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
package com.codenvy.analytics.metrics.accounts;

import java.io.IOException;
import java.util.List;

/**
 * @author Alexander Reshetnyak
 * @author Anatoliy Bazko
 */
public interface RemoteResourceFetcher {
    /**
     * Makes requests and transforms JSON into DTO object.
     *
     * @param method
     *         REST method, for instance POST, GET etc
     * @param relPath
     *         relative path at remote server
     * @return DTO object
     * @throws IOException
     *         if I/O error occurred
     */
    public <DTO> DTO fetchResource(Class<DTO> dtoClass, String method, String relPath) throws IOException;

    /**
     * Makes requests and transforms JSON into the list of DTO objects.
     *
     * @param method
     *         REST method, for instance POST, GET etc
     * @param relPath
     *         relative path at remote server
     * @return DTO object
     * @throws IOException
     *         if I/O error occurred
     */
    public <DTO> List<DTO> fetchResources(Class<DTO> dtoClass, String method, String relPath) throws IOException;

    /**
     * Makes requests and transforms JSON into the list of DTO objects.
     *
     * @param method
     *         REST method, for instance POST, GET etc
     * @param relPath
     *         relative path at remote server
     * @return DTO object
     * @throws IOException
     *         if I/O error occurred
     */
    public <DTO> List<DTO> fetchResources(Class<DTO> dtoClass, String method, String relPath, Object body) throws IOException;
}
