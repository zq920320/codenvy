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

import com.codenvy.api.subscription.shared.dto.Plan;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;

import java.util.List;

/**
 * DAO interface that performs CRUD operations with {@link Plan}.
 *
 * @author Alexander Garagatyi
 */
public interface PlanDao {
    /**
     * Retrieve plan with certain planId
     *
     * @param planId
     *         id of required plan
     * @return stored plan
     * @throws NotFoundException
     *         when account doesn't exist
     * @throws ServerException
     */
    Plan getPlanById(String planId) throws NotFoundException, ServerException;

    /**
     * Get all existing plans
     *
     * @return list of existing plans
     * @throws ServerException
     */
    List<Plan> getPlans() throws ServerException;
}
