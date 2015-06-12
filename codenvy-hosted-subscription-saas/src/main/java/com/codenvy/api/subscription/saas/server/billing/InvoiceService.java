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
package com.codenvy.api.subscription.saas.server.billing;

import com.codenvy.api.subscription.saas.shared.dto.Invoice;
import com.codenvy.api.subscription.saas.shared.dto.InvoiceDescriptor;
import com.google.common.annotations.Beta;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiParam;

import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.account.server.dao.Member;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.util.LinksHelper;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.user.User;
import org.eclipse.che.dto.server.DtoFactory;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

/**
 * @author Max Shaposhnik
 */
@Beta
@Api(value = "/invoice",
        description = "Invoice manager")
@Path("/invoice")
public class InvoiceService extends Service {
    private final BillingService           billingService;
    private final InvoiceTemplateProcessor invoiceTemplateProcessor;
    private final BillingPeriod            billingPeriod;
    private final AccountDao               accountDao;

    @Inject
    public InvoiceService(BillingService billingService,
                          InvoiceTemplateProcessor initializer,
                          BillingPeriod billingPeriod,
                          AccountDao accountDao) {
        this.billingService = billingService;
        this.invoiceTemplateProcessor = initializer;
        this.billingPeriod = billingPeriod;
        this.accountDao = accountDao;
    }

