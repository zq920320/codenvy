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
package com.codenvy.analytics.api;

import com.codenvy.analytics.Scheduler;
import com.codenvy.analytics.services.Feature;
import com.codenvy.analytics.util.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

/**
 * @author Anatoliy Bazko
 */
@Path("service")
public class Service {

    private static final Logger LOG = LoggerFactory.getLogger(Service.class);

    private final Utils utils;

    @Inject
    public Service(Utils utils) {
        this.utils = utils;
    }

    /**
     * Launches a specific job for arbitrary date interval.
     * This method avoids restarting tomcat to launch given job at startup.
     *
     * @param jobClass
     *         one of the jobs:
     *         analytics.scheduler.force_run_class=com.codenvy.analytics.services.PigRunnerFeature
     *         analytics.scheduler.force_run_class=com.codenvy.analytics.services.DataComputationFeature
     *         analytics.scheduler.force_run_class=com.codenvy.analytics.services.DataIntegrityFeature
     *         analytics.scheduler.force_run_class=com.codenvy.analytics.services.ViewBuilderFeature
     *         analytics.scheduler.force_run_class=com.codenvy.analytics.services.LogCheckerFeature
     *         analytics.scheduler.force_run_class=com.codenvy.analytics.services.ReportSenderFeature
     *         analytics.scheduler.force_run_class=com.codenvy.analytics.services.ActOnFeature
     *         analytics.scheduler.force_run_class=com.codenvy.analytics.services.MarketoInitializerFeature
     *         analytics.scheduler.force_run_class=com.codenvy.analytics.services.MarketoUpdaterFeature
     *         analytics.scheduler.force_run_class=com.codenvy.analytics.services.ReindexerFeature
     * @param fromDate
     *         the beginning of the date interval in format yyyyMMdd
     * @param toDate
     *         the ending of the date interval in format yyyyMMdd
     */
    @GET
    @Path("launch/{job}/{fromDate}/{toDate}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "system/admin", "system/manager"})
    public Response launchJob(@PathParam("job") String jobClass,
                              @PathParam("fromDate") String fromDate,
                              @PathParam("toDate") String toDate,
                              @Context SecurityContext securityContext) {
        if (!utils.isSystemUser(securityContext)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        try {
            Feature job = (Feature)Class.forName(jobClass).getConstructor().newInstance();
            Scheduler.executeJob(job, fromDate, toDate, true);
            return Response.status(Response.Status.OK).build();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException | ClassNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(jobClass + " not found").build();
        } catch (ParseException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
