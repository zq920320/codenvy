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
package com.codenvy.api.account.subscribtion.saas;

import com.codenvy.api.account.server.dao.Account;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.core.ApiException;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.Calendar;

/**
 * Service to call charging over REST
 *
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
    public void chargeAccountsForPreviousPeriod() throws ApiException {
        saasBillingService.chargeAccounts();
    }

    @POST
    @Path("charge/{accountId}")
    @RolesAllowed({"system/admin", "system/manager"})
    public void chargeAccountForPreviousPeriod(@PathParam("accountId") String accountId) throws ApiException {
        saasBillingService.chargeAccount(accountId);
    }

    @POST
    @Path("charge/{accountId}/current")
    @RolesAllowed({"system/admin", "system/manager"})
    public void chargeAccountForCurrentMonth(@PathParam("accountId") String accountId) throws ApiException {
        final Account account = accountDao.getById(accountId);

        final Calendar currentMonthStart = Calendar.getInstance();
        currentMonthStart.set(Calendar.DATE, currentMonthStart.getActualMinimum(Calendar.DAY_OF_MONTH));
        currentMonthStart.set(Calendar.HOUR_OF_DAY, currentMonthStart.getActualMinimum(Calendar.HOUR_OF_DAY));
        currentMonthStart.set(Calendar.MINUTE, 0);
        currentMonthStart.set(Calendar.SECOND, 0);
        currentMonthStart.set(Calendar.MILLISECOND, 0);

        final Calendar currentMonthEnd = Calendar.getInstance();
        currentMonthEnd.add(Calendar.MONTH, 1);
        currentMonthEnd.set(Calendar.DATE, currentMonthEnd.getActualMinimum(Calendar.DAY_OF_MONTH));
        currentMonthEnd.set(Calendar.HOUR_OF_DAY, currentMonthEnd.getActualMinimum(Calendar.HOUR_OF_DAY));
        currentMonthEnd.set(Calendar.MINUTE, 0);
        currentMonthEnd.set(Calendar.SECOND, 0);
        currentMonthEnd.set(Calendar.MILLISECOND, 0);

        saasBillingService.chargeAccount(account, currentMonthStart.getTime(), currentMonthEnd.getTime());
    }
}