    @GET
    @Path("/{invoiceId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "system/admin", "system/manager"})
    public InvoiceDescriptor getInvoiceJson(@ApiParam(value = "Invoice ID", required = true)
                                            @PathParam("invoiceId") long invoiceId)
            throws NotFoundException, ServerException, ForbiddenException {

        return toDescriptor(getInvoice(invoiceId));
    }

    @GET
    @Path("/{invoiceId}")
    @Produces(MediaType.TEXT_HTML)
    @RolesAllowed({"user", "system/admin", "system/manager"})
    public Response getInvoiceHtml(@ApiParam(value = "Invoice ID", required = true)
                                   @PathParam("invoiceId") final long invoiceId)
            throws NotFoundException, ServerException, ForbiddenException {

        final Invoice invoice = getInvoice(invoiceId);
        StreamingOutput response = new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                PrintWriter w = new PrintWriter(outputStream);
                try {
                    invoiceTemplateProcessor.processTemplate(invoice, w);
                } catch (ServerException | ForbiddenException | NotFoundException e) {
                    w.write(e.getLocalizedMessage());
                } finally {
                    w.flush();
                    w.close();
                }

            }
        };
        return Response.ok(response).build();
    }

    /**
     * Returns list of {@link InvoiceDescriptor} based on {@code accountId, maxItems, skipCount, startPeriod, endPeriod}.
     * NOTE: {@code AccountId} is required for all user except of <i>system/admin</i> and <i>system/manager</i>
     */
    @GET
    @Path("/find")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "system/admin", "system/manager"})
    public List<InvoiceDescriptor> getInvoices(@ApiParam(value = "Account ID", required = false)
                                               @QueryParam("accountId") String accountId,
                                               @ApiParam(value = "Max items count", required = false)
                                               @DefaultValue("-1") @QueryParam("maxItems") int maxItems,
                                               @ApiParam(value = "Skip count", required = false)
                                               @QueryParam("skipCount") int skipCount,
                                               @ApiParam(value = "Period start time", required = false)
                                               @DefaultValue("-1") @QueryParam("startPeriod") long startPeriod,
                                               @ApiParam(value = "Period end time", required = false)
                                               @DefaultValue("-1") @QueryParam("endPeriod") long endPeriod)
            throws NotFoundException, ServerException, ForbiddenException {
        final User currentUser = EnvironmentContext.getCurrent().getUser();
        final boolean isAdmin = currentUser.isMemberOf("system/admin") || currentUser.isMemberOf("system/manager");
        if (!isAdmin) {
            if (accountId == null) {
                throw new ForbiddenException("Missed value of account id parameter");
            } else if (!resolveRolesForSpecificAccount(accountId).contains("account/owner")) {
                throw new ForbiddenException("Access denied");
            }
        }

        List<InvoiceDescriptor> result = new ArrayList<>();
        InvoiceFilter filter = InvoiceFilter.builder()
                                            .withAccountId(accountId)
                                            .withMaxItems(maxItems)
                                            .withSkipCount(skipCount)
                                            .withFromDate(startPeriod)
                                            .withTillDate(endPeriod)
                                            .build();

        for (Invoice invoice : billingService.getInvoices(filter)) {
            result.add(toDescriptor(invoice));
        }
        return result;
    }

    @POST
    @Path("/generate")
    @Produces({TEXT_PLAIN})
    @RolesAllowed({"system/admin", "system/manager"})
    public String generateInvoices(@QueryParam("startPeriod") Long startPeriod,
                                   @QueryParam("endPeriod") Long endPeriod) throws ServerException {
        if (startPeriod == null) {
            startPeriod = billingPeriod.getCurrent().getPreviousPeriod().getStartDate().getTime();
        }

        if (endPeriod == null) {
            endPeriod = billingPeriod.getCurrent().getPreviousPeriod().getEndDate().getTime();
        }

        return String.valueOf(billingService.generateInvoices(startPeriod, endPeriod));
    }

    private Invoice getInvoice(long invoiceId) throws ServerException,
                                                      NotFoundException,
                                                      ForbiddenException {
        List<Invoice> invoices = billingService.getInvoices(InvoiceFilter.builder()
                                                                         .withId(invoiceId)
                                                                         .build());
        if (invoices.isEmpty()) {
            throw new NotFoundException("Account with id " + String.valueOf(invoiceId) + " was not found");
        }
        Invoice invoice = invoices.get(0);
        Set<String> roles = resolveRolesForSpecificAccount(invoice.getAccountId());
        final User currentUser = EnvironmentContext.getCurrent().getUser();
        final boolean isAdmin = currentUser.isMemberOf("system/admin") || currentUser.isMemberOf("system/manager");
        if (!isAdmin && !roles.contains("account/owner")) {
            throw new ForbiddenException("Access denied");
        }
        return invoice;
    }

    private InvoiceDescriptor toDescriptor(Invoice invoice) {
        return DtoFactory.getInstance().createDto(InvoiceDescriptor.class).withId(invoice.getId())
                         .withAccountId(invoice.getAccountId())
                         .withCreationDate(invoice.getCreationDate())
                         .withFromDate(invoice.getFromDate())
                         .withTillDate(invoice.getTillDate())
                         .withCharges(invoice.getCharges())
                         .withCreditCardId(invoice.getCreditCardId())
                         .withMailingDate(invoice.getMailingDate())
                         .withPaymentDate(invoice.getPaymentDate())
                         .withPaymentState(invoice.getPaymentState())
                         .withTotal(invoice.getTotal())
                         .withLinks(generateLinks(invoice));
    }

    private List<Link> generateLinks(Invoice invoice) {
        final UriBuilder uriBuilder = getServiceContext().getServiceUriBuilder();
        final Link jsonLink = LinksHelper.createLink(HttpMethod.GET,
                                                     uriBuilder.clone()
                                                               .path(getClass(), "getInvoiceJson")
                                                               .build(invoice.getId())
                                                               .toString(),
                                                     MediaType.APPLICATION_JSON,
                                                     null,
                                                     "self");
        final Link httpLink = LinksHelper.createLink(HttpMethod.GET,
                                                     uriBuilder.clone()
                                                               .path(getClass(), "getInvoiceHtml")
                                                               .build(invoice.getId())
                                                               .toString(),
                                                     MediaType.TEXT_HTML,
                                                     null,
                                                     "html view");
        return Arrays.asList(jsonLink, httpLink);
    }

    /**
     * Can be used only in methods that is restricted with @RolesAllowed. Require "user" role.
     *
     * @param accountId
     *         account id to resolve roles for
     * @return set of user roles
     */
    private Set<String> resolveRolesForSpecificAccount(String accountId) {
        try {
            final String userId = EnvironmentContext.getCurrent().getUser().getId();
            for (Member membership : accountDao.getByMember(userId)) {
                if (membership.getAccountId().equals(accountId)) {
                    return new HashSet<>(membership.getRoles());
                }
            }
        } catch (ApiException ignored) {
        }
        return Collections.emptySet();
    }
}
