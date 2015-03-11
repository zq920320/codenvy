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
package com.codenvy.api.account.billing;

import com.codenvy.api.account.impl.shared.dto.AccountResources;
import com.codenvy.api.account.impl.shared.dto.Resources;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.Service;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/billing")
public class BillingRestService extends Service {
    private final BillingService billingService;
    private final BillingPeriod  billingPeriod;

    @Inject
    public BillingRestService(BillingService billingService,
                              BillingPeriod billingPeriod) {
        this.billingService = billingService;
        this.billingPeriod = billingPeriod;
    }

    @GET
    @Path("/resources")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"system/admin", "system/manager"})
    public Resources getEstimatedResources(@QueryParam("startPeriod") Long startPeriod,
                                           @QueryParam("endPeriod") Long endPeriod) throws ServerException {
        if (startPeriod == null) {
            startPeriod = billingPeriod.getCurrent().getStartDate().getTime();
        }

        if (endPeriod == null) {
            endPeriod = System.currentTimeMillis();
        }

        return billingService.getEstimatedUsage(startPeriod, endPeriod);
    }

    @GET
    @Path("/resources/accounts")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"system/admin", "system/manager"})
    public List<AccountResources> getEstimatedResourcesByAccounts(@QueryParam("startPeriod") Long startPeriod,
                                                                  @QueryParam("endPeriod") Long endPeriod,
                                                                  @DefaultValue("-1") @QueryParam("maxItems") int maxItems,
                                                                  @QueryParam("skipCount") int skipCount,
                                                                  @QueryParam("freeGbH") Double freeGbH,
                                                                  @QueryParam("paidGbH") Double paidGbH,
                                                                  @QueryParam("prepaidGbH") Double prepaidGbH,
                                                                  @QueryParam("accountId") String accountId) throws ServerException {
        if (startPeriod == null) {
            startPeriod = billingPeriod.getCurrent().getStartDate().getTime();
        }

        if (endPeriod == null) {
            endPeriod = System.currentTimeMillis();
        }

        ResourcesFilter.Builder builder = ResourcesFilter.builder()
                                                         .withFromDate(startPeriod)
                                                         .withTillDate(endPeriod)
                                                         .withSkipCount(skipCount)
                                                         .withMaxItems(maxItems)
                                                         .withAccountId(accountId);

        if (freeGbH != null) {
            builder.withFreeGbHMoreThan(freeGbH);
        }

        if (prepaidGbH != null) {
            builder.withPrePaidGbHMoreThan(prepaidGbH);
        }
        if (paidGbH != null) {
            builder.withPaidGbHMoreThan(paidGbH);
        }

        return billingService.getEstimatedUsageByAccount(builder.build());
    }
}
