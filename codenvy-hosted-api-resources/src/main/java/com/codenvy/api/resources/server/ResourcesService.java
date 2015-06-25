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

import com.codenvy.api.metrics.server.dao.MeterBasedStorage;
import com.codenvy.api.metrics.server.period.MetricPeriod;
import com.codenvy.api.resources.shared.dto.WorkspaceResources;
import com.google.common.annotations.Beta;
import com.google.common.collect.FluentIterable;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.workspace.server.dao.Workspace;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;
import org.eclipse.che.dto.server.DtoFactory;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Sergii Leschenko
 */
@Beta
@Api(value = "/resources",
     description = "Resources")
@Path("/resources")
public class ResourcesService extends Service {
    private final MeterBasedStorage meterBasedStorage;
    private final WorkspaceDao      workspaceDao;
    private final MetricPeriod      metricPeriod;

    @Inject
    public ResourcesService(MeterBasedStorage meterBasedStorage,
                            WorkspaceDao workspaceDao,
                            MetricPeriod metricPeriod) {
        this.meterBasedStorage = meterBasedStorage;
        this.workspaceDao = workspaceDao;
        this.metricPeriod = metricPeriod;
    }

    /**
     * Returns used resources
     *
     * @param accountId
     *         account id
     */
    @ApiOperation(value = "Get used resources grouped by workspaces",
                  notes = "Returns used resources grouped by workspaces. Roles: account/owner, account/member, system/manager, system/admin.",
                  position = 2)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @GET
    @Path("/{accountId}/used")
    @RolesAllowed({"account/owner", "account/member", "system/manager", "system/admin"})
    @Produces(MediaType.APPLICATION_JSON)
    public List<WorkspaceResources> getUsedResources(@ApiParam(value = "Account ID")
                                                     @PathParam("accountId") String accountId,
                                                     @ApiParam(value = "Max items", required = false)
                                                     @DefaultValue("30") @QueryParam("maxItems") int maxItems,
                                                     @ApiParam(value = "Skip count", required = false)
                                                     @DefaultValue("0") @QueryParam("skipCount") int skipCount) throws ServerException,
                                                                                                                       NotFoundException,
                                                                                                                       ConflictException {
        final Map<String, Double> memoryUsedReport = meterBasedStorage.getMemoryUsedReport(accountId,
                                                                                           metricPeriod.getCurrent().getStartDate()
                                                                                                       .getTime(),
                                                                                           System.currentTimeMillis());

        List<Workspace> workspaces = workspaceDao.getByAccount(accountId);
        for (Workspace workspace : workspaces) {
            if (!memoryUsedReport.containsKey(workspace.getId())) {
                memoryUsedReport.put(workspace.getId(), 0D);
            }
        }

        List<WorkspaceResources> result = new ArrayList<>();
        for (Map.Entry<String, Double> usedMemory : memoryUsedReport.entrySet()) {
            result.add(DtoFactory.getInstance().createDto(WorkspaceResources.class)
                                 .withWorkspaceId(usedMemory.getKey())
                                 .withMemory(usedMemory.getValue()));
        }

        return FluentIterable.from(result)
                             .skip(skipCount)
                             .limit(maxItems)
                             .toList();
    }
}
