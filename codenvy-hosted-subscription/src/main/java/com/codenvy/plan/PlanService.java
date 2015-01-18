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
package com.codenvy.plan;

import com.codenvy.api.account.server.dao.PlanDao;
import com.codenvy.api.account.shared.dto.Plan;
import com.codenvy.api.core.ServerException;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Plan API
 *
 * @author Alexander Garagatyi
 */
@Path("admin/plan")
public class PlanService {
    private final PlanDao planDao;

    @Inject
    public PlanService(PlanDao planDao) {
        this.planDao = planDao;
    }

    /**
     * Get all existing plans
     *
     * @return list of existing plans
     * @throws ServerException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"system/admin", "system/manager"})
    public List<Plan> getPlans() throws ServerException {
        return planDao.getPlans();
    }
}
