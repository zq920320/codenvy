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
package com.codenvy.api.resources.server;

import com.codenvy.api.resources.shared.dto.UpdateResourcesDescriptor;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;

import java.util.List;

/**
 * Class for managing resources of workspaces
 *
 * @author Sergii Leschenko
 */
public interface ResourcesManager {
    /**
     * Redistributes resources between workspaces
     *
     * @param accountId
     *         account's id
     * @param updateResourcesDescriptors
     *         descriptor of resources for updating
     * @throws ForbiddenException
     *         when account hasn't permission for setting attribute in workspace
     * @throws NotFoundException
     *         when account or workspace with given id doesn't exist
     * @throws ConflictException
     *         when account hasn't required Saas subscription
     *         or user want to use more RAM than he has
     * @throws ServerException
     */
    void redistributeResources(String accountId, List<UpdateResourcesDescriptor> updateResourcesDescriptors)
            throws NotFoundException, ServerException, ConflictException, ForbiddenException;
}
