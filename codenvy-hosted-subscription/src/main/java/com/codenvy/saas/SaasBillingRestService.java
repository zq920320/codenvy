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
package com.codenvy.saas;

import com.codenvy.api.account.server.dao.Account;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.core.ApiException;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * @author Alexander Garagatyi
 */
// must be eager singleton
@Path("billing/saas")
@Singleton
public class SaasBillingRestService {

    private final SaasBillingService saasBillingService;
    private final AccountDao accountDao;

    @Inject
    public SaasBillingRestService(SaasBillingService saasBillingService, AccountDao accountDao) {
        this.saasBillingService = saasBillingService;
        this.accountDao = accountDao;
    }

    @POST
    @Path("charge")
    @RolesAllowed({"system/admin", "system/manager"})
    public void chargeAccountsForPreviousPeriodAsynchronously() throws ApiException {
        saasBillingService.chargeAccounts();
    }

    @POST
    @Path("charge/{accountId}")
    @RolesAllowed({"system/admin", "system/manager"})
    public void chargeAccountForPreviousPeriod(@PathParam("accountId") String accountId) throws ApiException {
        final Account account = accountDao.getById(accountId);

        saasBillingService.chargeAccount(account);
    }
}
